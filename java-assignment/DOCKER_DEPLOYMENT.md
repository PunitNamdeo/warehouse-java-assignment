# Docker Deployment Guide - Warehouse Management System

## Overview
This guide explains how to deploy the Warehouse Management System using Docker and Docker Compose.

## Prerequisites
- ✅ Docker Desktop installed and running
- ✅ Docker Compose installed (included with Docker Desktop)
- ✅ Maven 3.9+ installed
- ✅ Java 17+ installed

## Quick Start (Automated)

### Option 1: Use Batch File (Windows)
```batch
cd java-assignment
run-prod.bat
```

This script will:
1. ✅ Check Docker installation
2. ✅ Build the application (Maven package)
3. ✅ Verify JAR creation
4. ✅ Stop existing containers
5. ✅ Start PostgreSQL database
6. ✅ Start the Java application
7. ✅ Verify all systems are operational

### Result: Application running at http://localhost:8080

---

## Manual Deployment (Step-by-Step)

### Step 1: Build the Application
```bash
cd java-assignment
mvn clean package -DskipTests
```

**Expected Output:**
- JAR created at: `target/java-code-assignment-1.0.0-SNAPSHOT.jar`
- Build success message  ✅

### Step 2: Stop Any Running Containers
```bash
docker-compose down
```

### Step 3: Start PostgreSQL Database
```bash
docker-compose up -d postgres
```

**Verify:**
```bash
docker compose logs postgres
```
Should show: "database system is ready to accept connections"

**Wait 10-15 seconds for database to fully initialize**

### Step 4: Start Application
```bash
docker-compose up -d app
```

### Step 5: Verify Application is Running
```bash
docker ps
```

**Expected Output:**
```
CONTAINER ID   IMAGE              STATUS           PORTS
xxxxx          openjdk:17-slim    Up X seconds     8080->8080
xxxxx          postgres:13.3      Up X seconds     5432->5432
```

### Step 6: Test Application
```bash
curl http://localhost:8080/product
```

Should return: `[{"id":1,"name":"TONSTAD",...}]`

---

## API Endpoints

Once running, access these endpoints:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `GET /product` | GET | List all products |
| `POST /product` | POST | Create new product |
| `GET /product/{id}` | GET | Get product by ID |
| `DELETE /product/{id}` | DELETE | Delete product |
| `GET /warehouses` | GET | List all warehouses |
| `POST /warehouses` | POST | Create warehouse |
| `GET /stores` | GET | List all stores |
| `GET /fulfillment/warehouses` | GET | List fulfillment data |

---

## Database Connection

### Local Development
- **Host:** localhost
- **Port:** 5432
- **Database:** quarkus_test
- **User:** quarkus_test
- **Password:** quarkus_test

**Connect with psql:**
```bash
psql -h localhost -U quarkus_test -d quarkus_test
```

---

## Docker Commands Reference

### View Logs
```bash
# Application logs
docker-compose logs -f app

# Database logs
docker-compose logs -f postgres

# Both services
docker-compose logs -f
```

### Stop Services
```bash
docker-compose down
```

### Remove Everything (including data)
```bash
docker-compose down -v
```

### Restart Services
```bash
docker-compose restart
```

### View Container Status
```bash
docker ps -a
```

---

## Troubleshooting

### Issue: JAR not found error
**Solution:** Ensure JAR is built before starting containers:
```bash
mvn clean package -DskipTests
# Verify file exists at: target/java-code-assignment-1.0.0-SNAPSHOT.jar
```

### Issue: Port 8080 already in use
**Solution:** Change port in docker-compose.yml:
```yaml
ports:
  - "8081:8080"  # Use 8081 instead
```

Then access: `http://localhost:8081`

### Issue: Port 5432 already in use
**Solution:** Change port in docker-compose.yml:
```yaml
ports:
  - "5433:5432"  # Use 5433 instead
```

### Issue: Application can't connect to database
**Solution:** 
1. Verify PostgreSQL is running: `docker ps`
2. Check PostgreSQL logs: `docker-compose logs postgres`
3. Wait longer for database initialization (15-20 seconds)
4. Restart services: `docker-compose down && docker-compose up -d`

### Issue: Application crashes on startup
**Solution:**
1. Check application logs: `docker-compose logs app`
2. Common causes:
   - Database not ready yet
   - Port 8080 already in use
   - Out of memory
3. Increase Docker memory allocation in Docker Desktop settings

---

## Performance Notes

- **First startup:** 30-40 seconds (database initialization)
- **Subsequent startups:** 10-15 seconds
- **Memory usage:** ~500MB (PostgreSQL) + ~300MB (Java app)
- **Disk usage:** ~200MB

---

## Production Considerations

For production deployment, consider:

1. **Environment Variables:**
   ```bash
   docker-compose -e QUARKUS_DATASOURCE_PASSWORD=<secure_password> up -d
   ```

2. **Resource Limits:**
   Add to docker-compose.yml:
   ```yaml
   resources:
     limits:
       cpus: '1'
       memory: 1G
     reservations:
       cpus: '0.5'
       memory: 512M
   ```

3. **Volume Persistence:**
   Database data persists in Docker volume (postgres_data)
   
4. **Backup:**
   ```bash
   docker exec warehouse-postgres pg_dump -U quarkus_test quarkus_test > backup.sql
   ```

5. **Health Checks:**
   Monitor with: `docker stats`

---

## Helpful Links

- Docker Documentation: https://docs.docker.com/
- Docker Compose: https://docs.docker.com/compose/
- Quarkus Guide: https://quarkus.io/guides/
- PostgreSQL: https://www.postgresql.org/docs/

---

## Summary

✅ **Application is Docker-ready!**

The warehouse management system is configured to run in Docker with:
- PostgreSQL database for data persistence
- OpenJDK 17 slim image for minimal footprint
- Auto-health checks for database readiness
- Production-ready configuration

**To deploy now:**
```bash
cd java-assignment
run-prod.bat  # Windows
# OR
./run-prod.sh # (create this for Linux/Mac)
```

**Access:** http://localhost:8080

---

## Support

If you encounter issues:
1. Check Docker status: `docker ps -a`
2. View logs: `docker-compose logs`
3. Verify JAR: `ls -la target/java-code-assignment*SNAPSHOT.jar`
4. Ensure Docker daemon is running

