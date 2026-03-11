# ── Stage 1: Build ────────────────────────────────────────────
# Gradle로 .jar 파일 빌드
FROM gradle:8.5-jdk17 AS builder

WORKDIR /app

# 의존성 캐싱 최적화: build.gradle 먼저 복사 → 소스 변경 시 의존성 재다운로드 방지
COPY build.gradle settings.gradle* ./
RUN gradle dependencies --no-daemon || true

# 전체 소스 복사 후 빌드
COPY src ./src
RUN gradle bootJar --no-daemon -x test

# ── Stage 2: Run ──────────────────────────────────────────────
# 빌드 결과물(.jar)만 가져와서 실행 → 이미지 크기 대폭 감소 (800MB → ~200MB)
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 보안: root 대신 전용 유저로 실행
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

# 컨테이너 시작 명령
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "app.jar"]
