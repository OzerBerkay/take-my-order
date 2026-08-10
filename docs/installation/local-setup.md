# Take My Order - Yerel (Local) Kurulum Rehberi (Uçtan Uca)

Bu doküman, projeye yeni katılan bir geliştiricinin bilgisayarında (Windows/Mac/Linux) **Take My Order** projesini sıfırdan, en ufak bir komut satırını dahi atlamadan nasıl ayağa kaldıracağını uçtan uca anlatmaktadır.

## 🏛️ Mimari Yaklaşım: Pragmatik İç Döngü (Inner Loop)
Geliştirici deneyimini (Developer Experience - DX) maksimize etmek için **Pragmatik Hibrit** bir yaklaşım benimsiyoruz:
- **Altyapı (Infrastructure):** Veritabanı, Kafka, Zookeeper, Keycloak ve Schema Registry tamamen izole bir şekilde `docker-compose` ile ayağa kalkar.
- **Mikroservisler (Business Logic):** IntelliJ IDEA (veya Eclipse/VSCode) üzerinden doğrudan çalıştırılır. Böylece ağ krizleri yaşanmaz, tek tıkla Debug (hata ayıklama) yapılabilir.

---

## 🛠️ Ön Koşullar (Prerequisites)
Aşağıdaki araçların bilgisayarınızda kurulu ve yollarının (PATH) tanımlı olduğundan emin olun:
- **Java 21 (JDK 21)**
- **Maven 3.8+**
- **Docker Desktop** (Running state'de olmalı)
- **IntelliJ IDEA** (Ultimate önerilir ama Community de yeterlidir)

---

## 🚀 Adım Adım Kurulum

### Adım 1: Projeyi İndirme (Clone)
Terminalinizi açın ve projeyi yerel bilgisayarınıza indirin:
```bash
git clone <proje-repo-url>
cd take-my-order
```

### Adım 2: Altyapıyı (Infrastructure) Ayağa Kaldırma
Projenin kök dizinindeyken altyapıyı Docker Compose üzerinden başlatın.
```bash
docker-compose up -d
```
Bu komut arka planda şu servisleri başlatacaktır:
- **PostgreSQL 15:** (`localhost:5432`) Ayağa kalkarken `local-env/postgres/init.sql` dosyasını okuyarak gerekli tüm veritabanı şemalarını (`order`, `payment`, `restaurant`, `identity`, `keycloak`) otomatik oluşturur.
- **Zookeeper & Kafka:** (`localhost:31091`) Mesaj kuyruğu sisteminiz.
- **Schema Registry:** (`localhost:30081`) Avro şema yönetimi.
- **Keycloak:** (`localhost:30080`) Kimlik doğrulama sunucusu.
- **Kafka UI:** (`localhost:8082`) Kafka'yı canlı izleyebileceğiniz görsel arayüz.

**Doğrulama:**
```bash
docker ps
```
Komutu ile tüm servislerin `Up` (Çalışıyor) durumunda olduğunu teyit edin. Eğer Kafka UI için `http://localhost:8082` adresine gidebiliyorsanız her şey yolundadır.

### Adım 3: Keycloak Konfigürasyonu
Keycloak ayağa kalktıktan sonra, projenin çalışabilmesi için özel claim'lerin JWT içine basılması gerekir.
- **Senaryo A (Elinizde Yedek Varsa):** Ekip liderinizden aldığınız `realm-export.json` dosyasını `local-env/keycloak/` dizinine kopyaladıysanız, docker-compose bu dosyayı **otomatik** import etmiştir. Bir şey yapmanıza gerek yok.
- **Senaryo B (Sıfırdan Kurulum):** Elinizde yedek yoksa, Keycloak'u manuel yapılandırmanız şarttır. Bunun için [docs/installation/keycloak-setup.md](keycloak-setup.md) dosyasındaki adımları (Realm, Client, User Attribute Mappers) harfiyen uygulayın.

### Adım 4: Projeyi Derleme (Build)
Tüm kodları ve özellikle Avro (Schema Registry) sınıflarını derlemek için projenin kök dizininde şu komutu çalıştırın:
```bash
mvn clean compile
```
> [!TIP]
> Bu komut, `.avro` şemalarınızı okuyup Java class'larına çevirir. Eğer IDE üzerinde "Sınıf bulunamadı" hataları alırsanız sebebi bu komutun çalıştırılmamış olmasıdır.

### Adım 5: Mikroservisleri Çalıştırma (IDE Üzerinden)
Altyapımız hazır! Şimdi IntelliJ IDEA üzerinden servislerimizi sırayla ayağa kaldıracağız.
Projeyi IntelliJ ile açın ve aşağıdaki servislerin `Application.java` sınıflarındaki yeşil ▷ Run butonuna basın. 

*Not: Application.yml dosyaları özel olarak docker-compose portlarına (`31091`, `30080`, `30081` vb.) göre ayarlandığı için hiçbir ENV variable veya konfigürasyon değiştirmenize **gerek yoktur**.*

**Tavsiye Edilen Çalıştırma Sırası:**
1. `identity-service` (Kullanıcı veritabanını ve ilk olayları hazırlar)
2. `restaurant-service` (Restoran verilerini hazırlar)
3. `payment-service` 
4. `order-service` (Sipariş alabilmesi için diğerlerinin hazır olması iyidir)
5. `gateway-service` (Tüm trafiği karşılayacak API Ağ Geçidi - Port: `8184`)

### Adım 6: Test ve Doğrulama
Servisler ayağa kalkarken Flyway sayesinde kendi veri tablolarını otomatik oluşturacaktır.

- **Kafka Testi:** `http://localhost:8082` adresinden Kafka UI'a girin. `Topics` sekmesinde `payment-request`, `restaurant-approval-request` gibi topic'lerin otomatik oluştuğunu göreceksiniz.
- **Gateway Testi:** Postman üzerinden Keycloak'tan (`localhost:30080`) token alın. Token'ı Header'a `Authorization: Bearer <token>` olarak ekleyip `http://localhost:8184/orders` gibi Gateway üzerinden ilgili servislere istek atın.

## ⚠️ Sık Karşılaşılan Sorunlar (Troubleshooting)

**1. Gateway Token Doğrularken "403 Forbidden" Veriyor**
- **Sebep:** Keycloak kurulumunda `User Attribute Mapper` ayarlarını yapmadınız veya `role_ids` / `organizational_unit_ids` alanlarında "Multivalued" butonunu açmayı unuttunuz. [Keycloak Kurulum Rehberine](keycloak-setup.md) geri dönün.

**2. Flyway Tablo Bulamadı Hatası ("relation does not exist")**
- **Sebep:** Postgresql boş olarak kalktı ve `init.sql` çalışmadı.
- **Çözüm:** Docker Desktop'tan Postgres volume'unu silin (`docker volume rm take-my-order_postgres-data`) ve `docker-compose up -d postgres` ile tertemiz baştan kaldırın.

**3. "Could not resolve schema" veya Avro Sınıfı Bulunamadı Hatası**
- **Sebep:** Maven derlemesini yapmadınız.
- **Çözüm:** Kök dizinde `mvn clean compile` çalıştırın ve IntelliJ'de projeye sağ tıklayıp `Maven -> Reload project` yapın.

---
*Tebrikler! Local ortamınız %100 uyumlulukla, bir Baş Mimar standartlarında ayağa kalkmıştır.*
