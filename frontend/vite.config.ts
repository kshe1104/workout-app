/**
 * [vite.config.ts - Vite 빌드 도구 설정]
 *
 * Vite란? 매우 빠른 프론트엔드 개발 서버입니다.
 * npm run dev → localhost:3000 에서 개발 서버 실행
 * npm run build → 운영 배포용 파일 생성
 */
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000, // React 개발 서버 포트 (Spring Boot는 8080)
    // proxy: API 요청을 Spring Boot로 전달 (개발 환경 CORS 우회 대안)
    // 현재는 SecurityConfig에서 CORS 설정으로 해결하므로 주석 처리
    // proxy: {
    //   '/api': 'http://localhost:8080',
    // },
  },
});
