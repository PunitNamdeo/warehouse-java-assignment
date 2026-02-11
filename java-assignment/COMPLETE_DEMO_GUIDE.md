# 🎯 End-to-End Warehouse Management System - Complete Demo Guide

## 📋 System Overview

This is a **Warehouse Colocation Management System** with 4 main entities all interconnected:

```
┌─────────────────────────────────────────────────────────────────┐
│                    SYSTEM ARCHITECTURE                          │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  LOCATIONS (Domain-based)                                      │
│  ✓ ZWOLLE-001, AMSTERDAM-001, TILBURG-001, etc.              │
│                 ↓                                               │
│  WAREHOUSES ←──────→ PRODUCTS ←──────→ STORES                 │
│  (MWH.001, etc)      (Furniture)      (5 retail stores)       │
│         ↓                                    ↓                  │
│         └─ WAREHOUSE-PRODUCT-STORE ASSOCIATIONS ─────┘        │
│            (Fulfillment: Links for distribution)              │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🚀 Setup Instructions

### Prerequisites
```bash
✓ PostgreSQL running (admin / admin123 / mydatabase)
✓ Java 21+ installed
✓ Maven 3.9+ installed
✓ Web browser for testing
```

### Start the Application
```bash
cd C:\Users\c plus\Downloads\fcs-interview-code-assignment-main\java-assignment
mvn quarkus:dev
```

**Expected Output:**
```
__  ____  __  _____   ___  __ ____  ______
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/

Listening on: http://localhost:8080
```

---

## 📊 Complete Data Relationships

### 1️⃣ LOCATIONS (Pre-loaded in System)
These are geographical areas where warehouses can be established:
```
ZWOLLE-001          - Max 1 warehouse, Max 40 capacity
AMSTERDAM-001       - Max 5 warehouses, Max 100 capacity
TILBURG-001         - Max 1 warehouse, Max 40 capacity
ROTTERDAM-001       - Max capacity 1000
UTRECHT-001         - Max capacity 400
```

### 2️⃣ STORES (5 Retail Stores)
```
ID=1  | TONSTAD Store    | Quantity: 50 products
ID=2  | KALLAX Store     | Quantity: 35 products
ID=3  | BESTÅ Store      | Quantity: 25 products
ID=4  | EKTORP Store     | Quantity: 45 products
ID=5  | MALM Store       | Quantity: 60 products
```

### 3️⃣ PRODUCTS (6 Furniture Items)
```
ID=1 | TONSTAD Sofa        | Price: $299.99 | Stock: 50
ID=2 | KALLAX Shelf        | Price: $149.99 | Stock: 75
ID=3 | BESTÅ Cabinet       | Price: $199.99 | Stock: 40
ID=4 | EKTORP Chair        | Price: $129.99 | Stock: 100
ID=5 | MALM Bed Frame      | Price: $349.99 | Stock: 35
ID=6 | LAPPVIKEN Door      | Price: $49.99  | Stock: 200
```

### 4️⃣ WAREHOUSES (5 Distribution Centers)
```
MWH.001 | ZWOLLE-001      | Capacity: 500 | Stock: 150
MWH.012 | AMSTERDAM-001   | Capacity: 800 | Stock: 200
MWH.023 | TILBURG-001     | Capacity: 600 | Stock: 180
MWH.034 | ROTTERDAM-001   | Capacity: 1000| Stock: 250
MWH.045 | ROTTERDAM-001   | Capacity: 400 | Stock: 120
```

### 5️⃣ WAREHOUSE-PRODUCT-STORE ASSOCIATIONS (Fulfillment)
**Constraint Validation:**
- ✓ Each Product can be fulfilled by **max 2 warehouses per store**
- ✓ Each Store can be fulfilled by **max 3 warehouses**
- ✓ Each Warehouse can store **max 5 product types**

**Data Mapping:**
```
Store 1 (TONSTAD):
  ├─ Fulfilled by: MWH.001, MWH.012, MWH.023 (3 warehouses)
  └─ Products: Sofa, Shelf, Chair, Bed Frame (4 products)

