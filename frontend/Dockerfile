# ── Stage 1: Build ────────────────────────────────────────────
FROM node:18-alpine AS builder

WORKDIR /app

# 의존성 캐싱 최적화
COPY package*.json ./
RUN npm ci

# 전체 소스 복사 후 빌드
COPY . .
RUN npm run build
# 빌드 결과물은 /app/dist 에 생성됨

# ── Stage 2: Serve with Nginx ─────────────────────────────────
# 정적 파일을 Nginx로 서빙 (Node.js 불필요 → 이미지 크기 최소화)
FROM nginx:alpine

# 커스텀 Nginx 설정 복사
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 빌드된 React 파일 복사
COPY --from=builder /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
