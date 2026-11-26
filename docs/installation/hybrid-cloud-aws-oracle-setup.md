# Alternative 4-Node Hybrid Setup (OCI + AWS)

This step-by-step operational runbook walks you through provisioning the **Take My Order** production environment using **Option A — 4-Node Hybrid Multi-Cloud Architecture** (`1x Oracle Cloud Always Free ARM64` + `1x AWS Free Tier x86_64`) powered by **GitHub Actions Multi-Arch CI/CD** (`.github/workflows/deploy.yml`) and **Docker Hub**. Every console click, default toggle, blank field, manifest adaptation, command, and verification gate is documented in exact execution order.

!!! tip "Have Two Oracle Cloud Always Free Accounts Instead?"
    If you have **2x Oracle Cloud accounts in the same region**, use the active **Option B — 7-Node Cross-Tenancy OCI Architecture** with private Local Peering Gateway (LPG) networking and native ARM64 Jenkins CI/CD. Follow the [Production 7-Node Cluster Setup (2x OCI)](seven-node-cluster-setup.md) guide instead.

!!! warning "Architectural Reference & Proprietary License Restriction"
    This production runbook is published strictly as **technical and architectural documentation** demonstrating the author's alternative hybrid cloud topology. Under the repository's root `LICENSE` (**All Rights Reserved**), deploying, hosting, modifying, or republishing this project (in whole or in part) on any remote or cloud server or third-party repository is strictly prohibited without prior express written permission from the Copyright Holder. Only personal code study and local workstation (`localhost`) evaluation are permitted.

---

## 1. Option A vs. Option B & Matrix

| Architectural Dimension | Option A: 4-Node Hybrid Multi-Cloud *(This Guide)* | Option B: 7-Node Cross-Tenancy OCI *(Active Standard)* |
| :--- | :--- | :--- |
| **Required Cloud Accounts** | **1x Oracle Cloud Account + 1x AWS Free Tier Account** | **2x Oracle Cloud Accounts** in the same region |
| **Compute & CPU Arch** | **4 Nodes (Multi-Arch):** 3x OCI ARM64 (`4 OCPU / 24 GB`) + 1x AWS `t2.micro` `x86_64` (`1 vCPU / 1 GB`) | **7 Nodes (Native ARM64):** 6x K3s Nodes + 1x Jenkins Server (`8 OCPU / 48 GB`) |
| **Inter-Node Networking** | **Cross-Cloud Flannel VXLAN (`UDP 8472`)** over `/32`-restricted Public IPs (`--node-external-ip`) | **Private OCI Local Peering Gateway (LPG)** (`10.0.0.0/16` ↔ `10.1.0.0/16`, `< 0.5 ms`) |
| **CI/CD & Registry** | **GitHub Actions** (`.github/workflows/deploy.yml`) + QEMU/Buildx (`amd64,arm64`) + **Docker Hub** (`imagePullPolicy: Always`) | **Stand-Alone ARM64 Jenkins** (`Jenkinsfile`) + Private SSH `containerd` streaming (`imagePullPolicy: Never`) |

### 1.1 Accounts & Local Checklist

Prepare these five items before starting Section 2:

1.  **One Oracle Cloud Infrastructure (OCI) Always Free Account:** Recommend **`eu-frankfurt-1` — Germany Central (Frankfurt)** so Oracle and AWS Frankfurt sit in the same city (`< 5 ms` cross-cloud RTT).
2.  **One AWS Free Tier Account:** Use region **`eu-central-1` — Europe (Frankfurt)** to match your OCI region.
3.  **One Free Docker Hub Account:** Hosts the 5 public multi-arch (`linux/amd64,linux/arm64`) microservice image repositories.
4.  **Authorized GitHub Repository Access *(Prior Written License Required)*:** Configuring **Actions Secrets**, editing Option A manifests, and running `.github/workflows/deploy.yml` require your own authorized repository copy—which is permitted **only** with prior express written permission from the Copyright Holder under the root `LICENSE`.
5.  **Domain Name, Free Cloudflare Account & Local Workstation:**
    *   Domain + free Cloudflare account are used for HTTPS in Section 9 (Sections 1–8 also work over plain server IPs first).
    *   Windows 10/11 (**PowerShell** with built-in OpenSSH `ssh` & `scp`) or macOS / Linux (**Terminal**), plus a local clone of the authorized repository (`git clone https://github.com/<AUTHORIZED_GITHUB_OWNER>/take-my-order.git`).
    *   Open a blank local notepad to record **8 IP addresses** (4 Public + 4 Private) as you create servers—never commit that file to Git.

### 1.2 Four-Node Hardware Matrix

In Option A, **`k8s-master-infra`** (`2 OCPU / 12 GB RAM`) hosts the K3s Control Plane, Bare-Metal PostgreSQL 16, Kafka/Zookeeper/Schema Registry, Keycloak, Redis, **and** the observability stack (Prometheus, Grafana, Zipkin, Kafka UI). The two Oracle ARM64 workers run the 4 domain microservices, while the AWS `t2.micro` (`x86_64`) acts as an isolated DMZ node exclusively for `gateway-service`.

| Node & Cloud | Arch & Shape | CPU / RAM / SSD | Private IP | K3s Role & `node-type` Label |
| :--- | :--- | :---: | :---: | :--- |
| **1. `k8s-master-infra`**<br>Oracle Cloud (OCI) | `arm64`<br>`VM.Standard.A1.Flex` | 2 / 12 GB / **60 GB** | `<MASTER_PRIVATE_IP>`<br>*(10.0.0.x)* | K3s Master (`infra`) + PG 16 + Monitoring |
| **2. `k8s-worker-01`**<br>Oracle Cloud (OCI) | `arm64`<br>`VM.Standard.A1.Flex` | 1 / 6 GB / 50 GB | `<WORKER1_PRIVATE_IP>`<br>*(10.0.0.x)* | App Worker 1 (`app`) |
| **3. `k8s-worker-02`**<br>Oracle Cloud (OCI) | `arm64`<br>`VM.Standard.A1.Flex` | 1 / 6 GB / 50 GB | `<WORKER2_PRIVATE_IP>`<br>*(10.0.0.x)* | App Worker 2 (`app`) |
| **4. `aws-gateway-worker`**<br>AWS EC2 | `amd64`<br>`t2.micro` | 1 / 1 GB / 30 GB | `<AWS_PRIVATE_IP>`<br>*(172.31.x.x)* | Edge Gateway (`gateway`) |

!!! note "Why 160 GB (`60 + 50 + 50 GB`) on Oracle Cloud?"
    OCI Always Free grants 200 GB of Block Storage. Allocating 160 GB intentionally keeps **40 GB in reserve** so `k8s-master-infra` can expand its `60 GB` boot volume online or attach a dedicated database volume later—cloud boot volumes can expand online, never shrink.

