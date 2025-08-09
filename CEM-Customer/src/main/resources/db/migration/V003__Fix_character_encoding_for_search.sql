-- Ensure proper character encoding for text fields
-- This migration fixes issues with Vietnamese character search

-- First, ensure the database uses UTF8 encoding (if not already set)
-- Note: This might already be set at database level, but we ensure column level

-- Recreate indexes to ensure proper collation
DROP INDEX IF EXISTS idx_customers_name;
DROP INDEX IF EXISTS idx_customers_email;

-- Recreate the indexes with proper collation
CREATE INDEX idx_customers_name ON customers(name varchar_pattern_ops);
CREATE INDEX idx_customers_email ON customers(email varchar_pattern_ops);

-- Add indexes for other searchable text fields  
CREATE INDEX IF NOT EXISTS idx_customers_legal_representative ON customers(legal_representative varchar_pattern_ops);
CREATE INDEX IF NOT EXISTS idx_customers_company_name ON customers(company_name varchar_pattern_ops);

-- Ensure all text columns are properly set as text type (not bytea)
-- This helps PostgreSQL understand these are text fields for LOWER() function
ALTER TABLE customers ALTER COLUMN name TYPE VARCHAR(255) USING name::VARCHAR(255);
ALTER TABLE customers ALTER COLUMN email TYPE VARCHAR(255) USING email::VARCHAR(255);
ALTER TABLE customers ALTER COLUMN legal_representative TYPE VARCHAR(255) USING legal_representative::VARCHAR(255);
ALTER TABLE customers ALTER COLUMN company_name TYPE VARCHAR(255) USING company_name::VARCHAR(255);
ALTER TABLE customers ALTER COLUMN title TYPE VARCHAR(255) USING title::VARCHAR(255);
ALTER TABLE customers ALTER COLUMN identity_issue_place TYPE VARCHAR(255) USING identity_issue_place::VARCHAR(255);