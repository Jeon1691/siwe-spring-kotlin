# SIWE Spring Boot (Kotlin) starter

Minimal working Spring Boot 3 + Kotlin project that implements the SIWE (EIP-4361) flow:

- `GET /api/auth/siwe/nonce` issues a one-time nonce (stored in-memory, TTL-based)
- Client builds an EIP-4361 message and signs it with `personal_sign`
- `POST /api/auth/siwe/verify` validates fields + verifies signature and returns a simple access token

## Requirements
- Java 21+
- Gradle installed (wrapper is not included in this zip)

## Run
```bash
./gradlew bootRun
```
Or import into IntelliJ IDEA and run `SiweSpringKotlinApplication`.

## Endpoints

### Get nonce
```bash
curl http://localhost:8080/api/auth/siwe/nonce
```

### Verify
Request body:
```json
{
  "message": "your EIP-4361 message string",
  "signature": "0x..."
}
```

Response body:
```json
{
  "address": "0xabc...",
  "accessToken": "..."
}
```

## Config
`src/main/resources/application.yml`
