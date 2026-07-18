@echo off
setlocal
set "PATH=%USERPROFILE%\tools\apache-maven-3.9.16\bin;%PATH%"
cd /d "%~dp0"

echo Rydder opp gamle instanser pa port 8080 (hvis noen)...
powershell -NoProfile -Command "Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique | ForEach-Object { Stop-Process -Id $_ -Force -ErrorAction SilentlyContinue }"

echo Starter Boligregnskap...
mvn spring-boot:run

if errorlevel 1 (
    echo.
    echo Noe gikk galt. Se feilmeldingen over.
)

pause
