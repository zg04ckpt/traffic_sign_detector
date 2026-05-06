@echo off
setlocal enabledelayedexpansion

set "NAMESPACE=server5"
if not "%~1"=="" set "NAMESPACE=%~1"

echo [INFO] Stopping services in namespace: %NAMESPACE%

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

set SERVICES=ai-training-service training-orchestrator-service aimodel-service dataset-service api-gateway rabbitmq config-server discovery-server

for %%S in (%SERVICES%) do (
    echo [INFO] Scaling deployment/%%S to 0...
    kubectl scale deployment/%%S --replicas=0 -n %NAMESPACE% >nul 2>&1
    if errorlevel 1 (
        echo [WARN] Could not scale deployment/%%S.
    ) else (
        echo [OK] deployment/%%S scaled to 0.
    )
)

echo [INFO] Scaling statefulset/postgres to 0...
kubectl scale statefulset/postgres --replicas=0 -n %NAMESPACE% >nul 2>&1
if errorlevel 1 (
    echo [WARN] Could not scale statefulset/postgres.
) else (
    echo [OK] statefulset/postgres scaled to 0.
)

echo.
echo [INFO] Current workload status:
kubectl get deployment,statefulset -n %NAMESPACE%

echo.
echo [DONE] Stop sequence completed.
exit /b 0
