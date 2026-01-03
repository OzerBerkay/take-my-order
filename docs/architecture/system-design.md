## 🏗️ System Architecture & Microservices Breakdown

The system is composed of localized microservices, each with a dedicated responsibility and strict isolation boundaries. While **API Gateway** acts as the secure entry point on the edge, internal services operate within a protected overlay network.

| Service | Role & Infrastructure | Core Responsibility |
| :--- | :--- | :--- |
| **🛡️ API Gateway** | **Edge / Security**<br>*(Running on AWS x86)* | Acts as the single entry point. Handles **SSL**, **Rate Limiting**, and performs **OAuth2/OIDC** validation (via Keycloak) before relaying traffic to the internal network. |
| **🧠 Order Service** | **Saga Orchestrator**<br>*(Running on Oracle ARM64)* | The brain of the ecosystem. Manages the order lifecycle (**PENDING** → **PAID** → **APPROVED**) and coordinates distributed transactions via **SAGA**. |
| **💳 Payment Service** | **Transaction Handler**<br>*(Running on Oracle ARM64)* | Manages customer credit balances. Consumes payment requests responsibly and emits `PaymentCompleted` or `PaymentFailed` events using the **Outbox Pattern**. |
| **🍽️ Restaurant Service** | **Domain Validator**<br>*(Running on Oracle ARM64)* | Validates product availability, pricing, and menu consistency. Emits `OrderApproved` or `OrderRejected` events to finalize the Saga flow. |
| **👤 Customer Service** | **User Management**<br>*(Running on Oracle ARM64)* | Handles customer onboarding and profile management. Serves as the *Source of Truth* for user data |
