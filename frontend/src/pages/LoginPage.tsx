/**
 * [pages/LoginPage.tsx - 로그인 페이지]
 *
 * [React의 controlled input 패턴]
 * input의 value를 state로 관리하면 React가 입력값을 완전히 제어합니다.
 * value={email} onChange={e => setEmail(e.target.value)}
 * → 입력할 때마다 state가 업데이트되고, state가 바뀌면 input이 리렌더링됩니다.
 */
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authApi } from '../api';
import { useAuthStore } from '../stores/authStore';
import styles from './AuthPage.module.css';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const { login } = useAuthStore(); // Zustand store의 login 액션
  const navigate = useNavigate();   // 페이지 이동

  const handleLogin = async () => {
    if (!email || !password) {
      setError('이메일과 비밀번호를 입력해주세요');
      return;
    }

    setIsLoading(true);
    setError('');

    try {
      const response = await authApi.login({ email, password });
      const { accessToken, refreshToken, email: userEmail, nickname } = response.data;

      // 토큰 저장 + 전역 상태 업데이트
      login({ email: userEmail, nickname }, accessToken, refreshToken);

      // 대시보드로 이동
      navigate('/dashboard');
    } catch (err: any) {
      setError(err.response?.data?.message ?? '로그인에 실패했습니다');
    } finally {
      setIsLoading(false);
    }
  };

  // Enter 키로 로그인
  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') handleLogin();
  };

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <div className={styles.logoArea}>
          <span className={styles.logoEmoji}>💪</span>
          <h1 className={styles.logoTitle}>운동 기록</h1>
          <p className={styles.logoSubtitle}>나의 운동을 기록하고 성장하세요</p>
        </div>

        <div className={styles.form}>
          <div className={styles.inputGroup}>
            <label className={styles.inputLabel}>이메일</label>
            <input
              type="email"
              className={styles.input}
              placeholder="example@email.com"
              value={email}
              onChange={e => setEmail(e.target.value)}
              onKeyDown={handleKeyDown}
            />
          </div>

          <div className={styles.inputGroup}>
            <label className={styles.inputLabel}>비밀번호</label>
            <input
              type="password"
              className={styles.input}
              placeholder="비밀번호 입력"
              value={password}
              onChange={e => setPassword(e.target.value)}
              onKeyDown={handleKeyDown}
            />
          </div>

          {/* 에러 메시지 */}
          {error && <p className={styles.error}>{error}</p>}

          <button
            className={styles.submitBtn}
            onClick={handleLogin}
            disabled={isLoading}
          >
            {isLoading ? '로그인 중...' : '로그인'}
          </button>

          <p className={styles.switchLink}>
            계정이 없으신가요?{' '}
            <Link to="/signup">회원가입</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
