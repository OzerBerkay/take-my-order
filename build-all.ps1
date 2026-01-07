# Creating images of all microservices for local development
# Stop on first error
$ErrorActionPreference = "Stop"

Write-Host ">>> Starting Docker Builds..." -ForegroundColor Green

# 1. Gateway Service
Write-Host "-> Building Gateway Service..." -ForegroundColor Cyan
docker build -t berkayozr/gateway-service:v1 -f gateway-service/Dockerfile gateway-service

# 2. Customer Service
Write-Host "-> Building Customer Service..." -ForegroundColor Cyan
docker build -t berkayozr/customer-service:v1 -f customer-service/Dockerfile customer-service

# 3. Order Service
Write-Host "-> Building Order Service..." -ForegroundColor Cyan
docker build -t berkayozr/order-service:v1 -f order-service/Dockerfile order-service

# 4. Payment Service
Write-Host "-> Building Payment Service..." -ForegroundColor Cyan
docker build -t berkayozr/payment-service:v1 -f payment-service/Dockerfile payment-service

# 5. Restaurant Service
Write-Host "-> Building Restaurant Service..." -ForegroundColor Cyan
docker build -t berkayozr/restaurant-service:v1 -f restaurant-service/Dockerfile restaurant-service

Write-Host "*** ALL BUILDS COMPLETED SUCCESSFULLY! ***" -ForegroundColor Green