#!/usr/bin/env pwsh
# Production Deployment Script for Warehouse Management System
# This script automates the entire production setup

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Warehouse Management System - PROD Setup" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Check Docker
Write-Host "[STEP 1] Checking Docker installation..." -ForegroundColor Yellow
$dockerCheck = docker --version 2>&1 | Select-String "Docker"
if (!$dockerCheck) {
    Write-Host "ERROR: Docker is not installed or not in PATH" -ForegroundColor Red
    Write-Host "Please install Docker Desktop from: https://www.docker.com/products/docker-desktop" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host "  ✓ Docker found: $dockerCheck" -ForegroundColor Green

# Step 2: Build Application
Write-Host ""
Write-Host "[STEP 2] Building application..." -ForegroundColor Yellow
Write-Host "This may take 1-3 minutes on first build..." -ForegroundColor Gray
$buildOutput = mvn clean package -DskipTests -q 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Maven build failed" -ForegroundColor Red
    Write-Host $buildOutput -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host "  ✓ Build completed successfully" -ForegroundColor Green

# Step 3: Check JAR file
Write-Host ""
Write-Host "[STEP 3] Verifying JAR creation..." -ForegroundColor Yellow
$jarFile = Get-ChildItem -Path "target" -Filter "*runner.jar" -ErrorAction SilentlyContinue
if (!$jarFile) {
    Write-Host "ERROR: JAR file not found" -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host "  ✓ JAR file created: $($jarFile.Name)" -ForegroundColor Green

# Step 4: Stop existing containers
Write-Host ""
Write-Host "[STEP 4] Cleaning up existing containers..." -ForegroundColor Yellow
docker-compose down 2>&1 | Out-Null
Write-Host "  ✓ Old containers removed" -ForegroundColor Green

# Step 5: Start PostgreSQL
Write-Host ""
Write-Host "[STEP 5] Starting PostgreSQL database..." -ForegroundColor Yellow
docker-compose up -d postgres 2>&1 | Out-Null
Start-Sleep -Seconds 10
$dbCheck = docker exec warehouse-postgres psql -U quarkus_test -d quarkus_test -c "SELECT version();" 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: PostgreSQL failed to start" -ForegroundColor Red
    Write-Host $dbCheck -ForegroundColor Red
    Read-Host "Press Enter to exit"
    exit 1
}
Write-Host "  ✓ PostgreSQL is running and ready" -ForegroundColor Green

# Step 6: Start Application
Write-Host ""
Write-Host "[STEP 6] Starting application..." -ForegroundColor Yellow
docker-compose up -d app 2>&1 | Out-Null
Start-Sleep -Seconds 5

# Step 7: Verify Application
Write-Host ""
Write-Host "[STEP 7] Verifying application..." -ForegroundColor Yellow
$appReady = $false
for ($i = 1; $i -le 10; $i++) {
    $appCheck = curl -s http://localhost:8080/product 2>&1
    if ($appCheck -like "*TONSTAD*") {
        Write-Host "  ✓ Application is responding and healthy" -ForegroundColor Green
        $appReady = $true
        break
    }
    Start-Sleep -Seconds 2
}
if (!$appReady) {
    Write-Host "  ⚠ Application may still be starting, check logs with: docker-compose logs -f app" -ForegroundColor Yellow
}

# Success Output
Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "PRODUCTION DEPLOYMENT COMPLETE" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "🎯 API Endpoints:" -ForegroundColor Green
Write-Host "   Products:    http://localhost:8080/product" -ForegroundColor Cyan
Write-Host "   Warehouses:  http://localhost:8080/warehouses" -ForegroundColor Cyan
Write-Host "   Stores:      http://localhost:8080/stores" -ForegroundColor Cyan
Write-Host "   Fulfillment: http://localhost:8080/fulfillment/warehouses/1" -ForegroundColor Cyan
Write-Host ""
Write-Host "🐳 Docker Commands:" -ForegroundColor Green
Write-Host "   docker-compose logs -f app      (View app logs)" -ForegroundColor Cyan
Write-Host "   docker-compose logs -f postgres (View DB logs)" -ForegroundColor Cyan
Write-Host "   docker ps                       (View containers)" -ForegroundColor Cyan
Write-Host "   docker-compose down             (Stop services)" -ForegroundColor Cyan
Write-Host ""
Write-Host "📊 Database Info:" -ForegroundColor Green
Write-Host "   Host:     localhost" -ForegroundColor Cyan
Write-Host "   Port:     5432" -ForegroundColor Cyan
Write-Host "   Database: quarkus_test" -ForegroundColor Cyan
Write-Host "   User:     quarkus_test" -ForegroundColor Cyan
Write-Host "   Password: quarkus_test" -ForegroundColor Cyan
Write-Host ""
Write-Host "✅ Application is ready for testing!" -ForegroundColor Green
Write-Host ""

# Open browser
$openBrowser = Read-Host "Open browser to API? (Y/n)"
if ($openBrowser -ne 'n') {
    Start-Process "http://localhost:8080/product"
}
