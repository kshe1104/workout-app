/**
 * [api/axios.ts - Axios 인스턴스 설정]
 *
 * ── 보안 개선 사항 ──────────────────────────────────────────────
 *
 * 1. baseURL 환경변수 처리
 *    - 기존: 'http://localhost:8080' 하드코딩
 *    - 변경: import.meta.env.VITE_API_BASE_URL 환경변수에서 읽어옴
 *    - Nginx 프록시 사용 시 빈 문자열 → 상대경로 '/api/...' 로 요청
 *
 * 2. Access Token 메모리 저장 (XSS 방어)
 *    - 기존: localStorage.getItem('accessToken') → XSS 공격 시 탈취 가능
 *    - 변경: 모듈 내 변수(메모리)에 저장 → JS로 접근 불가
 *    - Refresh Token은 localStorage 유지 (로그인 유지 목적)
 *      → httpOnly 쿠키가 가장 안전하지만 백엔드 수정 필요하므로 현재는 유지
 */
import axios from 'axios';

// ── Access Token 메모리 저장소 ────────────────────────────────
// localStorage 대신 모듈 변수에 저장 → XSS로 탈취 불가
// 단점: 새로고침 시 사라짐 → 응답 인터셉터에서 Refresh Token으로 자동 복구
let accessTokenInMemory: string | null = null;

export function setAccessToken(token: string | null) {
  accessTokenInMemory = token;
}

export function getAccessToken(): string | null {
  return accessTokenInMemory;
}

// ── Axios 인스턴스 ────────────────────────────────────────────
// VITE_API_BASE_URL:
//   - 로컬 개발 (.env.local): 'http://localhost:8080'
//   - Docker/배포 환경: '' (빈 문자열) → Nginx가 /api/* 를 백엔드로 프록시
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// ── 요청 인터셉터 ────────────────────────────────────────────
apiClient.interceptors.request.use(
  (config) => {
    const token = getAccessToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ── 응답 인터셉터 ────────────────────────────────────────────
apiClient.interceptors.response.use(
  (response) => response,

  async (error) => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        // Refresh Token은 localStorage에 유지 (로그인 유지 목적)
        const refreshToken = localStorage.getItem('refreshToken');
        if (!refreshToken) {
          handleLogout();
          return Promise.reject(error);
        }

        const response = await axios.post(
          `${import.meta.env.VITE_API_BASE_URL ?? ''}/api/auth/refresh`,
          { refreshToken }
        );

        const newAccessToken = response.data.accessToken;

        // 새 Access Token은 메모리에 저장
        setAccessToken(newAccessToken);

        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        return apiClient(originalRequest);

      } catch {
        handleLogout();
        return Promise.reject(error);
      }
    }

    return Promise.reject(error);
  }
);

function handleLogout() {
  setAccessToken(null);
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('auth-storage'); // Zustand persist 키
  window.location.href = '/login';
}

export default apiClient;
