# 🚀 배포 가이드 (Docker + AWS EC2 + GitHub Actions CI/CD)

> **전체 흐름**
> ```
> 로컬 개발 → GitHub push → GitHub Actions 자동 실행
>   → Docker 이미지 빌드 → AWS ECR 푸시 → EC2 자동 배포
> ```

---

## 📐 아키텍처 다이어그램

```
[GitHub]
    │  push to main
    ▼
[GitHub Actions]
    ├─ 백엔드 Docker 빌드 → AWS ECR 푸시
    └─ 프론트엔드 Docker 빌드 → AWS ECR 푸시
                              │
                              │ SSH + docker compose up
                              ▼
                     [AWS EC2 (t2.micro)]
                     ┌──────────────────────┐
                     │  Nginx (port 80)     │  ← 사용자 접속
                     │  Spring Boot (8080)  │
                     │  MySQL (3306)        │
                     └──────────────────────┘
```

---

## 1단계: 로컬에서 Docker로 전체 실행해보기

### 1-1. 환경변수 파일 생성

```bash
cd workout-app
cp .env.example .env
```

`.env` 파일을 열어서 값 채우기:

```env
DB_ROOT_PASSWORD=mypassword123
JWT_SECRET=myJwtSecretKeyThatIsAtLeast32CharactersLong!
```

### 1-2. 전체 실행

```bash
# 이미지 빌드 + 컨테이너 시작 (처음 실행 시 5~10분 소요)
docker compose up --build

# 백그라운드 실행
docker compose up --build -d
```

### 1-3. 확인

| 서비스 | URL |
|--------|-----|
| 프론트엔드 | http://localhost |
| 백엔드 API | http://localhost/api/... (Nginx 프록시) |
| 백엔드 직접 | http://localhost:8080 |

### 자주 쓰는 명령어

```bash
# 로그 확인
docker compose logs -f backend
docker compose logs -f frontend

# 컨테이너 상태 확인
docker compose ps

# 컨테이너 중지
docker compose down

# 컨테이너 + 볼륨(DB 데이터) 모두 삭제
docker compose down -v
```

---

## 2단계: AWS 세팅

### 2-1. ECR (Elastic Container Registry) 리포지토리 생성

이미지를 저장하는 AWS의 Docker Hub입니다.

1. AWS 콘솔 → **ECR** 검색 → **리포지토리 생성**
2. 리포지토리 이름: `workout-backend` → 생성
3. 같은 방식으로 `workout-frontend` 리포지토리 생성
4. 각 리포지토리의 **URI** 복사해두기
   - 형식: `123456789.dkr.ecr.ap-northeast-2.amazonaws.com/workout-backend`

### 2-2. IAM 사용자 생성 (GitHub Actions용)

1. AWS 콘솔 → **IAM** → **사용자** → **사용자 생성**
2. 사용자 이름: `github-actions-deployer`
3. **권한 정책 직접 연결** → 아래 정책 추가:
   - `AmazonEC2ContainerRegistryFullAccess` (ECR 푸시/풀)
4. 생성 완료 후 **액세스 키 만들기**
   - 유형: **서드 파티 서비스** 선택
   - **Access Key ID**와 **Secret Access Key** 복사해두기 (다시 볼 수 없음!)

### 2-3. EC2 인스턴스 생성

1. AWS 콘솔 → **EC2** → **인스턴스 시작**
2. 설정:

| 항목 | 값 |
|------|----|
| 이름 | workout-server |
| AMI | Ubuntu Server 22.04 LTS (프리티어) |
| 인스턴스 유형 | t2.micro (프리티어) |
| 키 페어 | 새 키 페어 생성 → `workout-key.pem` 다운로드 |
| 스토리지 | 20GB gp3 |

3. **보안 그룹 규칙** 설정:

| 유형 | 포트 | 소스 | 용도 |
|------|------|------|------|
| SSH | 22 | 내 IP | 서버 접속 |
| HTTP | 80 | 0.0.0.0/0 | 웹 서비스 |
| HTTPS | 443 | 0.0.0.0/0 | (나중에 SSL 추가 시) |

4. **인스턴스 시작** → **퍼블릭 IP 주소** 복사

### 2-4. EC2 초기 세팅

로컬 터미널에서 EC2 접속:

```bash
chmod 400 workout-key.pem
ssh -i workout-key.pem ubuntu@{EC2_퍼블릭_IP}
```

EC2 서버에서 아래 명령어 순서대로 실행:

```bash
# 패키지 업데이트
sudo apt update && sudo apt upgrade -y

# Docker 설치
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker ubuntu
newgrp docker  # 그룹 즉시 적용

# Docker Compose 설치
sudo apt install docker-compose-plugin -y

# AWS CLI 설치
sudo apt install awscli -y

# AWS 자격증명 설정 (IAM 사용자 키 입력)
aws configure
# AWS Access Key ID: (위에서 복사한 키)
# AWS Secret Access Key: (위에서 복사한 키)
# Default region name: ap-northeast-2
# Default output format: json

# 앱 디렉토리 생성
mkdir -p ~/workout-app
cd ~/workout-app

# .env 파일 생성 (서버용)
cat > .env << 'EOF'
DB_ROOT_PASSWORD=서버용_강력한_비밀번호
JWT_SECRET=서버용_JWT_시크릿_키_32자_이상
EOF

# docker-compose.yml을 서버에 생성
# (GitHub에서 직접 클론하거나, 아래처럼 직접 작성)
```

