/**
 * [api/axios.ts - Axios 인스턴스 설정]
 *
 * Axios란? HTTP 요청을 쉽게 보낼 수 있는 라이브러리입니다.
 * fetch()보다 인터셉터, 에러처리, 자동 JSON 변환 등이 편리합니다.
 *
 * [인터셉터(Interceptor)란?]
 * 요청을 보내기 전, 응답을 받은 후 자동으로 실행되는 미들웨어입니다.
 *
 * 요청 인터셉터: 모든 요청에 JWT 토큰을 자동으로 헤더에 추가
 * 응답 인터셉터: 401 에러 시 Refresh Token으로 자동 재발급 후 재시도
 */
import axios from 'axios';

// 기본 설정이 적용된 Axios 인스턴스 생성
const apiClient = axios.create({
  baseURL: 'http://localhost:8080', // Spring Boot 서버 주소
  timeout: 10000,                   // 10초 이내 응답 없으면 에러
  headers: {
    'Content-Type': 'application/json',
  },
});

// ── 요청 인터셉터 ───────────────────────────────────────────────────
// 모든 API 요청 전에 자동으로 실행됩니다.
apiClient.interceptors.request.use(
  (config) => {
    // localStorage에서 Access Token 꺼내기
    const token = localStorage.getItem('accessToken');
    if (token) {
      // "Authorization: Bearer {token}" 헤더 자동 추가
      // 이 덕분에 각 API 함수마다 토큰을 직접 넣을 필요가 없습니다
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// ── 응답 인터셉터 ───────────────────────────────────────────────────
// 모든 API 응답 후에 자동으로 실행됩니다.
apiClient.interceptors.response.use(
  // 성공(2xx) 응답은 그대로 통과
  (response) => response,

  // 에러 응답 처리
  async (error) => {
    const originalRequest = error.config;

    // 401 Unauthorized: Access Token 만료된 경우
    // _retry: 무한 루프 방지 (재발급 요청 자체가 401이면 루프를 막아야 함)
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshToken = localStorage.getItem('refreshToken');
        if (!refreshToken) {
          // Refresh Token도 없으면 → 로그아웃 처리
          handleLogout();
          return Promise.reject(error);
        }

        // 새 Access Token 요청
        const response = await axios.post('http://localhost:8080/api/auth/refresh', {
          refreshToken,
        });

        const newAccessToken = response.data.accessToken;

        // 새 토큰 저장
        localStorage.setItem('accessToken', newAccessToken);

        // 실패했던 원래 요청을 새 토큰으로 재시도
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
        return apiClient(originalRequest);

      } catch (refreshError) {
        // Refresh Token도 만료됐으면 → 로그아웃
        handleLogout();
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

// 로그아웃 처리 (토큰 삭제 + 로그인 페이지로 이동)
function handleLogout() {
  localStorage.removeItem('accessToken');
  localStorage.removeItem('refreshToken');
  window.location.href = '/login';
}

export default apiClient;
