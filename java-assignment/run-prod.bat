@echo off
REM Production Deployment Script for Warehouse Management System
REM This script automates the entire production setup

setlocal enabledelayedexpansion

echo.
echo ========================================
echo Warehouse Management System - PROD Setup
echo ========================================
echo.

REM Step 1: Check Docker
echo [STEP 1] Checking Docker installation...
docker --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker is not installed or not in PATH
    echo Please install Docker Desktop from: https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)
echo   ✓ Docker found: %docker_version%

REM Step 2: Build Application
echo.
echo [STEP 2] Building application...
call mvn clean package -DskipTests -q
if errorlevel 1 (
    echo ERROR: Maven build failed
    pause
    exit /b 1
)
echo   ✓ Build completed successfully

REM Step 3: Check JAR file
echo.
echo [STEP 3] Verifying JAR creation...
if not exist "target\java-code-assignment-1.0.0-SNAPSHOT-runner.jar" (
    echo ERROR: JAR file not found
    pause
    exit /b 1
)
echo   ✓ JAR file created

REM Step 4: Stop existing containers
echo.
echo [STEP 4] Cleaning up existing containers...
docker-compose down >nul 2>&1
echo   ✓ Old containers removed

REM Step 5: Start PostgreSQL
echo.
echo [STEP 5] Starting PostgreSQL database...
docker-compose up -d postgres
timeout /t 10 /nobreak
docker exec warehouse-postgres psql -U quarkus_test -d quarkus_test -c "SELECT version();" >nul 2>&1
if errorlevel 1 (
    echo ERROR: PostgreSQL failed to start
    pause
    exit /b 1
)
echo   ✓ PostgreSQL is running

REM Step 6: Start Application
echo.
echo [STEP 6] Starting application...
docker-compose up -d app
timeout /t 5 /nobreak

REM Step 7: Verify Application
echo.
echo [STEP 7] Verifying application...
for /L %%i in (1,1,10) do (
    curl -s http://localhost:8080/product >nul 2>&1
    if errorlevel 0 (
        echo   ✓ Application is responding
        goto :success
    )
    timeout /t 2 /nobreak
)
echo WARNING: Application may still be starting
goto :startup_complete

:success
echo   ✓ All systems operational

:startup_complete
echo.
echo ========================================
echo PRODUCTION DEPLOYMENT COMPLETE
echo ========================================
echo.
echo API Endpoints:
echo   Products:    http://localhost:8080/product
echo   Warehouses:  http://localhost:8080/warehouses
echo   Stores:      http://localhost:8080/stores
echo   Fulfillment: http://localhost:8080/fulfillment/warehouses
echo.
echo Docker Commands:
echo   docker-compose logs -f app      (View app logs)
echo   docker-compose logs -f postgres (View DB logs)
echo   docker-compose down             (Stop services)
echo.
echo Database Info:
echo   Host:     localhost
echo   Port:     5432
echo   Database: quarkus_test
echo   User:     quarkus_test
echo   Password: quarkus_test
echo.
pause
