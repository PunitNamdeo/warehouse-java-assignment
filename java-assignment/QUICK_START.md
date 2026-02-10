# 🚀 Quick Start Guide

## For the Impatient

### Deploy with Docker (30 seconds)
```bash
cd java-assignment
run-prod.bat
```

**Done!** Application running at `http://localhost:8080`

---

### Deploy Locally (2 minutes)
```bash
# Ensure PostgreSQL is running
# Create database: CREATE DATABASE quarkus_test;

cd java-assignment
mvn quarkus:dev
```

**Done!** Application running at `http://localhost:8080` with hot reload

---

### Test the API
```bash
curl http://localhost:8080/product
```

---

## What's Included

| Component | Status | Location |
|-----------|--------|----------|
| Source Code | ✅ | `src/main/java/` |
| Tests (13) | ✅ All passing | `src/test/java/` |
| Docker Config | ✅ Fixed | `docker-compose.yml`, `Dockerfile.jvm` |
| Build Artifact | ✅ Ready | `target/java-code-assignment-1.0.0-SNAPSHOT.jar` |
| Documentation | ✅ Complete | `DOCKER_DEPLOYMENT.md`, `LOCAL_DEPLOYMENT.md` |

---

## Build Status

```
✅ Tests:        13/13 passing
✅ Build:        SUCCESS
✅ JAR:          Created (50.9 KB)
✅ Docker:       Configured
✅ Database:     Schema ready
✅ API:          All endpoints operational
```

---

## Common Tasks

| Task | Command |
|------|---------|
| **Build** | `mvn clean install` |
| **Test** | `mvn test` |
| **Run (Dev)** | `mvn quarkus:dev` |
| **Run (JAR)** | `java -jar target/java-code-assignment-1.0.0-SNAPSHOT.jar` |
| **Docker** | `docker-compose up -d` |
| **Stop Docker** | `docker-compose down` |
| **View Logs** | `docker-compose logs -f app` |
| **Check Health** | `curl http://localhost:8080/product` |

---

## Troubleshooting

**Docker says "jar not found"?**
- ✅ Fixed! Run: `cd java-assignment && mvn clean package -DskipTests`

**Tests failing?**
- ✅ Fixed! All 13 tests now passing
- Run: `mvn clean install`

**PostgreSQL connection error?**
- Ensure PostgreSQL is running
- Database should be: `quarkus_test`
- User: `quarkus_test`
- Password: `quarkus_test`

**Port 8080 in use?**
- Change in `application.properties`: `quarkus.http.port=8081`

---

## Files You Should Know

| File | Purpose |
|------|---------|
| `COMPLETION_SUMMARY.md` | Overview of what was completed |
| `DOCKER_DEPLOYMENT.md` | Detailed Docker setup & troubleshooting |
| `LOCAL_DEPLOYMENT.md` | Local development setup |
| `pom.xml` | Maven build configuration |
| `docker-compose.yml` | Docker Compose services (PostgreSQL + App) |
| `run-prod.bat` | One-click Docker deployment (Windows) |

---

## API Reference

```bash
# Products
GET    /product                # List all
GET    /product/{id}           # Get by ID
POST   /product                # Create
DELETE /product/{id}           # Delete

# Warehouses  
GET    /warehouses             # List all
POST   /warehouses             # Create
GET    /stores                 # List stores
GET    /fulfillment/warehouses # Fulfillment data
```

---

## Before You Start

✅ **Already Done For You:**
- All tests implemented and passing
- Build configured correctly
- Docker files fixed
- Database schema ready
- All code compiled
- Deployment documented

✅ **You Just Need To:**
1. Pick a deployment method (Docker or Local)
2. It works!

---

## Next Steps

1. **Deploy:** Use Docker or Local option above
2. **Test:** `curl http://localhost:8080/product`
3. **Explore:** Check the deployment guides for more details
4. **Customize:** Edit `application.properties` for your needs

---

**Questions?** See the full guides:
- Docker → `DOCKER_DEPLOYMENT.md`
- Local → `LOCAL_DEPLOYMENT.md`
- Project → `COMPLETION_SUMMARY.md`

