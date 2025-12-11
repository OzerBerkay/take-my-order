FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY common common
COPY infrastructure infrastructure
COPY gateway-service gateway-service
COPY customer-service customer-service
COPY order-service order-service
COPY payment-service payment-service
COPY restaurant-service restaurant-service
# Sadece order service'i derliyoruz
RUN mvn clean package -DskipTests -pl order-service -am

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Derlenen jar dosyasını alıyoruz
COPY --from=builder /app/order-service/order-container/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]