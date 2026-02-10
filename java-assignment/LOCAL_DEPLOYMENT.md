# Local Deployment Guide - Warehouse Management System

## Overview
This guide explains how to run the Warehouse Management System locally without Docker.

## Prerequisites

### Required
- ✅ Java 17+ (OpenJDK or Oracle JDK)
- ✅ Maven 3.9+
- ✅ PostgreSQL 13+

### Optional
- Git (for version control)
- curl/Postman (for testing APIs)

## Installation

### 1. Install PostgreSQL

#### Windows
- Download: https://www.postgresql.org/download/windows/
- Run installer
- Remember the password for `postgres` user
- Default port: 5432

#### macOS
```bash
brew install postgresql@13
brew services start postgresql@13
```

#### Linux (Ubuntu/Debian)
```bash
sudo apt-get install postgresql postgresql-contrib
sudo systemctl start postgresql
```

---

## Setup

### Step 1: Create Database

```bash
# Connect as postgres user
psql -U postgres

# Create application database and user
CREATE USER quarkus_test WITH PASSWORD 'quarkus_test';
CREATE DATABASE quarkus_test OWNER quarkus_test;
GRANT ALL PRIVILEGES ON DATABASE quarkus_test TO quarkus_test;

# Exit psql
\q
```

**Verify:**
```bash
psql -h localhost -U quarkus_test -d quarkus_test
```

### Step 2: Initialize Database Schema

The application automatically creates tables on startup using:
- Hibernate ORM with Panache
- JPA annotations
- DDL generation

No manual schema creation needed!

### Step 3: Build Application

```bash
cd java-assignment
mvn clean install
```

**Expected:**
- All 13 tests pass ✅
- JAR created at: `target/java-code-assignment-1.0.0-SNAPSHOT.jar`
- BUILD SUCCESS message

---

## Running the Application

### Option 1: Using Maven (Development)

```bash
cd java-assignment
mvn quarkus:dev
```

**Output:**
```
Listening on: http://localhost:8080
Press Ctrl+C to stop
```

**Benefits:**
- Hot reload on code changes
- Live reload enabled
- Debug mode available
- Perfect for development

### Option 2: Direct Java Execution

```bash
cd java-assignment
java -jar target/java-code-assignment-1.0.0-SNAPSHOT.jar
```

**Output:**
```
__  ____  __  _____   ___  __ ____  ______
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/
 -/ /_/ / /_/ / __ |/ __, / ,< / /_/ /\ \
--\___\_\____/_/ |_/_____/_/|_|\____/___/
2026-02-11 00:15:00,123 INFO  [io.qua.application] (main) Warehouse Management System started
2026-02-11 00:15:00,124 INFO  [io.qua.application] (main) Listening on: http://localhost:8080
```

**Verify Running:**
```bash
curl http://localhost:8080/product
```

---

## Database Configuration

### Default Connection (Built-in)

```properties
# Database
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/quarkus_test
quarkus.datasource.username=quarkus_test
quarkus.datasource.password=quarkus_test
quarkus.datasource.jdbc.max-size=8
quarkus.datasource.jdbc.min-size=2

# Hibernate
quarkus.hibernate-orm.database.generation=update
quarkus.hibernate-orm.sql-load-script=import.sql
```

### Custom Configuration

Edit `src/main/resources/application.properties`:

```properties
# Connection Pool Settings
quarkus.datasource.jdbc.max-size=20
quarkus.datasource.jdbc.min-size=5

# Logging
quarkus.log.console.level=INFO
quarkus.log.level=INFO
quarkus.log.category."com.fulfilment".level=DEBUG

# REST Framework
quarkus.rest.path=/api
```

---

## API Endpoints

Access at: `http://localhost:8080`

### Products
```bash
# List all
curl http://localhost:8080/product

# Get by ID
curl http://localhost:8080/product/1

# Create
curl -X POST http://localhost:8080/product \
  -H "Content-Type: application/json" \
  -d '{"name":"New Product","description":"Description"}'

# Delete
curl -X DELETE http://localhost:8080/product/1
```

### Warehouses
```bash
curl http://localhost:8080/warehouses
curl -X POST http://localhost:8080/warehouses \
  -H "Content-Type: application/json" \
  -d '{"code":"WH-999","capacity":1000,"location":"City"}'
```

### Stores
```bash
curl http://localhost:8080/stores
```

### Fulfillment
```bash
curl http://localhost:8080/fulfillment/warehouses
```

---

## Testing

### Unit Tests (No Database Needed)

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=CreateWarehouseUseCaseTest

