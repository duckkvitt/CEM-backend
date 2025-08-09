-- Fix suppliers table column types for PostgreSQL
-- Convert bytea columns to proper text/varchar types

-- First, check if suppliers table exists, if not create it with proper types
CREATE TABLE IF NOT EXISTS suppliers
(
    id               BIGSERIAL PRIMARY KEY,
    company_name     VARCHAR(255) NOT NULL,
    contact_person   VARCHAR(255) NOT NULL,
    email            VARCHAR(255) NOT NULL,
    phone            VARCHAR(20)  NOT NULL,
    fax              VARCHAR(20),
    address          TEXT         NOT NULL,
    tax_code         VARCHAR(50),
    business_license VARCHAR(100),
    website          VARCHAR(255),
    description      TEXT,
    status           VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    created_at       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP,
    CONSTRAINT chk_supplier_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))
);

-- Create junction table for supplier spare part types if it doesn't exist
CREATE TABLE IF NOT EXISTS supplier_spare_part_types
(
    supplier_id      BIGINT       NOT NULL,
    spare_part_type  VARCHAR(255) NOT NULL,
    PRIMARY KEY (supplier_id, spare_part_type),
    FOREIGN KEY (supplier_id) REFERENCES suppliers (id) ON DELETE CASCADE
);

-- If the table already exists but has wrong column types, fix them
DO $$
BEGIN
    -- Check if company_name column exists and has wrong type
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'suppliers' 
        AND column_name = 'company_name' 
        AND data_type = 'bytea'
    ) THEN
        -- Convert bytea columns to proper text types
        ALTER TABLE suppliers ALTER COLUMN company_name TYPE VARCHAR(255) USING encode(company_name, 'escape');
    END IF;
    
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'suppliers' 
        AND column_name = 'contact_person' 
        AND data_type = 'bytea'
    ) THEN
        ALTER TABLE suppliers ALTER COLUMN contact_person TYPE VARCHAR(255) USING encode(contact_person, 'escape');
    END IF;
    
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'suppliers' 
        AND column_name = 'email' 
        AND data_type = 'bytea'
    ) THEN
        ALTER TABLE suppliers ALTER COLUMN email TYPE VARCHAR(255) USING encode(email, 'escape');
    END IF;
    
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'suppliers' 
        AND column_name = 'phone' 
        AND data_type = 'bytea'
    ) THEN
        ALTER TABLE suppliers ALTER COLUMN phone TYPE VARCHAR(20) USING encode(phone, 'escape');
    END IF;
    
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'suppliers' 
        AND column_name = 'fax' 
        AND data_type = 'bytea'
    ) THEN
        ALTER TABLE suppliers ALTER COLUMN fax TYPE VARCHAR(20) USING encode(fax, 'escape');
    END IF;
    
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'suppliers' 
        AND column_name = 'address' 
        AND data_type = 'bytea'
    ) THEN
        ALTER TABLE suppliers ALTER COLUMN address TYPE TEXT USING encode(address, 'escape');
    END IF;
    
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'suppliers' 
        AND column_name = 'tax_code' 
        AND data_type = 'bytea'
    ) THEN
        ALTER TABLE suppliers ALTER COLUMN tax_code TYPE VARCHAR(50) USING encode(tax_code, 'escape');
    END IF;
    
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'suppliers' 
        AND column_name = 'business_license' 
        AND data_type = 'bytea'
    ) THEN
        ALTER TABLE suppliers ALTER COLUMN business_license TYPE VARCHAR(100) USING encode(business_license, 'escape');
    END IF;
    
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'suppliers' 
        AND column_name = 'website' 
        AND data_type = 'bytea'
    ) THEN
        ALTER TABLE suppliers ALTER COLUMN website TYPE VARCHAR(255) USING encode(website, 'escape');
    END IF;
    
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'suppliers' 
        AND column_name = 'description' 
        AND data_type = 'bytea'
    ) THEN
        ALTER TABLE suppliers ALTER COLUMN description TYPE TEXT USING encode(description, 'escape');
    END IF;
    
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'suppliers' 
        AND column_name = 'status' 
        AND data_type = 'bytea'
    ) THEN
        ALTER TABLE suppliers ALTER COLUMN status TYPE VARCHAR(50) USING encode(status, 'escape');
    END IF;
END $$;

-- Create indexes for better query performance if they don't exist
CREATE INDEX IF NOT EXISTS idx_suppliers_company_name ON suppliers(company_name);
CREATE INDEX IF NOT EXISTS idx_suppliers_contact_person ON suppliers(contact_person);
CREATE INDEX IF NOT EXISTS idx_suppliers_email ON suppliers(email);
CREATE INDEX IF NOT EXISTS idx_suppliers_status ON suppliers(status);
CREATE INDEX IF NOT EXISTS idx_supplier_spare_part_types_supplier_id ON supplier_spare_part_types(supplier_id);
CREATE INDEX IF NOT EXISTS idx_supplier_spare_part_types_type ON supplier_spare_part_types(spare_part_type);