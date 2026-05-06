@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

where docker >nul 2>nul
if errorlevel 1 (
  echo Docker CLI not found in PATH.
  exit /b 1
)

echo Building Java service images from %CD% ...
docker build -f discovery-server/Dockerfile -t server5/discovery-server:local . || exit /b 1
docker build -f config-server/Dockerfile -t server5/config-server:local . || exit /b 1
docker build -f api-gateway/Dockerfile -t server5/api-gateway:local . || exit /b 1
docker build -f dataset-service/Dockerfile -t server5/dataset-service:local . || exit /b 1
docker build -f aimodel-service/Dockerfile -t server5/aimodel-service:local . || exit /b 1
docker build -f training-orchestrator-service/Dockerfile -t server5/training-orchestrator-service:local . || exit /b 1

echo Building ai-training-service...
docker build -f ai-training-service/Dockerfile -t server5/ai-training-service:local ai-training-service || exit /b 1

echo Done. Restart workloads: kubectl rollout restart deployment -n server5
echo Or run k8s\start.bat from server5\k8s after loading images into your cluster.
