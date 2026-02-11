
# Entity Relations & System Flow - Interview Explanation Guide

This document explains the relationships between entities in the Warehouse Management System and how the data flow works. Use this to explain your implementation to an interviewer.

---

## 🏗️ System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────┐
│                 WAREHOUSE MANAGEMENT SYSTEM                     │
├─────────────────────────────────────────────────────────────────┤
│                                                                 │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │         REST API LAYER (JAX-RS Controllers)              │  │
│  ├──────────────────────────────────────────────────────────┤  │
│  │ ProductResource   │ StoreResource   │ WarehouseResource  │  │
│  │ LocationResource  │ FulfillmentResource                  │  │
│  └──────────────────────────────────────────────────────────┘  │
│           ↓                ↓                ↓                    │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │         USE CASE LAYER (Business Logic)                 │  │
│  ├──────────────────────────────────────────────────────────┤  │
│  │ CreateWarehouseUseCase      CreateProductUseCase        │  │
│  │ ReplaceWarehouseUseCase     CreateStoreUseCase          │  │
│  │ ArchiveWarehouseUseCase     AssociateWarehouseUC        │  │
│  └──────────────────────────────────────────────────────────┘  │
│           ↓                ↓                ↓                    │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │      DOMAIN & GATEWAY LAYER (Business Rules)             │  │
│  ├──────────────────────────────────────────────────────────┤  │
│  │ LocationGateway (8 pre-defined locations)               │  │
│  │ LegacyStoreManagerGateway (integration point)           │  │
│  │ Domain Models & Validations                             │  │
│  └──────────────────────────────────────────────────────────┘  │
│           ↓                ↓                ↓                    │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │      PERSISTENCE LAYER (Repositories)                    │  │
│  ├──────────────────────────────────────────────────────────┤  │
│  │ ProductRepository    │ StoreRepository                   │  │
│  │ WarehouseRepository  │ WarehouseProductStoreRepository  │  │
│  └──────────────────────────────────────────────────────────┘  │
│           ↓                ↓                ↓                    │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │           DATABASE (PostgreSQL JPA Entities)             │  │
│  ├──────────────────────────────────────────────────────────┤  │
│  │ PRODUCT                                                  │  │
│  │ STORE                                                    │  │
│  │ WAREHOUSE                                                │  │
│  │ WAREHOUSE_PRODUCT_STORE (Fulfillment Associations)      │  │
│  └──────────────────────────────────────────────────────────┘  │
│                                                                 │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📊 Core Entity Relationships

### 1️⃣ **LOCATION** (Domain-Only Entity)

**Type**: Reference Data (NOT Database Table)  
**Purpose**: Geographic regions where warehouses can be established  
**Implementation**: Static list defined in `LocationGateway` class

```
LocationGateway.resolveByIdentifier("ZWOLLE-001")
├─ Returns Location object with:
│  ├─ identification: "ZWOLLE-001"
│  ├─ maxNumberOfWarehouses: 1 (location capacity constraint)
│  └─ maxCapacity: 40 (total units this location can hold)
└─ Predefined 8 Locations:
   ├─ ZWOLLE-001 (strict: 1 warehouse max, 40 capacity)
   ├─ AMSTERDAM-001 (flexible: 5 warehouses max, 100 capacity)
   ├─ TILBURG-001, ROTTERDAM-001, UTRECHT-001, HELMOND-001, EINDHOVEN-001, VETSBY-001
```

**Key Characteristic**: Locations are **NOT persisted to database**. They're validation rules for warehouse deployment.

**Relationship to Other Entities**:
```
Location → Warehouse (1:M)
├─ When creating warehouse, LocationGateway validates:
│  ├─ Location exists (resolveByIdentifier returns non-null)
│  ├─ Haven't exceeded max warehouses at this location
│  └─ Haven't exceeded total capacity at this location
└─ Query: How many warehouses already at AMSTERDAM-001?
   Answer: Look at Warehouse table filtered by location = "AMSTERDAM-001"
```

---

### 2️⃣ **PRODUCT** (Database Entity)

