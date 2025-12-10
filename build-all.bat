@echo off
echo ==============================================
echo MIKROSERVIS DOCKER INSAATI BASLIYOR...
echo ==============================================

echo.
echo [1/5] Gateway Service Insa Ediliyor...
docker build -t berkay/gateway-service:v1 -f gateway-service/Dockerfile .

echo.
echo [2/5] Customer Service Insa Ediliyor...
docker build -t berkay/customer-service:v1 -f customer-service/Dockerfile .

echo.
echo [3/5] Order Service Insa Ediliyor...
docker build -t berkay/order-service:v1 -f order-service/Dockerfile .

echo.
echo [4/5] Payment Service Insa Ediliyor...
docker build -t berkay/payment-service:v1 -f payment-service/Dockerfile .

echo.
echo [5/5] Restaurant Service Insa Ediliyor...
docker build -t berkay/restaurant-service:v1 -f restaurant-service/Dockerfile .

echo.
echo ==============================================
echo TUM IMAJLAR HAZIR! DOCKER IMAGES LISTESI:
echo ==============================================
docker images | findstr "berkay/"
pause