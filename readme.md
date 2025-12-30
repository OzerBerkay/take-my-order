# PRODUCTION ORTAMININ KURULUMU (FULL FREE)

## 1. Hybrid Multi-Architecture Kubernetes Cluster Kurulumu (Infrastructure Provisioning)

Herhangi bir kod dağıtımı yapmadan önce Pod envanterini ve buna bağlı olarak sunucu ihtiyaçlarını belirlemek gerekmektedir. Buna göre;

### 1.1. Pod Envanteri ve Kaynak Dağılımı

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

> **NOT:** Tamamıyla ücretsiz bir production ortamı hazırlandığından, production-grade seviyesinde genellikle Database per Service mantığı işlese de, hem projenin çapının büyük olmaması hem de yeterli sunucu sayısı ve gücüne sahip olunmadığından tek bir DB üzerinde **Schema per Service** mantığı ile veritabanı kurgulanmıştır.

### 1.2. Sunucu Kaynakları (Oracle & AWS Free Tier)

Bu proje için gerekli olan sunucuları ücretsiz biçimde Oracle Always Free ve AWS Free Tier ile elde edebilmekteyiz.

* **Oracle Always Free:**
  * 24 GB Ram, 4 OCPU, 200 GB Storage hakkı, Architecture (Ampere ARM / aarch64)
  * En güzel yanı, tüm bu hakkı tek bir sunucuda veya istersek maksimum 4 farklı sunucuya bölerek kullanabiliriz.
  * Biz bu noktada 3 farklı sunucuda kullanacağız. 12 GB Ram 2 OCPU 60 GB Storage / 2x (6 GB Ram, 1 OCPU, 50 GB Storage) biçiminde.
* **AWS Free Tier:**
  * 1 GB ram, 1 vCPU, 30 GB Storage hakkı, Architecture (Amd64 / x86_64)

### 1.3. Pod Envanterinin Sunuculara Dağılımı

* **Master Node:**
  * Monitoring & Infrastructure başlığı altında yer alan tüm servisler burada kurulu ve çalışıyor bulunacaktır.
  * Aynı zamanda Kubernetes Cluster'ın yöneticisi de bu sunucuda yer alacaktır. Tüm cluster bu sunucudan yönetilecektir.
  * En büyük iş yükünü burası sırtlayacağından ötürü de sunucu kaynaklarındaki pastanın en büyüğünü yani 12 GB Ram 2 OCPU'luk Oracle Sunucusunu burası alacaktır.
* **Worker Node 1-2:**
  * 2 adet 6 GB Ram, 1 OCPU, 50 GB Storage'dan oluşan worker suncuulardır.
  * Bu iki sunucu elimizdeki 5 mikroservisten 4'ünü (Payment, Restaurant, Order, Customer), aralarında belirli bir dağılım stratejisi ile paylaşarak üzerlerinde çalıştıracaktır.
  * Bu sunucular'da yer alan Pod'lar, Master Node tarafından yönetilecektir.
* **Gateway Node:**
  * Bu mikroservis diğer mikroservislerden bağımsız bir ortamda kendi başına çalışacaktır.
  * Bunun için zayıf olan AWS sunucusu seçilmiştir ve iş görecektir.

### 1.4. Oracle Sunucularının Oluşturma Konfigürasyonları

**A) VCN (Virtual Cloud Network) Kurulumu:**

Oracle üzerindeki 3 sunucu ortak bir network ile birbirlerine bağlı olacak. Böylece veri akış hızı çok yüksek gecikmeler çok düşük olacak. Bunun için Virtual Cloud Network ayarlamalarının yapılması gerekmekte.

1. Menüden Networking > Virtual Cloud Networks > Create VCN
2. **Name:** K8S-VCN
3. Diğer seçenekler default kalsın
4. Sonrasında K8S-VCN içerisine git Security > Default Security List for K8S-VCN > Security Rules > Add Ingress Rules
5. Aşağıdaki tabloda yer alan kuralları gir:

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
| 10.0.0.0/16 | All Protocols | - | - | Cluster Internal Traffic (Kubernetes sunucuları arası tam iletişim) |
| {aws public ip}/32 | TCP | Empty (ALL) | 6443 | API Server Erişimi (gateway) |
| {aws public ip}/32 | TCP | Empty (ALL) | 10250 | Kubelet Metrics (aws) |
| {aws public ip}/32 | UDP | Empty (ALL) | 8472 | Flannel VXLAN (Tunnel - gateway) |