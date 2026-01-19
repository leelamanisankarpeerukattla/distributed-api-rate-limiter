# Distributed API Rate Limiter & Protection Service
![CI](https://github.com/leelamanisankarpeerukattla/distributed-api-rate-limiter/actions/workflows/ci.yml/badge.svg)
![License](https://img.shields.io/badge/license-MIT-blue.svg)


**Java • Spring Boot • Redis (Lua) • Docker • GitHub Actions • Actuator/Prometheus**

A production-style, distributed rate limiting service that enforces per-user, per-IP, and per-endpoint limits using **atomic Redis Lua scripts**. Supports **Token Bucket** and **Sliding Window Log** algorithms, configurable policies, and standard rate-limit headers.

---

## At-a-glance

- **Token Bucket** (burst-friendly)
- **Sliding Window Log** (precise window enforcement)
- **Per-user / per-IP / per-endpoint** policies
- **Fail-open / Fail-closed** behavior on Redis outages
- **Metrics** + Prometheus endpoint via Actuator

---

## Architecture

```text
Client
  |
  |  POST /v1/ratelimit/check
  v
Spring Boot API
  |
  |  atomic eval via Redis Lua scripts
  v
Redis (hash/zset)
```

### Request flow
### Sequence Diagram
```mermaid
sequenceDiagram
  autonumber
  participant C as Client
  participant API as RateLimiter API (Spring Boot)
  participant PM as Policy Matcher
  participant KR as Key Resolver
  participant R as Redis (Lua)

  C->>API: POST /v1/ratelimit/check { endpoint, tokens, key? }
  API->>PM: Match policy for endpoint
  PM-->>API: policy (id, algorithm, limits, keyType, mode)
  API->>KR: Resolve identifier (USER / API / IP)
  KR-->>API: resolvedKey + redisKey
  API->>R: EVAL Lua script (Token Bucket / Sliding Window)
  R-->>API: decision (allowed, remaining, resetEpochMs, retryAfterMs)
  API-->>C: JSON response + headers
```

### Decision Flow
```mermaid
flowchart TD
  A[Incoming request] --> B[POST /v1/ratelimit/check]
  B --> C[Match policy by endpoint]
  C --> D[Resolve key: USER / API / IP]
  D --> E[Build Redis key]
  E --> F{Algorithm?}

  F -->|Token Bucket| G[Lua: refill + consume tokens]
  F -->|Sliding Window| H[Lua: prune window + count requests]

  G --> I{Allowed?}
  H --> I{Allowed?}

  I -->|Yes| J[allowed=true, Retry-After=0]
  I -->|No| K[allowed=false, retryAfterMs > 0]

  J --> L[Set rate-limit headers]
  K --> L
  L --> M[Return response]
```


---

## API

### Check a request
`POST /v1/ratelimit/check`

**Body**
```json
{
  "key": "user:123",
  "endpoint": "POST:/orders",
  "tokens": 1
}
```

**Allowed**
```json
{
  "allowed": true,
  "policyId": "perUserOrders",
  "key": "user:123",
  "endpoint": "POST:/orders",
  "limit": 20,
  "remaining": 19,
  "resetEpochMs": 1730000000000,
  "retryAfterMs": 0,
  "modeUsed": "FAIL_CLOSED"
}
```

**Blocked**
```json
{
  "allowed": false,
  "policyId": "perUserOrders",
  "key": "user:123",
  "endpoint": "POST:/orders",
  "limit": 20,
  "remaining": 0,
  "resetEpochMs": 1730000000000,
  "retryAfterMs": 1200,
  "modeUsed": "FAIL_CLOSED"
}
```

**Curl**
```bash
curl -s http://localhost:8085/v1/ratelimit/check \
  -H 'Content-Type: application/json' \
  -H 'X-User-Id: 123' \
  -d '{"endpoint":"POST:/orders","tokens":1}' | jq
```

### View policies
`GET /v1/ratelimit/policies`

---

## Configuration

Policies live in `src/main/resources/application.yml`:

```yml
ratelimiter:
  defaultMode: FAIL_CLOSED
  keyPrefix: rl
  policies:
    - id: perUserOrders
      match:
        endpoint: "POST:/orders"
      keyType: USER
      algorithm: TOKEN_BUCKET
      capacity: 20
      refillTokens: 20
      refillPeriodMs: 60000

    - id: perIpGlobal
      match:
        endpoint: "*"
      keyType: IP
      algorithm: SLIDING_WINDOW
      limit: 300
      windowMs: 60000
```

Key resolution:
- `USER` → `X-User-Id` header (or explicit `key` in request)
- `API` → `X-Api-Key` header
- `IP` → `X-Forwarded-For` (first value) or request remote address

---

## Design Decisions

- **Redis Lua scripts** ensure rate limit checks are **atomic** and safe under concurrency.
- **Token Bucket** provides burst handling while enforcing sustained rate limits.
- **Sliding Window Log** offers precise window enforcement (useful for strict APIs).
- **Fail-open / fail-closed** behavior is configurable to trade off availability vs security.

---

## Metrics & Observability

- Health: `GET /actuator/health`
- Prometheus: `GET /actuator/prometheus`


---

## Quick start 

### Prerequisites
- Docker Desktop

### Run everything
```bash
docker compose up --build
```

Service runs at:
- `http://localhost:8085`

---

## Testing

```bash
mvn test
```

---