Store 2 (KALLAX):
  ├─ Fulfilled by: MWH.012, MWH.034 (2 warehouses)
  └─ Products: Shelf, Chair, Cabinet, Sofa, Door (5 products)

Store 3 (BESTÅ):
  ├─ Fulfilled by: MWH.023, MWH.045 (2 warehouses)
  └─ Products: Cabinet, Bed Frame, Door, Shelf (4 products)

Store 4 (EKTORP):
  ├─ Fulfilled by: MWH.001, MWH.034 (2 warehouses)
  └─ Products: Chair, Door, Bed Frame, Sofa (4 products)

Store 5 (MALM):
  ├─ Fulfilled by: MWH.012, MWH.023, MWH.045 (3 warehouses)
  └─ Products: Bed Frame, Shelf, Sofa, Chair, Door (5 products)
```

---

## 🧪 LIVE DEMONSTRATION SEQUENCE

### **PHASE 1: Review Web UI** (2 minutes)

#### Step 1: Open Web Dashboard
```
Browser: http://localhost:8080
```

Shows:
- ✓ AngularJS-based Product Management Interface
- ✓ Real-time add/edit/delete operations
- ✓ Database persistence demonstrated

---

### **PHASE 2: PRODUCTS API DEMO** (3 minutes)

#### 2.1 List All Products
```bash
curl http://localhost:8080/product
```

**Expected Response:** All 6 products with detailed information
```json
[
  {
    "id": 1,
    "name": "TONSTAD Sofa",
    "description": "Three-seat sofa with modern design",
    "price": 299.99,
    "stock": 50
  },
  ...
]
```

#### 2.2 Get Specific Product
```bash
curl http://localhost:8080/product/1
```

#### 2.3 Create New Product
```bash
curl -X POST http://localhost:8080/product \
  -H "Content-Type: application/json" \
  -d '{
    "name": "MARKUS Desk",
    "description": "Work desk with cable management",
    "price": 179.99,
    "stock": 20
  }'
```

#### 2.4 Update Product
```bash
curl -X PUT http://localhost:8080/product/1 \
  -H "Content-Type: application/json" \
  -d '{
    "name": "TONSTAD Sofa",
    "description": "Premium three-seat sofa - Updated",
    "price": 349.99,
    "stock": 45
  }'
```

#### 2.5 Delete Product
```bash
curl -X DELETE http://localhost:8080/product/1
```

---

### **PHASE 3: STORES API DEMO** (2 minutes)

#### 3.1 List All Stores
```bash
curl http://localhost:8080/store
```

**Shows:** All 5 stores with their capacity information

#### 3.2 Get Store Detail
```bash
curl http://localhost:8080/store/2
```

#### 3.3 Create New Store
```bash
curl -X POST http://localhost:8080/store \
  -H "Content-Type: application/json" \
  -d '{
    "name": "IKEA Downtown",
    "quantityProductsInStock": 120
  }'
```

#### 3.4 Update Store
```bash
curl -X PATCH http://localhost:8080/store/2 \
  -H "Content-Type: application/json" \
  -d '{
    "quantityProductsInStock": 50
  }'
