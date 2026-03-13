# 💪 운동 기록 앱 (Workout Tracker)

> **달력 기반 운동 기록 웹 애플리케이션**  
> React + TypeScript 프론트엔드와 Spring Boot 백엔드로 구성된 풀스택 프로젝트

<br>

## 📸 주요 기능

| 기능 | 설명 |
|------|------|
| 🔐 회원가입 / 로그인 | JWT 기반 인증 (Access + Refresh Token) |
| 📅 달력 뷰 | 월별 운동 여부 및 부위 시각화 |
| 📝 운동 기록 | 종목별 세트 / 무게 / 횟수 입력 |
| 🔍 종목 필터 | 부위별 필터링 + 커스텀 종목 직접 추가 |
| ⭐ 즐겨찾기 | 자주 하는 종목을 별표로 빠르게 접근 |
| ✏️ 수정 / 삭제 | 기록된 운동 수정 및 삭제 |

<br>

## 🛠 기술 스택

### Frontend
- **React 18** + **TypeScript**
- **Vite** (빌드 도구)
- **React Query (TanStack Query v5)** - 서버 상태 관리 / 캐싱
- **Zustand** - 전역 인증 상태 관리
- **Axios** - HTTP 클라이언트 (JWT 인터셉터 포함)
- **React Router v6** - 클라이언트 사이드 라우팅
- **Day.js** - 날짜 처리
- **CSS Modules** - 컴포넌트 스코프 스타일링

### Backend
- **Java 17** + **Spring Boot 3.2**
- **Spring Security** - 인증 / 인가
- **Spring Data JPA** + **QueryDSL** - ORM / 동적 쿼리
- **JWT (jjwt 0.12.3)** - Access Token + Refresh Token
- **MySQL 8** - 메인 데이터베이스
- **Lombok** - 보일러플레이트 코드 제거
- **Gradle** - 빌드 도구

<br>

## 📁 프로젝트 구조

```
workout-app/
├── backend/                    # Spring Boot 서버
│   └── src/main/java/com/workout/app/
│       ├── config/             # Security, JPA 설정
│       ├── security/           # JWT Filter, Provider
│       ├── domain/
│       │   ├── user/           # 회원가입 / 로그인
│       │   ├── workout/        # 운동 기록 CRUD
│       │   └── exercise/       # 종목 / 부위 관리
│       ├── dto/                # 요청 / 응답 DTO
│       └── exception/          # 전역 예외 처리
│
└── frontend/                   # React + TypeScript 클라이언트
    └── src/
        ├── api/                # Axios 인스턴스 + API 함수
        ├── components/         # Calendar, WorkoutForm, WorkoutDetail
        ├── hooks/              # React Query 커스텀 훅
        ├── pages/              # Login, Signup, Dashboard
        ├── stores/             # Zustand 인증 스토어
        └── types/              # TypeScript 타입 정의
```

<br>

## ⚙️ 시작하기

### 사전 준비

- Java 17+
- Node.js 18+
- MySQL 8.x

### 1. 데이터베이스 생성

```sql
CREATE DATABASE workout_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 백엔드 설정 및 실행

`backend/src/main/resources/application.yml` 수정:

```yaml
spring:
  datasource:
    username: root           # 본인 MySQL 계정
    password: your_password  # 본인 MySQL 비밀번호

  sql:
    init:
      mode: always           # ← 반드시 추가 (초기 데이터 로딩)
```

```bash
cd backend
./gradlew bootRun
# → http://localhost:8080
```

> **⚠️ `sql.init.mode: always` 설정 필수**  
> Spring Boot 2.5+ 에서 `ddl-auto: update` 사용 시 `data.sql`이 자동 실행되지 않습니다.  
> 이 설정이 없으면 운동 부위(body_parts)와 기본 종목(exercises) 데이터가 로드되지 않아 종목 선택 화면이 비어 보입니다.

### 3. 프론트엔드 실행

```bash
cd frontend
npm install
npm run dev
# → http://localhost:3000
```

<br>

## 🔌 API 명세

### 인증 API

| Method | URL | 설명 | 인증 필요 |
|--------|-----|------|-----------|
| `POST` | `/api/auth/signup` | 회원가입 | ❌ |
| `POST` | `/api/auth/login` | 로그인 | ❌ |
| `POST` | `/api/auth/refresh` | Access Token 재발급 | ❌ |
| `DELETE` | `/api/auth/logout` | 로그아웃 | ✅ |

### 운동 기록 API

| Method | URL | 설명 | 인증 필요 |
|--------|-----|------|-----------|
| `GET` | `/api/workouts/calendar?year=&month=` | 월별 달력 조회 | ✅ |
| `GET` | `/api/workouts/{date}` | 날짜별 상세 조회 | ✅ |
| `POST` | `/api/workouts` | 운동 기록 저장 | ✅ |
| `PUT` | `/api/workouts/{date}` | 운동 기록 수정 | ✅ |
| `DELETE` | `/api/workouts/{date}` | 운동 기록 삭제 | ✅ |

### 종목 API

| Method | URL | 설명 | 인증 필요 |
|--------|-----|------|-----------|
| `GET` | `/api/exercises` | 종목 목록 조회 | ✅ |
| `GET` | `/api/exercises/body-parts` | 운동 부위 목록 | ✅ |
| `POST` | `/api/exercises` | 커스텀 종목 추가 | ✅ |
| `DELETE` | `/api/exercises/{id}` | 커스텀 종목 삭제 | ✅ |

<br>

## 🔐 인증 흐름

```
로그인 → Access Token (15분) + Refresh Token (7일) 발급
         ↓
     API 요청 시 Authorization: Bearer {accessToken} 헤더 첨부
         ↓
     Access Token 만료 시 → /api/auth/refresh 로 자동 재발급 (Axios 인터셉터)
