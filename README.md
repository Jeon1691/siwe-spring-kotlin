# SIWE (Sign-In with Ethereum) Spring Boot Starter

Spring Boot 3 + Kotlin 기반의 SIWE (EIP-4361) 인증 구현체입니다.
OAuth2 표준에 가까운 JWT 기반 인증 및 Refresh Token 기능을 제공합니다.

## 주요 기능
- **Nonce 발급**: 재전송 공격 방지를 위한 1회용 Nonce 생성 (`GET /api/auth/siwe/nonce`)
- **SIWE 검증**: EIP-4361 메시지 파싱 및 서명 검증 (`POST /api/auth/siwe/verify`)
- **JWT 발급**: 검증 성공 시 Access Token 및 Refresh Token 발급 (OAuth2 포맷)
- **토큰 갱신**: Refresh Token을 사용한 Access Token 재발급 및 Rotation (`POST /api/auth/siwe/refresh`)
- **Swagger UI**: API 문서 자동화 및 테스트 도구 제공
- **테스트 클라이언트**: 메타마스크 연동 테스트를 위한 내장 웹 페이지 제공

## 요구 사항 (Requirements)
- Java 21+
- Gradle (Wrapper 포함됨)
- Docker & Docker Compose (선택 사항)

## 실행 방법 (Run)

### 1. 로컬 실행 (Gradle)
터미널에서 아래 명령어를 실행하거나, IntelliJ IDEA에서 `SiweSpringKotlinApplication`을 실행하세요.

```bash
./gradlew bootRun
```

### 2. Docker 실행
Docker가 설치되어 있다면 아래 명령어로 컨테이너를 실행할 수 있습니다.

```bash
docker-compose up --build -d
```

서버는 기본적으로 `http://localhost:8080`에서 실행됩니다.

### 3. 테스트 실행
단위 테스트 및 통합 테스트를 실행하려면 아래 명령어를 사용하세요.

```bash
./gradlew test
```

## API 사용법

### 1. Nonce 발급
```bash
curl http://localhost:8080/api/auth/siwe/nonce
```

### 2. 로그인 (Verify)
클라이언트에서 생성한 SIWE 메시지와 서명을 전송합니다.

**Request:**
```http
POST /api/auth/siwe/verify
Content-Type: application/json

{
  "message": "localhost wants you to sign in with your Ethereum account:\n0x...",
  "signature": "0x..."
}
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJIUzI1Ni...",
  "refresh_token": "eyJhbGciOiJIUzI1Ni...",
  "token_type": "Bearer",
  "expires_in": 3600
}
```

### 3. 토큰 갱신 (Refresh)
만료된 Access Token을 갱신합니다.

**Request:**
```http
POST /api/auth/siwe/refresh
Content-Type: application/json

{
  "refresh_token": "eyJhbGciOiJIUzI1Ni..."
}
```

## 테스트 도구

### 1. 웹 클라이언트 (MetaMask 연동)
브라우저에서 아래 주소로 접속하여 메타마스크를 통한 로그인 및 토큰 갱신을 직접 테스트할 수 있습니다.
- URL: [http://localhost:8080/index.html](http://localhost:8080/index.html)

### 2. Swagger UI
API 명세를 확인하고 직접 요청을 보낼 수 있습니다.
- URL: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

## 설정 (Configuration)
`src/main/resources/application.yml`에서 주요 설정을 변경할 수 있습니다.

```yaml
siwe:
  allowed-domains:
    - "localhost"
    - "*.develicit.dev"
  nonce-ttl-minutes: 10

jwt:
  secret: "YOUR_SECRET_KEY..."
  access-token-validity-seconds: 3600    # 1시간
  refresh-token-validity-seconds: 604800 # 7일
```
