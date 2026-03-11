/**
 * [main.tsx - 애플리케이션 진입점]
 *
 * React 앱을 index.html의 <div id="root">에 마운트합니다.
 * React 18부터는 ReactDOM.render() 대신 createRoot()를 사용합니다.
 */
import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './index.css'; // 전역 스타일

ReactDOM.createRoot(document.getElementById('root')!).render(
  // StrictMode: 개발 모드에서 잠재적 문제를 감지하기 위해 컴포넌트를 두 번 렌더링
  // 운영 빌드에서는 한 번만 렌더링됨
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
