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
INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, createdAt, archivedAt) 
VALUES (1, 'MWH.001', 'ZWOLLE-001', 500, 150, '2024-07-01', null);

INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, createdAt, archivedAt)
VALUES (2, 'MWH.012', 'AMSTERDAM-001', 800, 200, '2023-07-01', null);

INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, createdAt, archivedAt)
VALUES (3, 'MWH.023', 'TILBURG-001', 600, 180, '2021-02-01', null);

INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, createdAt, archivedAt)
VALUES (4, 'MWH.034', 'ROTTERDAM-001', 1000, 250, '2024-01-15', null);

INSERT INTO warehouse(id, businessUnitCode, location, capacity, stock, createdAt, archivedAt)
VALUES (5, 'MWH.045', 'UTRECHT-001', 400, 120, '2024-03-20', null);

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
VALUES (1, 'MWH.001', 1, 1, '2024-08-01');
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (2, 'MWH.001', 2, 1, '2024-08-01');
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (3, 'MWH.001', 4, 1, '2024-08-01');

INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (4, 'MWH.012', 1, 1, '2024-08-02');
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (5, 'MWH.012', 3, 1, '2024-08-02');

INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (6, 'MWH.023', 5, 1, '2024-08-03');

-- Store 2 (KALLAX) is fulfilled by warehouses 2, 4
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (7, 'MWH.012', 2, 2, '2024-08-04');
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (8, 'MWH.012', 4, 2, '2024-08-04');
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (9, 'MWH.012', 6, 2, '2024-08-04');

INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (10, 'MWH.034', 1, 2, '2024-08-05');
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (11, 'MWH.034', 3, 2, '2024-08-05');

-- Store 3 (BESTÅ) is fulfilled by warehouses 3, 5
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (12, 'MWH.023', 3, 3, '2024-08-06');
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (13, 'MWH.023', 5, 3, '2024-08-06');
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (14, 'MWH.023', 6, 3, '2024-08-06');

INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (15, 'MWH.045', 2, 3, '2024-08-07');

-- Store 4 (EKTORP) is fulfilled by warehouses 1, 4
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (16, 'MWH.001', 4, 4, '2024-08-08');
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (17, 'MWH.001', 6, 4, '2024-08-08');

INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (18, 'MWH.034', 5, 4, '2024-08-09');
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (19, 'MWH.034', 1, 4, '2024-08-09');

-- Store 5 (MALM) is fulfilled by warehouses 2, 3, 5
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (20, 'MWH.012', 5, 5, '2024-08-10');
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (21, 'MWH.012', 2, 5, '2024-08-10');

INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (22, 'MWH.023', 1, 5, '2024-08-11');
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (23, 'MWH.023', 4, 5, '2024-08-11');

INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (24, 'MWH.045', 6, 5, '2024-08-12');
INSERT INTO warehouse_product_store(id, warehouseBusinessUnitCode, productId, storeId, createdAt)
VALUES (25, 'MWH.045', 3, 5, '2024-08-12');

ALTER SEQUENCE warehouse_product_store_seq RESTART WITH 26;
