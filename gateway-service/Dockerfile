# ----------------------------
# AŞAMA 1: MUTFAK (BUILDER)
# ----------------------------
# Maven yüklü, Java 21 destekli bir imaj kullanıyoruz.
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

# Çalışma klasörünü belirliyoruz.
WORKDIR /app

# Projenin kök dizinindeki POM dosyasını (Parent POM) kopyalıyoruz.
# (Not: Multi-module proje olduğu için ana pom'a ihtiyacımız var)
COPY pom.xml .

# Tüm modüllerin kaynak kodlarını kopyalıyoruz.
# (Common, Infrastructure, Gateway vs. hepsi lazım çünkü birbirine bağlılar)
COPY common common
COPY infrastructure infrastructure
COPY gateway-service gateway-service
COPY customer-service customer-service
COPY order-service order-service
COPY payment-service payment-service
COPY restaurant-service restaurant-service

# Şimdi yemeği pişiriyoruz! (Testleri atlayarak .jar dosyasını üretiyoruz)
# Sadece gateway-service'i ve bağımlılıklarını derle diyoruz (-pl)
RUN mvn clean package -DskipTests -pl gateway-service -am

# ----------------------------------------
#  AŞAMA 2: SERVİS TABAĞI (RUNTIME)
# ----------------------------------------
# Sadece Java çalıştırmak için gereken hafif (Alpine) imajı seçiyoruz.
FROM eclipse-temurin:21-jre-alpine

# Güvenlik için uygulama klasörü oluşturuyoruz.
WORKDIR /app

# İlk aşamadan (builder), sadece pişmiş yemeği (.jar) alıyoruz.
# Maven, kaynak kodlar vs. çöpe gidiyor.
COPY --from=builder /app/gateway-service/target/*.jar app.jar

#  SECRET MANAGEMENT:
# Uygulama çalışırken Environment Variable'ları okuyacak.
# Biz buraya varsayılan bir değer yazmıyoruz, boş bırakıyoruz.
# Docker run sırasında dışarıdan verilecek.

# Uygulamayı başlatıyoruz.
ENTRYPOINT ["java", "-jar", "app.jar"]