# Microservices Learning Plan — Simple "Book Store"

Goal: learn microservice concepts step by step using a tiny domain.
Business logic is intentionally simple — the focus is on how services talk, scale, and fail.

**Stack:** Java 21, Spring Boot 4.1.2, Spring Cloud 2025.1.x (from Phase 3), Maven (multi-module), H2, Docker

> Note: `JAVA_HOME` on this machine points to Java 8. Run Maven with Java 21:
> `export JAVA_HOME=/usr/lib/jvm/jdk-21.0.7-oracle-x64`

---

## 1. Architecture Overview

```
                ┌──────────────────┐
  Client ──────▶│   API Gateway    │  :8080
                └────────┬─────────┘
                         │ routes by service name (lb://)
        ┌────────────────┼────────────────┐
        ▼                ▼                ▼
 ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
 │ book-service│  │ user-service│  │order-service│──calls──▶ book & user
 │    :8081    │  │    :8082    │  │    :8083    │
 └─────────────┘  └─────────────┘  └──────┬──────┘
                                          │ publishes OrderPlacedEvent
                                          ▼
                                   ┌──────────────┐     ┌──────────────────────┐
                                   │    Kafka     │────▶│ notification-service │ :8084
                                   └──────────────┘     └──────────────────────┘

  Supporting:  discovery-server (Eureka) :8761   |   config-server :8888   |   Zipkin :9411
```

## 2. Services

| Service | Port | Responsibility | Database |
|---|---|---|---|
| discovery-server | 8761 | Eureka service registry | — |
| config-server | 8888 | Central config for all services | — |
| api-gateway | 8080 | Single entry point, routing | — |
| book-service | 8081 | CRUD for books | H2 (own) |
| user-service | 8082 | CRUD for users | H2 (own) |
| order-service | 8083 | Place orders (calls book + user) | H2 (own) |
| notification-service | 8084 | Consume order events, log "notification sent" | — |

### Data models (keep them minimal)

```
Book   { id, title, author, price, stock }
User   { id, name, email }
Order  { id, userId, bookId, quantity, totalPrice, status, createdAt }
```

### Endpoints

**book-service**
- `GET    /books`
- `GET    /books/{id}`
- `POST   /books`
- `PUT    /books/{id}`
- `DELETE /books/{id}`
- `PUT    /books/{id}/reduce-stock?qty=N`

**user-service**
- `GET    /users`
- `GET    /users/{id}`
- `POST   /users`
- `DELETE /users/{id}`

**order-service**
- `POST   /orders`  body: `{ "userId": 1, "bookId": 1, "quantity": 2 }`
- `GET    /orders`
- `GET    /orders/{id}`

### Order logic (the only "business logic")
1. Call user-service → user exists? else 404
2. Call book-service → book exists and `stock >= quantity`? else 400
3. `totalPrice = book.price * quantity`
4. Call book-service to reduce stock
5. Save order with status `PLACED`
6. (Phase 7) Publish `OrderPlacedEvent` to Kafka

---

## 3. Project Structure

```
Microservice-Learning/
├── PLAN.md
├── pom.xml                    # parent POM: modules, Java version, common deps, Spring Cloud BOM
├── mvnw / .mvn/               # Maven wrapper
├── discovery-server/
├── config-server/
│   └── src/main/resources/config/   # book-service.yml, order-service.yml ...
├── api-gateway/
├── book-service/
├── user-service/
├── order-service/
├── notification-service/
└── docker-compose.yml
```

Each business service follows the same package layout:

```
com.learning.<service>/
├── controller/     # REST endpoints
├── service/        # simple logic
├── repository/     # Spring Data JPA
├── entity/         # JPA entities
├── dto/            # request/response objects
├── client/         # Feign clients (order-service only)
├── exception/      # global exception handler
└── config/         # sample data loader
```

### Build & run

```bash
./mvnw clean verify                         # build + test all modules
./mvnw -pl book-service spring-boot:run     # run one service
java -jar book-service/target/book-service-0.0.1-SNAPSHOT.jar
```

---

## 4. Phases

Finish and run each phase before moving to the next. Commit after each phase.

### Phase 1 — Independent services ✅
- [x] Maven multi-module project (parent `pom.xml` with `<modules>`)
- [x] Create `book-service` (Web MVC, JPA, H2, Lombok, Validation, Actuator)
- [x] Create `user-service` (same dependencies)
- [x] Add seed data via `CommandLineRunner` (`DataLoader`)
- [x] Global exception handler (`@RestControllerAdvice`)
- [x] Test with curl / Postman