```

- Access Token은 메모리(Zustand)에 저장
- Refresh Token은 로컬 스토리지에 저장
- 로그아웃 시 서버의 Refresh Token도 함께 삭제

<br>

## 🗃️ 기본 데이터

앱 최초 실행 시 `data.sql`을 통해 아래 데이터가 자동 삽입됩니다.

**운동 부위 (8개):** 가슴, 등, 어깨, 이두, 삼두, 하체, 복근, 유산소

**기본 종목 (33개):** 벤치프레스, 데드리프트, 스쿼트, 풀업 등

---

## 🐛 트러블슈팅

### 1. 종목 선택 화면이 비어있다

**원인:** `application.yml`에 `sql.init.mode` 설정 누락 → `data.sql` 미실행

**해결:** `application.yml`에 아래 설정 추가 후 서버 재시작

```yaml
spring:
  sql:
    init:
      mode: always
```

---

### 2. CORS 오류 (프론트엔드에서 API 요청 실패)

**원인:** 프론트(3000포트)와 백엔드(8080포트)의 Origin이 달라 브라우저가 요청을 차단

**해결:** `SecurityConfig.java`의 CORS 허용 Origin 확인

```java
configuration.setAllowedOrigins(List.of("http://localhost:3000"));
```

프론트엔드 개발 서버 포트가 다르다면 해당 포트로 변경

---

### 3. JWT 토큰 서명 오류 (SignatureException)

**원인:** `application.yml`의 `jwt.secret` 키가 256비트(32자) 미만

**해결:** secret 키를 32자 이상으로 변경

```yaml
jwt:
  secret: workoutAppSecretKeyForJwtTokenGenerationMustBeLongEnough256bits
```

---

### 4. QueryDSL Q클래스 생성 오류

**원인:** 과거 `com.ewerk.gradle.plugins.querydsl` 플러그인은 Gradle 8.x와 호환되지 않음

**해결:** `build.gradle`에서 ewerk 플러그인을 제거하고 `annotationProcessor` 방식만 사용

```groovy
// ❌ 제거
// id 'com.ewerk.gradle.plugins.querydsl'

// ✅ 유지
annotationProcessor 'com.querydsl:querydsl-apt:5.0.0:jakarta'
annotationProcessor 'jakarta.annotation:jakarta.annotation-api'
annotationProcessor 'jakarta.persistence:jakarta.persistence-api'
```

이후 아래 명령어로 Q클래스 재생성:

```bash
./gradlew clean compileJava
```

---

### 5. 프론트엔드 빌드 오류 (TypeScript)

```bash
cd frontend
npm run build
```

타입 오류가 있다면 아래를 확인하세요:

- `workout.ts` 타입 정의와 API 응답 구조 일치 여부
- `useWorkout.ts` 훅의 제네릭 타입 확인

---

### 6. MySQL 한글 깨짐

**원인:** DB 혹은 테이블 charset이 `utf8mb4`가 아닌 경우

**해결:** DB 생성 시 반드시 아래 명령어 사용

```sql
CREATE DATABASE workout_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

또는 `application.yml` URL에 `characterEncoding=UTF-8` 파라미터 확인:

```yaml
url: jdbc:mysql://localhost:3306/workout_db?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
```

---

### 7. Refresh Token으로 재발급이 안 된다 (401 루프)

**원인:** 로컬 스토리지의 Refresh Token이 만료되었거나, 서버 재시작으로 인메모리 토큰이 초기화된 경우

**해결:** 로컬 스토리지 초기화 후 재로그인

```javascript
// 브라우저 콘솔에서 실행
localStorage.clear();
location.reload();
```

프로덕션 환경에서는 Refresh Token을 DB나 Redis에 영구 저장하도록 구조 개선 필요

<br>


### 8. MultipleBagFetchException
원인: WorkoutLog.exercises(List)와 WorkoutExercise.sets(List) 두 컬렉션을 동시에 fetch join → Hibernate가 다중 Bag fetch 거부

해결: 두 필드를 List → Set(LinkedHashSet)으로 변경

java
private Set<WorkoutExercise> exercises = new LinkedHashSet<>();
private Set<WorkoutSet> sets = new LinkedHashSet<>();

## 📝 환경변수 설정 (운영 환경)

`application.yml`의 민감한 정보는 환경변수로 관리하는 것을 권장합니다.

```yaml
spring:
  datasource:
    password: ${DB_PASSWORD}
jwt:
  secret: ${JWT_SECRET}
```

```bash
export DB_PASSWORD=your_password
export JWT_SECRET=your_jwt_secret_key_must_be_at_least_32_characters
```
