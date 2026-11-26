# Key Engineering Competencies & Architecture Patterns

This document outlines the core architectural patterns, distributed systems paradigms, security controls, and cloud-native operational practices actively implemented across the **Take My Order** ecosystem.

---

## 1. Clean & Hexagonal Architecture (Ports and Adapters)

*   **Ports & Adapters Boundary Isolation:** Domain cores maintain strict decoupling from Spring Boot, JPA, HTTP controllers, and Kafka drivers through explicit input and output port interfaces.
*   **Rich Domain Model:** Business rules, state transition invariants (`OrderStatus`, wallet and escrow lifecycles, role/user rules), and validation logic are encapsulated inside domain entities and aggregates rather than anemic services.
*   **Framework-Decoupled Domain Cores:** `order-domain-core`, `payment-domain-core`, `restaurant-domain-core`, and `identity-domain-core` declare only `common-domain` (plus `uuid-creator` for UUIDv7 generation in Identity) in their module POMs and keep zero Spring, JPA/Hibernate, Web, or Kafka imports in source code (relying solely on Java 21, Lombok/SLF4J, and pure domain models).

---

## 2. Distributed Consistency, SAGA Orchestration & CQRS

*   **Orchestrated 10-State Forward & Cancel SAGA (`order-service`):** Coordinates long-running distributed order lifecycles (`PENDING` → `PAID` → `WAITING_FOR_APPROVAL` → `APPROVED` → `PREPARING` → `READY_FOR_PICKUP` → `IN_DELIVERY` → `DELIVERED` / `CANCELLING` → `CANCELLED`) through state machines and asynchronous outbox events without synchronous service-to-service HTTP coupling.
*   **Cancel SAGA Fork-Join Barrier & Material Actions (`OrderCancelHelper`, `OrderPaymentSaga`, `OrderApprovalSaga`):** Evaluates cancellation eligibility and `MaterialAction` (`RESTORE_STOCK` vs. `RECORD_WASTE`), handles zero-Kafka customer-fault cancellation shortcuts, and finalizes `CANCELLING` → `CANCELLED` only after both payment refund and restaurant compensation branches converge as `COMPENSATED`.
*   **Hybrid Consistency & LWW Eventual Projections:** Enforces ACID guarantees inside local aggregate/outbox boundaries while propagating cross-service read state via Kafka — including verified post-delivery buyer reviews and Last-Write-Wins (`updatedAt`) rating updates (`RestaurantRatingMessageListenerImpl`) that discard stale out-of-order events.

---

## 3. Database Architecture & Persistence Strategy

*   **Logical Schema Isolation (Schema-per-Service):** Services connect to dedicated PostgreSQL schemas (`order`, `payment`, `restaurant`, `identity`, plus `keycloak` for IAM) configured at the JDBC connection-pool and Flyway schema levels (`?currentSchema=<service>`), keeping query scopes and JPA models strictly isolated per service boundary without cross-schema joins.
*   **Bare-Metal Host Persistence Engine:** PostgreSQL 16 runs directly on the ARM64 master host (`k8s-master-infra`), leveraging host SSD storage and Linux page cache without container filesystem virtualization overhead.
*   **Deterministic Schema Evolution:** Structural database versioning across local Docker Compose and Kubernetes overlays is governed by **Flyway** migrations packaged inside each service's `*-dataaccess` module (`hibernate.ddl-auto: validate`).

---

## 4. Data Integrity, Idempotency & Asynchronous Messaging Governance

*   **Transactional Outbox Pattern:** Dual-write anomalies are prevented by persisting outbox event records within the same local ACID transaction as aggregate state mutations.
*   **Outbox Polling & Contention Safeguards:** Scheduled outbox pollers relay `STARTED` messages to Kafka, utilizing `@Version` optimistic locking on JPA outbox entities (such as `PaymentOutboxEntity`, `ApprovalOutboxEntity`, and `RoleOutboxEntity`) alongside PostgreSQL `SELECT ... FOR UPDATE SKIP LOCKED` locking in `RoleOutboxJpaRepository` (`identity-service`).
*   **Apache Avro & Confluent Schema Registry:** Inter-service events use **Apache Avro** (`SpecificRecordBase`, `specific.avro.reader: true`) serialized via `KafkaAvroSerializer` and `KafkaAvroDeserializer` with **Confluent Schema Registry** (`confluentinc/cp-schema-registry:7.2.5`) for compact binary transfer and backward schema compatibility.
*   **Multi-Layer Idempotency & Consumer Deduplication:** Public mutating endpoints (`POST /orders`, `POST /wallets/{ownerId}/deposit`, `POST /wallets/{ownerId}/withdraw`) require `Idempotency-Key` headers backed by unique database constraints (`orders_idempotency_key_key`, `wallet_transactions_idempotency_key_key`), while Kafka consumers guard against at-least-once redeliveries via outbox `(type, sagaId, status)` lookups, pessimistic `FOR UPDATE` locking (`order_escrows`), existence checks (`wallets`), and `updatedAt` LWW timestamp comparisons.

