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

# Sadece Customer Service'i derliyoruz (DİKKAT: multimodule proje olduğunda /****-container ekledik)
RUN mvn clean package -DskipTests -pl customer-service/customer-container -am

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Derlenen jar dosyasını alıyoruz
COPY --from=builder /app/customer-service/customer-container/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]