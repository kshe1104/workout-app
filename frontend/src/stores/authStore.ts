/**
 * [stores/authStore.ts - 로그인 상태 전역 관리]
 *
 * [Zustand란?]
 * React의 전역 상태 관리 라이브러리입니다.
 * Redux보다 훨씬 간단하고 보일러플레이트(반복 코드)가 거의 없습니다.
 *
 * [왜 전역 상태가 필요한가?]
 * 로그인 정보(닉네임, 로그인 여부)는 Navbar, 대시보드, 등 여러 컴포넌트에서 필요합니다.
 * props로 일일이 전달하면 너무 복잡해지므로 전역 상태로 관리합니다.
 *
 * [사용 방법]
 * const { user, isLoggedIn, login, logout } = useAuthStore();
 */
import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { AuthUser } from '../types/workout';

interface AuthState {
  user: AuthUser | null;
  isLoggedIn: boolean;

  // 로그인 성공 시 호출: 토큰 저장 + 상태 업데이트
  login: (user: AuthUser, accessToken: string, refreshToken: string) => void;

  // 로그아웃 시 호출: 토큰 삭제 + 상태 초기화
  logout: () => void;
}

// persist 미들웨어: 상태를 localStorage에 자동 저장
// → 새로고침해도 로그인 상태 유지
export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      isLoggedIn: false,

      login: (user, accessToken, refreshToken) => {
        // 토큰을 localStorage에 저장 (axios 인터셉터에서 읽어서 사용)
        localStorage.setItem('accessToken', accessToken);
        localStorage.setItem('refreshToken', refreshToken);

        // Zustand 상태 업데이트
        set({ user, isLoggedIn: true });
      },

      logout: () => {
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        set({ user: null, isLoggedIn: false });
      },
    }),
    {
      name: 'auth-storage', // localStorage 키 이름
      // 토큰은 localStorage에 직접 저장하므로 user 정보만 persist
      partialize: (state) => ({ user: state.user, isLoggedIn: state.isLoggedIn }),
    }
  )
);
