# Sadece bizim mikroservisleri yenile, altyapıya (Kafka, DB) dokunma
Write-Host "🔄 Uygulamalar Guncelleniyor..." -ForegroundColor Cyan
kubectl rollout restart deployment gateway-service customer-service order-service payment-service restaurant-service -n take-my-order
Write-Host "✅ Islem baslatildi. İzlemek icin: kubectl get pods -n take-my-order -w" -ForegroundColor Green