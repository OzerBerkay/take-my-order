# Restart only the 5 application microservices in Local Kubernetes without touching platform pods (Kafka, Keycloak, Redis)
$ErrorActionPreference = "Stop"

Write-Host ">>> Triggering rolling restart for all 5 microservices in namespace take-my-order..." -ForegroundColor Cyan
kubectl rollout restart deployment gateway-service identity-service order-service payment-service restaurant-service -n take-my-order
if ($LASTEXITCODE -ne 0) {
    throw "kubectl rollout restart failed (exit code: $LASTEXITCODE)"
}

Write-Host ">>> Restart initiated. Watch status with: kubectl get pods -n take-my-order -w" -ForegroundColor Green
