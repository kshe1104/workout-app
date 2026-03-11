/**
 * [components/common/ProtectedRoute.tsx - 인증 보호 라우트]
 *
 * 로그인하지 않은 사용자가 대시보드 등 보호된 페이지에 접근하면
 * 자동으로 로그인 페이지로 리다이렉트합니다.
 *
 * 사용법:
 * <Route path="/dashboard" element={
 *   <ProtectedRoute>
 *     <DashboardPage />
 *   </ProtectedRoute>
 * } />
 */
import { Navigate } from 'react-router-dom';
import { useAuthStore } from '../../stores/authStore';

interface Props {
  children: React.ReactNode;
}

export default function ProtectedRoute({ children }: Props) {
  const { isLoggedIn } = useAuthStore();

  // 로그인 안 됐으면 로그인 페이지로 보냄
  // replace: 뒤로가기 시 무한 리다이렉트 방지
  if (!isLoggedIn) {
    return <Navigate to="/login" replace />;
  }

  return <>{children}</>;
}
