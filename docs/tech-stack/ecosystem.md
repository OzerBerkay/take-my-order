# Technology Ecosystem

**Take My Order** combines a Java 21 / Spring Boot 3.4 microservices backend, an Apache Kafka / Avro streaming backbone, a Bare-Metal PostgreSQL 16 persistence tier, Keycloak OIDC + Redis/Caffeine security controls, and a Cross-Tenancy ARM64 K3s infrastructure orchestrated by native ARM64 Jenkins CI/CD.

---

## 1. Core Stack Overview

| Category | Technologies, Versions & Production Role |
| :--- | :--- |
| **Language & Backend Frameworks** | **Java 21 (LTS)** (`eclipse-temurin:21-jre-alpine`), **Spring Boot 3.4.5**, **Spring Cloud 2024.0.0** (Reactive WebFlux Gateway), Spring WebMVC, Spring Data JPA (Hibernate), Spring Security & OAuth2 Resource Server, Jakarta Validation, **Lombok 1.18.38**, **uuid-creator 5.3.7** (UUIDv7 in `identity-service`), JUnit 5 / Mockito *(+ H2 for `restaurant-dataaccess` tests)* |
| **Messaging & Event Streaming** | **Apache Kafka** (`confluentinc/cp-kafka:7.2.5`, 3-broker cluster), **Apache Zookeeper** (`confluentinc/cp-zookeeper:7.2.5`), **Confluent Schema Registry** (`confluentinc/cp-schema-registry:7.2.5`), **Apache Avro 1.12.0** (`kafka-avro-serializer 8.0.0`), **Spring Kafka 3.2.4**, **Kafka UI** (`provectuslabs/kafka-ui:latest`) |
| **Data, Caching & Migrations** | **PostgreSQL 16** (Bare-Metal Host on `k8s-master-infra` in production; Host `:5432` / optional Compose `:5433` in Mode A; dev-only in-cluster `postgres:16` pod in Mode B — Schema-per-Service), **Redis** (`redis:alpine`, multi-key token revocation), **Caffeine** (L1 scalar RBAC cache), **Flyway** (`flyway-core` + `flyway-database-postgresql`, `ddl-auto: validate`) |
| **Security & IAM** | **Keycloak 24.0.1** (`quay.io/keycloak/keycloak:24.0.1`, OAuth2 / OIDC), **keycloak-admin-client 25.0.6** & **RESTEasy 6.2.4.Final** (`identity-infrastructure`), Headless IAM facade, Dual-Tier RBAC with local replicated role tables + scalar Caffeine caches (`@PreAuthorize`), Redis `multiGet` token revocation (`user:`, `sid:`, `role:`) |
| **Cloud, Edge & Orchestration** | **K3s** (`containerd`), **Flannel VXLAN CNI** (`10.42.0.0/16`, `UDP 8472`), **2x Oracle Cloud ARM64 Ampere A1** (**8 OCPU / 48 GB RAM / 400 GB SSD** total: 6 K3s cluster nodes = **7 OCPU / 42 GB RAM** + 1 standalone Jenkins server = **1 OCPU / 6 GB RAM**), **OCI VCN Local Peering Gateway (LPG)** (`10.0.0.0/16` ↔ `10.1.0.0/16`), **Cloudflare Flexible SSL + Origin Rules** (Zero-Ingress Edge), **Bare-Metal Host Nginx** (`:30088` on `k8s-worker-edge`), **Docker & Docker Compose** (local development) |
| **Observability & Diagnostics** | **Prometheus** (`prom/prometheus:v2.45.0`, `ClusterIP :9090`), **Grafana** (`grafana/grafana:latest`, `NodePort :30030`), **Zipkin** (`openzipkin/zipkin`, `ClusterIP :9411`), **Micrometer** (`micrometer-registry-prometheus`, `micrometer-tracing-bridge-brave`, `zipkin-reporter-brave`), **Spring Boot Actuator** (`/actuator/health`, `/actuator/prometheus`) |
| **DevOps, CI/CD & API Tooling** | **Jenkins** (Native ARM64 on `devops-jenkins-server`, 1 OCPU / 6 GB RAM; parameterized Maven `-pl -am`, SSH `docker save` / `gzip` → `k3s ctr images import`, automatic `kubectl rollout undo`), **GitHub Actions** (manual `workflow_dispatch` QEMU + Docker Buildx multi-arch fallback; automated **MkDocs Material** GitHub Pages publishing), **Kustomize** (`base/` + `overlays/dev/` & `overlays/central-hub/`), **Apache Maven**, **SpringDoc OpenAPI 2.8.5** (Swagger Hub), **Postman** (`TakeMyOrder.postman_collection.json`, 106 endpoints) |
| **Frontend Multi-Portal Client** *(Private Companion Repo)* | **Angular 22**, **TypeScript 6**, **RxJS 7.8**, **Node.js 22 LTS** — 3 role-isolated SPA portals (`customer`, `merchant`, `admin`) built via Jenkins `git diff` change-detection and deployed over private `rsync` to Host Nginx (`:30088`) |