```mermaid
flowchart TD
    subgraph OCI ["Oracle Cloud Always Free — K8S-VCN (10.0.0.0/16 · ARM64 / aarch64)"]
        direction LR
        M["1. k8s-master-infra<br/>2 OCPU · 12 GB · 60 GB<br/>node-type=infra<br/>K3s Master · PostgreSQL 16<br/>3x Kafka · ZK · SR · Redis<br/>Keycloak · Grafana · Prom · Zipkin"]
        W1["2. k8s-worker-01<br/>1 OCPU · 6 GB · 50 GB<br/>node-type=app<br/>Domain Microservices"]
        W2["3. k8s-worker-02<br/>1 OCPU · 6 GB · 50 GB<br/>node-type=app<br/>Domain Microservices &amp; HPA"]
        M ~~~ W1 ~~~ W2
    end

    Tunnel["Cross-Cloud Bridge — Locked to Static /32 Public IPs<br/>K3s Control Plane (TCP 6443 / 10250) · Flannel VXLAN Overlay (UDP 8472)"]

    subgraph Bottom ["AWS Edge Node (x86_64) &amp; Multi-Arch CI/CD Pipeline"]
        direction LR
        GW["4. aws-gateway-worker (AWS t2.micro)<br/>1 vCPU · 1 GB RAM (+ 2 GB Swap) · 30 GB<br/>node-type=gateway · amd64<br/>Gateway :30180 · Optional Nginx :30088"]
        GHA["GitHub Actions (.github/workflows/deploy.yml)<br/>QEMU + Buildx (linux/amd64, linux/arm64)<br/>SSH Rollout Restart → k8s-master-infra"]
        DH["Docker Hub (5 Public Repos :v1)<br/>imagePullPolicy: Always<br/>OCI pulls arm64 · AWS pulls amd64"]
        GW ~~~ GHA -->|"Push Multi-Arch"| DH
    end

    OCI ~~~ Tunnel ~~~ Bottom
```

---

## 2. Provision Oracle Cloud (3 Nodes)

### 2.1 Create OCI VCN (`K8S-VCN`)

1.  Sign in to **Oracle Cloud Console** and confirm your region (e.g., `eu-frankfurt-1`) in the top-right header.
2.  Open the top-left Hamburger Menu (**`☰`**) → **Networking → Virtual cloud networks**.
3.  On the left sidebar under **List scope → Compartment**, confirm your root compartment **`(root)`** is selected.
4.  Click **Start VCN Wizard** → select **`Create VCN with Internet Connectivity`** → click **Start VCN Wizard**.
5.  Configure the fields:
    *   **VCN name:** `K8S-VCN` | **Compartment:** Leave `(root)`.
    *   **VCN IPv4 CIDR block:** Leave default **`10.0.0.0/16`**.
    *   **Enable IPv6 in this VCN:** Leave **unchecked / OFF**.
    *   **Use DNS hostnames in this VCN:** Leave **checked / ON**.
    *   **Public subnet IPv4 CIDR block:** Leave default **`10.0.0.0/24`**.
    *   **Private subnet IPv4 CIDR block:** Leave default **`10.0.1.0/24`**.
    *   **Tags:** Leave empty.
6.  Click **Next** → **Create** → **View Virtual Cloud Network**.

### 2.2 Launch 3 ARM64 Ubuntu Nodes

Open **`☰` → Compute → Instances** and click **Create instance**.

**Server #1 — `k8s-master-infra` (Field-by-Field Settings):**

1.  **Name:** `k8s-master-infra` | **Compartment:** Leave `(root)`.
2.  **Placement:** Select **`AD-1`** *(try `AD-2` or `AD-3` if out of ARM capacity)*; expand **Show advanced options** → confirm **Capacity type** = **`On-demand capacity`** *(**NEVER** Preemptible)*; leave **Cluster placement group** OFF and **Fault domain** default.
3.  **Image and shape:**
    *   **Change image** → **Canonical Ubuntu** → **`Ubuntu 24.04`** → **Select image**.
    *   **Change shape** → **Instance type:** `Virtual machine` → **Shape series:** **`Ampere`** → select **`VM.Standard.A1.Flex`** (*Always Free-eligible*) → **Number of OCPUs:** **`2`**, **Amount of memory (GB):** **`12`** → **Select shape**.
4.  **Security:** Leave **Shielded instance** and **Confidential computing** **OFF**.
5.  **Primary VNIC information:**
    *   **VNIC name:** Leave **blank** *(keeps Ubuntu hostname identical to `k8s-master-infra`)*.
    *   **Primary network:** Existing VCN **`K8S-VCN`** | **Subnet:** **`Public Subnet-K8S-VCN`** (`10.0.0.0/24`).
    *   **Public IPv4 address:** Check **`Assign a public IPv4 address`**.
