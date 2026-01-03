<div align="center">

<h1>🍱 Take My Order</h1>
<h3>Hybrid Cloud & Event-Driven Microservices Architecture</h3>

<!-- Backend & Frameworks -->
<img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java"/>
<img src="https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot"/>
<img src="https://img.shields.io/badge/Spring_Cloud-Gateway-6DB33F?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Cloud"/>
<br>
<br>
<!-- Infrastructure & Cloud -->
<img src="https://img.shields.io/badge/Kubernetes%20(K3s)-326CE5?style=for-the-badge&logo=kubernetes&logoColor=white" alt="Kubernetes"/>
<img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white" alt="Docker"/>
<img src="https://img.shields.io/badge/Oracle_Cloud_(ARM64)-F80000?style=for-the-badge&logo=oracle&logoColor=white" alt="Oracle Cloud"/>
<img src="https://img.shields.io/badge/AWS_(x86)-232F3E?style=for-the-badge&logo=amazon-aws&logoColor=white" alt="AWS"/>
<br>
<br>

<!-- Data & Messaging -->
<img src="https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL"/>
<img src="https://img.shields.io/badge/Apache_Kafka-Event_Driven-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white" alt="Kafka"/>
<img src="https://img.shields.io/badge/Keycloak-IAM-ADD8E6?style=for-the-badge&logo=keycloak&logoColor=black" alt="Keycloak"/>
<br>
<br>

<!-- DevOps & Observability -->
<img src="https://img.shields.io/badge/GitHub_Actions-CI%2FCD-2088FF?style=for-the-badge&logo=github-actions&logoColor=white" alt="Actions"/>
<img src="https://img.shields.io/badge/Grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white" alt="Grafana"/>
<img src="https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white" alt="Prometheus"/>

<br>

<p>
  <b>Production-grade</b> order management ecosystem developed with <b>Java 21</b> and <b>Spring Boot 3</b>
  running on a geographically distributed <b>Hybrid Multi-Cloud Kubernetes</b> cluster, featuring
  <b>Event-Driven</b>, <b>DDD</b>, and <b>Hexagonal Architecture</b> principles with advanced data consistency patterns.
</p>

<!-- Repo Stats -->
<img src="https://img.shields.io/github/license/OzerBerkay/take-my-order?style=flat-square&color=blue" alt="License"/>
<img src="https://img.shields.io/github/repo-size/OzerBerkay/take-my-order?style=flat-square&color=orange" alt="Size"/>
<img src="https://img.shields.io/github/last-commit/OzerBerkay/take-my-order?style=flat-square&color=green" alt="Commit"/>

</div>

---

## 📖 Executive Summary

**Take My Order** is an advanced engineering R&D initiative demonstrating a **production-grade order management ecosystem** developed with **Java 21** and **Spring Boot 3**. Unlike standard microservice templates, this project is architected to operate on a **geographically distributed Hybrid Multi-Cloud Kubernetes cluster** (Oracle OCI & AWS), proving the ability to orchestrate heterogeneous workloads (**ARM64 & x86**) in a unified network.

The system is designed with a strict adherence to **Hexagonal Architecture (Ports & Adapters)** and **Domain-Driven Design (DDD)** to prevent anti-patterns like *Anemic Domain Models*. It addresses the inherent complexities of distributed systems—such as network partitions and data inconsistency—by implementing robust patterns including:

*   **SAGA (Orchestration)** for long-running transactions.
*   **Transactional Outbox** for ensuring atomic writes and eliminating "Dual-Write" risks.
*   **Idempotent Consumers** for guaranteeing exactly-once processing.

From **Infrastructure as Code (IaC)** to **Zero-Downtime Deployment** pipelines, every component is built to simulate a real-world enterprise environment.
