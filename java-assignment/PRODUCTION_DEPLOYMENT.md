# Production Deployment Guide - Warehouse Management System

## Prerequisites

### 1. Install Docker Desktop
- Download from: https://www.docker.com/products/docker-desktop
- Install and start Docker Desktop
- Verify: `docker --version`

### 2. Build Status
- Application package build: **IN PROGRESS**
- Expected output: `java-code-assignment-1.0.0-SNAPSHOT-runner.jar`

---

## Quick Start (After Prerequisites)

### Step 1: Navigate to Project
```powershell
cd "c:\Users\c plus\Downloads\fcs-interview-code-assignment-main\java-assignment"
```

### Step 2: Start PostgreSQL Database
```powershell
docker run -d `
  --name warehouse-postgres `
  -e POSTGRES_USER=quarkus_test `
  -e POSTGRES_PASSWORD=quarkus_test `
  -e POSTGRES_DB=quarkus_test `
  -p 5432:5432 `
  postgres:13.3

# Wait for PostgreSQL to start
Start-Sleep -Seconds 10

# Verify it's running
docker ps
```

### Step 3: Run Application
```powershell
# Using Docker Compose (Recommended)
docker-compose up

# Or run JAR directly if not using Docker
java -jar target/java-code-assignment-1.0.0-SNAPSHOT-runner.jar
```

### Step 4: Test Application
```powershell
# List products
curl http://localhost:8080/product

# List warehouses
curl http://localhost:8080/warehouses

# List stores
curl http://localhost:8080/stores
```

---

## Production Architecture

```
┌─────────────────────────────────────────────────────────┐
│                   CLIENT REQUESTS                       │
│                   http://localhost:8080                 │
└──────────────────────┬──────────────────────────────────┘
                       │
        ┌──────────────▼──────────────┐
        │   WAREHOUSE APPLICATION     │
        │   Port: 8080                │
        │   JVM Process               │
        │   (java-code-assignment-    │
        │    1.0.0-SNAPSHOT-runner)   │
        └──────────────┬──────────────┘
                       │
        ┌──────────────▼──────────────┐
        │  PostgreSQL Database        │
        │  Port: 5432 (Internal)      │
        │  Database: quarkus_test     │
        │  User: quarkus_test         │
        └─────────────────────────────┘
```

---

## Configuration Details

### Environment Variables
```properties
# Database Configuration
QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://localhost:5432/quarkus_test?sslmode=disable
QUARKUS_DATASOURCE_USERNAME=quarkus_test
QUARKUS_DATASOURCE_PASSWORD=quarkus_test
QUARKUS_DATASOURCE_JDBC_MAX_SIZE=8
QUARKUS_DATASOURCE_JDBC_MIN_SIZE=2

# Hibernate ORM
QUARKUS_HIBERNATE_ORM_DATABASE_GENERATION=drop-and-create
QUARKUS_HIBERNATE_ORM_LOG_SQL=true
QUARKUS_HIBERNATE_ORM_SQL_LOAD_SCRIPT=import.sql
```

### Database Initialization
- On first startup, Hibernate automatically:
  - Drops and recreates schema
  - Loads sample data from `import.sql`
  - Creates tables for:
    - Products (3 items)
    - Locations (various warehouses)
    - Warehouses
    - Stores
    - Warehouse-Product-Store Associations

---

## API Endpoints Available

### Products
- `GET /product` - List all products
- `DELETE /product/{id}` - Delete product

### Locations
- `GET /location/{id}` - Resolve location

### Stores  
- `GET /stores` - List stores
- `POST /stores` - Create store
- `PATCH /stores/{id}` - Update store

### Warehouses (TASK 3)
- `GET /warehouses` - List all warehouses
- `POST /warehouses` - Create warehouse
- `GET /warehouses/{businessUnitCode}` - Get warehouse
- `DELETE /warehouses/{businessUnitCode}` - Archive warehouse
- `PUT /warehouses/{businessUnitCode}` - Replace warehouse

