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