**Type**: JPA Panache Entity  
**Table**: `PRODUCT`  
**Purpose**: Inventory items that need fulfillment

```
PRODUCT
├─ id (Primary Key)
├─ name (Unique, e.g., "TONSTAD Sofa")
├─ description
├─ price ($299.99)
└─ stock (50 units)

Example Products:
├─ ID=1 | TONSTAD Sofa | $299.99 | Stock: 50
├─ ID=2 | KALLAX Shelf | $149.99 | Stock: 75
├─ ID=3 | BESTÅ Cabinet | $199.99 | Stock: 40
├─ ID=4 | EKTORP Chair | $129.99 | Stock: 100
├─ ID=5 | MALM Bed Frame | $349.99 | Stock: 35
└─ ID=6 | LAPPVIKEN Door | $49.99 | Stock: 200
```

**Key Characteristics**:
- Simple CRUD (Create, Read, Update, Delete)
- Independent entity (no strong dependencies)
- Stock quantity indicates supply level

**Relationship to Other Entities**:
```
Product → Warehouse-Product-Store Associations (1:M)
├─ One product can be fulfilled by multiple warehouses to multiple stores
├─ Query: Which warehouses fulfill Product ID=1 (TONSTAD Sofa)?
│  Answer: Select from WAREHOUSE_PRODUCT_STORE where productId=1
└─ Constraint: One product can be fulfilled by max 2 warehouses per store
   (enforced in AssociateWarehouseUseCase)
```

---

### 3️⃣ **STORE** (Database Entity)

**Type**: JPA Panache Entity  
**Table**: `STORE`  
**Purpose**: Retail locations where products are sold

```
STORE
├─ id (Primary Key)
├─ name (Unique, e.g., "TONSTAD Store")
└─ quantityProductsInStock (50 units total in store)

Example Stores:
├─ ID=1 | TONSTAD Store | Qty: 50
├─ ID=2 | KALLAX Store | Qty: 35
├─ ID=3 | BESTÅ Store | Qty: 25
├─ ID=4 | EKTORP Store | Qty: 45
└─ ID=5 | MALM Store | Qty: 60
```

**Key Characteristics**:
- Tracks total products in stock at store (simple sum)
- Integrated with `LegacyStoreManagerGateway` for downstream system sync
- **IMPORTANT**: Changes are synced to legacy system AFTER database commit (transactional consistency)

**Relationship to Other Entities**:
```
Store → Warehouse-Product-Store Associations (1:M)
├─ One store receives fulfillment from multiple warehouses
├─ Query: Which warehouses fulfill Store ID=1 (TONSTAD)?
│  Answer: Select distinct warehouseBusinessUnitCode from 
│          WAREHOUSE_PRODUCT_STORE where storeId=1
└─ Constraint: Each store fulfilled by max 3 warehouses
   (enforced in AssociateWarehouseUseCase)

Store → Legacy System Gateway
├─ When store is created/updated: LegacyStoreManagerGateway is called
├─ Timing: ONLY after transaction commits (transactional safety)
└─ Purpose: Keep external system synchronized with authoritative system
```

---

### 4️⃣ **WAREHOUSE** (Database Entity)

**Type**: JPA Entity with Complex Use Cases  
**Table**: `WAREHOUSE`  
**Purpose**: Distribution centers that fulfill products to stores

```
WAREHOUSE
├─ id (Primary Key, auto-generated)
├─ businessUnitCode (Unique, e.g., "MWH.001")
│  └─ KEY INSIGHT: This code is REUSED when warehouse is replaced
├─ location (String reference to location ID, e.g., "ZWOLLE-001")
├─ capacity (500-1000 units, warehouse size)
├─ stock (Current inventory level)
├─ createdAt (Timestamp)
└─ archivedAt (Null=active, Timestamp=archived warehouse)

Example Warehouses:
├─ MWH.001 (createdAt: 2024-01-01, archivedAt: null) → Location: ZWOLLE-001, Capacity: 500
├─ MWH.012 (createdAt: 2024-02-01, archivedAt: null) → Location: AMSTERDAM-001, Capacity: 800
├─ MWH.023 (createdAt: 2024-03-01, archivedAt: null) → Location: TILBURG-001, Capacity: 600
├─ MWH.034 (createdAt: 2024-04-01, archivedAt: null) → Location: ROTTERDAM-001, Capacity: 1000
└─ MWH.045 (createdAt: 2024-05-01, archivedAt: null) → Location: ROTTERDAM-001, Capacity: 400
```

