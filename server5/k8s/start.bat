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

REM Order: infra first, then Postgres (StatefulSet), RabbitMQ, then app Deployments.
set INFRA=discovery-server config-server

for %%S in (%INFRA%) do (
    echo [INFO] Scaling deployment/%%S to 1...
    kubectl scale deployment/%%S --replicas=1 -n %NAMESPACE% >nul 2>&1
    if errorlevel 1 (
        echo [WARN] Could not scale deployment/%%S.
    ) else (
        echo [OK] deployment/%%S scaled to 1.
    )
)

echo [INFO] Scaling statefulset/postgres to 1...
kubectl scale statefulset/postgres --replicas=1 -n %NAMESPACE% >nul 2>&1
if errorlevel 1 (
    echo [WARN] Could not scale statefulset/postgres.
) else (
    echo [OK] statefulset/postgres scaled to 1.
)

echo [INFO] Waiting for Postgres pod...
kubectl rollout status statefulset/postgres -n %NAMESPACE% --timeout=180s >nul 2>&1
if errorlevel 1 (
    echo [WARN] postgres StatefulSet not ready within timeout. Dataset/aimodel may fail until DB is up.
) else (
    echo [OK] postgres is ready.
)

set SERVICES=rabbitmq api-gateway dataset-service aimodel-service training-orchestrator-service ai-training-service

for %%S in (%SERVICES%) do (
    echo [INFO] Scaling deployment/%%S to 1...
    kubectl scale deployment/%%S --replicas=1 -n %NAMESPACE% >nul 2>&1
    if errorlevel 1 (
        echo [WARN] Could not scale deployment/%%S.
    ) else (
        echo [OK] deployment/%%S scaled to 1.
    )
)

echo.
echo [INFO] Waiting for deployments to become ready...
for %%S in (discovery-server config-server rabbitmq api-gateway dataset-service aimodel-service training-orchestrator-service ai-training-service) do (
    kubectl rollout status deployment/%%S -n %NAMESPACE% --timeout=180s >nul 2>&1
    if errorlevel 1 (
        echo [WARN] deployment/%%S not ready yet ^(or rollout check failed^).
    ) else (
        echo [OK] deployment/%%S is ready.
    )
)

echo.
echo [INFO] Current workload status:
kubectl get deployment,statefulset,pods -n %NAMESPACE%

echo.
echo [DONE] Start sequence completed.
exit /b 0
