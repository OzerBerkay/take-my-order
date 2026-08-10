# Keycloak Sıfırdan Kurulum ve Konfigürasyon Rehberi

Bu belge, **Take My Order** projesinin hem `Local` hem de `Production` ortamlarında çalışabilmesi için Keycloak kimlik sağlayıcısının (Identity Provider) sıfırdan nasıl yapılandırılacağını adım adım anlatmaktadır.

Gateway üzerindeki `JwtSecurityFilter` sınıfımız, Keycloak'tan dönen JWT token'ın içinde özel claim'ler (`user_type`, `internal_id`, `account_status`, vb.) beklemektedir. Bu rehber, bu entegrasyonu sağlamak içindir.

## 0. Keycloak Konteynerinin / Podunun Kurulması

Keycloak arayüzüne girip ayar yapabilmek için öncelikle onu sisteminizde (Local veya Prod) ayağa kaldırmalısınız.

### 0.1 Local (Yerel) Kurulum (Docker Compose)
Kök dizindeki `docker-compose.yml` dosyası içerisine şu bloğun eklendiğinden emin olun (Bizim projemizde zaten eklenmiştir):

```yaml
  keycloak:
    image: quay.io/keycloak/keycloak:24.0.1
    environment:
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres:5432/takemyorder?currentSchema=keycloak
      KC_DB_USERNAME: postgres
      KC_DB_PASSWORD: admin
      KC_BOOTSTRAP_ADMIN_USERNAME: admin
      KC_BOOTSTRAP_ADMIN_PASSWORD: admin
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
    command: start-dev --import-realm
    ports:
      - "30080:8080"
    depends_on:
      - postgres
    volumes:
      - ./local-env/keycloak:/opt/keycloak/data/import
```
Terminalden `docker-compose up -d keycloak` (veya tüm altyapı için `docker-compose up -d`) diyerek çalıştırın.

### 0.2 Production (Canlı) Kurulum (Kubernetes)
Production K8s (Örn: Oracle Cloud veya AWS) ortamında `infrastructure/k8s/base/services/keycloak.yaml` (veya benzeri bir isimle) şu K8s manifestini kullanabilirsiniz:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: keycloak-service
  namespace: take-my-order
spec:
  replicas: 1
  selector:
    matchLabels:
      app: keycloak
  template:
    metadata:
      labels:
        app: keycloak
    spec:
      containers:
        - name: keycloak-service
          image: quay.io/keycloak/keycloak:24.0.1
          args: ["start-dev"]
          env:
            - name: KEYCLOAK_ADMIN
              valueFrom:
                secretKeyRef:
                  name: keycloak-secret
                  key: admin-username
            - name: KEYCLOAK_ADMIN_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: keycloak-secret
                  key: admin-password
            - name: KC_DB
              value: "postgres"
            - name: KC_DB_URL
              value: "jdbc:postgresql://postgres-service:5432/takemyorder?currentSchema=keycloak"
            - name: KC_DB_USERNAME
              valueFrom:
                secretKeyRef:
                  name: postgres-secret
                  key: db-username
            - name: KC_DB_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: postgres-secret
                  key: db-password
          ports:
            - containerPort: 8080
              name: http
---
apiVersion: v1
kind: Service
metadata:
  name: keycloak-service
  namespace: take-my-order
spec:
  selector:
    app: keycloak
  ports:
    - protocol: TCP
      port: 8080
      targetPort: 8080
```
`kubectl apply -f keycloak.yaml` diyerek canlıya alabilirsiniz.

Her iki ortamdan birinde Keycloak'u ayağa kaldırdıktan sonra aşağıdaki yapılandırma adımlarına geçebilirsiniz.

## 1. Realm Oluşturulması
Keycloak admin paneline giriş yapın (`http://localhost:8080` veya Prod adresiniz).
Kullanıcı adı: `admin`, Şifre: `admin` (veya belirlediğiniz şifre).
1. Sol üst köşedeki **Master** yazısına tıklayın ve **Create Realm** butonuna basın.
2. **Realm Name:** `take-my-order`
3. **Create** butonuna basarak Realm'i oluşturun.

