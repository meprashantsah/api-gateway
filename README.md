# API Gateway

Single entry point for all microservices with JWT authentication, rate limiting, circuit breakers, and observability.

## Tech Stack

- Spring Boot 4.1
- Java 25
- Spring Cloud Gateway
- Spring Security 7
- Redis (Rate Limiting)
- Resilience4j (Circuit Breaker)
- Gradle

## Architecture

```
Client -> API Gateway -> Auth Service (login/refresh)
                    -> Order Service (protected)
                    -> Product Service (protected)
                    -> Admin Service (protected)
```

## Setup

### 1. Prerequisites

- Java 25
- Gradle 8.x
- Redis 7+ (for rate limiting)

### 2. Configure JWT Public Key

Update `src/main/resources/application.yml` with your Auth Service's RSA public key:

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
| `/api/auth/**` | Auth Service | No (public paths) |
| `/api/orders/**` | Order Service | Yes |
| `/api/products/**` | Product Service | Yes |
| `/api/admin/**` | Admin Service | Yes |
| `/actuator/health` | Gateway | No |
| `/actuator/prometheus` | Gateway | No |

## Headers Forwarded to Downstream

When a valid JWT is present, the Gateway forwards:

- `X-User-Id` - UUID of authenticated user
- `X-Username` - Username
- `X-Roles` - Comma-separated roles (e.g., `USER,ADMIN`)
- `X-Correlation-Id` - Trace ID for distributed logging

## Public Paths (No Auth)

Configured in `application.yml`:

- `/api/auth/login`
- `/api/auth/register`
- `/api/auth/refresh`
- `/api/auth/validate`
- `/actuator/**`
- `/fallback/**`

## Rate Limiting

Uses Redis-based rate limiting:
- Authenticated users: 100 req/sec (by userId)
- Anonymous users: 10 req/sec (by IP)

## Circuit Breaker

Fallback endpoints activated when downstream services are down:
- `/fallback/auth`
- `/fallback/orders`
- `/fallback/products`
- `/fallback/admin`
