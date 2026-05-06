@echo off
setlocal enabledelayedexpansion

set "NAMESPACE=server5"
if not "%~1"=="" set "NAMESPACE=%~1"

echo [INFO] Starting services in namespace: %NAMESPACE%

where kubectl >nul 2>&1
if errorlevel 1 (
    echo [ERROR] kubectl not found in PATH.
    exit /b 1
)

kubectl get namespace %NAMESPACE% >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Namespace %NAMESPACE% not found.
    exit /b 1
)

set SERVICES=discovery-server config-server api-gateway postgres rabbitmq dataset-service aimodel-service training-orchestrator-service ai-training-service

for %%S in (%SERVICES%) do (
    echo [INFO] Scaling deployment/%%S to 1...
    kubectl scale deployment/%%S --replicas=1 -n %NAMESPACE% >nul 2>&1
    if errorlevel 1 (
        echo [WARN] Could not scale deployment/%%S. Check if deployment exists.
    ) else (
        echo [OK] deployment/%%S scaled to 1.
    )
)

echo.
echo [INFO] Waiting for key services to become ready...
for %%S in (discovery-server config-server api-gateway postgres rabbitmq dataset-service aimodel-service training-orchestrator-service ai-training-service) do (
    kubectl rollout status deployment/%%S -n %NAMESPACE% --timeout=180s >nul 2>&1
    if errorlevel 1 (
        echo [WARN] deployment/%%S not ready yet ^(or rollout check failed^).
    ) else (
        echo [OK] deployment/%%S is ready.
    )
)

echo.
echo [INFO] Current deployment status:
kubectl get deployment -n %NAMESPACE%

echo.
echo [DONE] Start sequence completed.
exit /b 0