---

## 5. Enterprise Identity, Access Management & Security (IAM)

*   **Headless IAM & Portal-Segregated Facade:** Angular frontends never redirect to Keycloak or call Keycloak endpoints directly; `gateway-service` routes `/auth/login/{customer|merchant|internal}` through `identity-service` (`AuthCommandHandler` + `UserAuthController`), pre-checking local `AccountStatus.BANNED` status and verifying that the issued JWT `user_type` claim matches the target portal.
*   **Split-Horizon JWT Verification & JWK Caching:** `gateway-service` and downstream microservices validate RS256 JWT signatures locally in memory via Spring Security `ReactiveJwtDecoder` (gateway) and `JwtDecoder` (`JwtSecurityFilter` in downstream services) using JWK public keys fetched internally over `KEYCLOAK_SERVER_URL` (`http://keycloak-service:8080`) while matching the public `iss` claim (`KEYCLOAK_ISSUER_URI`).
*   **Dual-Tier RBAC & Scalar Caffeine Caching:** Supports Flat platform roles (`INTERNAL` / `SYSTEM_ADMIN`) and tenant-scoped restaurant roles (`MERCHANT`) under strict permission subset delegation. `JwtSecurityFilter` extracts `internal_id`, `user_type`, `sid`, and `role_ids`, while `@PreAuthorize` evaluators (`OrderAuthService`, `PaymentAuthService`, `RestaurantAuthService`) verify real-time `organizational_unit_id` boundaries and permission sets against local replicated role tables backed by scalar **Caffeine** caches (`CacheConfig`, caching permission sets and org-unit UUIDs rather than heavy JPA entities, evicted on role/permission mutations) — avoiding stale-token 403 errors and JWT bloat from deprecated `organizational_unit_ids` claims.
*   **Redis Multi-Key Token Revocation (`multiGet`):** Role updates/deletions, role assignments, profile updates, account bans, and single/global logouts write revocation entries to Redis (`user:<id>:revoke` and `role:<roleId>:revoke` with `iat` timestamps evaluated under a `5s` clock-skew grace period, plus `sid:<sid>:revoke` session-block markers for single logout). `JwtSecurityFilter` evaluates all applicable keys in a single Redis `multiGet` lookup, throwing `ACCESS_TOKEN_REVOKED` or `ALL_TOKENS_REVOKED` so clients refresh access tokens silently on permission updates.
*   **Crash-Safe Dual-Store User Mutation (`UserUpdateIntent`):** Distributed user mutations spanning Keycloak and PostgreSQL are coordinated by single-active-intent partial unique indexes (`idx_user_active_intent WHERE status IN ('STARTED', 'KEYCLOAK_DONE')`) on `user_update_intent` and recovered asynchronously via `UserUpdateIntentScheduler` (with a 15-minute stale-intent timeout).

---

## 6. Financial Integrity & Escrow Mechanics

*   **Mini Order Escrow Pool (`payment.order_escrows`):** Customer checkout payments are debited into isolated, order-scoped escrow records (`HOLDING`) keyed by unique `order_id` rather than credited directly to merchant balances, preventing early withdrawals prior to delivery or cancellation refunds.
*   **Row-Isolated Pessimistic Concurrency:** Order-level `order_escrows` rows combined with `SELECT ... FOR UPDATE` locks (`findByOrderIdWithLock`, `findByOwnerIdWithLock`) prevent cross-order lock contention during concurrent checkouts and payouts.
*   **Asynchronous Delivery Settlement Choreography:** Upon `DELIVERED`, an asynchronous `order-delivered` Kafka event triggers `OrderDeliveredMessageListenerImpl` in `payment-service` to transition escrow to `RELEASED` idempotently and credit the merchant wallet with an immutable `ORDER_PAYOUT` ledger record.

