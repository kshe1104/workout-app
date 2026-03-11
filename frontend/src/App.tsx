/**
 * [App.tsx - 라우팅 설정]
 *
 * [React Router란?]
 * URL 경로에 따라 다른 컴포넌트를 보여주는 라이브러리입니다.
 *
 * / → 로그인 여부에 따라 대시보드 또는 로그인 페이지로 리다이렉트
 * /login → 로그인 페이지
 * /signup → 회원가입 페이지
 * /dashboard → 대시보드 (ProtectedRoute로 보호 - 로그인 필요)
 *
 * [QueryClientProvider란?]
 * React Query가 동작하려면 최상위에 QueryClientProvider를 감싸야 합니다.
 * 모든 하위 컴포넌트에서 useQuery, useMutation을 사용할 수 있게 됩니다.
 */
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import DashboardPage from './pages/DashboardPage';
import ProtectedRoute from './components/common/ProtectedRoute';
import { useAuthStore } from './stores/authStore';

// QueryClient 설정
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      // 윈도우 포커스 시 자동 재요청 비활성화 (앱 전환할 때마다 요청 방지)
      refetchOnWindowFocus: false,
      // 에러 발생 시 재시도 횟수
      retry: 1,
    },
  },
});

function AppRoutes() {
  const { isLoggedIn } = useAuthStore();

  return (
    <Routes>
      {/* 루트: 로그인 여부에 따라 리다이렉트 */}
      <Route
        path="/"
        element={<Navigate to={isLoggedIn ? '/dashboard' : '/login'} replace />}
      />

      {/* 공개 페이지 */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/signup" element={<SignupPage />} />

      {/* 보호된 페이지 - 로그인 필요 */}
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <DashboardPage />
          </ProtectedRoute>
        }
      />
    </Routes>
  );
}

export default function App() {
  return (
    // QueryClientProvider: React Query 사용을 위한 최상위 Provider
    <QueryClientProvider client={queryClient}>
      {/* BrowserRouter: HTML5 History API 기반 라우팅 활성화 */}
      <BrowserRouter>
        <AppRoutes />
      </BrowserRouter>
    </QueryClientProvider>
  );
}