### Product-Warehouse-Store Association (BONUS)
- `GET /fulfillment/warehouses/{storeId}` - List associations
- `POST /fulfillment/warehouses/{productId}/{storeId}/{warehouseCode}` - Create association
- `DELETE /fulfillment/warehouses/{productId}/{storeId}/{warehouseCode}` - Remove association

---

## Docker Compose Commands

```powershell
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f app
docker-compose logs -f postgres

# Stop services
docker-compose down

# Restart application
docker-compose restart app

# Restart database
docker-compose restart postgres

# View running containers
docker ps

# Access PostgreSQL shell
docker exec -it warehouse-postgres psql -U quarkus_test -d quarkus_test

# View database tables
docker exec warehouse-postgres psql -U quarkus_test -d quarkus_test -c "\dt"
```

---

## Troubleshooting

### Port Already in Use
```powershell
# Check what's using port 8080
netstat -ano | findstr :8080

# Use different port in docker-compose.yml
# Change: - "8080:8080" to "8081:8080"
```

### PostgreSQL Connection Failed
```powershell
# Verify PostgreSQL is running
docker ps

# Check logs
docker logs warehouse-postgres

# Restart database
docker-compose restart postgres
```

### Build Failed
```powershell
# Clean and rebuild
mvn clean
mvn package -DskipTests

# Check for Java version
java -version
# Requires: Java 11 or higher
```

### Application Won't Start
```powershell
# Check application logs
docker-compose logs app

# Verify database is ready
docker exec warehouse-postgres psql -U quarkus_test -d quarkus_test -c "SELECT 1;"

# Check application port is accessible
curl http://localhost:8080/product
```

---

## Performance Tuning

### Database Connection Pool
Currently configured in `application.properties`:
```properties
%prod.quarkus.datasource.jdbc.max-size=8    # Max connections
%prod.quarkus.datasource.jdbc.min-size=2    # Min connections
```

For high load, increase max-size to 16-20.

### PostgreSQL Container Resources
Add to docker-compose.yml:
```yaml
postgres:
  ...
  deploy:
    resources:
      limits:
        cpus: '1'
        memory: 1G
      reservations:
        cpus: '0.5'
        memory: 512M
```

### Application JVM Tuning
```bash
java -Xmx512m -Xms256m -jar app.jar
```

---

## Monitoring

### Check Application Health
```powershell
# Health check endpoint (if available)
curl http://localhost:8080/health

# Simple connectivity test
curl http://localhost:8080/product
```

### Database Monitoring
```powershell
# View active connections
docker exec warehouse-postgres psql -U quarkus_test -d quarkus_test -c "SELECT datname, count(*) FROM pg_stat_activity GROUP BY datname;"

# View table sizes
docker exec warehouse-postgres psql -U quarkus_test -d quarkus_test -c "SELECT schemaname, tablename, pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) FROM pg_tables WHERE schemaname != 'pg_catalog' ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC;"
```

---

## Scaling Considerations

For production with multiple instances:

1. **Database**: Use managed PostgreSQL (AWS RDS, Azure Database, etc.)
2. **Application**: Use Docker + Kubernetes (EKS, AKS)
3. **Load Balancing**: Use reverse proxy (Nginx, HAProxy)
4. **Caching**: Add Redis for session management
5. **Monitoring**: Use Prometheus + Grafana

---

## Next Steps

1. ✅ Install Docker Desktop
2. ✅ Ensure Maven build completes
3. ⏳ Start PostgreSQL container
4. ⏳ Run application
5. ⏳ Test endpoints
6. ⏳ Deploy to production environment

---

## Support

For issues, check:
- Application logs: `docker-compose logs app`
- Database logs: `docker-compose logs postgres`
- Docker status: `docker ps`
- Network connectivity: `docker network ls`