```

---

### **PHASE 4: LOCATIONS API DEMO** (1 minute)

#### 4.1 Resolve Location by ID
```bash
curl http://localhost:8080/location/ZWOLLE-001
```

**Shows:** Location details with constraints
```json
{
  "identification": "ZWOLLE-001",
  "maxNumberOfWarehouses": 1,
  "maxCapacity": 40
}
```

#### 4.2 Try Invalid Location
```bash
curl http://localhost:8080/location/INVALID
```

**Shows:** 404 error handling

---

### **PHASE 5: WAREHOUSES API DEMO** (4 minutes) ⭐ MAIN FEATURE

#### 5.1 List All Warehouses
```bash
curl http://localhost:8080/warehouse
```

**Shows:** All 5 warehouses with business unit codes
```json
[
  {
    "businessUnitCode": "MWH.001",
    "location": "ZWOLLE-001",
    "capacity": 500,
    "stock": 150,
    "createdAt": "2024-07-01T00:00:00",
    "archivedAt": null
  },
  ...
]
```

#### 5.2 Get Warehouse by ID
```bash
curl http://localhost:8080/warehouse/MWH.001
```

#### 5.3 Create New Warehouse (WITH VALIDATIONS)
```bash
curl -X POST http://localhost:8080/warehouse \
  -H "Content-Type: application/json" \
  -d '{
    "businessUnitCode": "MWH.NEW01",
    "location": "AMSTERDAM-001",
    "capacity": 250,
    "stock": 100
  }'
```

**Validations Checked:**
- ✓ Business Unit Code unique?
- ✓ Location exists?
- ✓ Location capacity not exceeded?
- ✓ Stock <= Capacity?
- ✓ Max warehouses not reached for location?

#### 5.4 Replace Warehouse (UNIQUE FEATURE) 🌟
```bash
curl -X PUT http://localhost:8080/warehouse/MWH.001/replacement \
  -H "Content-Type: application/json" \
  -d '{
    "businessUnitCode": "MWH.001",
    "location": "ZWOLLE-001",
    "capacity": 600,
    "stock": 150
  }'
```

**What Happens:**
- ✓ Old warehouse (MWH.001) archived
- ✓ New warehouse created with same businessUnitCode
- ✓ Stock transferred
- ✓ History maintained

#### 5.5 Archive Warehouse
```bash
curl -X DELETE http://localhost:8080/warehouse/MWH.NEW01
```

**Result:** Warehouse marked as archived (not deleted)

---

### **PHASE 6: FULFILLMENT API DEMO** (3 minutes) ⭐ BONUS TASK

#### 6.1 List All Associations
```bash
curl http://localhost:8080/fulfillment/warehouse-product-store
```

**Shows:** All 25 warehouse-product-store associations

#### 6.2 View Associations for Specific Store
```bash
curl http://localhost:8080/fulfillment/warehouse-product-store | jq '.[] | select(.storeId==1)'
```

**Shows:** Only warehouses that fulfill products for Store 1

#### 6.3 Create New Association
```bash
curl -X POST http://localhost:8080/fulfillment/warehouse-product-store \
  -H "Content-Type: application/json" \
  -d '{
    "warehouseBusinessUnitCode": "MWH.001",
    "productId": 3,
    "storeId": 4
  }'
```

**Constraints Validated:**
- ✓ Product not already fulfilled by 2+ warehouses for this store?
- ✓ Store not fulfilled by 3+ warehouses?
- ✓ Warehouse doesn't already store 5+ product types?

#### 6.4 Remove Association
```bash
curl -X DELETE http://localhost:8080/fulfillment/warehouse-product-store/25
```

---

### **PHASE 7: COMPREHENSIVE DATA INTEGRITY CHECK** (2 minutes)

#### 7.1 Get All Products with Full Details
```bash
curl http://localhost:8080/product | jq '.[].name'
```

#### 7.2 Get All Stores and Their Fulfillment
```bash
curl http://localhost:8080/store | jq '.[] | {id, name}'
```

#### 7.3 Get All Warehouses and Their Load
```bash
curl http://localhost:8080/warehouse | jq '.[] | {businessUnitCode, capacity, stock}'
```

#### 7.4 Verify Association Constraints
```bash
# Count warehouses per store (should be ≤ 3)
curl http://localhost:8080/fulfillment/warehouse-product-store | \
  jq 'group_by(.storeId) | map({storeId: .[0].storeId, warehouseCount: map(.warehouseBusinessUnitCode) | unique | length})'

