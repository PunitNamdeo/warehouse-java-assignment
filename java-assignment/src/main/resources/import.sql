-- =====================================================================
-- STORES DATA
-- =====================================================================
-- Physical stores where products are sold to customers
INSERT INTO store(id, name, quantityProductsInStock) VALUES (1, 'TONSTAD Store', 50);
INSERT INTO store(id, name, quantityProductsInStock) VALUES (2, 'KALLAX Store', 35);
INSERT INTO store(id, name, quantityProductsInStock) VALUES (3, 'BESTÅ Store', 25);
INSERT INTO store(id, name, quantityProductsInStock) VALUES (4, 'EKTORP Store', 45);
INSERT INTO store(id, name, quantityProductsInStock) VALUES (5, 'MALM Store', 60);
ALTER SEQUENCE store_seq RESTART WITH 6;

-- =====================================================================
-- PRODUCTS DATA
-- =====================================================================
-- Goods that are sold in stores and stored in warehouses
INSERT INTO product(id, name, description, price, stock) VALUES (1, 'TONSTAD Sofa', 'Three-seat sofa with modern design', 299.99, 50);
INSERT INTO product(id, name, description, price, stock) VALUES (2, 'KALLAX Shelf', 'Shelving unit with clean lines', 149.99, 75);
INSERT INTO product(id, name, description, price, stock) VALUES (3, 'BESTÅ Cabinet', 'Storage cabinet with glass doors', 199.99, 40);
INSERT INTO product(id, name, description, price, stock) VALUES (4, 'EKTORP Chair', 'Comfortable lounge chair', 129.99, 100);
INSERT INTO product(id, name, description, price, stock) VALUES (5, 'MALM Bed Frame', 'Queen-size bed frame', 349.99, 35);
INSERT INTO product(id, name, description, price, stock) VALUES (6, 'LAPPVIKEN Door', 'Cabinet door with soft-close', 49.99, 200);
ALTER SEQUENCE product_seq RESTART WITH 7;

-- =====================================================================
-- WAREHOUSES DATA
-- =====================================================================
-- Warehouses where products are kept for distribution to stores
-- Each warehouse has a unique Business Unit Code and is located in a specific location
-- Database primary key (id) is used for API lookups -> GET /warehouse/{id}
-- Business unit code is used for replacements -> POST /warehouse/{businessUnitCode}/replacement

-- Clean existing warehouse data
DELETE FROM warehouse_product_store WHERE warehouseBusinessUnitCode IN ('AMST.EU.001', 'ROTT.EU.002', 'ZWOLLE.EU.003', 'TILB.EU.004', 'UTRE.EU.005');
DELETE FROM warehouse WHERE businessUnitCode IN ('AMST.EU.001', 'ROTT.EU.002', 'ZWOLLE.EU.003', 'TILB.EU.004', 'UTRE.EU.005');

-- Insert new warehouse entries with updated schema
INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, createdAt, archivedAt) 
VALUES (1, 'AMST.EU.001', 'AMSTERDAM-001', 1000, 450, CURRENT_TIMESTAMP(), null);

INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, createdAt, archivedAt)
VALUES (2, 'ROTT.EU.002', 'ROTTERDAM-001', 1200, 520, CURRENT_TIMESTAMP(), null);

INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, createdAt, archivedAt)
VALUES (3, 'ZWOLLE.EU.003', 'ZWOLLE-001', 800, 380, CURRENT_TIMESTAMP(), null);

INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, createdAt, archivedAt)
VALUES (4, 'TILB.EU.004', 'TILBURG-001', 900, 410, CURRENT_TIMESTAMP(), null);

INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, createdAt, archivedAt)
VALUES (5, 'UTRE.EU.005', 'UTRECHT-001', 750, 320, CURRENT_TIMESTAMP(), null);

ALTER SEQUENCE warehouse_seq RESTART WITH 6;

-- =====================================================================
-- WAREHOUSE-PRODUCT-STORE ASSOCIATIONS (FULFILLMENT)
-- =====================================================================
-- Associations that link warehouses as fulfillment units for products in specific stores
-- Constraints:
-- 1. Each Product can be fulfilled by max 2 different Warehouses per Store
-- 2. Each Store can be fulfilled by max 3 different Warehouses
-- 3. Each Warehouse can store max 5 types of Products

-- Store 1 (TONSTAD) is fulfilled by warehouses 1, 2, 3
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (1, 'AMST.EU.001', 1, 1, CURRENT_TIMESTAMP());
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (2, 'AMST.EU.001', 2, 1, CURRENT_TIMESTAMP());
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (3, 'AMST.EU.001', 4, 1, CURRENT_TIMESTAMP());

INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (4, 'ROTT.EU.002', 1, 1, CURRENT_TIMESTAMP());
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (5, 'ROTT.EU.002', 3, 1, CURRENT_TIMESTAMP());

INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (6, 'ZWOLLE.EU.003', 5, 1, CURRENT_TIMESTAMP());

-- Store 2 (KALLAX) is fulfilled by warehouses 2, 4
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (7, 'ROTT.EU.002', 2, 2, CURRENT_TIMESTAMP());
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (8, 'ROTT.EU.002', 4, 2, CURRENT_TIMESTAMP());
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (9, 'ROTT.EU.002', 6, 2, CURRENT_TIMESTAMP());

INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (10, 'TILB.EU.004', 1, 2, CURRENT_TIMESTAMP());
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (11, 'TILB.EU.004', 3, 2, CURRENT_TIMESTAMP());

-- Store 3 (BESTÅ) is fulfilled by warehouses 3, 5
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (12, 'ZWOLLE.EU.003', 3, 3, CURRENT_TIMESTAMP());
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (13, 'ZWOLLE.EU.003', 5, 3, CURRENT_TIMESTAMP());
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (14, 'ZWOLLE.EU.003', 6, 3, CURRENT_TIMESTAMP());

INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (15, 'UTRE.EU.005', 2, 3, CURRENT_TIMESTAMP());

-- Store 4 (EKTORP) is fulfilled by warehouses 1, 4
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (16, 'AMST.EU.001', 4, 4, CURRENT_TIMESTAMP());
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (17, 'AMST.EU.001', 6, 4, CURRENT_TIMESTAMP());

INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (18, 'TILB.EU.004', 5, 4, CURRENT_TIMESTAMP());
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (19, 'TILB.EU.004', 1, 4, CURRENT_TIMESTAMP());

-- Store 5 (MALM) is fulfilled by warehouses 2, 3, 5
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (20, 'ROTT.EU.002', 5, 5, CURRENT_TIMESTAMP());
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (21, 'ROTT.EU.002', 2, 5, CURRENT_TIMESTAMP());

INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (22, 'ZWOLLE.EU.003', 1, 5, CURRENT_TIMESTAMP());
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (23, 'ZWOLLE.EU.003', 4, 5, CURRENT_TIMESTAMP());

INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (24, 'UTRE.EU.005', 6, 5, CURRENT_TIMESTAMP());
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (25, 'UTRE.EU.005', 3, 5, CURRENT_TIMESTAMP());

ALTER SEQUENCE warehouse_product_store_seq RESTART WITH 26;