---

## 7. Cross-Tenancy Cloud Infrastructure & Edge Architecture

*   **7-Node Cross-Tenancy OCI Topology:** Orchestrates **8 OCPU / 48 GB RAM** across two independent Oracle Cloud ARM64 accounts (**6 K3s cluster nodes** — 1 Master, 3 App Workers, 1 Edge Worker, 1 Observability Worker — plus **1 standalone Jenkins server**).
*   **Local Peering Gateway (LPG) Private Mesh:** Interconnects both cloud VCNs (`10.0.0.0/16` ↔ `10.1.0.0/16`) over Oracle's private optical backplane with `< 0.5 ms` latency and zero public egress for PostgreSQL (`:5432`), K3s control plane (`:6443`), and Kafka brokers (`:9092`).
*   **Horizontal Pod Autoscaling (HPA) & Anti-Affinity:** `autoscaling/v2` HPAs target `70%` CPU utilization (`maxReplicas: 2` for `gateway-service`, `maxReplicas: 3` for domain microservices) paired with `podAntiAffinity` (`preferredDuringSchedulingIgnoredDuringExecution`) to spread replicas across separate app worker nodes.
*   **Zero-Ingress Edge (`Cloudflare Flexible SSL + Origin Rules` + Bare-Metal Host Nginx):** Routes the 7 subdomains cleanly through Cloudflare Origin Rules (`Host Header: Preserve`) directly to Kubernetes NodePorts and Edge Bare-Metal Host Nginx (`:30088` serving the 3 Angular SPA virtual hosts without conflicting with K3s Traefik `80`/`443`).

---

## 8. Observability & Site Reliability Engineering (SRE)

*   **HTTP Distributed Tracing (`Zipkin`):** All five Spring Boot services (`gateway`, `identity`, `order`, `payment`, `restaurant`) export trace spans via `micrometer-tracing-bridge-brave` and `zipkin-reporter-brave` to an internal `ClusterIP` Zipkin instance (`:9411`, `0.1` production sampling ratio).
*   **Metrics Collection & Out-of-Band Monitoring:** **Prometheus** (`ClusterIP :9090`) scrapes `kubernetes-apiservers` (`15s` global interval) and each microservice's `/actuator/prometheus` endpoint (`5s` scrape interval) on `k8s-worker-obs`, powering **Grafana** (`NodePort :30030`) alongside login-protected **Kafka UI** (`NodePort :30091`). Prometheus and Zipkin remain strictly internal `ClusterIP` services with no public port exposure.
*   **Zero-Downtime Probes & Rolling Updates:** All five microservice deployments enforce `maxSurge: 1`, `maxUnavailable: 0`, and HTTP `livenessProbe` / `readinessProbe` (`/actuator/health`) so Kubernetes verifies readiness and drains active traffic cleanly before terminating old pods.

---

## 9. Unified Jenkins CI/CD & Fallback GitHub Workflows

*   **Self-Hosted Native ARM64 Jenkins Factory (`Jenkinsfile` & `take-my-order-fe/Jenkinsfile`):** Active production CI/CD runs on `devops-jenkins-server` (`<JENKINS_PRIVATE_IP>`). The backend pipeline compiles targeted Maven modules natively on ARM64 without QEMU emulation, streams compressed Docker images over private SSH (`docker save | gzip` → `gunzip | sudo k3s ctr -n k8s.io images import -`) directly into cluster `containerd` caches with zero external registry dependency, and executes `kubectl rollout restart` + `kubectl rollout status --timeout=300s` (with automatic `kubectl rollout undo` rollback). For the private frontend monorepo, Jenkins inspects `git diff` paths (`Node.js 22`, `npm ci --legacy-peer-deps`) and delivers built bundles over private `rsync` to Host Nginx.
*   **GitHub Actions Manual Multi-Arch Fallback & Docs Publishing (`.github/workflows/`):** `.github/workflows/deploy.yml` retains QEMU + Docker Buildx multi-arch (`linux/amd64`, `linux/arm64`) compilation and SSH rollout strictly under manual `workflow_dispatch` triggers (push triggers disabled in favor of Jenkins), while `.github/workflows/docs.yml` automates MkDocs builds and GitHub Pages deployment on `prod` documentation changes.
