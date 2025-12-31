# PRODUCTION ENVIRONMENT SETUP (FULL FREE)

## 1. Hybrid Multi-Architecture Kubernetes Cluster Installation (Infrastructure Provisioning)

Before any code deployment, it is necessary to determine the Pod inventory and the corresponding server requirements. Accordingly;

### 1.1. Pod Inventory and Resource Distribution

* **Monitoring & Infrastructure:**
  * Kubernetes (K3s)
  * 3 adet Kafka Broker
  * Kafka UI
  * Keycloak
  * Grafana
  * Prometheus
  * Zipkin
  * Init Kafka
  * Schema Registry
  * Zookeeper
  * Postgres (Bare Metal)
  * PVC's (Burada nelerin pvc'leri vardı belirtmemiz lazım hatta alt alta da sıralayabiliriz.)
* **Microservices:**
  * Payment
  * Restaurant
  * Customer
  * Order
  * Gateway Service

> **NOTE:** Since a completely free production environment is being prepared, although the Database per Service logic usually operates at the production-grade level, the database is structured with the **Schema per Service** logic on a single DB because the project scope is not large and we do not have sufficient server count and power.

### 1.2. Server Resources (Oracle & AWS Free Tier)

We can obtain the necessary servers for this project for free using **Oracle Always Free** and **AWS Free Tier**.

* **Oracle Always Free:**
  * 24 GB RAM, 4 OCPU, 200 GB Storage right, Architecture (Ampere ARM / aarch64)
  * The best part is that we can use all this right on a single server or split it into a maximum of 4 different servers if we want.
  * At this point, we will use it on 3 different servers. In the form of 12 GB RAM 2 OCPU 60 GB Storage / 2x (6 GB RAM, 1 OCPU, 50 GB Storage).
* **AWS Free Tier:**
  * 1 GB RAM, 1 vCPU, 30 GB Storage right, Architecture (Amd64 / x86_64)

### 1.3. Distribution of Pod Inventory to Servers

* **Master Node:**
  * All services under the Monitoring & Infrastructure heading will be installed and running here.
  * At the same time, the manager of the Kubernetes Cluster will be located on this server. The entire cluster will be managed from this server.
  * Since this place will shoulder the largest workload, it will take the largest piece of the pie in server resources, namely the 12 GB RAM 2 OCPU Oracle Server.
* **Worker Node 1-2:**
* **Worker Node 1-2:**
  * These are 2 worker servers consisting of 6 GB RAM, 1 OCPU, 50 GB Storage.
  * These two servers will run 4 of the 5 microservices we have (Payment, Restaurant, Order, Customer) on them by sharing them with a specific distribution strategy.
  * The Pods located on these servers will be managed by the Master Node.
* **Gateway Node:**
  * This microservice will run on its own in an environment independent of other microservices.
  * For this, the weak AWS server was chosen and it will do the job.

### 1.4. Creation Configurations of Oracle Servers

#### A) VCN (Virtual Cloud Network) Installation::

The 3 servers on Oracle will be connected to each other via a common network. Thus, data flow speed will be very high and latencies will be very low. For this, Virtual Cloud Network adjustments need to be made.

1. Go to Networking > Virtual Cloud Networks > Create VCN from the menu
2. **Name:** K8S-VCN
3. Leave other options as default
4. Then go into K8S-VCN > Security > Default Security List for K8S-VCN > Security Rules > Add Ingress Rules
5. Enter the rules in the table below:

| Source CIDR | IP Protocol | Source Port Range | Destination Port Range | Description |
| :--- | :--- | :--- | :--- | :--- |
| 0.0.0.0/0 | TCP | Empty (ALL) | 80 | Web ve K8s |
| 0.0.0.0/0 | TCP | Empty (ALL) | 443 | Web ve K8s |
| 0.0.0.0/0 | TCP | Empty (ALL) | 6443 | Web ve K8s |
| 0.0.0.0/0 | TCP | 30000-32767 | 30000-32767 | NodePorts |
| 0.0.0.0/0 | TCP | Empty (ALL) | 30091 | KAFKA_UI |
| 0.0.0.0/0 | TCP | Empty (ALL) | 30080 | Keycloak |
| 0.0.0.0/0 | TCP | Empty (ALL) | 30030 | Grafana |
| 0.0.0.0/0 | TCP | Empty (ALL) | 30090 | Prometheus |
| 0.0.0.0/0 | TCP | Empty (ALL) | 30094 | Zipkin |
| 10.0.0.0/16 | All Protocols | - | - | Cluster Internal Traffic (Full communication between Kubernetes servers) |
| {aws public ip}/32 | TCP | Empty (ALL) | 6443 | API Server Access (gateway) |
| {aws public ip}/32 | TCP | Empty (ALL) | 10250 | Kubelet Metrics (aws) |
| {aws public ip}/32 | UDP | Empty (ALL) | 8472 | Flannel VXLAN (Tunnel - gateway) |
#
#### B) Instances
Go to Instances from the menu and create a separate instance for each server:

**1. Server: k8s-master-infra**
* **Name:** k8s-master-infra
* **Placement:** Mümkünse AD-1 veya AD-2 seç
* **Image:** Ubuntu 24.04 (Canonical Ubuntu).
* **Shape:** Ampere (VM.Standard.A1.Flex).
* **OCPU:** 2
* **Memory (RAM):** 12 GB
* **Networking:**
    * **Primary Network:** Select your existing VCN (K8S-VCN).
    * **Subnet:** Select the existing Public Subnet.
    * **Public IPv4 Address:** The "Assign a public IPv4 address" option should be **YES/CHECKED**
