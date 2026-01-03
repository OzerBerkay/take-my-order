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