**Warehouse States**:
```
LIFECYCLE:
1. ACTIVE: Operational warehouse, can fulfill orders
2. ARCHIVED: Retired warehouse (replacement occurred)
   ├─ archivedAt timestamp recorded
   ├─ Cost history preserved
   └─ No new associations can be added, but historical data remains queryable
```

**Key Characteristics**:
1. **Business Unit Code Reuse**: When old warehouse is archived, new warehouse can reuse the same code
   - Example: Old MWH.001 (archived 2025-01-01) → New MWH.001 (created 2025-01-02)
   - This preserves business continuity while maintaining historical separation

2. **Location Constraints**: Creation subject to location validation
   - ZWOLLE-001 allows max 1 warehouse: Only 1 active warehouse can exist with location=ZWOLLE-001
   - AMSTERDAM-001 allows max 5 warehouses: At most 5 concurrent warehouses at location
   - New warehouse cannot be created if it would violate location max warehouses constraint

3. **Capacity Tracking**: Stock ≤ Capacity always enforced
   - If warehouse capacity = 500, stock cannot exceed 500
   - Used by fulfillment logic to determine if warehouse can accept new inventory

**Relationship to Other Entities**:
```
Warehouse → Location (Validation)
├─ At CREATE: LocationGateway.resolveByIdentifier() validates location exists
├─ At CREATE: Check current warehouse count at location doesn't exceed max
└─ Query: "Can I create another warehouse at ZWOLLE-001?"
   Answer: SELECT COUNT(*) FROM WAREHOUSE 
           WHERE location='ZWOLLE-001' AND archivedAt IS NULL
           Result: 0 (OK), 1 (NOT OK - max already reached)

Warehouse → Warehouse-Product-Store Associations (1:M)
├─ One warehouse fulfills products to many stores
├─ Query: "Which products does MWH.001 fulfill?"
│  Answer: SELECT DISTINCT productId FROM WAREHOUSE_PRODUCT_STORE 
│          WHERE warehouseBusinessUnitCode='MWH.001'
└─ Constraint: One warehouse stores max 5 product types
   (enforced in AssociateWarehouseUseCase)

Warehouse → Warehouse Replacement (1:1 Sequential)
├─ When replaced:
│  ├─ Old warehouse: UPDATE archivedAt = NOW()
│  └─ New warehouse: INSERT with same businessUnitCode, new location data
├─ Cost tracking: Old warehouse cost history preserved via archive
└─ Fulfillment redirection: Optional - new warehouse can inherit old associations
```

---

### 5️⃣ **WAREHOUSE-PRODUCT-STORE** (Fulfillment Association Entity)

**Type**: JPA Entity - First-Class Fulfillment Model  
**Table**: `WAREHOUSE_PRODUCT_STORE`  
**Purpose**: explicit representation of "which warehouse fulfills which product to which store"

```
WAREHOUSE_PRODUCT_STORE
├─ id (Primary Key)
├─ productId (Foreign Key → Product)
│  └─ Example: 1 (TONSTAD Sofa)
├─ storeId (Foreign Key → Store)
│  └─ Example: 1 (TONSTAD Store)
├─ warehouseBusinessUnitCode (Foreign Key → Warehouse)
│  └─ Example: "MWH.001"
├─ createdAt (Timestamp)
└─ UNIQUE Constraint: (productId, storeId, warehouseBusinessUnitCode)
   └─ Only ONE association per warehouse-product-store combination

Example Fulfillment Network (25 associations in demo):
├─ ASSOCIATION #1: Product=TONSTAD Sofa, Store=TONSTAD, Warehouse=MWH.001
├─ ASSOCIATION #2: Product=TONSTAD Sofa, Store=TONSTAD, Warehouse=MWH.012
├─ ASSOCIATION #3: Product=TONSTAD Sofa, Store=TONSTAD, Warehouse=MWH.023
├─ ASSOCIATION #4: Product=KALLAX Shelf, Store=TONSTAD, Warehouse=MWH.001
├─ ...
└─ ASSOCIATION #25: ...
```