* Select the "Generate a key pair for me" option.
* Click the "Save Private Key" button and download. (Let's name it `cluster-key.key`).
* Click the "Save Public Key" button and download. (Let's name it `cluster-key.pub`).
* **Boot Volume (Very Important ⚠️):**
    * Check the "Specify a custom boot volume size" box.
    * Set the size to **60 GB**. (For Logs and Kafka data).

**2. Server: k8s-worker-01**
* **Name:** k8s-worker-01
* **Image/Shape:** Ubuntu 24.04 / Ampere.
* **OCPU:** 1
* **Memory (RAM):** 6 GB
* **Networking:** Same VCN, Same Subnet, Public IP **YES**.
* **Add SSH Keys:** Upload the **Public** one (.pub) of your `cluster-key` file.
* **Boot Volume:**
    * "Specify a custom boot volume size" -> **50GB**.

**3. Server: k8s-worker-02**
* **Name:** k8s-worker-02
* **Image/Shape:** Ubuntu 24.04 / Ampere.
* **OCPU:** 1
* **Memory (RAM):** 6 GB
* **Networking:** Same VCN, Same Subnet, Public IP **YES**.
* **Add SSH Keys:** Upload the **Public** one (.pub) of your `cluster-key` file.
* **Boot Volume:**
    * "Specify a custom boot volume size" -> **50GB**.

### 1.5. Creation Configurations of AWS Gateway Server

This server is also our 3rd Worker server, but we define it as Gateway with a special naming.
Create an instance. Fill in the following information on the creation screen:

#### A) Determining Server Specifications:
* **Name:** k8s-gateway-aws
* **OS (AMI):** Ubuntu Server 22.04 LTS (HVM), SSD Volume Type. (64-bit (x86)).
* **Instance Type:** `t2.micro` (Must see "Free tier eligible" text).
* **Key Pair (Login):**
    * "Create new key pair" de.
    * **Name:** aws-gateway-key
    * **Type:** RSA
    * **Format:** .pem (For OpenSSH).
    * Download and keep.
* **VPC:** `vpc-xxxxxxxxxx` (default)
* **Alt ağ:** No preference
* **Auto-assign Public IP:** Enable

#### B) Security Group:
* **Create security group:** Selected
* **Security group name:** `k8s-gateway-sg`
* **Description:** Oracle Hybrid Cluster Connectivity
* Under "Security group rule 1", Source part is selected as Anywhere (0.0.0.0/0) for port 22. Let's change this for security.
* **Source type:** Select **My IP** option instead of Anywhere.
* Thus, AWS will automatically detect your home IP address and allow only you. We will have closed the door to hackers.

Press the "Add security group rule" button **4 times** and enter the following lines one by one.

* **Rule 2 (Kubernetes API - For Master Management):**
    * **Type:** Custom TCP
    * **Port range:** 6443
    * **Source type:** Custom
    * **Source:** `{Oracle Master Public Ip}/32`
    * **Description:** K8s API Oracle Access

* **Rule 3 (Kubelet Metrics - For Health Check):**
    * **Type:** Custom TCP
    * **Port range:** 10250
    * **Source type:** Custom
    * **Source:** `{Oracle Master Public Ip}/32`
    * **Description:** Kubelet Oracle Access

* **Rule 4 (Flannel VXLAN - For Tunnel Connection - VERY IMPORTANT):**
    * **Type:** Custom UDP <--- **Attention: It will be UDP not TCP!**
    * **Port range:** 8472
    * **Source type:** Custom
    * **Source:** `{Oracle Master Public Ip}/32`
    * **Description:** Flannel Overlay Network

* **Rule 5 (Gateway Traffic - For Outside World):**
    * **Type:** Custom TCP
    * **Port range:** 8080
    * **Source type:** Anywhere
    * **Source:** `0.0.0.0/0`
    * **Description:** Public Gateway Traffic

> **Why Did We Enter Only Oracle IP as Source?**
>
> Normally in novice setups, "Allow All Traffic" is said and done. This is like removing the door of the house and sitting. We did this: "Only 2 people in the world can touch this server:"
>
> 1. **You (My IP):** To manage (SSH).
> 2. **Oracle Master Server:** To give work.
>
> **Some Ports Used:**
> * **TCP 22 (SSH):** This is your **Keyhole**. For you to enter the server and write commands. We opened it only to your home IP so that no one else can even try passwords.
> * **TCP 10250 (Kubelet API):** This is the **"Pulse Check"**. The Oracle Master server asks the worker in AWS these questions from this port: "Are you working?", "How are your logs?", "Are the Pods healthy?". If you close this, the Master sees the AWS server as "NotReady" (Dead).
> * **UDP 8472 (Flannel VXLAN - The Most Critical!):** This is the **"Secret Tunnel"**. When the **Order Service** in Oracle sends data to the **Gateway Service** in AWS, packets pass through this port. It is like an encrypted, virtual pipeline over the internet. It is UDP for speed.
> * **TCP 8080 (Gateway):** This is the **"Customer Door"**. If you noticed, we made only this one `0.0.0.0/0`. Because we cannot know where the customers (Website, Mobile app) who will use your application will come from. It must be open to everyone.

## 2. Preparing the Production Environment and Uniting Servers on Common Network

### 2.1. Iptable Cleanup
* Servers on Oracle act as if they are in the same LAN environment using a common VCN and communicate over each other's private ip addresses. In this way, traffic becomes light-speed and free.
* Gateway differs at this point. Since Gateway is located on the AWS server and is separate from the Oracle environment, we need to connect these two servers using tunneling. We created the necessary ingress rules and security groups settings for this at the beginning above.
* However, creating these rules only covers the external security of the relevant server services. Servers also have internal security when they are created themselves, and these security rules can override external rules. In order for Master and Worker servers to communicate, iptables on the servers must be cleaned. For this, the following commands must be run on all servers:

```
- sudo iptables -F
- sudo iptables -X
- sudo iptables -t nat -F
- sudo iptables -t nat -X
- sudo iptables -t mangle -F
- sudo iptables -t mangle -X
- sudo iptables -P INPUT ACCEPT
- sudo iptables -P FORWARD ACCEPT
- sudo iptables -P OUTPUT ACCEPT
```
* Make the setting permanent (So it doesn't break upon restart):
```
- sudo apt-get install -y iptables-persistent
- sudo netfilter-persistent save
```
* Iptables cleanup does not make servers insecure. Oracle and AWS already have the necessary traffic control rules for these servers.

### 2.2. Customizing Gateway Server Name
* AWS names the server itself by default. We can update its name ourselves so that its name does not get confused within the Cluster.
* Update server name:
>  * **Command**: sudo hostnamectl set-hostname aws-gateway-worker
* Update host name
>  * **Command:** sudo nano /etc/hosts
  * When entered, add the following under the text 127.0.0.1 localhost:
>  * 127.0.0.1 aws-gateway-worker
* After restart, prevent the cloud provider from resetting the Server name (hostname).
>  * **Command:** sudo sed -i 's/preserve_hostname: false/preserve_hostname: true/g' /etc/cloud/cloud.cfg

## 3. Kubernetes (K3s) Installation
### 3.1 Kubernetes Installation on Master Server
* First connect to the Master server:
>  * **Command:** ssh -i cluster-key.key ubuntu@{master public ip}
* After connecting, paste the following command at once. This command installs K3s, uses {master private ip} for internal communication but allows you to connect from outside with {master public ip}.
> * **Command:** curl -sfL https://get.k3s.io | sh -s - server --node-ip {master private ip} --advertise-address {master private ip} --tls-san {master public ip}
* When the installation is finished (takes 15-20 sec), copy the Entry Ticket (Token) required to connect Workers and note it down somewhere:
> * **Command:** sudo cat /var/lib/rancher/k3s/server/node-token

### 3.2  Connecting Worker-01
* Now connect to Worker-01
> * **Command:** ssh -i cluster-key.key ubuntu@{worker 1 public ip}
* Install K3s on the server. In the command below, we copy and paste the key created inside the master server.
> * **Command:** curl -sfL https://get.k3s.io | K3S_URL=https://{master private  ip}:6443 K3S_TOKEN={master k3s token} sh -s - agent --node-ip {worker 1 private ip}
* If it lights up green and appears active (running) in the output, it has been successfully created. However, if it gives an error, make sure you entered the commands correctly or that the iptable update is saved.
* Afterwards, go back and restart k3s-agent.
> * **Commands:**
>   * sudo systemctl restart k3s-agent
>   * sudo systemctl status k3s-agent

### 3.3 Connecting Worker-02
* Perform the same operations as in option 3.2, filling in only with information belonging to worker-02.

### 3.4 Connecting Gateway
* Connect to AWS server
> * **Command:** ssh -i "aws-gateway-key.pem" ubuntu@<AWS_PUBLIC_IP>
* Install K3s on the server. In the command below, we copy and paste the key created inside the master server.
> * **Command:** curl -sfL https://get.k3s.io | K3S_URL=https://{Master Public Ip}:6443 K3S_TOKEN="{PASTE_TOKEN_HERE}" sh -s - agent --node-external-ip {AWS_PUBLIC_IP} --node-ip {AWS_PRIVATE_IP}
* --node-external-ip: For Oracle Master to reach AWS
* --node-ip: For network traffic inside AWS

### 3.5 Final Check:
* Enter the Oracle Master Server and write the following command to check if the AWS server is inside the kubernetes network:
> * **Command:**  sudo kubectl get nodes
* If the status of all our nodes is "Ready", it means the K3s Cluster has been successfully created.

## 4. Labeling/Node Selector

### 4.1 Reason for Use
* Kubernetes sees all nodes as "equal" by default. If we don't tell it "This place is AWS, this place is Oracle Master", it might go and try to install your Order Pod on that small server in AWS by mistake or bury the Gateway inside Oracle.
* To prevent this, each service includes the label information of the server or servers it will be installed on within the yaml configurations. At the same time, the relevant servers must also be labeled. Thus, K3s foresees which Pod can be found on which Node or Nodes.
* All commands used to create labels will be run in the terminal of the **Master server** (k8s-master-infra) because kubectl is the "Captain's Bridge" of Kubernetes. As the captain, orders are given from the bridge (Master); there is no need to go to the engine room (Workers) or the deck (AWS) and shout one by one. You say "Label!" from Master, Master goes and finds those servers and pastes their labels.
  These labels give labels to the servers in the kubernetes network. Thus, when kubernetes needs to open a new pod or create a copy in case a pod is insufficient, it goes and creates the new pod in the most suitable one among the servers with the relevant label.

### 4.2 Labeling Process:
* **Master Node:**
  * **“infra” =>** It is the label of the Master Node. All platforms that will run on the Master Node are labeled with this.
  * **Note:** init-kafka is also a job definition. Job definitions run as pods even if for a short time. For this reason, label definition is required.
  > * **Command:** sudo kubectl label nodes k8s-master-infra node-type=infra --overwrite
  * With this command, the master server will be customized for Grafana, Prometheus, Zipkin, kafka-broker-1/2/3, schema-registry, keycloak, kafka-ui, zookeeper.
---
* **Worker 1 and Worker 2:**
  * **"app” =>** It is the label of the Nodes where other microservices except Gateway will be installed.
  > * **Command:** sudo kubectl label nodes k8s-worker-01 node-type=app --overwrite
  > * **Command:** sudo kubectl label nodes k8s-worker-02 node-type=app --overwrite
---
* **Gateway Sunucusu:**
  * **"gateway" =>** It is the label of the Node where the Gateway microservice will be installed.
  > * **Command:** sudo kubectl label nodes aws-gateway-worker node-type=gateway --overwrite
---
* **Check:**
  > * **Command:** sudo kubectl get nodes --show-labels
  > * **Output:** It is done the moment you see the labels (node-type=...)

## 5. Scheduling Strategy:
* Scheduling strategy is the methods by which Kubernetes determines which servers to install its Pods on. Thanks to these strategies, service load is distributed with performance or cost-oriented approaches.
* In this project, we distribute Pods with two different strategies.
### 5.1 Node Selector
* We defined labels for each of the Servers (Nodes) before. This is a Definition process. It is like sticking a sticky note on Kubernetes servers (Node). You assign a property to the server, but this alone is useless.
  * **Example:** You stuck the label **"node-type=infra"** to the **k8s-master-infra** server.
  * Result: Currently the server only has a label. Pods can still distribute randomly.
* The complementary step of this process is to make Node Selector definitions.
* This is a **rule setting** process. It is the **main routing mechanism** of our project. You go to the Pod's definition (YAML file) and say, "You can only work on servers with this label".
  * **Example:** You said **"nodeSelector: node-type: infra"** to the Kafka Pod.
  * **Result:** Kubernetes does not put this Pod on any server that does not have the label **"node-type=infra"**.
* Necessary definitions for this are ready in .yaml files.

### 5.2 LeastAllocated (n-th Node / n-th Pod):
* There are **resources (requests/limits)** definitions in all YAML files.
* These definitions determine how much resource the relevant Pod will reserve on the server by default and limit the maximum amount of resource it can use in case of need.
* Thanks to the use of this method, no Pod can do resource hogging on the server. It cannot attempt to exploit the resources of other Pods. Otherwise, the problem we call **"Noisy Neighbor"** appears and the system becomes unstable.
* The moment these definitions are written, Kubernetes Scheduler starts doing math. Without us needing to make an extra setting, K3s/Kubernetes uses the **LeastAllocated (Select the Least Loaded)** strategy by default.
  * **Example:** Worker-1's RAM is 60% full, Worker-2's RAM is 10% full. When a new Pod comes, Scheduler puts it on Worker-2. It tries to spread the load.

> * **Note:** For Pods to be installed on a specific single Node such as Kafka, Keycloak, Gateway, the 2nd method is not important, but it is critical for our other 4 microservices as they will be installed on Worker-1 and Worker-2.

### 5.3 Alternative Strategies and Reasons for Not Using:
#### A) Resource Bin Packing (MostAllocated):
* It is a cost-oriented strategy. It focuses on reducing the server need by squeezing Pods into the minimum number of servers possible.
  * **Example:** Worker-1 is already 80% full, wait let me fill 100% of it so that Worker-2 remains completely empty, maybe we can unplug it
* There are two reasons for not using this strategy:
  * The fact that existing free resources are already very limited
  * Being able to observe how microservices interact by running on different Nodes

#### B) PodAntiAffinity / Affinity:
* This is the more "smart and flexible" version of Node Selector. While Node Selector says "Either here or nowhere"; **Affinity** can say "If possible let it be here, but if there is no room, another place is fine too".
  * We do not need it since we make the necessary resource adjustments directly according to the need.
* **Anti-Affinity** implies: "Order Service Pod already exists on Worker-1, do not put the second one there, put it on Worker-2".
  * It is divided into Hard and Soft.
  * **Hard:** Strictly enforces separate servers.
  * **Soft:** Says separate servers if possible.
  * In the YAML configuration of all our services, the “replicas” value is determined as 1. Only a single pod will run at the startup moment.
  * Also, **HPA (Horizontal Pod Autoscaler)** was not used due to resource insufficiency. Therefore, it was not used as the creation of replica pods is not expected.

#### C) Topology Spread Constraints:
* This is to say "Let an equal number of Pods fall to each data center" in very large (e.g., 50 servers, spread over 3 different data centers) systems.
  * There is no need for this in our 4-server simple structure.

#### D) HPA (Horizontal Pod Autoscaler)
* It is a system established within Kubernetes so that Pods can be replicated in necessary situations against system bottlenecks.
* Against high resource usages such as Memory Leak or Attack situations, the maximum number of replica Pods that can be created is limited with **“maxReplicas”** in order to reduce cost. It can be increased later if deemed necessary.
* If the resources of the available Nodes are also insufficient, a new server is purchased with Cluster Autoscaler.
  * **HPA (Software Layer):** Looks at "How many soldiers do I need?". The limit is determined by maxReplicas. HPA limit is not enough but there is Hardware. **-> Solution:** maxReplicas in YAML must be increased.
  * **Cluster Autoscaler (Hardware Layer):** Looks at "Do I have a place (Node) to put these soldiers?". HPA wants to increase (Limit is enough) but Hardware (Node) ran out. **-> Solution:** Cluster Autoscaler must purchase a new server.
* We do not need these high-cost methods in our modest project.

## 6. Installing PostgreSQL on Master Server as Bare Metal
### 6.1 PostgreSQL Installation
* Update packages and install Postgres
>* **Commands:**
>  * sudo apt update 
>  * sudo apt install postgresql postgresql-contrib -y 
* Start the service and have it run automatically at every startup
> * Commands:
>   * sudo systemctl start postgresql 
>   * sudo systemctl enable postgresql

### 6.2 Database and User Creation
* The password you determine must be compatible with your Kubernetes Secrets **(central-secrets.env)**.
* Switch to Postgres user:
> * **Command:** sudo -i -u postgres
* Enter SQL console
> * **Command:** psql
* Run the following commands in order:
> * CREATE USER tmoadmin WITH PASSWORD '{password}'; 
> * CREATE DATABASE takemyorder OWNER tmoadmin; 
> * \c takemyorder 
> * CREATE SCHEMA IF NOT EXISTS keycloak AUTHORIZATION tmoadmin; 
> * \q

### 6.3 PostgreSQL Access and Security Settings
* **Opening to Outside (Critical Settings):** We are opening the doors so that Pods can access this database.
> * **Command:** sudo nano /etc/postgresql/{version number}/main/postgresql.conf
* Go to **listen_addresses** line
  * Remove the '#' sign
  * Write * instead of localhost
  * listen_addresses = '*' must be
* **pg_hba.conf editing:**
> * **Command:** sudo nano /etc/postgresql/{version number}/main/pg_hba.conf
* Add the following to the bottom of the file:
> * host all all 10.0.0.0/16 scram-sha-256

> **What does "host all all 10.0.0.0/16 scram-sha-256" mean?**
> * This setting means **"Only servers (Workers) located in my private network (VCN) inside Oracle Cloud can access my database"**. If we wanted everyone to access, we would select this:
>   * 10.0.0.0/16 md5
> * Why Did We Choose This Now?
>   * **Resetting Attack Surface:** No hacker on the internet can reach your database port. The door is open only to those inside the house (Order Service, Payment Service).
>   * **Worker-Master Relationship:** Microservices will talk to Master over 10.0.0.X IPs. That's why allowing the entire VCN block (10.0.0.0/16) is the cleanest management.
> * Those who want to connect from outside will need to do **SSH Tunneling**.
* Just adding this is not enough. **10.0.x.x** is the IP block of your servers (Nodes) among themselves. However, we also need to give permission for the virtual ip block that K3s distributes to pods. Otherwise Postgres will recognize the server but will not recognize the Pod inside it (e.g. Keycloak).
* Also, the Node in AWS is not inside the same **Oracle VCN**. Gateway Pod will also want to access the database thanks to **Flannel VXLAN** tunneling. For this, we also add the following to the bottom inside **pg_hba.conf**:
> * **10.{x}.0.0**
* To find out the x value here, you can use the following command:
> * **Command:** sudo kubectl get pods -A -o wide
* **Restart Postgres:** sudo systemctl restart postgresql
* **Go to Worker 1 and test::**
  > * **Command (install client quickly if psql is not installed):** sudo apt install postgresql-client -y
  > * **Command (Try connecting):** psql -h 10.0.0.121 -U myuser -d order_db

> * **NOTE:** Gateway Node is still not able to access the database directly within the server, but the Gateway Pod can access the database thanks to tunneling. Only Worker-1 and Worker-2 located within the same VCN have gained the right to access the DB directly on a server basis.

## 7. CI/CD Pipeline Setup:
### 7.1 Continuous Integration
#### A) Hybrid-Cloud Multi-Arch Docker Image:
* When creating Docker images, these images must also be compatible with the processor architecture they run on.
* In the current project, servers with both **Arm (aarch64)** and **AMD(x86_64)** processor architectures are located within the Kubernetes Cluster.
* We do not create images manually one by one and upload them to docker-hub when switching to prod environment in our project. For this reason, we do not need to update each dockerfile file separately or create a common dockerfile.
* We leave this task to **GithubActions** with **deploy.yml**. Since our project is **Mono-Repo**, the entire project is compiled, images are created and uploaded to docker-hub every time.
* What is important here is to tell github one by one what images of this project will be created inside deploy.yml (dockerfile of each service etc.) and to specify which processor architectures these images will be compatible with.
* At this point, **QEMU Emulator** and **Buildx** come into play.
* By including these inside the deploy.yml file, we tell GitHub;
  * Install an **Arm** emulator to be able to build with **aarch64** architecture
  * Create multi-arch image using **Buildx**.

#### B) Docker Hub Repositories
* Images created on Github Actions need to be saved somewhere so they can be used later. At this point, we use the Docker Hub platform.
* We create separate repositories for our 5 microservices via Docker Hub.
* To keep the system completely free, we will store all images in public repos. The names of the repositories we will create are as follows:
  > * restaurant-service
  > * payment-service
  > * order-service
  > * customer-service
  > * gateway-service

#### C) Automatically Creating Images and Uploading to Docker Hub
* Github Actions feature is used to accomplish this part.
* **.github/workflows** file path is opened in the base directory of the project. **deploy.yml** is added inside. Here, information regarding **which branch** will be used, which **Java version** will be used, **dockerfile files** of the services that will create the image, **Emulator and Buildx** definitions if images will be created with different processor architecture (multi-arch), but most importantly **Docker Hub login information** where images will be uploaded are located.
* Docker Hub user information is not written hardcoded into the file. Key is found inside Yml, value is pulled via GitHub.
  * It is added with the **New Repository Secret** button in the **Secrets and variables/Actions** section in the settings part of the project.
  * Name: **The key (DOCKER_USERNAME, DOCKER_PASSWORD)** that will also be used in the yml file
  * **Value:** is the value to be injected into the yml file, that is, the username or password itself.
* Thus, whenever something new is added to the Branch named **“Prod”**, Actions will work automatically and create images according to the configurations in deploy.yml and upload them to the repositories inside Docker Hub.

### 7.2 Continuous Delivery
* In this part, we benefited from the **Push-Based Deployment** method.
  * According to this method, when GitHub Actions completes the **CI** process, it sends a request to the master server. When the Master server receives this request, it automatically pulls the latest images via Docker Hub and reruns the relevant deployments.
  * **"imagePullPolicy"** value must absolutely be selected as **“Always”** in the **.yaml** configuration files of all microservices.
  * In our current structure, we do not do versioning for every new version. All versions go out to Docker Hub with the “v1” tag. For this reason, in every rollout situation, we pull and update the latest image without distinguishing between new and old, without saying the same image exists.

#### A) Create Key and Lock
* To be able to set up this system, first a public private key needs to be created.
* Run the following command on your local computer:
> * **Command:** ssh-keygen -t rsa -b 4096 -C "github-actions-deploy" -f ./github_deploy_key
* It shouldn't ask for a password (passphrase), pass with enter-enter. Because Github cannot enter a manual password while accessing the server with a key.
* With this command, two files named **github_deploy_key and github_deploy_key.pub** will be created.
  * **Public Key (the one with .pub extension):** This is the "Lock". We will put it on your server.
  * **Private Key (the one without extension):** This is the "Key". We will give it to GitHub.

#### B) Put the Lock on Server (Public Key)
* We will give the one without extension from these files to github. The content of the file with .pub extension will go to the master server.
* **To read the content of the .pub file:**
> * **Command:** cat github_deploy_key.pub
* It should have given a text starting with **ssh-rsa**. After copying this text, we will connect to the **master server**, enter the following command, go to **authorized_keys**, paste it at the bottom of the file, save and exit:
> * **Command:** nano ~/.ssh/authorized_keys

#### C) Give the Key to GitHub (Private Key)
* Now read the file without extension you created:
> * **Command:** cat github_deploy_key
* **Very Important:** Copy the WHOLE BLOCK starting with **"-----BEGIN OPENSSH PRIVATE KEY-----"** and ending with **"-----END OPENSSH PRIVATE KEY-----"**
* Go to GitHub:
  * Enter the GitHub page of the project. Click on the Settings tab at the top.
  * Click on the Secrets and variables -> Actions option from the menu on the left and press the New repository Secret button.
* Create Secret:
  * **Name:** SERVER_SSH_KEY 
  * **Secret:** Paste the private key you copied here.
  * Press Add secret button.

#### C) Give Other Information to GitHub as well
* GitHub needs to know not only the key, but also which IP address and which username it will connect with. Create two more secrets on the same "Actions Secrets" page:
* **Server IP Address:**
  * **Name:** SERVER_IP
  * **Secret:** Write the Public IP address of the Master server
* **Username:**
  * **Name:** SERVER_USER
  * **Secret:** ubuntu (Or the user you use while connecting to the server).

#### D) deploy.yml
* Within the deploy.yml file, after the parts where the entire **CI** process is completed **(because build operations must be finished, images must be sent to Docker Hub)**, there is that script that will provide connection to the master node and restarting the Pods. Thus, when Github reads the deploy.yml file, it will send a request to the server and give the rollout command.
* script inside deploy.yml:
> * **Command:** sudo kubectl rollout restart deployment -n take-my-order

## 8. Final Deployment & Verification
* After all these operations are completed, we will now create our pods using our K8s configuration files.
* The labeling system will also help very much at this point.
  * We assigned a specific label to each server. At the same time, we assigned a label to each service in the configuration files. At the end of the day, when we run all configuration files (yaml) inside the master node, the master will go and look at the label of the service and decide on which server the pod should be opened and inform the relevant server about opening the pod. The worker node receiving this information will go and create the requested pod.
* Pod’lar oluştuğunda da servisler otomatik biçimde Docker Hub üzerinden imajlarını çekecek ve çalışmaya başlayacak.
* First, we need to upload the k8s folder to the master node with scp.
  * Create infrastructure/k8s folders in the home directory inside the master node.
> * **Command:** mkdir infrastructure 
> * **Command:** mkdir k8s
* Upload the k8s folder here in the next step
> * **Note:** Do not forget to create monitoring-secrets and central-secrets files. Examples are located inside overlays/dev.
       
> * Command:  scp -r -i "{full path}\cluster-key.key" .\k8s ubuntu@{master public ip}:~/infrastructure/                                                                     
* Afterwards, enter inside the K8s folder in the server's terminal and fire up the prod environment:
> * **Command:** kubectl apply -k overlays/central-hub
* After all these operations, just creating is not enough, it is also necessary to make sure they work successfully.
* To examine all pods live regardless of namespace:
> * **Command:** watch kubectl get pods -A -o wide
