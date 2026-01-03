<div align="center">

# 🍱 Take My Order
### Hybrid Cloud & Event-Driven Microservices Architecture

<!-- Backend & Frameworks -->
![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-Gateway-6DB33F?style=for-the-badge&logo=spring&logoColor=white)

<!-- Infrastructure & Cloud -->
![Kubernetes](https://img.shields.io/badge/Kubernetes%20(K3s)-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![Oracle Cloud](https://img.shields.io/badge/Oracle_Cloud_(ARM64)-F80000?style=for-the-badge&logo=oracle&logoColor=white)
![AWS](https://img.shields.io/badge/AWS_(x86)-232F3E?style=for-the-badge&logo=amazon-aws&logoColor=white)

<!-- Data & Messaging -->
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Apache Kafka](https://img.shields.io/badge/Apache_Kafka-Event_Driven-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white)
![Keycloak](https://img.shields.io/badge/Keycloak-IAM-ADD8E6?style=for-the-badge&logo=keycloak&logoColor=black)

<!-- DevOps & Observability -->
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-CI%2FCD-2088FF?style=for-the-badge&logo=github-actions&logoColor=white)
![Grafana](https://img.shields.io/badge/Grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white)
![Prometheus](https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white)

<br>

<p align="center">
  <b>Production-grade</b> order management ecosystem developed with <b>Java 21</b> and <b>Spring Boot 3</b>;<br>
  running on a geographically distributed <b>Hybrid Multi-Cloud Kubernetes</b> cluster, featuring <br>
  <b>Event-Driven</b>, <b>DDD</b>, and <b>Hexagonal Architecture</b> principles with advanced data consistency patterns.
</p>

<a href="https://ozerberkay.github.io/take-my-order/">
  <img src="https://img.shields.io/badge/📖_Explore_Full_Architecture_&_Docs-black?style=for-the-badge" height="35" alt="Explore Docs"/>
</a>

<!-- Repo Stats -->
![License](https://img.shields.io/github/license/OzerBerkay/take-my-order?style=flat-square&color=blue)
![Repo Size](https://img.shields.io/github/repo-size/OzerBerkay/take-my-order?style=flat-square&color=orange)
![Last Commit](https://img.shields.io/github/last-commit/OzerBerkay/take-my-order?style=flat-square&color=green)

</div>

---

## 📖 Executive Summary

**Take My Order** is an advanced engineering R&D initiative demonstrating a **production-grade order management ecosystem** developed with **Java 21** and **Spring Boot 3**. Unlike standard microservice templates, this project is architected to operate on a **geographically distributed Hybrid Multi-Cloud Kubernetes cluster** (Oracle OCI & AWS), proving the ability to orchestrate heterogeneous workloads (**ARM64 & x86**) in a unified network.

The system is designed with a strict adherence to **Hexagonal Architecture (Ports & Adapters)** and **Domain-Driven Design (DDD)** to prevent anti-patterns like *Anemic Domain Models*. It addresses the inherent complexities of distributed systems—such as network partitions and data inconsistency—by implementing robust patterns including:

*   **SAGA (Orchestration)** for long-running transactions.
*   **Transactional Outbox** for ensuring atomic writes and eliminating "Dual-Write" risks.
*   **Idempotent Consumers** for guaranteeing exactly-once processing.

From **Infrastructure as Code (IaC)** to **Zero-Downtime Deployment** pipelines, every component is built to simulate a real-world enterprise environment.

---

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


---

## 🏗️ System Architecture & Microservices Breakdown

The system is composed of localized microservices, each with a dedicated responsibility and strict isolation boundaries. While **API Gateway** acts as the secure entry point on the edge, internal services operate within a protected overlay network.

| Service | Role & Infrastructure | Core Responsibility |
| :--- | :--- | :--- |
| **🛡️ API Gateway** | **Edge / Security**<br>*(Running on AWS x86)* | Acts as the single entry point. Handles **SSL**, **Rate Limiting**, and performs **OAuth2/OIDC** validation (via Keycloak) before relaying traffic to the internal network. |
| **🧠 Order Service** | **Saga Orchestrator**<br>*(Running on Oracle ARM64)* | The brain of the ecosystem. Manages the order lifecycle (**PENDING** → **PAID** → **APPROVED**) and coordinates distributed transactions via **SAGA**. |
| **💳 Payment Service** | **Transaction Handler**<br>*(Running on Oracle ARM64)* | Manages customer credit balances. Consumes payment requests responsibly and emits `PaymentCompleted` or `PaymentFailed` events using the **Outbox Pattern**. |
| **🍽️ Restaurant Service** | **Domain Validator**<br>*(Running on Oracle ARM64)* | Validates product availability, pricing, and menu consistency. Emits `OrderApproved` or `OrderRejected` events to finalize the Saga flow. |
| **👤 Customer Service** | **User Management**<br>*(Running on Oracle ARM64)* | Handles customer onboarding and profile management. Serves as the *Source of Truth* for user data |

---

## 🛠️ Technology Ecosystem

This project leverages a modern, cutting-edge technology stack designed for high concurrency, scalability, and observability.

| Category | Technology & Tools |
| :--- | :--- |
| **Language & Frameworks** | **Java 21 (LTS)**, **Spring Boot 3.x**, Spring Cloud Gateway, Spring Data JPA, Spring Security |
| **Messaging & Streaming** | **Apache Kafka**, Zookeeper, Confluent Schema Registry, Apache Avro |
| **Data & Persistence** | **PostgreSQL 16** (Bare Metal / Schema-per-Service), **Flyway** (Migration) |
| **Infrastructure & Cloud** | **Kubernetes (K3s)**, Docker, **Oracle Cloud (ARM64)**, **AWS EC2 (x86)**, Flannel (CNI) |
| **DevOps & CI/CD** | **GitHub Actions**, Docker Buildx (Multi-Arch), QEMU, **Kustomize**, Maven, SSH Automation |
| **Observability** | **Prometheus**, **Grafana**, **Zipkin** (Distributed Tracing), Micrometer |
| **Security & IAM** | **Keycloak** (OAuth2 / OIDC), OpenSSL |

---

## 🌍 Real-World Production Environment

This project runs on a live **Hybrid Multi-Cloud Cluster** built with **Zero Cost** using Free Tier resources.

### Cluster Specifications (Summary)

The production environment consists of **4 nodes** connected via a secure Overlay Network.

| Provider | Server Name | Specs                        | Role |
| :--- | :--- |:-----------------------------| :--- |
| **Oracle OCI** | `k8s-master-infra` | **2 OCPU, 12GB RAM** (ARM64) | **Master Node** (Control Plane + DB + Kafka) |
| **Oracle OCI** | `k8s-worker-01` | **1 OCPU, 6GB RAM** (ARM64)  | **Worker Node** (Java Workloads) |
| **Oracle OCI** | `k8s-worker-02` | **1 OCPU, 6GB RAM** (ARM64)  | **Worker Node** (Java Workloads) |
| **AWS EC2** | `aws-gateway-worker`| **1 vCPU, 1GB RAM** (x86_64) | **Gateway Node** (Edge Entry Point) |

---

## ⚙️ DevOps & Multi-Arch CI/CD Pipeline

The project utilizes a fully automated **"Push-Based" Deployment Pipeline** powered by GitHub Actions. The pipeline is specifically engineered to handle the complexity of a heterogeneous cluster (**Oracle ARM64** workers + **AWS x86** Gateway) by seamlessly producing multi-architecture Docker images.

### 🔄 Pipeline Workflow

Every commit to the `prod` branch triggers the following workflow:

1.  **Environment Setup (Cross-Platform):**
  *   Sets up **Java 21 (Temurin)** for compilation.
  *   Installs **QEMU Emulators** to enable building ARM64 images on standard GitHub Runners.
  *   Initializes **Docker Buildx** for advanced multi-platform support.

2.  **Build & Test:**
  *   Runs unit tests and compiles JAR artifacts using Maven.
  *   `mvn clean package -DskipTests`

3.  **Multi-Arch Containerization:**
  *   Instead of standard builds, it constructs a single Docker image manifest capable of running on both Intel and ARM processors.
  *   **Target Platforms:** `linux/amd64`, `linux/arm64`
  *   **Registry:** Pushes images to Docker Hub repositories.

4.  **Production Deployment (Push-Based):**
  *   The pipeline establishes a secure **SSH Connection** to the Oracle Master Node.
  *   It executes a command to trigger a **Rolling Update**, ensuring the cluster pulls the new image digests without downtime.

### 🚀 Deployment Strategy

The pipeline executes the following logic remotely on the Master Node to ensure service continuity:

* Executed via ssh-action on Oracle Master Node
*  sudo kubectl rollout restart deployment -n take-my-order