**Key Characteristics**:
1. **Many-to-Many with First Class Integration**
   - Not just a junction table (would be anonymous)
   - Explicit entity with timestamp and queryability
   - Enables cost tracking at transaction level (each fulfillment is tracked)

2. **Business Rule Constraints**:
   ```
   Rule 1: Product Distribution Limit
   ├─ Max 2 warehouses fulfilling same product to same store
   ├─ Prevents over-fulfillment
   └─ Example: TONSTAD Store gets TONSTAD Sofa from max 2 warehouses
   
   Rule 2: Store Fulfillment Limit
   ├─ Max 3 warehouses fulfilling any products to same store
   ├─ Prevents store from being overwhelmed with suppliers
   └─ Example: TONSTAD Store receives from max 3 warehouses total
   
   Rule 3: Warehouse Product Range
   ├─ Max 5 different product types per warehouse
   ├─ Prevents warehouse from becoming too generic
   └─ Example: MWH.001 stores max 5 different furniture SKUs
   ```

**Relationship to Other Entities**:
```
FULFILLMENT NETWORK QUERIES:

Query 1: "How is Product A distributed?"
├─ SELECT * FROM WAREHOUSE_PRODUCT_STORE WHERE productId=1
├─ Shows all (store, warehouse) pairs receiving Product A
└─ Answer: Product 1 (Sofa) → Store 1,2,3 via Warehouse MWH.001, MWH.012, MWH.023

Query 2: "Which products does Store X receive?"
├─ SELECT DISTINCT productId FROM WAREHOUSE_PRODUCT_STORE WHERE storeId=1
├─ Shows all products distributed to Store X
└─ Answer: Store 1 receives 4 products from 3 warehouses

Query 3: "What's Warehouse Y inventory range?"
├─ SELECT DISTINCT productId FROM WAREHOUSE_PRODUCT_STORE WHERE warehouseBusinessUnitCode='MWH.001'
├─ Shows all products in warehouse
└─ Answer: MWH.001 holds 4 product types (Sofa, Shelf, Chair, Bed)

Query 4: "Can I add new association?"
├─ Check if adding (Product X, Store Y, Warehouse Z) violates constraints
├─ Count of (X, Y, *) associations: should be < 2
├─ Count of (*, Y, *) associations: should be < 3
├─ Distinct products in Z: should be < 5
└─ Answer: YES if all constraints satisfied, NO with error explanation
```

---

## 🔄 System Flow Diagrams

### Flow 1: Create Warehouse

```
User Request: POST /warehouse with (businessUnitCode, location, capacity, stock)
        ↓
CreateWarehouseUseCase
├─ Step 1: Validate Business Unit Code
│  └─ Query: SELECT COUNT(*) FROM WAREHOUSE WHERE businessUnitCode='MWH.NEW01'
│     Result: Must be 0 (code must be unique)
│
├─ Step 2: Validate Location
│  ├─ Call LocationGateway.resolveByIdentifier(locationId)
│  └─ Result: Must return non-null Location object (location must exist)
│
├─ Step 3: Check Location Capacity
│  ├─ Query: SELECT COUNT(*) FROM WAREHOUSE 
│           WHERE location='AMSTERDAM-001' AND archivedAt IS NULL
│  ├─ Check: count < location.maxNumberOfWarehouses
│  └─ Result: If AMSTERDAM-001 has max 5, current 4, new warehouse OK. If current 5, REJECTED.
│
├─ Step 4: Validate Stock ≤ Capacity
│  ├─ Assertion: stock <= capacity
│  └─ Result: If stock=600, capacity=500, REJECTED. If stock=400, ACCEPTED.
│
└─ Step 5: Persist to Database
   ├─ INSERT into WAREHOUSE table with provided values
   ├─ Set createdAt = NOW()
   ├─ Set archivedAt = NULL (active state)
   └─ RETURN: HTTP 201 Created + warehouse object

Success Response: HTTP 201
{
  "businessUnitCode": "MWH.NEW01",
  "location": "AMSTERDAM-001",
  "capacity": 700,
  "stock": 200,
  "createdAt": "2025-02-11T10:30:00",
  "archivedAt": null
}

Failure Response Example: HTTP 400
{
  "error": "Business Unit Code MWH.001 already exists"
}
```