**Concepts:** service independence, database per service, separate ports

### Phase 2 — Inter-service communication (sync)
- [ ] Create `order-service`
- [ ] First version: call book/user using `RestClient` with hardcoded URLs
- [ ] Second version: replace with **OpenFeign** (`@FeignClient`)
- [ ] Handle errors when a called service returns 404

**Concepts:** synchronous REST calls, DTOs across services, why hardcoded URLs are bad

### Phase 3 — Service discovery
- [ ] Create `discovery-server` with `@EnableEurekaServer`
- [ ] Register book, user, order services as Eureka clients
- [ ] Change Feign clients to use names: `@FeignClient(name = "book-service")`
- [ ] Run 2 instances of book-service (ports 8081 & 8091) and observe load balancing

**Concepts:** service registry, client-side load balancing, dynamic instances

### Phase 4 — API Gateway
- [ ] Create `api-gateway` (Spring Cloud Gateway)
- [ ] Routes:
  - `/api/books/**`  → `lb://book-service`
  - `/api/users/**`  → `lb://user-service`
  - `/api/orders/**` → `lb://order-service`
- [ ] Add a simple global logging filter (log path + response time)

**Concepts:** single entry point, routing, cross-cutting filters

### Phase 5 — Centralized configuration
- [ ] Create `config-server` (`@EnableConfigServer`, native/file-based backend)
- [ ] Move service configs (ports, DB, custom messages) into config-server
- [ ] Services use `spring.config.import=optional:configserver:http://localhost:8888`
- [ ] Try `dev` vs `prod` profiles

**Concepts:** externalized config, profiles, config refresh

### Phase 6 — Resilience
- [ ] Add **Resilience4j** to order-service
- [ ] `@CircuitBreaker` + fallback on book-service call
- [ ] Add `@Retry` and a timeout
- [ ] Stop book-service → verify fallback response; restart → verify recovery
- [ ] View circuit state via `/actuator/circuitbreakers`

**Concepts:** circuit breaker states (CLOSED / OPEN / HALF_OPEN), fallback, retry

### Phase 7 — Asynchronous messaging
- [ ] Run Kafka via Docker
- [ ] order-service publishes `OrderPlacedEvent { orderId, userId, bookId, totalPrice }`
- [ ] Create `notification-service` that consumes the event and logs `"Notification sent for order X"`
- [ ] Stop notification-service, place orders, restart → events still delivered

**Concepts:** event-driven architecture, loose coupling, sync vs async

### Phase 8 — Observability
- [ ] Add Actuator to all services (`/actuator/health`, `/actuator/info`)
- [ ] Add Micrometer Tracing + Zipkin
- [ ] Place an order through the gateway, trace it end-to-end in Zipkin UI
- [ ] Include traceId in logs

**Concepts:** health checks, distributed tracing, correlated logs

### Phase 9 — Containerization
- [ ] `Dockerfile` for each service
- [ ] `docker-compose.yml` with all services + Kafka + Zipkin
- [ ] Use service names as hostnames inside Docker network
- [ ] Start the full system with `docker compose up`

**Concepts:** containers, networking, environment-based config

### Phase 10 — Security (optional)
- [ ] JWT validation at the API Gateway (simple auth-service or Keycloak)
- [ ] Pass user identity downstream via headers

**Concepts:** edge authentication, token propagation

---

## 5. Startup Order (local, without Docker)

1. discovery-server
2. config-server
3. book-service, user-service
4. order-service
5. notification-service
6. api-gateway

## 6. Sample Test Flow

```bash
# create a user
curl -X POST localhost:8080/api/users -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@example.com"}'

# create a book
curl -X POST localhost:8080/api/books -H "Content-Type: application/json" \
  -d '{"title":"Learning Microservices","author":"Someone","price":20.0,"stock":5}'

# place an order
curl -X POST localhost:8080/api/orders -H "Content-Type: application/json" \
  -d '{"userId":1,"bookId":1,"quantity":2}'

# check stock reduced
curl localhost:8080/api/books/1
```

## 7. Concepts Checklist

- [x] Database per service
- [ ] Synchronous communication (REST / Feign)
- [ ] Service discovery (Eureka)
- [ ] Client-side load balancing
- [ ] API Gateway
- [ ] Centralized configuration
- [ ] Circuit breaker / retry / fallback
- [ ] Asynchronous messaging (Kafka)
- [ ] Distributed tracing
- [ ] Containerization (Docker Compose)
- [ ] Gateway security (optional)