# Count product types per warehouse (should be ≤ 5)
curl http://localhost:8080/fulfillment/warehouse-product-store | \
  jq 'group_by(.warehouseBusinessUnitCode) | map({warehouse: .[0].warehouseBusinessUnitCode, productCount: map(.productId) | unique | length})'
```

---

### **PHASE 8: ERROR HANDLING DEMO** (2 minutes)

#### 8.1 Invalid Product ID
```bash
curl http://localhost:8080/product/99999
```
**Expected:** 404 Not Found

#### 8.2 Invalid Store ID
```bash
curl http://localhost:8080/store/99999
```
**Expected:** 404 Not Found

#### 8.3 Invalid Warehouse
```bash
curl http://localhost:8080/warehouse/INVALID
```
**Expected:** 404 Not Found

#### 8.4 Duplicate Business Unit Code
```bash
curl -X POST http://localhost:8080/warehouse \
  -H "Content-Type: application/json" \
  -d '{
    "businessUnitCode": "MWH.001",
    "location": "AMSTERDAM-001",
    "capacity": 250,
    "stock": 100
  }'
```
**Expected:** 400 Bad Request (Code already exists)

#### 8.5 Invalid Location
```bash
curl -X POST http://localhost:8080/warehouse \
  -H "Content-Type: application/json" \
  -d '{
    "businessUnitCode": "MWH.NEW02",
    "location": "INVALID-LOCATION",
    "capacity": 250,
    "stock": 100
  }'
```
**Expected:** 404 Not Found (Location doesn't exist)

#### 8.6 Capacity Exceeded
```bash
curl -X POST http://localhost:8080/warehouse \
  -H "Content-Type: application/json" \
  -d '{
    "businessUnitCode": "MWH.NEW03",
    "location": "ZWOLLE-001",
    "capacity": 100,
    "stock": 100
  }'
```
**Expected:** 400 Bad Request (Exceeds location capacity)

---

## 📈 Database Schema

```
PRODUCT
├─ id (PK)
├─ name (UNIQUE)
├─ description
├─ price
└─ stock

STORE
├─ id (PK)
├─ name (UNIQUE)
└─ quantityProductsInStock

WAREHOUSE
├─ id (PK)
├─ businessUnitCode (UNIQUE)
├─ location
├─ capacity
├─ stock
├─ createdAt
└─ archivedAt

WAREHOUSE_PRODUCT_STORE (Fulfillment Associations)
├─ id (PK)
├─ warehouseBusinessUnitCode (FK)
├─ productId (FK)
├─ storeId (FK)
├─ createdAt
└─ UNIQUE(warehouseBusinessUnitCode, productId, storeId)
```

---

## ✨ Key Features Demonstrated

| Feature | Demo | API | Status |
|---------|------|-----|--------|
| **Product CRUD** | Phase 2 | `/product` | ✓ Complete |
| **Store Management** | Phase 3 | `/store` | ✓ Complete |
| **Location Resolution** | Phase 4 | `/location/{id}` | ✓ Complete |
| **Warehouse CRUD** | Phase 5.1-2 | `/warehouse` | ✓ Complete |
| **Warehouse Replace** | Phase 5.4 | `PUT /warehouse/{code}/replacement` | ✓ Complete |
| **Warehouse Archive** | Phase 5.5 | `DELETE /warehouse/{id}` | ✓ Complete |
| **Fulfillment Assoc.** | Phase 6 | `/fulfillment/warehouse-product-store` | ✓ Complete |
| **Constraint Validation** | Phase 8 | All endpoints | ✓ Complete |
| **Data Integrity** | Phase 7 | Query endpoints | ✓ Complete |

---

## 🎯 Summary

This demonstration shows:
- ✅ All 4 entities working together
- ✅ Complete REST API with CRUD operations
- ✅ Advanced features (warehouse replacement)
- ✅ Business logic & constraints validation
- ✅ Data persistence to PostgreSQL
- ✅ Error handling & edge cases
- ✅ Fulfillment/association management (BONUS)

**Result:** Fully functional Warehouse Management System ready for production! 🚀