---

### Flow 2: Replace Warehouse

```
User Request: PUT /warehouse/MWH.001/replacement with (capacity, stock)
        ↓
ReplaceWarehouseUseCase
├─ Step 1: Find Old Warehouse
│  ├─ Query: SELECT * FROM WAREHOUSE WHERE businessUnitCode='MWH.001' AND archivedAt IS NULL
│  └─ Result: Must find exactly 1 (uniqueness + active status)
│
├─ Step 2: Archive Old Warehouse
│  ├─ UPDATE WAREHOUSE SET archivedAt = NOW() WHERE id = <old_id>
│  ├─ Cost history preserved: old warehouse row remains queryable for analytics
│  └─ Effect: Old warehouse now invisible in normal queries (active warehouses only)
│
├─ Step 3: Create New Warehouse
│  ├─ Validate new warehouse parameters same as CREATE flow
│  ├─ INSERT new warehouse with same businessUnitCode (KEY REUSE)
│  ├─ Set createdAt = NOW()
│  └─ Set archivedAt = NULL
│
└─ Step 4: Return Both States
   └─ RETURN: HTTP 200 + {oldWarehouse (archived), newWarehouse (active)}

Before Replacement:
├─ MWH.001 (active): WAREHOUSE table row with archivedAt=null, location=ZWOLLE-001
└─ Cost tracking: All fulfillment costs 2024-2025 attributed to MWH.001

After Replacement:
├─ MWH.001 (archived): WAREHOUSE table row with archivedAt=2025-02-11T10:30:00
├─ MWH.001 (new active): WAREHOUSE table row with createdAt=2025-02-11T10:30:01, archivedAt=null
└─ Cost tracking: 
   ├─ Old MWH.001 costs: Queryable via WHERE businessUnitCode='MWH.001' AND archivedAt IS NOT NULL
   ├─ New MWH.001 costs: Queryable via WHERE businessUnitCode='MWH.001' AND archivedAt IS NULL
   └─ Enables before/after comparison for cost control

Key Insight: Business Unit Code is NOT unique to a single Warehouse entity anymore.
Instead, (businessUnitCode, archivedAt) combination identifies a specific warehouse generation.
```

---

### Flow 3: Create Fulfillment Association

