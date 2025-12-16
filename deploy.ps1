# Servis Listesi (Dizi olarak tanımladık)
$servisler = @("gateway-service", "customer-service", "order-service", "payment-service", "restaurant-service")

# ADIM 1: Java kodlarını derle
Write-Host "1. MAVEN CALISIYOR..." -ForegroundColor Cyan
mvn clean package -DskipTests

# Hata varsa dur (Maven patlarsa devam etme)
if ($LASTEXITCODE -ne 0) {
    Write-Host "Maven Hatasi! Durduruluyor." -ForegroundColor Red
    exit
}

# ADIM 2: Döngü
foreach ($servis in $servisler) {
    Write-Host ">> $servis isleniyor..." -ForegroundColor Yellow

    # Klasöre gir
    cd $servis

    # --- KRİTİK DÜZELTME BURADA ---
    # Değişkeni "$($servis)" şeklinde yazarak PowerShell'e bunu zorla okuttuk.
    $imageName = "berkayozr/$($servis):v1"

    # Docker Build
    docker build --no-cache -t $imageName .

    # Docker Push
    docker push $imageName

    # Ana klasöre dön
    cd ..

    # Podu sil
    kubectl delete pod -n take-my-order -l app=$servis
}

Write-Host "BITTI! Podlari izlemek icin: kubectl get pods -n take-my-order -w" -ForegroundColor Green