**docker-compose.yml을 서버에 올리는 방법 (선택):**

```bash
# 방법 A: scp로 업로드
scp -i workout-key.pem docker-compose.yml ubuntu@{EC2_IP}:~/workout-app/

# 방법 B: git clone (레포가 public인 경우)
git clone https://github.com/{username}/{repo}.git .
```

---

## 3단계: GitHub Secrets 설정

GitHub 레포지토리 → **Settings** → **Secrets and variables** → **Actions** → **New repository secret**

| Secret 이름 | 값 | 설명 |
|-------------|-----|------|
| `AWS_ACCESS_KEY_ID` | IAM 액세스 키 ID | ECR 인증용 |
| `AWS_SECRET_ACCESS_KEY` | IAM 시크릿 액세스 키 | ECR 인증용 |
| `AWS_ACCOUNT_ID` | AWS 계정 ID (12자리 숫자) | ECR URL 구성용 |
| `EC2_HOST` | EC2 퍼블릭 IP | SSH 접속 주소 |
| `EC2_SSH_KEY` | workout-key.pem 전체 내용 | SSH 인증용 |

> **EC2_SSH_KEY 입력 방법:**
> ```bash
> cat workout-key.pem
> # 출력된 내용 전체 (-----BEGIN RSA PRIVATE KEY----- 포함) 를 복사해서 붙여넣기
> ```

> **AWS_ACCOUNT_ID 확인 방법:**
> AWS 콘솔 오른쪽 위 계정 이름 클릭 → 12자리 숫자

---

## 4단계: 첫 배포

모든 설정이 완료되면 GitHub에 push합니다.

```bash
git add .
git commit -m "feat: Docker + CI/CD 배포 설정 추가"
git push origin main
```

GitHub → **Actions** 탭에서 파이프라인 진행 상황을 실시간으로 확인할 수 있습니다.

**배포 소요 시간:** 약 5~8분

---

## 배포 흐름 상세

```
git push
  │
  ▼
GitHub Actions 트리거
  ├─ Job1: 백엔드 빌드 (gradle bootJar → Docker build → ECR push)
  ├─ Job2: 프론트엔드 빌드 (npm build → Docker build → ECR push)
  │         ↑ 두 Job은 병렬 실행
  └─ Job3: EC2 SSH 접속 → docker pull → docker compose up -d
              (DB 컨테이너는 그대로 유지 → 데이터 보존)
```

---

## 🔧 트러블슈팅

### "permission denied" - Docker 권한 오류

```bash
sudo usermod -aG docker ubuntu
newgrp docker
# 또는 서버 재접속
```

### ECR 로그인 실패

```bash
# EC2에서 직접 테스트
aws ecr get-login-password --region ap-northeast-2 | \
  docker login --username AWS --password-stdin \
  {AWS_ACCOUNT_ID}.dkr.ecr.ap-northeast-2.amazonaws.com
```

### 백엔드가 DB에 연결 못 함

`docker compose logs backend`로 확인 후, DB가 먼저 healthy 상태인지 확인:

```bash
docker compose ps  # db가 healthy인지 확인
docker compose restart backend  # DB 준비 후 백엔드 재시작
```

### 프론트엔드에서 API 응답이 없음

Nginx 프록시 설정 확인:

```bash
docker compose exec frontend nginx -t  # Nginx 설정 문법 검사
docker compose logs frontend
```

### t2.micro 메모리 부족 (Gradle 빌드 실패)

Gradle 빌드는 GitHub Actions에서 수행하므로 EC2 메모리 부족 문제는 발생하지 않습니다.
EC2에서는 이미 빌드된 이미지를 pull만 합니다.

---

## 💰 예상 비용 (프리티어 기준)

| 서비스 | 프리티어 한도 | 초과 시 비용 |
|--------|-------------|-------------|
| EC2 t2.micro | 750시간/월 무료 | ~$0.015/시간 |
| ECR | 500MB/월 무료 | $0.10/GB |
| 데이터 전송 | 15GB/월 무료 | $0.09/GB |

> **팁:** 사용하지 않을 때 EC2 인스턴스를 중지하면 비용 절약 (EBS 스토리지 비용만 발생)

---

## 🔒 보안 체크리스트

- [ ] `.env` 파일이 `.gitignore`에 포함되어 있는가
- [ ] GitHub Secrets에 민감한 정보를 저장했는가 (코드에 하드코딩 금지)
- [ ] EC2 보안 그룹에서 22번 포트를 내 IP만 허용했는가
- [ ] `application.yml`의 JWT secret이 환경변수로 처리되었는가
- [ ] IAM 사용자에게 최소 권한만 부여했는가