## 2. Client (İstemci) Oluşturulması
Mikroservislerimizin Keycloak ile iletişim kurabilmesi için bir istemciye ihtiyacımız var.
1. Sol menüden **Clients** sekmesine gidin ve **Create client** butonuna tıklayın.
2. **Client ID:** `take-my-order-client` (Tüm platform için tek client) Name: Take My Order Client sonrasında Next de.
3. **Capability config (Kritik Ayarlar):** 
   - **Client authentication:** `On` (Bunu mutlaka AÇ. Bize bir "Secret" lazım).
   - **Authorization:** `Off` (Kapalı kalsın).
   - **Standard Flow:** `On` (İşaretli olsun).
   - **Direct Access Grants:** `On` (Bunu mutlaka AÇ. Postman'den kullanıcı adı/şifre ile test yapmak için lazım).
   - **Next** -> **Save** de.

4. Client oluştuğunda hemen üst sekmelerden **Credentials** sekmesine tıkla ve orada yazan **Client Secret** değerini kopyala (Test için lazım olacak).

## 3. JWT Token Mappers (Özel Claim'lerin Eklenmesi)
Uygulamamızın çalışabilmesi için Keycloak'un ürettiği JWT Token içinde `internal_id`, `role_ids` gibi özel (custom) değerlerin bulunması şarttır. Bu işlemi "Mappers" ile yapıyoruz.

1. Sol menüden **Clients** sekmesine gidin ve oluşturduğunuz `take-my-order-client` içine tıklayın.
2. Üst sekmelerden **Client scopes** sekmesine tıklayın.
3. Listede yer alan ve sonu `-dedicated` ile biten linke tıklayın (Örn: `take-my-order-client-dedicated`). Bu, sadece bu Client'a özel bir scope'tur.
4. Açılan ekranda **Add mapper** butonuna tıklayıp **By configuration** seçeneğini seçin.
5. Çıkan listeden **User Attribute** seçeneğine tıklayın.

Aşağıdaki mapper'ları tek tek oluşturun:

| Name | User Attribute | Token Claim Name | Claim JSON Type | Multivalued |
| :--- | :--- | :--- | :--- | :--- |
| `internal_id` | `internal_id` | `internal_id` | `String` | OFF |
| `user_type` | `user_type` | `user_type` | `String` | OFF |
| `account_status` | `account_status` | `account_status` | `String` | OFF |
| `role_ids` | `role_ids` | `role_ids` | `String` | **ON** |
| `organizational_unit_ids` | `organizational_unit_ids` | `organizational_unit_ids` | `String` | **ON** |

> [!NOTE]
> **Keycloak 24+ Arayüz İpuçları:**
> - **User Attribute Alanı:** Dropdown menüde `username`, `email` gibi seçenekler görürsünüz. Özel (custom) bir değer girmek için doğrudan metin kutusuna `internal_id` yazın. Menünün kaybolması (veya Custom Attribute seçildiğinde boşalması) normaldir; siz yazdığınız metni bırakın, Keycloak onu kabul edecektir.
> - **Diğer Toggle Butonları:** Tabloda belirtilmeyen diğer tüm "On/Off" seçenekleri varsayılan değerlerinde bırakılmalıdır. Yani: `Add to ID token`, `Add to access token`, `Add to userinfo`, `Add to token introspection` özellikleri **ON** kalmalıdır. Sadece `Add to lightweight access token` ve `Aggregate attribute values` özellikleri **OFF** kalmalıdır.

> [!WARNING]
> `role_ids` ve `organizational_unit_ids` alanlarında **Multivalued** (Çoklu Değer) seçeneğini kesinlikle **ON** yapmalısınız. Aksi takdirde Gateway'deki Security Filter bu listeleri UUID Array olarak okuyamaz ve sistem HTTP 500 veya 403 hatası verir.

## 4. Test Kullanıcısı Oluşturulması
Sisteminizi test edebilmek için Admin veya Merchant rolünde bir kullanıcı ekleyin. (Normalde bunu `identity-service` Rest API üzerinden otomatik yapar).
1. Sol menüden **Users** sekmesine gidin -> **Add user**.
2. **Username:** (Kullanıcının maili veya id'si).
### 4.1 Özel Niteliklerin (Attributes) Eklenmesi
> [!NOTE]
> **Keycloak 24+ Önemli Not (Declarative User Profile):** Yeni sürümlerde doğrudan kullanıcıya rastgele Attribute eklenemez. Önce bu alanları Realm'e tanıtmalısınız!
> 1. Sol menüden **Realm settings** sekmesine gidin.
> 2. Üstteki sekmelerden **User profile** sekmesine tıklayın.
> 3. **Attributes** tablosunda sağ üstteki **Create attribute** butonuna basarak `internal_id`, `user_type`, `account_status`, `role_ids` ve `organizational_unit_ids` isimli Attribute'ları tek tek ekleyin.
>    - `internal_id`, `user_type`, `account_status` için sadece **Attribute Name** yazıp **Save** demeniz yeterlidir (Diğer tüm ayarlar varsayılan kalabilir).
>    - `role_ids` ve `organizational_unit_ids` için **Attribute Name** yazdıktan sonra **Multivalued** ayarını mutlaka **ON** yapıp öyle **Save** deyin (Çünkü bunlar liste tipindedir).

Bu tanımlamaları yaptıktan sonra tekrar kullanıcınızın **Details** ekranına döndüğünüzde (sayfanın alt kısmında veya **Attributes** sekmesinde) bu alanların belirdiğini göreceksiniz. Buralara şu değerleri verin:
- `internal_id` : `123e4567-e89b-12d3-a456-426614174000`
- `user_type` : `SUPER_ADMIN` (veya `MERCHANT`, `CUSTOMER`)
- `account_status` : `ACTIVE`

> [!TIP]
> **Mimari Bilgi: Neden bu alanları iki farklı yere (Mappers ve User Profile) ekledik?**
> - **User Profile (Realm Settings):** Bu adım "Veri Saklama (Schema)" adımıdır. Keycloak'a, sistemimizdeki kullanıcıların standart veriler dışında `internal_id` gibi özel verilere sahip olduğunu öğrettik. Bunu yapmasaydık Keycloak bu verileri kaydetmemize izin vermezdi.
> - **Mappers (Client Scopes):** Bu adım "Veri Taşıma (Token Claims)" adımıdır. Keycloak'ta bir kullanıcının `internal_id` gibi değerlere sahip olması, bu değerlerin üretilecek olan JWT Token'ın içine otomatik olarak konulacağı anlamına gelmez! `identity-service` mikroservisi kullanıcı adına (login aşamasında) bir token talep ettiğinde, Mappers sayesinde Keycloak veritabanındaki bu değeri alır ve Token'ın içine (payload) enjekte eder (Map eder). Eğer bu adım eksik olursa Token boş gelir ve Gateway gelen token'da bu bilgileri bulamayacağı için yetkilendirmeyi yapamaz (HTTP 403 / 500).

## 5. Realm'in Dışa Aktarılması (Export) ve Local'e Alınması
Tüm bu ayarları tamamladığınızda, aynı işlemi her kurulumda (Özellikle Local ortamda konteyner silindiğinde) tekrar yapmamak için yedeğini alın.
1. Keycloak sol menüsünde **Realm Settings**'e gidin.
2. Sağ üstteki **Action** menüsünden **Partial Export** butonuna tıklayın (Tüm opsiyonları seçili bırakın, Users vs dahil).
3. İndirilen `realm-export.json` dosyasını alın ve ana projenizdeki `take-my-order/local-env/keycloak/` klasörünün içine yerleştirin.
4. Artık Docker Compose (Local) her çalıştığında Keycloak bu klasöre bakacaktır. **Ancak dikkat:** Keycloak bu dosyayı sadece veritabanında bu isimde bir Realm **yoksa** (yani ilk kurulumda veya veritabanı tamamen silindiğinde) import eder. Eğer Realm zaten varsa, mevcut verilerinizi bozmamak için import işlemini atlar (Skip eder).