---

## 2. Runtime Container & Environment Matrix

| Component | Local Development (`docker-compose.yml` / IDE) | Production Cluster (`overlays/central-hub/` on OCI ARM64) |
| :--- | :--- | :--- |
| **Microservices (5x)** | Run directly on JDK 21 (IDE / Maven) on ports `8080`, `8181–8184` | `eclipse-temurin:21-jre-alpine` pods scheduled on `k8s-worker-01..03` & `k8s-worker-edge` (`gateway-service` `NodePort :30180`) |
| **PostgreSQL** | Host **PostgreSQL 16** (`localhost:5432`) / optional Compose `postgres:16` (`:5433`); dev-only `postgres:16` pod in K8s `overlays/dev` | **Bare-Metal PostgreSQL 16** on `k8s-master-infra` (`<MASTER_PRIVATE_IP>:5432`, private LPG mesh only) |
| **Kafka & Zookeeper** | `confluentinc/cp-kafka:7.2.5` (3 brokers on `localhost:31091–31093`) & `cp-zookeeper:7.2.5` (`:2181`) | `confluentinc/cp-kafka:7.2.5` (3 brokers, `ClusterIP :9092`) & `cp-zookeeper:7.2.5` (`ClusterIP :2181`) on `k8s-master-infra` |
| **Schema Registry** | `confluentinc/cp-schema-registry:7.2.5` (`localhost:30081`) | `confluentinc/cp-schema-registry:7.2.5` (`ClusterIP :8081`) on `k8s-master-infra` |
| **Redis** | `redis:alpine` (`localhost:6379`) | `redis:alpine` (`ClusterIP :6379`) on `k8s-master-infra` |
| **Keycloak IAM** | `quay.io/keycloak/keycloak:24.0.1` (`localhost:30080`) | `quay.io/keycloak/keycloak:24.0.1` (`NodePort :30080`) on `k8s-master-infra`, fronted by Cloudflare `443 → 30080` |
| **Kafka UI** | `provectuslabs/kafka-ui:latest` (`localhost:8082`) | `provectuslabs/kafka-ui:latest` (`NodePort :30091`, auth-protected) on `k8s-worker-obs` |
| **Zipkin** | `openzipkin/zipkin:latest` (`localhost:30094`) | `openzipkin/zipkin` (`ClusterIP :9411`, internal only) on `k8s-worker-obs` |
| **Prometheus & Grafana** | Deployed via Kustomize `overlays/dev/` when needed | `prom/prometheus:v2.45.0` (`ClusterIP :9090`) & `grafana/grafana:latest` (`NodePort :30030`) on `k8s-worker-obs` |
| **Frontend SPAs & Edge** | Angular Dev Server (`localhost:4200–4202`) | **Bare-Metal Host Nginx (`:30088`)** on `k8s-worker-edge` behind **Cloudflare Flexible SSL + Origin Rules** |

---

## 3. Architectural Technology Rationale

*   **Bare-Metal PostgreSQL 16 on `k8s-master-infra`:** Running PostgreSQL directly on the ARM64 master host gives database storage direct access to host SSDs and the Linux page cache without container filesystem virtualization overhead, while local developers run the same **PostgreSQL 16** major version natively (Mode A default), via optional Docker Compose, or inside the dev-only Kubernetes pod (Mode B).
*   **Provectus `kafka-ui:latest` Over CMAK:** Explicitly chosen in `infrastructure/k8s/base/platform/kafka-ui.yaml` because legacy Kafka Manager (CMAK) lacked ARM64 images, whereas `provectuslabs/kafka-ui` runs natively on both OCI Ampere ARM64 and x86_64 hosts with built-in login protection.
*   **Zero-Ingress Edge Over In-Cluster NGINX Ingress / Cert-Manager:** Although `infrastructure/k8s/base/global/ingress.yaml` is retained in the base Kustomize manifest tree as a declarative reference, active production routes all 7 subdomains through **Cloudflare Flexible SSL + Origin Rules** (`Host Header: Preserve`) directly to Kubernetes NodePorts and Edge Host Nginx (`:30088`), saving memory on Free-Tier ARM64 nodes and avoiding port `80`/`443` conflicts with K3s Traefik.
*   **Migration from QEMU-Emulated CI to Native ARM64 Jenkins:** Previously, building ARM64 images on GitHub Actions' `x86_64` `ubuntu-latest` runners required slow QEMU CPU emulation (`Docker Buildx`) and external Docker Hub push/pull cycles on every release. Moving active CI/CD to the dedicated ARM64 `devops-jenkins-server` (`1 OCPU / 6 GB RAM`) eliminates emulation overhead completely: targeted Maven modules compile natively on ARM64 and stream compressed images over private SSH (`docker save | gzip` → `gunzip | sudo k3s ctr -n k8s.io images import -`) directly into K3s `containerd` with zero external registry dependency. `.github/workflows/deploy.yml` remains available strictly as a manual `workflow_dispatch` fallback when multi-arch (`linux/amd64`, `linux/arm64`) Docker Hub builds are needed.