```
User Request: POST /fulfillment/warehouse-product-store with (productId, storeId, warehouseBusinessUnitCode)
        ↓
AssociateWarehouseToProductStoreUseCase
├─ Step 1: Find Warehouse
│  ├─ Query: SELECT * FROM WAREHOUSE WHERE businessUnitCode='MWH.001' AND archivedAt IS NULL
│  └─ Result: Must find active warehouse (archived warehouses never participate in new fulfillment)
│
├─ Step 2: Validate Product Exists
│  ├─ Query: SELECT * FROM PRODUCT WHERE id=<productId>
│  └─ Result: Product must exist in system
│
├─ Step 3: Validate Store Exists
│  ├─ Query: SELECT * FROM STORE WHERE id=<storeId>
│  └─ Result: Store must exist in system
│
├─ Step 4: Check Product Distribution Limit (Rule #1)
│  ├─ Query: SELECT COUNT(*) FROM WAREHOUSE_PRODUCT_STORE 
│           WHERE productId=<productId> AND storeId=<storeId>
│  ├─ Check: count < 2 (max 2 warehouses per product-store pair)
│  └─ Validation: If count already 2, REJECT new association with message 
│               "Product <id> already fulfilled to Store <id> by 2 warehouses"
│
├─ Step 5: Check Store Fulfillment Limit (Rule #2)
│  ├─ Query: SELECT COUNT(DISTINCT warehouseBusinessUnitCode) FROM WAREHOUSE_PRODUCT_STORE 
│           WHERE storeId=<storeId>
│  ├─ Check: count < 3 (max 3 warehouses per store)
│  └─ Validation: If count already 3, REJECT
│
├─ Step 6: Check Warehouse Product Range Limit (Rule #3)
│  ├─ Query: SELECT COUNT(DISTINCT productId) FROM WAREHOUSE_PRODUCT_STORE 
│           WHERE warehouseBusinessUnitCode='MWH.001'
│  ├─ Check: count < 5 (max 5 product types per warehouse)
│  └─ Validation: If count already 5, REJECT with message 
│               "Warehouse <code> already stores 5 product types, cannot add product <id>"
│
├─ Step 7: Check Uniqueness
│  ├─ Query: SELECT * FROM WAREHOUSE_PRODUCT_STORE 
│           WHERE warehouseBusinessUnitCode='MWH.001' AND productId=<productId> AND storeId=<storeId>
│  ├─ Check: result must be null (unique constraint)
│  └─ Validation: If found, REJECT "Association already exists"
│
└─ Step 8: Create Association
   ├─ INSERT into WAREHOUSE_PRODUCT_STORE
   │  ├─ productId = <provided>
   │  ├─ storeId = <provided>
   │  ├─ warehouseBusinessUnitCode = <provided>
   │  └─ createdAt = NOW()
   └─ RETURN: HTTP 201 Created + association object

Success Response: HTTP 201
{
  "id": 26,
  "productId": 4,
  "storeId": 2,
  "warehouseBusinessUnitCode": "MWH.034",
  "createdAt": "2025-02-11T11:00:00"
}

Failure Response Examples:

Case A: Product already fulfilled by 2 warehouses
{
  "error": "Product 1 is already fulfilled to Store 1 by 2 warehouses (MWH.001, MWH.012). Cannot add more."
}

Case B: Store already fulfilled by 3 warehouses
{
  "error": "Store 1 is already fulfilled by 3 warehouses. Cannot add Warehouse MWH.034."
}

Case C: Warehouse already stores 5 product types
{
  "error": "Warehouse MWH.001 stores 5 product types (Sofa, Shelf, Cabinet, Chair, Bed). Cannot add product Door."
}

Case D: Association already exists
{
  "error": "Association already exists: Product 4, Store 2 fulfilled by Warehouse MWH.034"
}
```

---

### Flow 4: Resolve Location

```
User Request: GET /location/ZWOLLE-001
        ↓
LocationResource.resolveLocation(pathParam id)
├─ Step 1: Call Gateway
│  └─ locationGateway.resolveByIdentifier("ZWOLLE-001")
│
├─ Step 2: Gateway Logic (8 predefined locations in static list)
│  ├─ IF "ZWOLLE-001" found in map:
│  │  └─ RETURN Location object {identification, maxNumberOfWarehouses, maxCapacity}
│  └─ IF id not found:
│     └─ RETURN null
│
├─ Step 3: Handle Result
│  ├─ If location != null:
│  │  └─ CREATE LocationDto from Location
│  │     ├─ identification: "ZWOLLE-001"
│  │     ├─ maxNumberOfWarehouses: 1
│  │     └─ maxCapacity: 40
│  │
│  └─ If location == null:
│     ├─ CREATE ErrorResponse
│     │  ├─ code: 404
│     │  └─ message: "Location with id 'ZWOLLE-001' not found"
│     └─ RETURN HTTP 404
│
└─ Return Response

Success Response (HTTP 200):
{
  "identification": "ZWOLLE-001",
  "maxNumberOfWarehouses": 1,
  "maxCapacity": 40
}

Failure Response (HTTP 404):
{
  "exceptionType": "jakarta.ws.rs.NotFoundException",
  "code": 404,
  "error": "Location with id 'INVALID' not found"
}

Key Insight: Location resolution is SYNCHRONOUS, NO database lookup.
Pure reference data validation against static configuration.
Used by Warehouse creation to validate location parameter.
```

---

### Flow 5: Update Store with Legacy System Sync

