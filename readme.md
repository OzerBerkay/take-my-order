# 🍔 Take My Order
### Event-Driven Microservices Architecture on Kubernetes

**Take My Order** is an **event-driven, cloud-native order management system** designed to demonstrate **real-world backend and microservices architecture practices** using **Spring Boot, Apache Kafka, PostgreSQL, Keycloak, Docker, and Kubernetes**.

This project intentionally focuses on **architectural correctness**, **clear service boundaries**, and **production-style infrastructure**, rather than feature completeness.

---

## 🎯 What This Project Demonstrates

This project is built to showcase the following backend and distributed systems concepts:

- Designing **independently deployable microservices**
- Implementing **event-driven communication** using Apache Kafka
- Applying the **API Gateway pattern** for centralized routing and security
- Handling **eventual consistency** instead of distributed transactions (2PC intentionally avoided)
- Externalizing configuration via **Kubernetes ConfigMaps and Secrets**
- Integrating **OAuth2 / OpenID Connect** authentication with Keycloak
- Deploying and operating services in a **containerized, Kubernetes-based environment**

The goal of this repository is to demonstrate **how modern microservices should be structured and operated**, not to provide a production-ready business product.

---

## 🧱 Architecture Overview

```
Client
  |
  v
API Gateway
  |
  +--> Customer Service
  +--> Order Service
  +--> Payment Service
  +--> Restaurant Service
            |
            v
          Kafka
            |
            v
        Other Services

Each service owns its business logic and persists its data in PostgreSQL.
Authentication and authorization are handled centrally by Keycloak.
```

### Core Architectural Principles

- **Loose coupling** through asynchronous messaging
- **Single responsibility per service**
- **Infrastructure as Code** using Kubernetes manifests
- **Failure isolation and scalability** via event-driven design

---

## 🧩 Service Responsibilities

| Service | Responsibility |
|------|---------------|
| gateway-service | API entry point, routing, authentication enforcement |
| customer-service | Customer creation and management |
| order-service | Order lifecycle management and state transitions |
| payment-service | Payment processing and payment-related events |
| restaurant-service | Restaurant and menu domain |
| common | Shared utilities and configuration |

All inter-service communication is handled through Kafka events rather than synchronous REST calls.

---

## 🔄 Event-Driven Flow (Simplified)

1. A client creates an order via the API Gateway
2. `order-service` persists the order and publishes an `OrderCreated` event
3. `payment-service` consumes the event and processes the payment
4. A payment result event is published
5. `order-service` updates the order state accordingly

This flow ensures **scalability, resilience, and eventual consistency**.

---

## 📋 Prerequisites

Before running the project, ensure that the following tools are installed and properly configured:

- **Java 21 (JDK)**  
  Required to build and run all Spring Boot microservices.

- **Maven (3.9.x)**  
  Used for dependency management and multi-module builds.

- **Docker Desktop**  
  Docker must be installed and **Kubernetes must be enabled**, as all services are containerized and deployed to a local Kubernetes cluster.

- **PostgreSQL**  
  Installed locally and accessible from Kubernetes pods.
    - Default port: **5432**
    - Must allow external connections (see Troubleshooting section if connection issues occur).

---

## 🏗️ Build & Docker Image Creation

### Automatic Build (Recommended)

On Windows, build all services with a single command:

```powershell
./build-all.bat
```

This script:
- Builds all Maven modules
- Creates Docker images for each microservice

### Manual Build (Single Service)

Example for `gateway-service`:

```bash
mvn clean package -DskipTests -pl gateway-service -am
docker build -t berkay/gateway-service:v1 -f gateway-service/Dockerfile .
```

> The trailing `.` is critical, as the Docker build context must include the `common` module.

---

## 🚀 Kubernetes Deployment Strategy

Deployment must follow the correct order. Services cannot start until the infrastructure is ready.

### Step 0: Namespace & Configuration

```bash
kubectl apply -f infrastructure/k8s/namespaces.yaml
kubectl apply -f infrastructure/k8s/config-maps.yaml
kubectl apply -f infrastructure/k8s/secrets.yaml
```

Verify:

```bash
kubectl get all -n take-my-order
```

### Step 1: Platform Components (Storage, Kafka, Keycloak)

```bash
kubectl apply -f infrastructure/k8s/platform/persistent-volumes.yaml
kubectl apply -f infrastructure/k8s/platform/
```

> Kafka and Zookeeper may take 1–2 minutes to become fully operational.  
> Wait until all broker pods reach the **Running** state.

```bash
kubectl get pods -n take-my-order -w
```

### Step 2: Microservices

```bash
kubectl apply -f infrastructure/k8s/services/
```

---

## 🔐 Authentication with Keycloak

Keycloak runs in **development mode**.

### Access Admin Console

```bash
kubectl port-forward svc/keycloak-service 8080:8080 -n take-my-order
```

Open: `http://localhost:8080`  
Credentials: `admin / admin`

### Required Configuration

- Realm: `take-my-order`
- Client ID: `take-my-order-client`
- Client Authentication: ENABLED
- Direct Access Grants: ENABLED
- Valid Redirect URIs: `*`
- Roles:
    - `CUSTOMER`
    - `ADMIN` (optional)

---

## 🛠️ Troubleshooting

### PostgreSQL Connection Refused

Ensure:

- `listen_addresses = '*'` in `postgresql.conf`
- Firewall allows inbound traffic on port **5432**
- `pg_hba.conf` contains:

```
host all all 0.0.0.0/0 scram-sha-256
```

### Schema Registry CrashLoopBackOff

If Schema Registry starts before Kafka is ready:

```bash
kubectl rollout restart deployment schema-registry -n take-my-order
```

### Gateway Cannot Reach Services

```bash
kubectl delete pod -l app=gateway -n take-my-order
```

Health check:

```
http://localhost:30090/actuator/health
```

---

## ⚡ Useful Commands

| Purpose | Command |
|------|--------|
| Watch pods | `kubectl get pods -n take-my-order -w` |
| Live logs | `kubectl logs -l app=order-service -n take-my-order -f` |
| Restart deployment | `kubectl rollout restart deployment order-service -n take-my-order` |
| Delete pod | `kubectl delete pod -l app=order-service -n take-my-order` |

---

## 🐳 Legacy: Local Development (Docker Compose)

If you prefer Docker Compose instead of Kubernetes:

- **Zookeeper**
  ```bash
  docker-compose -f common.yml -f zookeeper.yml up
  ```
  Health check:
  ```bash
  echo ruok | nc localhost 2181
  ```

- **Kafka Cluster**
  ```bash
  docker-compose -f common.yml -f kafka_cluster.yml up
  ```

- **Kafka Manager**
  ```
  http://localhost:9000
  ```
- **Create Topics (3 partition, 3 replication factor).**
  ```bash
  docker-compose -f common.yml -f init_kafka.yml up
  ```

---

**Author:** Berkay Özer  
**Repository:** https://github.com/OzerBerkay/take-my-order
