#!/usr/bin/env bash
# Build local Docker images for all 5 microservices (Mode B: Local Kubernetes overlays/dev)
set -euo pipefail

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
SERVICES=(
  "gateway-service"
  "identity-service"
  "order-service"
  "payment-service"
  "restaurant-service"
)

echo ">>> Building local Docker images from ${REPO_ROOT} ..."

for SERVICE in "${SERVICES[@]}"; do
  IMAGE_TAG="berkayozr/${SERVICE}:v1"
  echo "-> Building ${IMAGE_TAG} ..."
  docker build -t "${IMAGE_TAG}" -f "${REPO_ROOT}/${SERVICE}/Dockerfile" "${REPO_ROOT}/${SERVICE}"
done

echo "*** ALL 5 MICROSERVICE IMAGES BUILT SUCCESSFULLY! ***"
