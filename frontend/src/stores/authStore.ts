/**
 * [stores/authStore.ts - 로그인 상태 전역 관리]
 *
 * ── 보안 개선 사항 ──────────────────────────────────────────────
 * - Access Token: localStorage → 메모리(axios.ts의 setAccessToken) 저장
 * - Refresh Token: localStorage 유지 (로그인 유지 목적)
 * - persist 대상: user, isLoggedIn만 유지 (토큰 정보 제외)
 */
import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { AuthUser } from '../types/workout';
import { setAccessToken } from '../api/axios';

interface AuthState {
  user: AuthUser | null;
  isLoggedIn: boolean;
  login: (user: AuthUser, accessToken: string, refreshToken: string) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      user: null,
      isLoggedIn: false,

      login: (user, accessToken, refreshToken) => {
        // Access Token → 메모리 저장 (XSS 방어)
        setAccessToken(accessToken);
        // Refresh Token → localStorage (로그인 유지용)
        localStorage.setItem('refreshToken', refreshToken);
        set({ user, isLoggedIn: true });
      },

      logout: () => {
        setAccessToken(null);
        localStorage.removeItem('refreshToken');
        set({ user: null, isLoggedIn: false });
      },
    }),
    {
      name: 'auth-storage',
      // user, isLoggedIn만 persist — 토큰은 저장하지 않음
      partialize: (state) => ({ user: state.user, isLoggedIn: state.isLoggedIn }),
    }
  )
);
