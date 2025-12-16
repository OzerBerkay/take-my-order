# 1. Hafif Java 21 Runtime
FROM eclipse-temurin:21-jre-alpine

# 2. Çalışma dizini
WORKDIR /app

# 3. Hazır JAR dosyasını kopyala
COPY customer-container/target/*.jar app.jar
# 4. Başlat
ENTRYPOINT ["java", "-jar", "app.jar"]