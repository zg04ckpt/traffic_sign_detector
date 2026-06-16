@echo off
set NAMESPACE=server5
if not "%~1"=="" set "NAMESPACE=%~1"
for %%f in ("%~dp0deployments\*.yaml") do kubectl delete -f "%%~f" --ignore-not-found -n %NAMESPACE%
for %%f in ("%~dp0services\*.yaml") do kubectl delete -f "%%~f" --ignore-not-found -n %NAMESPACE%
for %%f in ("%~dp0storage\*.yaml") do kubectl delete -f "%%~f" --ignore-not-found -n %NAMESPACE%
for %%f in ("%~dp0config\*.yaml") do kubectl delete -f "%%~f" --ignore-not-found -n %NAMESPACE%
