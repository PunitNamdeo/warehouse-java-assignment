# 🏭 Warehouse Management System - Java Code Assignment

A complete **Warehouse Colocation Management System** built with Quarkus, Hibernate ORM Panache, and PostgreSQL. This assignment implements REST APIs for managing warehouses, products, stores, and their fulfillment associations.

## 📋 Quick Start

### Prerequisites
- **Java 21+** (JDK with JAVA_HOME environment variable set)
- **PostgreSQL 13+** running on `localhost:5432`
  - **Credentials**: `admin` / `admin123` / database: `mydatabase`
- **Maven 3.9+**

### Running the Application

**Development Mode** (with live reload):
```bash
cd java-assignment
mvn quarkus:dev
```

**Production Build**:
```bash
mvn clean package
java -jar target/quarkus-app/quarkus-run.jar
```

After startup, access the application at: **http://localhost:8080**

---

## 📚 Documentation Structure

| Document | Purpose |
|----------|---------|
| **[CODE_ASSIGNMENT.md](CODE_ASSIGNMENT.md)** | 📝 Assignment tasks and requirements (3 main tasks: Location, Store, Warehouse) |
| **[QUESTIONS.md](QUESTIONS.md)** | ❓ Architectural questions + Case Study answers with cost control strategies |
| **[COMPLETE_DEMO_GUIDE.md](COMPLETE_DEMO_GUIDE.md)** | 🎯 End-to-end demo with all API endpoints and test scenarios |
| **[ENTITY_RELATIONS_AND_FLOW.md](ENTITY_RELATIONS_AND_FLOW.md)** | 🔗 **INTERVIEW GUIDE**: Entity relationships, system flows, architecture patterns |
| **[../case-study/BRIEFING.md](../case-study/BRIEFING.md)** | 🏢 Domain overview and business context |
| **[../case-study/CASE_STUDY.md](../case-study/CASE_STUDY.md)** | 💼 Cost control scenarios with detailed answers |

---

## 🎯 System Overview

### Entities
- **Location** (Domain-only): 8 predefined geographical regions for warehouse deployment
- **Warehouse**: Distribution centers with capacity constraints and location validation
- **Product**: Inventory items (6 furniture products in demo)
- **Store**: Retail locations (5 stores in demo)
- **Warehouse-Product-Store**: Fulfillment associations with business rule constraints

### Key Features
✅ Full CRUD operations on all entities  
✅ Location resolution with constraint validation  
✅ Warehouse replacement (archive old, create new with same business unit code)  
✅ Fulfillment association management with constraints  
✅ Real-time database schema updates in dev mode  
✅ Comprehensive error handling and validation  

---

## 🚀 API Endpoints (All Working)

### Location API
```
GET /location/{id}                    → Resolve location details
```

### Product API
```
GET    /product                       → List all products
GET    /product/{id}                  → Get product details
POST   /product                       → Create product
PUT    /product/{id}                  → Update product
DELETE /product/{id}                  → Delete product
```

### Store API
```
GET    /store                         → List all stores
GET    /store/{id}                    → Get store details
POST   /store                         → Create store
PATCH  /store/{id}                    → Update store
```

### Warehouse API
```
GET    /warehouse                     → List all warehouses
GET    /warehouse/{code}              → Get warehouse details
POST   /warehouse                     → Create warehouse (with validations)
PUT    /warehouse/{code}/replacement  → Replace warehouse
DELETE /warehouse/{code}              → Archive warehouse
```

### Fulfillment API
```
GET    /fulfillment/warehouse-product-store                          → List all associations
GET    /fulfillment/warehouse-product-store/product/{id}/store/{id}  → Get warehouses for product-store
GET    /fulfillment/warehouse-product-store/store/{id}               → Get warehouses for store
GET    /fulfillment/warehouse-product-store/warehouse/{code}         → Get products for warehouse
POST   /fulfillment/warehouse-product-store                          → Create association
DELETE /fulfillment/warehouse-product-store/{id}                     → Remove association
```

---

## 🧪 Demo & Testing

### Run Full Demo Sequence
Follow the **[COMPLETE_DEMO_GUIDE.md](COMPLETE_DEMO_GUIDE.md)** for step-by-step API demonstrations including:
- Web UI interaction
- All CRUD operations
- Constraint validations
- Error handling scenarios
- Data integrity verification

**Est. time: 20-25 minutes**

### Run Unit Tests
```bash
mvn test
```

---

## 📦 Project Structure