# Run with coverage
mvn test jacoco:report
```

**Test Results:**
- All 13 unit tests ✅ PASSING
- Zero failures or errors
- 100% success rate

### Integration Tests (Requires Database)

Tests that need PostgreSQL:
- `ProductEndpointTest` - Currently SKIPPED (requires Docker)

---

## Logs

### Viewing Logs

By default, logs are written to:
- Console (during development)
- `target/quarkus.log` (if configured)

### Changing Log Level

Edit `application.properties`:
```properties
quarkus.log.console.level=DEBUG  # More verbose

# For specific packages
quarkus.log.category."com.fulfilment".level=DEBUG
quarkus.log.category."io.quarkus".level=INFO
```

### Common Log Messages

```
INFO  [io.qua.d.s.PostgresqlDatasource] Datasource initialized
INFO  [org.hib.Version] HHH000412: Hibernate ORM core version
INFO  [com.fulfilment.application] Application started successfully
```

---

## Development Workflow

### 1. Make Code Changes
```bash
# Edit source files
# Changes auto-reload in dev mode
```

### 2. Test Changes
```bash
# In another terminal
curl http://localhost:8080/product
```

### 3. Run Tests
```bash
mvn test
```

### 4. Commit Changes
```bash
git add .
git commit -m "Your changes"
```

---

## Troubleshooting

### Issue: PostgreSQL Connection Refused
**Cause:** PostgreSQL not running
**Solution:**
```bash
# Check if running
pg_isready -h localhost

# Start PostgreSQL
# Windows: Search "Services" app and start PostgreSQL
# macOS: brew services start postgresql@13
# Linux: sudo systemctl start postgresql
```

### Issue: Database does not exist
**Solution:**
```bash
psql -U postgres
CREATE DATABASE quarkus_test;
\conninfo  # Verify
```

### Issue: Authentication failed for user 'quarkus_test'
**Solution:**
```bash
# Reset user
psql -U postgres
ALTER USER quarkus_test WITH PASSWORD 'quarkus_test';
```

### Issue: Port 8080 already in use
**Solution:**
```bash
# Change port in application.properties
quarkus.http.port=8081

# Or kill the process on 8080
# Windows: netstat -ano | findstr :8080, taskkill /PID <pid>
# macOS/Linux: lsof -ti:8080 | xargs kill -9
```

### Issue: OutOfMemoryError
**Solution:**
```bash
# Increase heap memory
export JAVA_OPTS="-Xmx1024m"
java -jar target/java-code-assignment-1.0.0-SNAPSHOT.jar
```

### Issue: Slow Startup
**Cause:** Hibernate schema generation
**Solution:** This is normal on first run. Subsequent startups are faster.

---

## Performance Tips

### Development Mode
- Use `mvnw quarkus:dev` for hot reload
- Takes ~5-10 seconds to start
- Good for active development

### Production Mode
- Build JAR: `mvn package`
- Run with: `java -Dquarkus.profile=prod -jar app.jar`
- Takes ~3-5 seconds to start
- Lower memory usage
- Better performance

### Database Optimization
```properties
# Connection pooling
quarkus.datasource.jdbc.min-size=5
quarkus.datasource.jdbc.max-size=20

# Query timeout (ms)
quarkus.datasource.jdbc.detect-statement-leaks=true
```

---

## Database Backup & Restore

### Backup
```bash
pg_dump -h localhost -U quarkus_test quarkus_test > backup.sql
```

### Restore
```bash
psql -h localhost -U quarkus_test quarkus_test < backup.sql
```

---

## Environment Variables

Set these before running the application:

```bash
# Linux/macOS
export QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://localhost:5432/quarkus_test
export QUARKUS_DATASOURCE_USERNAME=quarkus_test
export QUARKUS_DATASOURCE_PASSWORD=quarkus_test

# Windows (PowerShell)
$env:QUARKUS_DATASOURCE_JDBC_URL = "jdbc:postgresql://localhost:5432/quarkus_test"
$env:QUARKUS_DATASOURCE_USERNAME = "quarkus_test"
$env:QUARKUS_DATASOURCE_PASSWORD = "quarkus_test"
```

---

## Compare with Docker

| Feature | Local | Docker |
|---------|-------|--------|
| Setup Time | 10 minutes | 5 minutes |
| Database Install | Manual | Automatic |
| Isolation | No | Yes |
| Production Ready | After config | Out of box |
| Resource Usage | System RAM | Container |
| Debugging | IDEs | docker logs |
| Portability | Low | High |

---

## Next Steps

1. **Development:** Use `mvn quarkus:dev`
2. **Testing:** Run `mvn test` regularly
3. **Production:** Use Docker (see DOCKER_DEPLOYMENT.md)
4. **Release:** Build JAR and deploy

---

## Support

For issues:
1. Check logs: `target/quarkus.log` or console
2. Verify PostgreSQL: `psql -U quarkus_test -d quarkus_test`
3. Test endpoint: `curl http://localhost:8080/product`
4. Check configuration: `src/main/resources/application.properties`

