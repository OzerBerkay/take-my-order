# Build local Docker images for all 5 microservices (Mode B: Local Kubernetes overlays/dev)
$ErrorActionPreference = "Stop"

$RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$Services = @(
    "gateway-service",
    "identity-service",
    "order-service",
    "payment-service",
    "restaurant-service"
)

Write-Host ">>> Building local Docker images from $RepoRoot ..." -ForegroundColor Green

foreach ($Service in $Services) {
    $ServiceDir = Join-Path $RepoRoot $Service
    $Dockerfile = Join-Path $ServiceDir "Dockerfile"
    $ImageTag = "berkayozr/${Service}:v1"

    Write-Host "-> Building $ImageTag ..." -ForegroundColor Cyan
    docker build -t $ImageTag -f $Dockerfile $ServiceDir
    if ($LASTEXITCODE -ne 0) {
        throw "Docker build failed for $Service (exit code: $LASTEXITCODE)"
    }
}

Write-Host "*** ALL 5 MICROSERVICE IMAGES BUILT SUCCESSFULLY! ***" -ForegroundColor Green
