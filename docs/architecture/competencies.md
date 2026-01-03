## 🎯 Key Engineering Competencies

This project serves as a practical implementation guide for the following advanced architectural patterns and engineering principles:

### 🏛️ Clean & Hexagonal Architecture
*   **Ports & Adapters:** Strictly isolated the business logic from external technologies (DB, Web, Msg) using input/output ports.
*   **Rich Domain Model:** Encapsulated business rules directly within entities to prevent the **"Anemic Domain Model"** anti-pattern and ensure high testability without framework dependencies.

### 🎼 Distributed Consistency & Tactical DDD
*   **SAGA Pattern (Orchestration):** Managed long-running distributed transactions across multiple services.
*   **Self-Healing:** Established automated **Compensating Transactions** to rollback operations in failure scenarios.
*   **Consistency Model:** Combining ACID within Aggregates and Eventual Consistency across Microservices.

### 💾 Database Architecture & Data Strategy
*   **Schema-per-Service Pattern:** Implemented strict **Logical Isolation** by assigning dedicated PostgreSQL schemas to each microservice. This simulates the standard "Database-per-Service" pattern while optimizing resources in a constrained environment.
*   **Bare Metal Performance:** Deployed PostgreSQL directly on the **Master Node (ARM64)** OS layer to maximize I/O throughput and eliminate containerization overhead.
*   **Schema Migration:** Managed structural consistency and versioning across environments using **Flyway**.

### 📦 Data Integrity & Schema Governance
*   **Transactional Outbox Pattern:** Eliminated **"Dual-Write"** risks by persisting the Event and Entity in the same atomic transaction.
*   **Type Safety:** Standardized asynchronous communication using **Apache Kafka**, **Avro**, and **Schema Registry**.
*   **Idempotency:** implemented the **Inbox Pattern** on consumers to prevent side effects from duplicate message delivery.

### 🛠️ Infrastructure as Code (IaC) & Configuration Management
*   **Environment Isolation:** Decoupled configuration from code using **Kustomize**.
    *   Managed **Base** manifests for common resources.
    *   Applied **Overlays (Dev/Prod)** for environment-specific patches (e.g., NodePorts for Dev vs. ClusterIP for Prod).
*   **Dynamic Configuration:** Utilized **Spring Profiles** (application-dev.yml / application-prod.yml) effectively to switch between local Docker setups and Kubernetes environments without code changes.

### ☁️ Hybrid Multi-Cloud Infrastructure (Cloud-Native)
*   **Geo-Distributed Cluster:** Orchestrated a unified Kubernetes cluster spanning **Oracle Cloud (ARM64)** and **AWS (x86)**.
*   **Heterogeneous Orchestration:** Managed a unified cluster spanning **Oracle (ARM64)** and **AWS (x86)** using strict **Node Affinity & Node Selectors** to pin workloads to compatible architectures.
*   **Overlay Networking:** Solved cross-cloud communication using custom **Flannel VXLAN** tunneling.
*   **Stateful Optimization:** Engineered a **Split-Disk PVC strategy** for Zookeeper/Kafka to separate WAL logs from snapshots, preventing I/O bottlenecks and **InconsistentClusterId** errors.
*   **Stateful Systems Resilience:**
    * Engineered a **Split-Disk PVC strategy** for **Zookeeper & Kafka** to separate WAL logs from data snapshots.
    * Prevented **InconsistentClusterId** errors and optimized I/O performance for high-throughput messaging.

### 📊 Deep Observability & Monitoring
*   **Distributed Tracing:** Integrated **Zipkin** to visualize request latency and trace propagation across microservices.
*   **Metrics & Visualization:** Configured **Prometheus** & **Grafana** to monitor JVM, System, and Kafka metrics in real-time.
*   **Zero-Downtime:** Configured **Liveness & Readiness Probes** to enable self-healing and traffic draining during rolling updates.

### 🚀 DevOps & Multi-Arch CI/CD
*   **Heterogeneous Builds:** Established an automated pipeline using **GitHub Actions**, **QEMU**, and **Docker Buildx** to build images for both **ARM64** and **AMD64** architectures.
*   **Push-Based Deployment:** Implemented **Zero-Downtime** rolling updates via server-integrated SSH automation.
*   **Optimization:** Containerized services using lightweight **JRE-Alpine** base images.

### 🛡️ Advanced Security
*   **Centralized IAM:** Integrated **Keycloak** implementing **OAuth2** and **OIDC** standards.
*   **Token Relay:** Configured the **API Gateway** as a Resource Server to validate and relay JWTs to downstream services.
*   **Hardening:** Minimized attack surface by restricting Database access strictly to the internal Cluster Pod CIDR via `pg_hba.conf`.