6.  **Add SSH keys:**
    *   Select **`Generate a key pair for me`**.
    *   Click **Save private key** → save and rename to **`cluster-key.key`** inside a local folder (e.g., `C:\ssh-keys\take-my-order\` or `~/ssh-keys/take-my-order/`).
    *   Click **Save public key** → save and rename to **`cluster-key.pub`** in the same folder.
7.  **Boot volume:** Check **`Specify a custom boot volume size and performance setting`** → type **`60`** GB; leave VPU `10 (Balanced)`, in-transit encryption ON, customer-managed key OFF, Block volumes empty.
8.  Click **Next** (if shown) → **Create**.

**Server #2 (`k8s-worker-01`) & Server #3 (`k8s-worker-02`):**

Click **Create instance** twice more with the exact same settings as Server #1 **except**:

| Field | `k8s-worker-01` | `k8s-worker-02` |
| :--- | :--- | :--- |
| **Name** | `k8s-worker-01` | `k8s-worker-02` |
| **Shape (`VM.Standard.A1.Flex`)** | **`1` OCPU / `6` GB RAM** | **`1` OCPU / `6` GB RAM** |
| **Add SSH keys** | Select **Upload public key files (.pub)** → upload **`cluster-key.pub`** | Select **Upload public key files (.pub)** → upload **`cluster-key.pub`** |
| **Boot volume size (GB)** | **`50`** | **`50`** |

### 2.3 SSH Key Permissions & IP Sheet

1.  Wait 1–2 minutes until all 3 Oracle instances turn **Running (Green)** and record each node's **Public IP** and **Private IP (`10.0.0.x`)** in your local notepad.
2.  Lock `cluster-key.key` permissions before connecting:
    *   **Windows (PowerShell):**
        ```powershell
        cd C:\ssh-keys\take-my-order
        icacls cluster-key.key /inheritance:r
        icacls cluster-key.key /grant:r "$($env:USERNAME):(R)"
        ```
    *   **macOS / Linux (Bash):**
        ```bash
        cd ~/ssh-keys/take-my-order
        chmod 400 cluster-key.key
        ```
3.  **Verification Gate:** Run `ssh -o StrictHostKeyChecking=accept-new -i cluster-key.key ubuntu@<ORACLE_MASTER_PUBLIC_IP> "hostname"` → prints `k8s-master-infra`.

---

## 3. Provision AWS EC2 Gateway Node

### 3.1 Launch AWS `t2.micro` Instance

1.  Sign in to the **AWS Management Console** and select region **Europe (Frankfurt) `eu-central-1`** in the top-right dropdown.
2.  Search for **`EC2`** in the top search bar → click **Instances** (left sidebar) → click orange **Launch instances**.
3.  Fill in every section carefully:
    *   **Name and tags:** **Name** = **`k8s-gateway-aws`**.
    *   **Application and OS Images (AMI):** Click **Ubuntu** → select **`Ubuntu Server 24.04 LTS (HVM), SSD Volume Type`** *(or `22.04 LTS`)* with architecture **`64-bit (x86)`** *(confirm the **Free tier eligible** badge appears)*.
    *   **Instance type:** Select **`t2.micro`** *(1 vCPU, 1 GiB Memory — confirm **Free tier eligible**)*.
    *   **Key pair (login):** Click **Create new key pair**:
        *   **Key pair name:** `aws-gateway-key`
        *   **Key pair type:** `RSA`
        *   **Private key file format:** **`.pem`** (for OpenSSH)
        *   Click **Create key pair** → move downloaded **`aws-gateway-key.pem`** into the same local folder (`C:\ssh-keys\take-my-order\` or `~/ssh-keys/take-my-order/`).
    *   **Network settings:** Click **Edit** (top right of box):
        *   **VPC:** Leave default VPC (`172.31.0.0/16`).
        *   **Subnet:** Leave **`No preference`**.
        *   **Auto-assign public IP:** Confirm **`Enable`**.
        *   **Firewall (security groups):** Select **`Create security group`** → **Security group name:** **`k8s-gateway-sg`** → **Description:** `Oracle Hybrid Cluster Connectivity` (we add all exact inbound rules in Section 3.2 right below).
    *   **Configure storage:** Change the default `8 GiB` root volume to **`30 GiB`** (`gp2` or `gp3` — AWS Free Tier maximum).
    *   **Advanced details:** Leave completely untouched.
4.  Click **Launch instance** → open **Instances**, wait until `k8s-gateway-aws` shows **Running**, and record its **Public IPv4 address (`<AWS_PUBLIC_IP>`)** and **Private IPv4 address (`<AWS_PRIVATE_IP>`, `172.31.x.x`)** in your notepad.
5.  **Lock `aws-gateway-key.pem` Permissions:**
    *   **Windows (PowerShell):**
        ```powershell
        icacls aws-gateway-key.pem /inheritance:r
        icacls aws-gateway-key.pem /grant:r "$($env:USERNAME):(R)"
        ```
    *   **macOS / Linux (Bash):** `chmod 400 aws-gateway-key.pem`.

!!! tip "Optional: Prevent AWS Public IP Changes with a Free Elastic IP"
    Standard EC2 public IPv4 addresses change whenever you **Stop** and **Start** an instance. To keep `<AWS_PUBLIC_IP>` permanent (free while attached to a running instance), go to **EC2 → Network & Security → Elastic IPs → Allocate Elastic IP address → Actions → Associate Elastic IP address → select `k8s-gateway-aws`**.

### 3.2 Configure AWS Security Group

Open **EC2 → Network & Security → Security Groups → `k8s-gateway-sg` → Inbound rules → Edit inbound rules**. Keep **Outbound rules** untouched (`All traffic → 0.0.0.0/0`). Configure these **8 Inbound Rules** (click **Add rule** for each):

| # | Type | Protocol | Port Range | Source Type & CIDR | Description |
| :---: | :--- | :---: | :---: | :--- | :--- |
| **1** | `SSH` | TCP | `22` | **`My IP`** | SSH access from your workstation only |
| **2** | `Custom TCP` | TCP | `6443` | `Custom` → **`<ORACLE_MASTER_PUBLIC_IP>/32`** | K3s API Server (Master ↔ AWS) |
| **3** | `Custom TCP` | TCP | `10250` | `Custom` → **`<ORACLE_MASTER_PUBLIC_IP>/32`** | Kubelet health & `kubectl logs/exec` |
| **4** | `Custom UDP` | **UDP** | `8472` | `Custom` → **`<ORACLE_MASTER_PUBLIC_IP>/32`** | Flannel VXLAN Tunnel (Master ↔ AWS) |
| **5** | `Custom UDP` | **UDP** | `8472` | `Custom` → **`<WORKER1_PUBLIC_IP>/32`** | Flannel VXLAN Tunnel (Worker 1 ↔ AWS) |
| **6** | `Custom UDP` | **UDP** | `8472` | `Custom` → **`<WORKER2_PUBLIC_IP>/32`** | Flannel VXLAN Tunnel (Worker 2 ↔ AWS) |
| **7** | `Custom TCP` | TCP | `30180` | `Anywhere-IPv4` → **`0.0.0.0/0`** | Public `gateway-service` NodePort & Swagger Hub |
| **8** | `Custom TCP` | TCP | `30088` | `Anywhere-IPv4` → **`0.0.0.0/0`** | Optional Host Nginx frontend portals |

Click **Save rules**. *(Double-check Rules #4–#6 use **UDP**, NOT TCP—Flannel VXLAN encapsulates pod traffic over `UDP 8472`!)*

### 3.3 Configure OCI Security List

Return to **Oracle Cloud Console → `☰` → Networking → Virtual cloud networks → `K8S-VCN` → `Security` → `Default Security List for K8S-VCN` → `Add Ingress Rules`**.

!!! danger "Three Golden OCI Security List Rules"
    1.  **NEVER touch Egress Rules** (`0.0.0.0/0 — All Protocols`).
    2.  **NEVER check the `STATELESS` checkbox** on Ingress Rules (leave unchecked).
    3.  **Keep default VCN Wizard Ingress Rules** (`0.0.0.0/0 TCP 22` and ICMP) intact, and leave **Source Port Range** blank (`All`).

Add these **8 Ingress Rules** (they apply to all 3 Oracle nodes in `K8S-VCN`):

| Source CIDR | IP Protocol | Source Port | Destination Port | Description |
| :--- | :--- | :---: | :---: | :--- |
| `10.0.0.0/16` | **All Protocols** | *(Blank)* | *(Blank)* | Oracle internal VCN traffic |
| `10.42.0.0/16` | **All Protocols** | *(Blank)* | *(Blank)* | K3s Flannel virtual pod network |
| `<AWS_PUBLIC_IP>/32` | TCP | *(Blank)* | `6443` | K3s API Server (AWS worker join) |
| `<AWS_PUBLIC_IP>/32` | TCP | *(Blank)* | `10250` | Kubelet API (AWS ↔ Oracle) |
| `<AWS_PUBLIC_IP>/32` | **UDP** | *(Blank)* | `8472` | Flannel VXLAN cross-cloud tunnel |
| `0.0.0.0/0` | TCP | *(Blank)* | `30080` | Keycloak IAM (`k8s-master-infra`) |
| `0.0.0.0/0` | TCP | *(Blank)* | `30030` | Grafana Dashboard (`k8s-master-infra`) |
| `0.0.0.0/0` | TCP | *(Blank)* | `30091` | Kafka UI Console (`k8s-master-infra`) |

---

## 4. OS Prep, Swap & PostgreSQL 16

### 4.1 Flush OS `iptables` (All 4 Nodes)

Oracle Ubuntu images ship with restrictive host-level `iptables` `REJECT` rules that block K3s and Flannel traffic even when cloud firewalls allow it. Run this single-line command on **all 4 nodes** (`k8s-master-infra`, `k8s-worker-01`, `k8s-worker-02` via `cluster-key.key`, and `k8s-gateway-aws` via `aws-gateway-key.pem`):

```bash
sudo iptables -F && sudo iptables -X && sudo iptables -t nat -F && \
  sudo iptables -t nat -X && sudo iptables -t mangle -F && \
  sudo iptables -t mangle -X && sudo iptables -P INPUT ACCEPT && \
  sudo iptables -P FORWARD ACCEPT && sudo iptables -P OUTPUT ACCEPT && \
  sudo DEBIAN_FRONTEND=noninteractive apt-get update -y && \
  sudo DEBIAN_FRONTEND=noninteractive apt-get install -y iptables-persistent && \
  sudo netfilter-persistent save
```

### 4.2 AWS Hostname & 2 GB Swap File

SSH into your **AWS EC2 instance** (`ssh -i aws-gateway-key.pem ubuntu@<AWS_PUBLIC_IP>`):

1.  **Set & Persist Hostname `aws-gateway-worker`:** By default AWS names Ubuntu nodes after their private IP (e.g., `ip-172-31-xx-xx`) and resets changes on reboot unless `cloud.cfg` is updated:
    ```bash
    sudo hostnamectl set-hostname aws-gateway-worker
    echo "127.0.0.1 aws-gateway-worker" | sudo tee -a /etc/hosts
    sudo sed -i 's/preserve_hostname: false/preserve_hostname: true/g' /etc/cloud/cloud.cfg
    ```
2.  **Add a 2 GB Swap File (Mandatory OOM Safety for 1 GB RAM `t2.micro`):**
    Because `gateway-service` uses `RollingUpdate` (`maxSurge: 1`), a new pod starts alongside the old pod briefly during deployments. Adding 2 GB swap on the 30 GB SSD prevents the Linux OOM killer from freezing the 1 GB `t2.micro` instance:
    ```bash
    sudo fallocate -l 2G /swapfile
    sudo chmod 600 /swapfile
    sudo mkswap /swapfile
    sudo swapon /swapfile
    echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
    ```
3.  **Verification Gate:** Run `exec bash && hostname && free -h` → confirms prompt shows `ubuntu@aws-gateway-worker` and `Swap: 2.0Gi`.

### 4.3 Bare-Metal PostgreSQL 16 Setup

SSH into **`k8s-master-infra`** (`ssh -i cluster-key.key ubuntu@<ORACLE_MASTER_PUBLIC_IP>`):

1.  **Install PostgreSQL 16** *(if Ubuntu shows a purple `Daemons using outdated libraries` prompt, press **`Enter`** on `<Ok>`)*:
    ```bash
    sudo apt update && sudo apt install postgresql postgresql-contrib -y
    sudo systemctl enable --now postgresql
    ```
2.  **Create User `<CHOOSE_DB_USER>`, Database `takemyorder`, and 5 Schemas** *(choose a lowercase alphanumeric `<CHOOSE_DB_USER>` and strong alphanumeric `<STRONG_DB_PASSWORD>` without `'` or `$`, and record both for Section 7.2)*:
    ```bash
    sudo -i -u postgres psql << 'EOF'
    CREATE USER <CHOOSE_DB_USER> WITH PASSWORD '<STRONG_DB_PASSWORD>';
    CREATE DATABASE takemyorder OWNER <CHOOSE_DB_USER>;
    \c takemyorder
    CREATE SCHEMA IF NOT EXISTS keycloak AUTHORIZATION <CHOOSE_DB_USER>;
    CREATE SCHEMA IF NOT EXISTS identity AUTHORIZATION <CHOOSE_DB_USER>;
    CREATE SCHEMA IF NOT EXISTS "order" AUTHORIZATION <CHOOSE_DB_USER>;
    CREATE SCHEMA IF NOT EXISTS payment AUTHORIZATION <CHOOSE_DB_USER>;
    CREATE SCHEMA IF NOT EXISTS restaurant AUTHORIZATION <CHOOSE_DB_USER>;
    GRANT ALL PRIVILEGES ON DATABASE takemyorder TO <CHOOSE_DB_USER>;
    EOF
    ```
3.  **Configure `postgresql.conf`:** Open `sudo nano /etc/postgresql/16/main/postgresql.conf`, press **`Ctrl + W`** to search `listen_addresses`, uncomment it and set `listen_addresses = '*'`, then save (**`Ctrl + O`** → **`Enter`** → **`Ctrl + X`**).
4.  **Configure `pg_hba.conf`:** Open `sudo nano /etc/postgresql/16/main/pg_hba.conf`, scroll to the bottom, append these **2 lines**, save, and restart PostgreSQL:
    ```text
    # 1. Oracle Cloud VCN Nodes (Master, Worker-01, Worker-02)
    host    all             all             10.0.0.0/16             scram-sha-256

    # 2. K3s Flannel Virtual Pod Network (Keycloak & Domain Service Pods)
    host    all             all             10.42.0.0/16            scram-sha-256
    ```
    ```bash
    sudo systemctl restart postgresql
    ```
5.  **Verification Gate (on `k8s-worker-01`):**
    ```bash
    ssh -i cluster-key.key ubuntu@<WORKER1_PUBLIC_IP>
    sudo apt install postgresql-client -y
    psql -h <MASTER_PRIVATE_IP> -U <CHOOSE_DB_USER> -d takemyorder -c "\dn"
    ```
    Expected output lists all 5 schemas (`identity`, `keycloak`, `order`, `payment`, `restaurant`) owned by `<CHOOSE_DB_USER>`.

---

## 5. Hybrid K3s Cluster & Labeling

### 5.1 Install K3s Master (Oracle)

SSH into **`k8s-master-infra`**. Including **`--tls-san <ORACLE_MASTER_PUBLIC_IP>`** is mandatory so the AWS worker can validate the Master's TLS certificate over the public internet:

```bash
curl -sfL https://get.k3s.io | sh -s - server \
  --node-ip <MASTER_PRIVATE_IP> \
  --advertise-address <MASTER_PRIVATE_IP> \
  --tls-san <ORACLE_MASTER_PUBLIC_IP>
```

Wait 15–20 seconds, then print and copy **`<K3S_TOKEN>`** (`K10...::server:...`):

```bash
sudo cat /var/lib/rancher/k3s/server/node-token
```

### 5.2 Join 2 Oracle Workers (VCN)

Both Oracle workers join over the fast private VCN IP (`10.0.0.x`):

```bash
# 1. On k8s-worker-01:
curl -sfL https://get.k3s.io | K3S_URL=https://<MASTER_PRIVATE_IP>:6443 \
  K3S_TOKEN="<K3S_TOKEN>" sh -s - agent --node-ip <WORKER1_PRIVATE_IP>

# 2. On k8s-worker-02:
curl -sfL https://get.k3s.io | K3S_URL=https://<MASTER_PRIVATE_IP>:6443 \
  K3S_TOKEN="<K3S_TOKEN>" sh -s - agent --node-ip <WORKER2_PRIVATE_IP>
```

### 5.3 Join AWS Gateway (External IP)

SSH into **`aws-gateway-worker`** (`aws-gateway-key.pem`). Connect to the Master's **Public IP** (`https://<ORACLE_MASTER_PUBLIC_IP>:6443`) and pass **`--node-external-ip <AWS_PUBLIC_IP>`** alongside **`--node-ip <AWS_PRIVATE_IP>`** so Flannel VXLAN routes cross-cloud `UDP 8472` traffic over public IPv4 rather than AWS's isolated `172.31.x.x` VPC:

```bash
curl -sfL https://get.k3s.io | K3S_URL=https://<ORACLE_MASTER_PUBLIC_IP>:6443 \
  K3S_TOKEN="<K3S_TOKEN>" sh -s - agent \
  --node-external-ip <AWS_PUBLIC_IP> \
  --node-ip <AWS_PRIVATE_IP>

# Confirm agent is active (running) on AWS:
sudo systemctl status k3s-agent --no-pager
```

**Verification Gate (on `k8s-master-infra`):**

```bash
sudo kubectl get nodes -o wide
```

Confirm all **4 nodes** (`k8s-master-infra`, `k8s-worker-01`, `k8s-worker-02`, `aws-gateway-worker`) report **`STATUS = Ready`** and `aws-gateway-worker` displays `<AWS_PUBLIC_IP>` under **`EXTERNAL-IP`**.

### 5.4 Label All 4 Nodes from Master

Run all three commands **exclusively on `k8s-master-infra`**:

```bash
sudo kubectl label nodes $(hostname) node-type=infra --overwrite
sudo kubectl label nodes k8s-worker-01 k8s-worker-02 node-type=app --overwrite
sudo kubectl label nodes aws-gateway-worker node-type=gateway --overwrite
```

!!! warning "Never Run a Second `node-type=monitoring` Label on Master"
    Kubernetes stores node labels as a single key-value map (`node-type` can hold only **one** value per node). Running `kubectl label nodes $(hostname) node-type=monitoring --overwrite` would **erase** `node-type=infra` and leave Kafka, Keycloak, and Redis stuck in `Pending` forever! Instead, Section 7.1 cleanly repoints the 4 monitoring manifests (`grafana`, `prometheus`, `zipkin`, `kafka-ui`) to `node-type: infra`.

**Verification Gate:** Run `sudo kubectl get nodes --show-labels` → confirm `infra`, `app` (x2), and `gateway`.

---

## 6. Docker Hub & GitHub Actions

Because your cluster spans **two CPU architectures** (`linux/arm64` on Oracle + `linux/amd64` on AWS), GitHub Actions uses **QEMU** + **Docker Buildx** (`.github/workflows/deploy.yml`) to build dual-architecture manifests pushed to Docker Hub so every node automatically pulls its native binary layer.

### 6.1 Create 5 Docker Hub Repos

Sign in to [hub.docker.com](https://hub.docker.com/) → click **Create repository** five times, keeping **Visibility** set to **`Public`** (free, no imagePullSecret needed) under your `<YOUR_DOCKER_USERNAME>` namespace:

1.  `<YOUR_DOCKER_USERNAME>/gateway-service`
2.  `<YOUR_DOCKER_USERNAME>/identity-service`
3.  `<YOUR_DOCKER_USERNAME>/order-service`
4.  `<YOUR_DOCKER_USERNAME>/payment-service`
5.  `<YOUR_DOCKER_USERNAME>/restaurant-service`

*(Recommended: Under Docker Hub **Account Settings → Personal access tokens → Generate new token**, create a **Read & Write** Personal Access Token to use as `DOCKER_PASSWORD` instead of your main account password.)*

### 6.2 Generate CI/CD SSH Deploy Key

Never upload your personal `cluster-key.key` to external CI runners. Generate a dedicated 4096-bit RSA key pair on your local workstation (**press Enter twice** when asked for a passphrase—leave passphrase empty!):

```powershell
cd C:\ssh-keys\take-my-order
ssh-keygen -t rsa -b 4096 -C "github-actions-deploy" -f ./github_deploy_key
```

1.  Print the **public key** (`github_deploy_key.pub`) on your workstation and copy the single `ssh-rsa ...` line:
    *   Windows PowerShell: `Get-Content .\github_deploy_key.pub` | macOS/Linux: `cat ./github_deploy_key.pub`
2.  Authorize that public key on **`k8s-master-infra`**:
    ```bash
    ssh -i cluster-key.key ubuntu@<ORACLE_MASTER_PUBLIC_IP>
    mkdir -p ~/.ssh
    nano ~/.ssh/authorized_keys
    # Paste the single ssh-rsa line on a new line at the very bottom, then Ctrl+O -> Enter -> Ctrl+X
    chmod 700 ~/.ssh && chmod 600 ~/.ssh/authorized_keys
    ```

### 6.3 Add 5 GitHub Repository Secrets

Open the authorized GitHub repository → **Settings → Secrets and variables → Actions → New repository secret**, and add all **5 secrets** required by `.github/workflows/deploy.yml`:

| # | Exact Secret Name | Value / Format | Purpose |
| :---: | :--- | :--- | :--- |
| **1** | `DOCKER_USERNAME` | `<YOUR_DOCKER_USERNAME>` | Docker Hub username & image namespace |
| **2** | `DOCKER_PASSWORD` | `<YOUR_DOCKER_PAT_OR_PASSWORD>` | Docker Hub Personal Access Token (Read & Write) |
| **3** | `SERVER_SSH_KEY` | Full contents of `./github_deploy_key` *(including `-----BEGIN OPENSSH PRIVATE KEY-----` and `-----END OPENSSH PRIVATE KEY-----`)* | Dedicated SSH key for Master rollout |
| **4** | `SERVER_IP` | `<ORACLE_MASTER_PUBLIC_IP>` | Public IPv4 of `k8s-master-infra` *(zero spaces!)* |
| **5** | `SERVER_USER` | `ubuntu` | Linux SSH user on `k8s-master-infra` |

### 6.4 Enable & Trigger Multi-Arch CI

In `.github/workflows/deploy.yml`, automatic `push` triggering on `prod` (lines 7–12) is commented out by default so it never races with Option B's Jenkins server, while manual `workflow_dispatch` is active:

*   **Manual Trigger (Zero Edits Needed):** Open the authorized GitHub repository → **Actions** tab → click **`Build and Push Microservices`** → click **Run workflow** → choose branch **`prod`** → click **Run workflow**.
*   **Automatic Push Trigger (Optional):** Uncomment lines 7–12 (`push: branches: [ "prod" ] ...`) in `.github/workflows/deploy.yml` and push to `prod`.

*(Wait to trigger your first workflow run until Section 7.3 below so the `take-my-order` namespace already exists when Step 12 executes `sudo kubectl rollout restart deployment -n take-my-order`!)*

---

## 7. Adapt Manifests & Deploy Cluster

### 7.1 Adapt Base Manifests for 4 Nodes

Because the repository's base manifests default to Option B (7-node native ARM64 Jenkins), apply these **three quick local adaptations** inside your cloned `take-my-order` folder before uploading `infrastructure/k8s` to `k8s-master-infra`:

1.  **Point the 5 Microservices to Your Docker Hub Repos (`imagePullPolicy: Always`):**
    In all 5 files (`gateway.yaml`, `identity.yaml`, `order.yaml`, `payment.yaml`, `restaurant.yaml` under `infrastructure/k8s/base/services/`), replace `berkayozr/` with `<YOUR_DOCKER_USERNAME>/` and change `imagePullPolicy: Never` to **`imagePullPolicy: Always`**.
2.  **Co-Locate the 4 Monitoring Workloads onto `k8s-master-infra` (`node-type: infra`):**
    In `infrastructure/k8s/base/monitoring/grafana.yaml`, `prometheus.yaml`, `zipkin.yaml`, and `infrastructure/k8s/base/platform/kafka-ui.yaml`, change `node-type: monitoring` to **`node-type: infra`**.
3.  **Right-Size HPA for 4 Nodes (`infrastructure/k8s/base/services/hpa.yaml`):**
    Change `gateway-service-hpa` `maxReplicas: 2` → **`maxReplicas: 1`** *(prevents OOM pressure on the 1 GB AWS `t2.micro`)*, and change the 4 domain service HPAs from `maxReplicas: 3` → **`maxReplicas: 2`** *(matching your 2 Oracle App Workers)*.

**Automate Edits 1 & 2 (pick your OS, run from repo root `take-my-order/`):**

*   **Windows (PowerShell):**
    ```powershell
    $user = "<YOUR_DOCKER_USERNAME>"
    Get-ChildItem "infrastructure\k8s\base\services\*.yaml" `
      -Exclude "kustomization.yaml","hpa.yaml" | ForEach-Object {
      (Get-Content $_ -Raw) `
        -replace "berkayozr/", "$user/" `
        -replace "imagePullPolicy: Never", "imagePullPolicy: Always" `
        | Set-Content $_ -NoNewline
    }
    @("monitoring\grafana.yaml","monitoring\prometheus.yaml",
      "monitoring\zipkin.yaml","platform\kafka-ui.yaml") | ForEach-Object {
      $f = "infrastructure\k8s\base\$_"
      (Get-Content $f -Raw) -replace "node-type: monitoring", "node-type: infra" `
        | Set-Content $f -NoNewline
    }
    ```
*   **macOS / Linux (Bash):**
    ```bash
    USER_NS="<YOUR_DOCKER_USERNAME>"
    sed -i.bak -e "s|berkayozr/|${USER_NS}/|g" \
      -e "s|imagePullPolicy: Never|imagePullPolicy: Always|g" \
      infrastructure/k8s/base/services/{gateway,identity,order}.yaml \
      infrastructure/k8s/base/services/{payment,restaurant}.yaml
    sed -i.bak "s|node-type: monitoring|node-type: infra|g" \
      infrastructure/k8s/base/monitoring/{grafana,prometheus,zipkin}.yaml \
      infrastructure/k8s/base/platform/kafka-ui.yaml
    rm -f infrastructure/k8s/base/*/*.bak
    ```

*(Then open `infrastructure/k8s/base/services/hpa.yaml` and update `gateway-service-hpa` `maxReplicas` to `1` and the 4 domain HPAs to `2`.)*

### 7.2 Create Prod Secrets & Upload

For security, `.gitignore` excludes both production secret files from GitHub, so create them locally before uploading:

1.  **Create `infrastructure/k8s/overlays/central-hub/central-secrets.env`** (all **14 required keys**):
    ```properties
    # Bare-Metal PostgreSQL 16 (must match Section 4.3)
    DB_USERNAME=<CHOOSE_DB_USER>
    DB_PASSWORD=<STRONG_DB_PASSWORD>

    # Keycloak Admin & Client (NEVER add a trailing '/' to KEYCLOAK_ISSUER_URI!)
    KEYCLOAK_ADMIN_USERNAME=<CHOOSE_KEYCLOAK_ADMIN_USER>
    KEYCLOAK_ADMIN_PASSWORD=<CHOOSE_STRONG_KEYCLOAK_PASS>
    KEYCLOAK_CLIENT_SECRET=replace-after-keycloak-setup
    KEYCLOAK_ISSUER_URI=http://<ORACLE_MASTER_PUBLIC_IP>:30080
    KC_PROXY=none

    # Kafka UI Login Form Credentials
    KAFKA_UI_USERNAME=<CHOOSE_KAFKA_UI_USER>
    KAFKA_UI_PASSWORD=<CHOOSE_STRONG_KAFKA_UI_PASS>

    # Master Node Addresses (MASTER_PRIVATE_IP is used by all DB connections)
    MASTER_PUBLIC_IP=<ORACLE_MASTER_PUBLIC_IP>
    MASTER_PRIVATE_IP=<MASTER_PRIVATE_IP>

    # API Gateway CORS Allowed Origins (updated to HTTPS in Section 9.3)
    ALLOWED_ORIGINS=http://localhost:4200,http://localhost:4201,http://localhost:4202,http://<AWS_PUBLIC_IP>:30088

    # Initial Platform Super Admin (auto-seeded by identity-service)
    SYSTEM_ADMIN_EMAIL=<CHOOSE_SYSTEM_ADMIN_EMAIL>
    SYSTEM_ADMIN_PASSWORD=<CHOOSE_STRONG_SYSTEM_ADMIN_PASS>
    ```
2.  **Create `infrastructure/k8s/overlays/central-hub/monitoring-secrets.env`** (2 required Grafana keys):
    ```properties
    GF_SECURITY_ADMIN_USER=<CHOOSE_GRAFANA_ADMIN_USER>
    GF_SECURITY_ADMIN_PASSWORD=<CHOOSE_STRONG_GRAFANA_PASS>
    ```
3.  **Sync `infrastructure/k8s/overlays/central-hub/patch-config-maps.yaml`:** Update line 11 (`KEYCLOAK_ISSUER_URI`) to `"http://<ORACLE_MASTER_PUBLIC_IP>:30080"` (no trailing `/`).
4.  **Upload `infrastructure/k8s` to `k8s-master-infra`:**
    ```powershell
    ssh -i "C:\ssh-keys\take-my-order\cluster-key.key" `
      ubuntu@<ORACLE_MASTER_PUBLIC_IP> "mkdir -p ~/infrastructure"
    scp -r -i "C:\ssh-keys\take-my-order\cluster-key.key" `
      .\infrastructure\k8s ubuntu@<ORACLE_MASTER_PUBLIC_IP>:~/infrastructure/
    ```
    *(On macOS/Linux, adjust the key path, use `\` instead of `` ` ``, and replace `.\infrastructure\k8s` with `./infrastructure/k8s`.)*

### 7.3 Deploy Overlay & Trigger Build

1.  **Apply the Production Overlay First (on `k8s-master-infra`):**
    ```bash
    cd ~/infrastructure/k8s
    sudo kubectl apply -k overlays/central-hub
    watch sudo kubectl get pods -A -o wide
    ```
    Infrastructure pods (`zookeeper`, `kafka-broker-1/2/3`, `schema-registry`, `keycloak`, `redis`, `kafka-ui`, `prometheus-deployment`, `grafana-deployment`, `zipkin`) start immediately on `k8s-master-infra`, and `Job/init-kafka` completes (`0/1 Completed`, 12 topics created). The 5 microservice pods wait in `ImagePullBackOff` until Step 2 pushes your `:v1` images to Docker Hub.
2.  **Run GitHub Actions Multi-Arch Build (`~12–18 minutes` under QEMU):**
    Open the authorized GitHub repository → **Actions → `Build and Push Microservices` → Run workflow → `prod` → Run workflow**.
    When all 12 workflow steps turn green, GitHub Actions has pushed all 5 multi-arch images (`amd64` + `arm64`) to Docker Hub and executed `sudo kubectl rollout restart deployment -n take-my-order` over SSH!
    *   `gateway-service` pulls the `linux/amd64` layer onto **`aws-gateway-worker`**.
    *   `identity`, `order`, `payment`, and `restaurant` pull `linux/arm64` layers onto **`k8s-worker-01`** and **`k8s-worker-02`**.

---

## 8. Keycloak Realm & Secret Sync

### 8.1 Plain-HTTP Admin Access (Pre-SSL)

*(Skip if Cloudflare HTTPS is already active.)* Once `keycloak` shows `1/1 Running`, disable `sslRequired=EXTERNAL` on the `master` realm from **`k8s-master-infra`**:

```bash
sudo kubectl exec -n take-my-order deployment/keycloak -- \
  /opt/keycloak/bin/kcadm.sh config credentials \
  --server http://localhost:8080 --realm master \
  --user <KEYCLOAK_ADMIN_USERNAME> --password '<KEYCLOAK_ADMIN_PASSWORD>'

sudo kubectl exec -n take-my-order deployment/keycloak -- \
  /opt/keycloak/bin/kcadm.sh update realms/master -s sslRequired=NONE
```

### 8.2 Import Realm & Rotate Secret

1.  Open **`http://<ORACLE_MASTER_PUBLIC_IP>:30080`** *(or `https://<keycloak-sub>.<YOUR_DOMAIN>` if Cloudflare is already active)* in a Private/Incognito browser window and sign in with `KEYCLOAK_ADMIN_USERNAME` / `KEYCLOAK_ADMIN_PASSWORD`.
2.  Click the top-left **Keycloak master** dropdown → **Create Realm → Browse...** → select **`local-env/keycloak/realm-export.json`** from your local clone → click **Create**.
    *   Provisions `take-my-order-realm`, `take-my-order-client`, all **5 Declarative User Profile attributes**, all **5 JWT mappers**, and production session/token lifespans (`7d` idle / `30d` max / `5m` access / `Revoke refresh tokens: OFF`). *(Prefer manual click-by-click setup? Follow [Keycloak Realm Configuration](keycloak-setup.md).)*
3.  If pre-Cloudflare plain HTTP: open **Realm settings → General**, set **Require SSL** to **`None`**, and click **Save**.
4.  **Rotate Client Secret (Mandatory):** Confirm `take-my-order-realm` is selected → **Clients → `take-my-order-client` → Credentials** → click **Regenerate** next to **Client Secret** (replaces `local-dummy-secret`) → click **Copy**.

### 8.3 Sync Secret & Restart Services

On **`k8s-master-infra`**, paste the copied secret into `KEYCLOAK_CLIENT_SECRET=` inside `central-secrets.env`, re-apply Kustomize, and restart only the 5 microservices:

```bash
nano ~/infrastructure/k8s/overlays/central-hub/central-secrets.env
sudo kubectl apply -k ~/infrastructure/k8s/overlays/central-hub
sudo kubectl rollout restart deployment \
  gateway-service identity-service order-service \
  payment-service restaurant-service -n take-my-order
```

**Verification Gate (allow 2 minutes):**

```bash
sudo kubectl get pods -n take-my-order -o wide
sudo kubectl logs deployment/identity-service -n take-my-order | tail -n 30
```

Confirm all **14 workloads** in `take-my-order` (`13` pods `1/1 Running` + `init-kafka` `0/1 Completed`), `gateway-service` running on `aws-gateway-worker`, and `identity-service` logging **`Default system admin created successfully.`**!

---

## 9. Cloudflare HTTPS & Edge Nginx

### 9.1 Onboard Domain & 6 DNS Records

1.  Sign in to [dash.cloudflare.com](https://dash.cloudflare.com/), click **Add a site**, enter **`<YOUR_DOMAIN>`**, select the **Free** plan, and update your registrar's nameservers to the two Cloudflare nameservers assigned to you.
2.  Open **DNS → Records**, delete any default registrar parking `A` records (`Edit → Delete`), and click **`+ Add record`** six times (**Proxy status = `Proxied` 🟠**, **TTL = `Auto`**):

| Type | Name | IPv4 Target | Proxy Status | Purpose |
| :---: | :--- | :--- | :---: | :--- |
| `A` | `api` | `<AWS_PUBLIC_IP>` | 🟠 Proxied | API Gateway & Swagger Hub (`aws-gateway-worker`) |
| `A` | `<keycloak-sub>` | `<ORACLE_MASTER_PUBLIC_IP>` | 🟠 Proxied | Keycloak OIDC Authority & Admin (`k8s-master-infra`) |
| `A` | `<grafana-sub>` | `<ORACLE_MASTER_PUBLIC_IP>` | 🟠 Proxied | Grafana Dashboard (`k8s-master-infra`) |
| `A` | `@` | `<AWS_PUBLIC_IP>` | 🟠 Proxied | B2C Customer Portal (`aws-gateway-worker`) |
| `A` | `partners` | `<AWS_PUBLIC_IP>` | 🟠 Proxied | B2B Merchant Portal (`aws-gateway-worker`) |
| `A` | `backoffice` | `<AWS_PUBLIC_IP>` | 🟠 Proxied | Internal Admin Portal (`aws-gateway-worker`) |

*(Use non-obvious prefixes for `<keycloak-sub>` and `<grafana-sub>` to deter automated scanners. Option A has no Jenkins server, so only 6 records are needed.)*

### 9.2 Flexible SSL & Origin Rules

1.  **SSL/TLS Mode:** Open **SSL/TLS → Overview → Configure**, select **`Flexible`**, and click **Save** *(leave the greyed-out lower box untouched)*.
2.  **Origin Rules:** Open **Rules → Overview → `+ Create rule` → `Origin Rule`**. On each rule, keep **Host Header**, **SNI**, and **DNS Record** set to **`Preserve`**, set **Destination Port** to **`Rewrite to...`**, verify green `Rule validation passed`, and click **Deploy**:

| Rule Name | Incoming Hostname Match | Destination Port | Target Node |
| :--- | :--- | :---: | :--- |
| `Gateway Port 30180` | `api.<YOUR_DOMAIN>` | **`30180`** | `aws-gateway-worker` |
| `Keycloak Port 30080` | `<keycloak-sub>.<YOUR_DOMAIN>` | **`30080`** | `k8s-master-infra` |
| `Grafana Port 30030` | `<grafana-sub>.<YOUR_DOMAIN>` | **`30030`** | `k8s-master-infra` |
| `Frontend Port 30088` *(optional)* | `<YOUR_DOMAIN>`, `partners.<YOUR_DOMAIN>`, `backoffice.<YOUR_DOMAIN>` | **`30088`** | `aws-gateway-worker` |

### 9.3 Switch Cluster to HTTPS

On **`k8s-master-infra`**, edit `~/infrastructure/k8s/overlays/central-hub/central-secrets.env` *(**no trailing `/`** on `KEYCLOAK_ISSUER_URI`!)*:

```properties
KEYCLOAK_ISSUER_URI=https://<keycloak-sub>.<YOUR_DOMAIN>
KC_PROXY=edge
ALLOWED_ORIGINS=https://<YOUR_DOMAIN>,https://partners.<YOUR_DOMAIN>,https://backoffice.<YOUR_DOMAIN>,http://localhost:4200,http://localhost:4201,http://localhost:4202
```

Update `KEYCLOAK_ISSUER_URI` in `~/infrastructure/k8s/overlays/central-hub/patch-config-maps.yaml` to `"https://<keycloak-sub>.<YOUR_DOMAIN>"`, then apply and restart:

```bash
sudo kubectl apply -k ~/infrastructure/k8s/overlays/central-hub
sudo kubectl rollout restart deployment \
  keycloak gateway-service identity-service \
  order-service payment-service restaurant-service -n take-my-order
```

### 9.4 Optional: AWS Host Nginx (`:30088`)

Complete this subsection **only** if you host the 3 Angular SPA portals on `aws-gateway-worker` (port `30088` avoids K3s Traefik ports `80`/`443`):

```bash
ssh -i aws-gateway-key.pem ubuntu@<AWS_PUBLIC_IP>
sudo apt update && sudo apt install -y nginx
sudo rm -f /etc/nginx/sites-enabled/default
sudo mkdir -p /var/www/take-my-order/{customer,merchant,admin}
sudo chown -R ubuntu:www-data /var/www/take-my-order
sudo chmod -R 755 /var/www/take-my-order
```

Create `/etc/nginx/sites-available/take-my-order.conf` (`sudo nano`) with one `server` block per portal (repeating for `partners.<YOUR_DOMAIN>` → `/merchant` and `backoffice.<YOUR_DOMAIN>` → `/admin`):

```nginx
server {
    listen 30088;
    server_name <YOUR_DOMAIN>;
    root /var/www/take-my-order/customer;
    index index.html;
    gzip on;
    gzip_types text/plain text/css application/json application/javascript;

    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header Referrer-Policy "no-referrer" always;

    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

```bash
sudo ln -sf /etc/nginx/sites-available/take-my-order.conf \
  /etc/nginx/sites-enabled/take-my-order.conf
sudo nginx -t && sudo systemctl enable --now nginx && sudo systemctl reload nginx
```

*(Deploy your compiled Angular portal bundles into `/var/www/take-my-order/{customer,merchant,admin}`—until files are copied there, Nginx returns `403`/`404` on empty folders.)*

---

## 10. Final Verification & Operations

### 10.1 Health, OIDC & Login Smoke Test

1.  **Verify All Pods Across Both Clouds (on `k8s-master-infra`):**
    ```bash
    sudo kubectl get pods -n take-my-order -o wide
    sudo kubectl get pods -n monitoring -o wide
    sudo kubectl get hpa -n take-my-order
    ```
    Confirm **14 workloads** in `take-my-order` (`gateway-service` on `aws-gateway-worker`, domain services on `k8s-worker-01/02`, infra & `kafka-ui` on `k8s-master-infra`) and **3 pods** in `monitoring` (`1/1 Running` on `k8s-master-infra`).
2.  **Verify Split-Horizon OIDC Issuer:**
    ```bash
    curl -s https://<keycloak-sub>.<YOUR_DOMAIN>/realms/take-my-order-realm/.well-known/openid-configuration \
      | grep '"issuer"'
    ```
    **Expected Result:** `"issuer":"https://<keycloak-sub>.<YOUR_DOMAIN>/realms/take-my-order-realm"`.
3.  **Verify Unified Swagger UI Hub:** Open **`https://api.<YOUR_DOMAIN>/swagger-ui.html`** and confirm all 5 definitions load *(Read-Only Showcase Mode is active in production)*.
4.  **Live Authentication Smoke Test:**
    ```bash
    curl -i -X POST https://api.<YOUR_DOMAIN>/auth/login/internal \
      -H "Content-Type: application/json" \
      -d '{"email":"<CHOOSE_SYSTEM_ADMIN_EMAIL>","password":"<CHOOSE_STRONG_SYSTEM_ADMIN_PASS>"}'
    ```
    **Expected Result:** HTTP **`200 OK`** returning `accessToken` and `refreshToken`.

### 10.2 Grafana Sources & Debug Tunnels

1.  Open **`https://<grafana-sub>.<YOUR_DOMAIN>`** (`monitoring-secrets.env` credentials) → **Connections → Data sources → Add data source**:
    *   **Prometheus:** `http://prometheus-service:9090` → **Save & test**.
    *   **Zipkin:** `http://zipkin:9411` → **Save & test**.
2.  **Kafka UI Console:** Open `http://<ORACLE_MASTER_PUBLIC_IP>:30091` (`central-secrets.env` credentials).
3.  **On-Demand Raw Prometheus / Zipkin Inspection (SSH Local Port Forward):**
    ```bash
    # 1. From your local workstation:
    ssh -i cluster-key.key -L 9090:127.0.0.1:9090 -L 9411:127.0.0.1:9411 \
      ubuntu@<ORACLE_MASTER_PUBLIC_IP>

    # 2. Inside that Master SSH session:
    sudo kubectl port-forward -n monitoring svc/prometheus-service 9090:9090 &
    sudo kubectl port-forward -n monitoring svc/zipkin 9411:9411
    ```
    Open `http://localhost:9090` and `http://localhost:9411` locally; press **`Ctrl + C`** and run `kill %1` when done.

### 10.3 Hybrid-Cloud Troubleshooting

*   **`gateway-service` on AWS cannot reach `identity-service` or `order-service` (HTTP `504 Gateway Timeout`):** Confirm `UDP 8472` (NOT TCP) is open in **both** AWS `k8s-gateway-sg` (for Master, Worker 1, and Worker 2 public `/32` IPs) **and** OCI `K8S-VCN` Security List (for `<AWS_PUBLIC_IP>/32`), and verify `iptables` was flushed on all 4 nodes (Section 4.1).
*   **Pods pull wrong CPU architecture (`exec format error`):** Verify `.github/workflows/deploy.yml` built with `platforms: linux/amd64,linux/arm64`, all 5 `services/*.yaml` set `imagePullPolicy: Always`, and the `image:` prefix matches `<YOUR_DOCKER_USERNAME>/`.
*   **Monitoring pods (`grafana`, `prometheus`, `zipkin`, `kafka-ui`) stuck in `Pending`:** Confirm you did NOT overwrite `node-type=infra` on `k8s-master-infra` and that all 4 manifests were updated from `node-type: monitoring` to `node-type: infra` (Section 7.1).
