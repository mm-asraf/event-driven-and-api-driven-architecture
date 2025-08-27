-- =====================================================
-- PostgreSQL Database Schema for Architectures Project
-- =====================================================
-- This file contains all table creation scripts
-- Run this file in your PostgreSQL database to create the schema
-- =====================================================

-- Create database (run this first if database doesn't exist)
-- CREATE DATABASE architectures_db;

-- Connect to the database
-- \c architectures_db;

-- =====================================================
-- 1. USER TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS tbl_user (
    id_user_details BIGSERIAL PRIMARY KEY,
    nm_user_name VARCHAR(100) NOT NULL,
    id_email VARCHAR(255) UNIQUE NOT NULL,
    nm_first VARCHAR(100) NOT NULL,
    nm_last VARCHAR(100) NOT NULL,
    num_phone VARCHAR(20),
    fl_active BOOLEAN DEFAULT true,
    ts_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ts_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 2. ADDRESS TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS tbl_address (
    id_address_details BIGSERIAL PRIMARY KEY,
    fk_id_user_details BIGINT NOT NULL,
    tx_address TEXT NOT NULL,
    nm_city VARCHAR(100) NOT NULL,
    tx_state VARCHAR(100) NOT NULL,
    cd_zip VARCHAR(20) NOT NULL,
    nm_country VARCHAR(100) NOT NULL,
    ts_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ts_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraint
    CONSTRAINT fk_address_user 
        FOREIGN KEY (fk_id_user_details) 
        REFERENCES tbl_user(id_user_details) 
        ON DELETE CASCADE
);

-- =====================================================
-- 3. PRODUCT TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS tbl_product (
    id_product_details BIGSERIAL PRIMARY KEY,
    nm_product VARCHAR(255) NOT NULL,
    num_price DECIMAL(10,2) NOT NULL CHECK (num_price >= 0),
    num_stock_quantity INTEGER DEFAULT 0 CHECK (num_stock_quantity >= 0),
    tx_description TEXT,
    ts_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ts_modified TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- 4. ORDER TABLE
-- =====================================================
CREATE TABLE IF NOT EXISTS tbl_order (
    id_order_details BIGSERIAL PRIMARY KEY,
    fk_id_user_details BIGINT NOT NULL,
    fk_id_user_address BIGINT NOT NULL,
    num_total_amount DECIMAL(10,2) NOT NULL CHECK (num_total_amount >= 0),
    enum_status VARCHAR(50) NOT NULL DEFAULT 'CREATED',
    ts_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_order_user 
        FOREIGN KEY (fk_id_user_details) 
        REFERENCES tbl_user(id_user_details) 
        ON DELETE CASCADE,
    CONSTRAINT fk_order_address 
        FOREIGN KEY (fk_id_user_address) 
        REFERENCES tbl_address(id_address_details) 
        ON DELETE CASCADE,
    
    -- Check constraint for status
    CONSTRAINT chk_order_status 
        CHECK (enum_status IN ('CREATED', 'PAYMENT_PROCESSED', 'SHIPPED', 'DELIVERED'))
);

-- =====================================================
-- 5. ORDER-PRODUCT BRIDGE TABLE (for manual control)
-- =====================================================
CREATE TABLE IF NOT EXISTS tbl_order_product_ids (
    fk_id_order_details BIGINT NOT NULL,
    fk_id_product_details BIGINT NOT NULL,
    num_quantity INTEGER DEFAULT 1,
    num_unit_price DECIMAL(10,2),
    ts_added TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Primary key (composite)
    PRIMARY KEY (fk_id_order_details, fk_id_product_details),
    
    -- Foreign key constraints
    CONSTRAINT fk_order_product_order 
        FOREIGN KEY (fk_id_order_details) 
        REFERENCES tbl_order(id_order_details) 
        ON DELETE CASCADE,
    CONSTRAINT fk_order_product_product 
        FOREIGN KEY (fk_id_product_details) 
        REFERENCES tbl_product(id_product_details) 
        ON DELETE CASCADE
);

-- =====================================================
-- 6. INDEXES FOR PERFORMANCE
-- =====================================================

-- User indexes
CREATE INDEX IF NOT EXISTS idx_user_email ON tbl_user(id_email);
CREATE INDEX IF NOT EXISTS idx_user_active ON tbl_user(fl_active);

-- Address indexes
CREATE INDEX IF NOT EXISTS idx_address_user ON tbl_address(fk_id_user_details);
CREATE INDEX IF NOT EXISTS idx_address_city ON tbl_address(nm_city);

-- Product indexes
CREATE INDEX IF NOT EXISTS idx_product_name ON tbl_product(nm_product);
CREATE INDEX IF NOT EXISTS idx_product_price ON tbl_product(num_price);
CREATE INDEX IF NOT EXISTS idx_product_stock ON tbl_product(num_stock_quantity);

-- Order indexes
CREATE INDEX IF NOT EXISTS idx_order_user ON tbl_order(fk_id_user_details);
CREATE INDEX IF NOT EXISTS idx_order_status ON tbl_order(enum_status);
CREATE INDEX IF NOT EXISTS idx_order_created ON tbl_order(ts_created);

-- Order-Product indexes
CREATE INDEX IF NOT EXISTS idx_order_product_order ON tbl_order_product_ids(fk_id_order_details);
CREATE INDEX IF NOT EXISTS idx_order_product_product ON tbl_order_product_ids(fk_id_product_details);

-- =====================================================
-- 7. SAMPLE DATA (Optional - for testing)
-- =====================================================

-- Insert sample user
INSERT INTO tbl_user (nm_user_name, id_email, nm_first, nm_last, num_phone, fl_active) 
VALUES ('john_doe', 'john.doe@example.com', 'John', 'Doe', '+1234567890', true)
ON CONFLICT (id_email) DO NOTHING;

-- Insert sample address
INSERT INTO tbl_address (fk_id_user_details, tx_address, nm_city, tx_state, cd_zip, nm_country)
SELECT id_user_details, '123 Main Street', 'New York', 'NY', '10001', 'USA'
FROM tbl_user WHERE id_email = 'john.doe@example.com'
ON CONFLICT DO NOTHING;

-- Insert sample products
INSERT INTO tbl_product (nm_product, num_price, num_stock_quantity, tx_description) VALUES
('Laptop', 999.99, 50, 'High-performance laptop for professionals'),
('Mouse', 29.99, 100, 'Wireless optical mouse'),
('Keyboard', 79.99, 75, 'Mechanical gaming keyboard')
ON CONFLICT DO NOTHING;

-- =====================================================
-- 8. COMMENTS FOR DOCUMENTATION
-- =====================================================
COMMENT ON TABLE tbl_user IS 'Stores user account information';
COMMENT ON TABLE tbl_address IS 'Stores user shipping addresses';
COMMENT ON TABLE tbl_product IS 'Stores product catalog information';
COMMENT ON TABLE tbl_order IS 'Stores customer orders';
COMMENT ON TABLE tbl_order_product_ids IS 'Bridge table linking orders to products via IDs';

COMMENT ON COLUMN tbl_user.id_user_details IS 'Primary key for user';
COMMENT ON COLUMN tbl_user.fl_active IS 'Flag indicating if user account is active';
COMMENT ON COLUMN tbl_address.fk_id_user_details IS 'Foreign key to tbl_user';
COMMENT ON COLUMN tbl_order.enum_status IS 'Order status: CREATED, PAYMENT_PROCESSED, SHIPPED, DELIVERED';
COMMENT ON COLUMN tbl_order_product_ids.fk_id_order_details IS 'Foreign key to tbl_order';
COMMENT ON COLUMN tbl_order_product_ids.fk_id_product_details IS 'Foreign key to tbl_product';

-- =====================================================
-- SCRIPT COMPLETED
-- =====================================================
-- Run this script in your PostgreSQL database to create the complete schema
-- Make sure to update connection details in application.yml before starting the application 