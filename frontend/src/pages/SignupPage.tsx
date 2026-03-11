import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { authApi } from '../api';
import styles from './AuthPage.module.css';

export default function SignupPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [nickname, setNickname] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const handleSignup = async () => {
    if (!email || !password || !nickname) {
      setError('모든 항목을 입력해주세요');
      return;
    }
    if (password.length < 8) {
      setError('비밀번호는 8자 이상이어야 합니다');
      return;
    }

    setIsLoading(true);
    setError('');

    try {
      await authApi.signup({ email, password, nickname });
      alert('회원가입이 완료되었습니다! 로그인해주세요.');
      navigate('/login');
    } catch (err: any) {
      setError(err.response?.data?.message ?? '회원가입에 실패했습니다');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className={styles.container}>
      <div className={styles.card}>
        <div className={styles.logoArea}>
          <span className={styles.logoEmoji}>💪</span>
          <h1 className={styles.logoTitle}>회원가입</h1>
          <p className={styles.logoSubtitle}>운동 기록을 시작해보세요</p>
        </div>

        <div className={styles.form}>
          <div className={styles.inputGroup}>
            <label className={styles.inputLabel}>이메일</label>
            <input type="email" className={styles.input}
              placeholder="example@email.com"
              value={email} onChange={e => setEmail(e.target.value)} />
          </div>

          <div className={styles.inputGroup}>
            <label className={styles.inputLabel}>닉네임</label>
            <input type="text" className={styles.input}
              placeholder="2~20자"
              value={nickname} onChange={e => setNickname(e.target.value)} />
          </div>

          <div className={styles.inputGroup}>
            <label className={styles.inputLabel}>비밀번호</label>
            <input type="password" className={styles.input}
              placeholder="8자 이상"
              value={password} onChange={e => setPassword(e.target.value)} />
          </div>

          {error && <p className={styles.error}>{error}</p>}

          <button className={styles.submitBtn} onClick={handleSignup} disabled={isLoading}>
            {isLoading ? '처리 중...' : '회원가입'}
          </button>

          <p className={styles.switchLink}>
            이미 계정이 있으신가요? <Link to="/login">로그인</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
