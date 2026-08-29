# API Gateway

Single entry point for all microservices with JWT authentication, rate limiting, circuit breakers, and observability.

## Tech Stack

- Spring Boot 4.1
- Java 25
- Spring Cloud Gateway
- Spring Security 7
- Redis (Rate Limiting)
- Resilience4j (Circuit Breaker)
- Eureka (Service Discovery)
- Gradle

## Architecture

```
Client -> API Gateway -> Auth Service  (/auth-service/**)
                    -> User Service    (/user-service/**)
```

The gateway validates JWTs locally (RS256 public key), enforces CORS / security
headers, rate limits by user/IP, opens circuit breakers with fallbacks, and
injects a correlation id across every request.

## Setup

### 1. Prerequisites

- Java 25
- Redis 7+ (for rate limiting)
- Eureka server running (default `http://localhost:8761/eureka/`)
- Registered downstream services (`auth-service`, `user-service`)

### 2. Configure JWT Public Key

`src/main/resources/application.yaml` must contain the auth-service's RSA **public** key:

```yaml
jwt:
  public-key: |
    -----BEGIN PUBLIC KEY-----
    YOUR_PUBLIC_KEY_HERE
    -----END PUBLIC KEY-----
```

### 3. Run Redis

```bash
docker run -d -p 6379:6379 --name redis redis:7-alpine
```

### 4. Build & Run

```bash
./gradlew bootRun
```

Or with dev profile:

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## Endpoints

| Path | Target Service | Auth Required |
|------|---------------|---------------|
| `/auth-service/**` | Auth Service | No (public: login/register/refresh/validate) |
| `/user-service/**` | User Service | Yes |
| `/actuator/health` | Gateway | No |
| `/actuator/prometheus` | Gateway | No |

Routes rewrite `/auth-service/**` -> `/api/auth/**` and
`/user-service/**` -> `/api/users/**` before forwarding downstream.

## Headers Forwarded to Downstream

When a valid JWT is present, the Gateway forwards:

- `X-User-Id` - UUID of authenticated user
- `X-Username` - Username
- `X-Correlation-Id` - Trace ID for distributed logging

## Public Paths (No Auth)

Configured in `application.yaml`:

- `/auth-service/login`
- `/auth-service/register`
- `/auth-service/refresh`
- `/auth-service/validate`
- `/actuator/**`
- `/fallback/**`

## Rate Limiting

Uses Redis-based rate limiting:
- Authenticated users: 100 req/sec (by `X-User-Id`)
- Anonymous users: 10 req/sec (by IP)

## Circuit Breaker

Fallback endpoints activated when downstream services are down:
- `/fallback/auth`  (auth-service)
- `/fallback/users` (user-service)