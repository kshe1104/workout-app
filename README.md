# 💪 운동 기록 앱 (Workout Tracker)

## 프로젝트 구조

```
workout-app/
├── backend/                          # Spring Boot 서버
│   ├── build.gradle
│   └── src/main/
│       ├── java/com/workout/app/
│       │   ├── WorkoutApplication.java
│       │   ├── config/
│       │   │   ├── JpaConfig.java            # JPA Auditing 설정
│       │   │   └── SecurityConfig.java       # Spring Security + CORS 설정
│       │   ├── security/
│       │   │   ├── JwtTokenProvider.java     # JWT 생성/검증
│       │   │   ├── JwtAuthenticationFilter.java  # 요청마다 토큰 검사
│       │   │   └── CustomUserPrincipal.java  # 인증된 사용자 정보
│       │   ├── domain/
│       │   │   ├── user/
│       │   │   │   ├── User.java             # 사용자 Entity
│       │   │   │   ├── Role.java             # 권한 Enum
│       │   │   │   ├── UserRepository.java
│       │   │   │   ├── AuthService.java      # 회원가입/로그인/로그아웃
│       │   │   │   └── AuthController.java   # /api/auth/**
│       │   │   ├── workout/
│       │   │   │   ├── WorkoutLog.java       # 날짜별 운동 기록 헤더
│       │   │   │   ├── WorkoutExercise.java  # 운동 항목
│       │   │   │   ├── WorkoutSet.java       # 세트별 무게/횟수
│       │   │   │   ├── WorkoutRepository.java
│       │   │   │   ├── WorkoutService.java
│       │   │   │   └── WorkoutController.java # /api/workouts/**
│       │   │   └── exercise/
│       │   │       ├── BodyPart.java         # 운동 부위
│       │   │       ├── Exercise.java         # 운동 종목
│       │   │       ├── BodyPartRepository.java
│       │   │       ├── ExerciseRepository.java
│       │   │       ├── ExerciseService.java
│       │   │       └── ExerciseController.java # /api/exercises/**
│       │   ├── dto/
│       │   │   ├── request/RequestDtos.java
│       │   │   └── response/ResponseDtos.java
│       │   └── exception/
│       │       ├── BusinessException.java
│       │       └── GlobalExceptionHandler.java
│       └── resources/
│           ├── application.yml               # DB/JWT/서버 설정
│           └── data.sql                      # 기본 운동 종목 33개 초기 데이터
│
└── frontend/                         # React + TypeScript 클라이언트
    ├── index.html
    ├── package.json
    ├── vite.config.ts
    ├── tsconfig.json
    └── src/
        ├── main.tsx                  # 앱 진입점
        ├── App.tsx                   # 라우팅 설정
        ├── index.css                 # 전역 스타일
        ├── types/
        │   └── workout.ts            # TypeScript 타입 정의
        ├── api/
        │   ├── axios.ts              # Axios 인스턴스 + JWT 인터셉터
        │   └── index.ts              # authApi / workoutApi / exerciseApi
        ├── stores/
        │   └── authStore.ts          # Zustand 전역 로그인 상태
        ├── hooks/
        │   └── useWorkout.ts         # React Query 커스텀 훅
        ├── pages/
        │   ├── LoginPage.tsx
        │   ├── SignupPage.tsx
        │   ├── DashboardPage.tsx     # 메인 (달력 + 상세 패널)
        │   ├── AuthPage.module.css
        │   └── DashboardPage.module.css
        └── components/
            ├── common/
            │   ├── Navbar.tsx
            │   ├── Navbar.module.css
            │   └── ProtectedRoute.tsx
            ├── Calendar/
            │   ├── WorkoutCalendar.tsx       # 운동 달력
            │   └── WorkoutCalendar.module.css
            ├── WorkoutDetail/
            │   ├── WorkoutDetailPanel.tsx    # 날짜 클릭 시 상세
            │   └── WorkoutDetailPanel.module.css
            └── WorkoutForm/
                ├── WorkoutForm.tsx           # 운동 기록 입력 폼
                └── WorkoutForm.module.css
```

---

## 시작하기

### 사전 준비
- Java 17+
- Node.js 18+
- MySQL 8.x

### 1. 데이터베이스 생성
```sql
CREATE DATABASE workout_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 백엔드 실행
```bash
cd backend

# application.yml에서 MySQL 접속 정보 수정
# username: root (본인 계정)
# password: your_password (본인 비밀번호)

./gradlew bootRun
# → http://localhost:8080
```

### 3. 프론트엔드 실행
```bash
cd frontend
npm install
npm run dev
# → http://localhost:3000
```

---

## API 목록

| Method | URL | 설명 | 인증 |
|--------|-----|------|------|
| POST | /api/auth/signup | 회원가입 | ❌ |
| POST | /api/auth/login | 로그인 | ❌ |
| POST | /api/auth/refresh | 토큰 재발급 | ❌ |
| DELETE | /api/auth/logout | 로그아웃 | ✅ |
| GET | /api/workouts/calendar?year=&month= | 월별 달력 | ✅ |
| GET | /api/workouts/{date} | 날짜별 상세 조회 | ✅ |
| POST | /api/workouts | 운동 기록 저장 | ✅ |
| PUT | /api/workouts/{date} | 운동 기록 수정 | ✅ |
| DELETE | /api/workouts/{date} | 운동 기록 삭제 | ✅ |
| GET | /api/exercises | 운동 종목 목록 | ✅ |
| GET | /api/exercises/body-parts | 운동 부위 목록 | ✅ |
| POST | /api/exercises | 커스텀 종목 추가 | ✅ |
| DELETE | /api/exercises/{id} | 커스텀 종목 삭제 | ✅ |
