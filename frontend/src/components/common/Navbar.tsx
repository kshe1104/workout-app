/**
 * [components/common/Navbar.tsx - 상단 네비게이션 바]
 */
import { useNavigate } from 'react-router-dom';
import { useAuthStore } from '../../stores/authStore';
import { authApi } from '../../api';
import styles from './Navbar.module.css';

export default function Navbar() {
  const { user, logout } = useAuthStore();
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await authApi.logout(); // 서버에서 Refresh Token 삭제
    } catch {
      // 서버 요청 실패해도 클라이언트 로그아웃은 진행
    } finally {
      logout(); // Zustand 상태 초기화 + localStorage 토큰 삭제
      navigate('/login');
    }
  };

  return (
    <nav className={styles.navbar}>
      <div className={styles.logo} onClick={() => navigate('/dashboard')}>
        💪 운동 기록
      </div>
      <div className={styles.userArea}>
        <span className={styles.nickname}>{user?.nickname}님</span>
        <button className={styles.logoutBtn} onClick={handleLogout}>
          로그아웃
        </button>
      </div>
    </nav>
  );
}
