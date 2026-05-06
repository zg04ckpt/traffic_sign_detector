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

set SERVICES=ai-training-service training-orchestrator-service aimodel-service dataset-service api-gateway rabbitmq postgres config-server discovery-server

for %%S in (%SERVICES%) do (
    echo [INFO] Scaling deployment/%%S to 0...
    kubectl scale deployment/%%S --replicas=0 -n %NAMESPACE% >nul 2>&1
    echo [INFO] Scaling %%S to 0...
    (kubectl scale deployment/%%S --replicas=0 -n %NAMESPACE% >nul 2>&1) || (kubectl scale sts/%%S --replicas=0 -n %NAMESPACE% >nul 2>&1)
    if errorlevel 1 (
        echo [WARN] Could not scale deployment/%%S. Check if deployment exists.
        echo [WARN] Could not scale %%S. Check if deployment or statefulset exists.
    ) else (
        echo [OK] deployment/%%S scaled to 0.
        echo [OK] %%S scaled to 0.
    )
)

echo.
echo [INFO] Current deployment status:
kubectl get deployment -n %NAMESPACE%

echo.
echo [DONE] Stop sequence completed.
exit /b 0