```
java-assignment/
├── src/main/java/com/fulfilment/application/monolith/
│   ├── location/           → Location gateway (domain-only service)
│   ├── products/           → Product entity & REST endpoint
│   ├── stores/             → Store entity & REST endpoint + legacy sync
│   ├── warehouses/         → Warehouse entity, use cases, REST endpoint
│   └── fulfillment/        → Fulfillment associations (warehouse-product-store)
├── src/main/resources/
│   ├── application.properties     → Database & Quarkus config
│   └── import.sql                 → Demo data (25 fulfillment associations)
├── src/test/java/               → Unit tests
├── CODE_ASSIGNMENT.md           → Tasks to complete
├── QUESTIONS.md                 → Discussion questions & answers
└── COMPLETE_DEMO_GUIDE.md       → Full API demo guide
```

---

## 🔧 Implementation Details

### Architecture Pattern
- **REST Controller** → **Use Case** → **Repository** → **JPA Entity**
- **Separation of Concerns**: Domain models separate from database entities
- **Validation**: Business rules enforced in Use Cases
- **Legacy Integration**: Store changes synced after database commit (Transactional)

### Database Configuration
```properties
quarkus.datasource.db-kind=postgresql
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/mydatabase
quarkus.datasource.username=admin
quarkus.datasource.password=admin123
quarkus.hibernate-orm.database.generation=update
```

### Key Technologies
- **Framework**: Quarkus 3.13.3
- **Database**: PostgreSQL with Hibernate ORM Panache
- **REST**: JAX-RS with @Path, @GET, @POST, etc.
- **JSON**: Jackson ObjectMapper
- **Build**: Maven with Maven Compiler Plugin

---

## ✨ Task Completion Status

| Task | Status | Notes |
|------|--------|-------|
| 1. Location Gateway - `resolveByIdentifier()` | ✅ Complete | Returns 8 predefined locations |
| 2. Store - Legacy System Sync | ✅ Complete | LegacyStoreManagerGateway called post-commit |
| 3. Warehouse CRUD | ✅ Complete | Create, Read, Replace, Archive with validation |
| 3a. Business Unit Code Validation | ✅ Complete | Ensures uniqueness |
| 3b. Location Validation | ✅ Complete | Validates location exists |
| 3c. Creation Feasibility | ✅ Complete | Checks max warehouses per location |
| 4. Fulfillment Associations | ✅ Complete | CRUD with constraint enforcement |
| Q&A Questions | ✅ Complete | Thoughtful architectural answers provided |

---

## 🐛 Troubleshooting

**Issue**: Database connection errors
- **Solution**: Ensure PostgreSQL is running on `localhost:5432` with credentials `admin/admin123/mydatabase`

**Issue**: Endpoints returning 404
- **Solution**: The application should be running on `http://localhost:8080`. Use `GET /product` to verify the app is alive.

**Issue**: Schema out of sync
- **Solution**: Application automatically creates/updates schema via Hibernate on startup

---

## 📖 Based On

This project is based on [Quarkus Quickstarts](https://github.com/quarkusio/quarkus-quickstarts) with significant enhancements for warehouse management scenario.

---

## 📝 Next Steps

1. ✅ Start the application with `mvn quarkus:dev`
2. ✅ Open [COMPLETE_DEMO_GUIDE.md](COMPLETE_DEMO_GUIDE.md) for interactive API testing
3. ✅ Review [CODE_ASSIGNMENT.md](CODE_ASSIGNMENT.md) for implementation details
4. ✅ Discuss [QUESTIONS.md](QUESTIONS.md) architectural decisions

When you're done iterating in developer mode, you can run the application as a conventional jar file.

First compile it:

```sh
./mvnw package
```

Next we need to make sure you have a PostgreSQL instance running (Quarkus automatically starts one for dev and test mode). To set up a PostgreSQL database with Docker:

```sh
docker run -it --rm=true --name quarkus_test -e POSTGRES_USER=quarkus_test -e POSTGRES_PASSWORD=quarkus_test -e POSTGRES_DB=quarkus_test -p 15432:5432 postgres:13.3
```

Connection properties for the Agroal datasource are defined in the standard Quarkus configuration file,
`src/main/resources/application.properties`.

Then run it:

```sh
java -jar ./target/quarkus-app/quarkus-run.jar
```
    Have a look at how fast it boots.
    Or measure total native memory consumption...


## See the demo in your browser

Navigate to:

<http://localhost:8080/index.html>

Have fun, and join the team of contributors!

## Troubleshooting

Using **IntelliJ**, in case the generated code is not recognized and you have compilation failures, you may need to add `target/.../jaxrs` folder as "generated sources".