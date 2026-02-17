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
cd your-path\fcs-interview-code-assignment-main\java-assignment
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
ZWOLLE-001          - Max 1 warehouse,  Max 40 capacity      (Current: 1 warehouse)
ZWOLLE-002          - Max 2 warehouses, Max 50 capacity      (Current: 0 warehouses)
AMSTERDAM-001       - Max 5 warehouses, Max 100 capacity     (Current: 1 warehouse)
AMSTERDAM-002       - Max 3 warehouses, Max 75 capacity      (Current: 0 warehouses)
TILBURG-001         - Max 1 warehouse,  Max 40 capacity      (Current: 1 warehouse)
HELMOND-001         - Max 1 warehouse,  Max 45 capacity      (Current: 0 warehouses)
EINDHOVEN-001       - Max 2 warehouses, Max 70 capacity      (Current: 0 warehouses)
ROTTERDAM-001       - Max 1 warehouse,  Max 1000 capacity    (Current: 1 warehouse)
ROTTERDAM-002       - Max 3 warehouses, Max 2000 capacity    (Current: 0 warehouses)
VETSBY-001          - Max 1 warehouse,  Max 90 capacity      (Current: 0 warehouses)
UTRECHT-001         - Max 2 warehouses, Max 400 capacity     (Current: 1 warehouse)
```

**Note:** Locations are **not** database entities but validation rules for warehouse deployment. Each warehouse must reference exactly one location.

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
ID=1 | AMST.EU.001    | AMSTERDAM-001  | Capacity: 1000 | Stock: 450
ID=2 | ROTT.EU.002    | ROTTERDAM-001  | Capacity: 1200 | Stock: 520
ID=3 | ZWOLLE.EU.003  | ZWOLLE-001     | Capacity: 800  | Stock: 380
ID=4 | TILB.EU.004    | TILBURG-001    | Capacity: 900  | Stock: 410
ID=5 | UTRE.EU.005    | UTRECHT-001    | Capacity: 750  | Stock: 320
```

### 5️⃣ WAREHOUSE-PRODUCT-STORE ASSOCIATIONS (Fulfillment)
**Constraint Validation:**
- ✓ Each Product can be fulfilled by **max 2 warehouses per store**
- ✓ Each Store can be fulfilled by **max 3 warehouses**
- ✓ Each Warehouse can store **max 5 product types**

**Data Mapping:**
```
Store 1 (TONSTAD):
  ├─ Fulfilled by: AMST.EU.001 (ID:1), ROTT.EU.002 (ID:2), ZWOLLE.EU.003 (ID:3)
  └─ Products: Sofa(1), Shelf(2), Chair(4), Bed Frame(5)

Store 2 (KALLAX):
  ├─ Fulfilled by: ROTT.EU.002 (ID:2), TILB.EU.004 (ID:4)
  └─ Products: Shelf(2), Chair(4), Door(6), Sofa(1), Cabinet(3)

Store 3 (BESTÅ):
  ├─ Fulfilled by: ZWOLLE.EU.003 (ID:3), UTRE.EU.005 (ID:5)
  └─ Products: Cabinet(3), Bed Frame(5), Door(6), Shelf(2)

Store 4 (EKTORP):
  ├─ Fulfilled by: AMST.EU.001 (ID:1), TILB.EU.004 (ID:4)
  └─ Products: Chair(4), Door(6), Bed Frame(5)

Store 5 (MALM):
  ├─ Fulfilled by: ROTT.EU.002 (ID:2), ZWOLLE.EU.003 (ID:3), UTRE.EU.005 (ID:5)
  └─ Products: Bed Frame(5), Shelf(2), Sofa(1), Chair(4), Door(6)
```

---

## 🧪 LIVE DEMONSTRATION SEQUENCE

### **PHASE 1: Review Web UI**

#### Step 1: Open Web Dashboard
```
Browser: http://localhost:8080
```

Shows:
- ✓ AngularJS-based Product Management Interface
- ✓ Real-time add/edit/delete operations
- ✓ Database persistence demonstrated

---

### **PHASE 2: PRODUCTS API DEMO**

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

### **PHASE 3: STORES API DEMO**

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

### **PHASE 5: WAREHOUSES API DEMO** ⭐ MAIN FEATURE

#### 5.1 List All Warehouses
```bash
curl http://localhost:8080/warehouse
```

**Shows:** All active (non-archived) warehouses with database IDs
```json
[
  {
    "id": "1",
    "businessUnitCode": "AMST.EU.001",
    "location": "AMSTERDAM-001",
    "capacity": 1000,
    "stock": 450,
    "archivedAt": null
  },
  {
    "id": "2",
    "businessUnitCode": "ROTT.EU.002",
    "location": "ROTTERDAM-001",
    "capacity": 1200,
    "stock": 520,
    "archivedAt": null
  },
  ...
]
```

**Note:** Archived warehouses (where `archivedAt` is not null) are automatically filtered from this list.

#### 5.2 Get Warehouse by Numeric Database ID
```bash
curl http://localhost:8080/warehouse/1
```

**Note:** Use the numeric `id` field (1, 2, 3, 4, 5) from the list response, NOT the business unit code.

#### 5.3 Create New Warehouse (WITH VALIDATIONS)
```bash
curl -X POST http://localhost:8080/warehouse \
  -H "Content-Type: application/json" \
  -d '{
    "businessUnitCode": "AMST.EU.NEW",
    "location": "AMSTERDAM-001",
    "capacity": 950,
    "stock": 300
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
curl -X POST http://localhost:8080/warehouse/AMST.EU.001/replacement \
  -H "Content-Type: application/json" \
  -d '{
    "location": "AMSTERDAM-001",
    "capacity": 1100,
    "stock": 450
  }'
```

**What Happens:**
- ✓ Old warehouse (AMST.EU.001) archived
- ✓ New warehouse created with same businessUnitCode
- ✓ Stock transferred to new warehouse
- ✓ Old warehouse history maintained for auditing
- ✓ New warehouse gets new numeric ID

#### 5.5 Archive Warehouse
```bash
curl -X DELETE http://localhost:8080/warehouse/1
```

**What Happens:**
- ✓ Warehouse marked as archived (soft-deleted)
- ✓ Cannot be used for new fulfillment associations
- ✓ Historical data retained for auditing
- ✓ Cannot be unarchived (permanent operation)

---

### **PHASE 6: FULFILLMENT API DEMO** ⭐ BONUS TASK

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

### **PHASE 7: COMPREHENSIVE DATA INTEGRITY CHECK**

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

### **PHASE 8: ERROR HANDLING DEMO**

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

#### 8.3 Invalid Warehouse ID (Non-Numeric)
```bash
curl http://localhost:8080/warehouse/INVALID_ID
```
**Expected:** 400 Bad Request (Invalid warehouse ID format. ID must be a valid number.)

#### 8.4 Warehouse Not Found (Valid ID but doesn't exist)
```bash
curl http://localhost:8080/warehouse/999
```
**Expected:** 404 Not Found (Warehouse with ID '999' not found.)

#### 8.5 Duplicate Business Unit Code
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