```
User Request: PATCH /store/1 with {quantityProductsInStock: 50}
        ↓
StoreResource.update(storeId, updateRequest)
├─ Step 1: Find Store
│  ├─ Query: SELECT * FROM STORE WHERE id=1
│  └─ Result: Must exist
│
├─ Step 2: Update Store Entity
│  ├─ UPDATE STORE SET quantityProductsInStock = 50 WHERE id = 1
│  └─ Transaction: Database commit happens
│
├─ Step 3: AFTER Transaction Commits (Critical!)
│  ├─ Call LegacyStoreManagerGateway.updateStore(storeId, newQuantity)
│  ├─ This simulates integration with downstream legacy system
│  └─ Why after commit: Ensures change is persisted before propagation
│       (If legacy sync fails, store still updated in authoritative system)
│
└─ Step 4: Return Result

Success Response (HTTP 200):
{
  "id": 1,
  "name": "TONSTAD Store",
  "quantityProductsInStock": 50,
  "updatedSyncedWithLegacy": true
}

Key Architectural Pattern:
──────────────────────────

WRONG (Pre-commit sync):
┌─────────────────────────────────┐
│ 1. Call Legacy System  ←─ RISK: If sync succeeds, DB commit fails
│ 2. Commit to DB           → Store updated in legacy but not here!
└─────────────────────────────────┘

CORRECT (Post-commit sync):
┌──────────────────────────────────────────┐
│ 1. Commit to DB ← All-or-nothing         │
│    (Transaction boundaries)              │
│ 2. Call Legacy System ← Safe: DB sync    │
│    (Eventual consistency pattern)        │
│ 3. If legacy fails ← Async retry queue   │
│    (Message queue handles retry)         │
└──────────────────────────────────────────┘

This ensures data consistency: legacy system NEVER has data the authoritative system doesn't have.
```

---

## 🎯 Key Architectural Patterns Explained

### Pattern 1: Domain-Only Entity (Location)
```
Why?
├─ Locations are business rules, not operational data
├─ Never change (static reference)
├─ Used for validation, not transactions
└─ No need for database persistence overhead

Implementation:
├─ Static List: 8 predefined locations hardcoded in LocationGateway
├─ Resolution: Fast O(1) lookup by ID
└─ Validation: Called by warehouse creation to enforce policy constraints

Query Pattern:
├─ Q: "Is ZWOLLE-001 a valid location?" A: locationGateway.resolveByIdentifier("ZWOLLE-001") != null
├─ Q: "What's the max warehouses at ZWOLLE-001?" A: location.maxNumberOfWarehouses
└─ Q: "What's the max capacity?" A: location.maxCapacity
```

### Pattern 2: Separate Entity States via Soft Delete
```
Why?
├─ Preserve historical data (cost tracking)
├─ Enable before/after comparison (replacement ROI)
└─ Avoid orphaning foreign key references

Implementation:
├─ Warehouse table includes archivedAt field
├─ Active warehouses: archivedAt IS NULL
├─ Archived warehouses: archivedAt IS NOT NULL (contains timestamp)
└─ Business Unit Code NOT unique; (code, archivedAt) combination is

Query Patterns:
├─ Get active warehouse: WHERE businessUnitCode='MWH.001' AND archivedAt IS NULL
├─ Get all versions of warehouse: WHERE businessUnitCode='MWH.001' (returns old + new)
└─ Cost comparison: Old costs from (archived=false), new costs from (archived=true)
```

### Pattern 3: Business Rules as Use Cases
```
Why?
├─ Separates business logic from database operations
├─ Makes constraints explicit and testable
└─ Enables complex validations before persistence

Constraints Enforced:
├─ Location validation (resolved before warehouse creation)
├─ Location capacity checks (count existing warehouses at location)
├─ Business unit code uniqueness (required for identification)
├─ Fulfillment association constraints (Rule 1: max 2, Rule 2: max 3, Rule 3: max 5)
└─ Stock ≤ capacity (physical constraint)

Implementation:
├─ UseCase classes: CreateWarehouseUseCase.execute()
├─ Validation logic: All checks before repository.persist()
└─ Fail-fast: Return informative error immediately on violation
```

### Pattern 4: Transactional Post-Action Callbacks
```
Why?
├─ Ensure data consistency across systems
├─ Implement eventual consistency pattern
└─ Decouple operational system from legacy integrations

Implementation:
├─ Store update
│  ├─ TRANSACTION START
│  ├─ UPDATE STORE in database
│  ├─ TRANSACTION COMMIT (all-or-nothing)
│  └─ TRANSACTION END
│
├─ AFTER transaction succeeds
│  └─ Call LegacyStoreManagerGateway.updateStore()
│
└─ If legacy call fails
   └─ Queue for async retry (eventual consistency)

Benefit:
├─ Authoritative system (this app) never loses data
├─ Legacy system eventually catches up
└─ No data inconsistency windows
```

---

## 📈 Query Patterns for Interview Explanation

### Query 1: "How many warehouses serve Store X?"

```sql
SELECT COUNT(DISTINCT warehouseBusinessUnitCode)
FROM WAREHOUSE_PRODUCT_STORE
WHERE storeId = ? AND createdAt >= ?
```

**Explanation**: Distinct warehouses because same warehouse might fulfill multiple products to same store. Using createdAt filter allows analyzing historical fulfillment patterns.

---

### Query 2: "Can I add a new warehouse at location Y?"

```query-logic
Step 1: Find location details
  location = LocationGateway.resolveByIdentifier("AMSTERDAM-001")
  
Step 2: Count existing warehouses
  existingCount = WAREHOUSE.count(
    location="AMSTERDAM-001" AND archivedAt IS NULL
  )
  
Step 3: Check constraint
  IF existingCount < location.maxNumberOfWarehouses
    RETURN "OK to create"
  ELSE
    RETURN "Location at capacity"
```

---

### Query 3: "What products does Warehouse W stock?"

```sql
SELECT DISTINCT p.id, p.name, p.price
FROM PRODUCT p
JOIN WAREHOUSE_PRODUCT_STORE wps ON p.id = wps.productId
WHERE wps.warehouseBusinessUnitCode = 'MWH.001'
```

**Explanation**: This shows the product diversity constraint. If result count >= 5, cannot add more products to this warehouse.

---

### Query 4: "What's the fulfillment network for Product P?"

```sql
SELECT 
  DISTINCT wps.storeId as destination_store,
  wps.warehouseBusinessUnitCode as origin_warehouse,
  w.location as warehouse_location,
  COUNT(*) as product_instances
FROM WAREHOUSE_PRODUCT_STORE wps
JOIN WAREHOUSE w ON wps.warehouseBusinessUnitCode = w.businessUnitCode
WHERE wps.productId = ? AND w.archivedAt IS NULL
GROUP BY wps.storeId, wps.warehouseBusinessUnitCode, w.location
```

**Explanation**: Maps how a product flows from warehouses to stores. The network visualization helps understand distribution strategy.

---

## 🏁 Summary for Interview

**Key Points to Emphasize:**

1. **Separation of Concerns**
   - REST Layer: Request parsing, response formatting
   - Use Case Layer: Business rule validation
   - Domain Layer: Reference data (Location)
   - Persistence Layer: Database operations
   - Each layer has a clear responsibility

2. **Business Rule Enforcement**
   - Location constraints enforced at warehouse creation
   - Fulfillment constraints enforced at association creation
   - 3 explicit fulfillment rules with clear error messages
   - Fail-fast validation prevents invalid data persists

3. **Data Consistency**
   - Transactional database operations (all-or-nothing)
   - Post-action callbacks for legacy system sync
   - Soft delete for historical preservation
   - Audit trail via timestamps on all operations

4. **Flexibility Through Reusable Codes**
   - Business Unit Code reuse enables warehouse replacement
   - Old and new warehouse distinct via archivedAt field
   - Cost history preserved for performance analysis
   - Before/after comparison enables cost control

5. **Query-Driven Design**
   - Fulfillment associations are queryable first-class entities
   - Enables cost tracking at atomic fulfillment level
   - Supports rich analytics and reporting
   - Foundation for forecasting and optimization
