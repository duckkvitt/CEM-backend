-- Fix suppliers table encoding and data type issues definitively
-- This migration ensures all text columns are properly typed and encoded

-- First, backup any existing data and recreate the table with proper types if needed
DO $$
DECLARE
    table_exists boolean := false;
    has_data boolean := false;
BEGIN
    -- Check if suppliers table exists
    SELECT EXISTS (
        SELECT FROM information_schema.tables 
        WHERE table_schema = 'public' 
        AND table_name = 'suppliers'
    ) INTO table_exists;
    
    IF table_exists THEN
        -- Check if table has data
        SELECT EXISTS (SELECT 1 FROM suppliers LIMIT 1) INTO has_data;
        
        -- If table has problematic bytea columns, fix them
        -- Fix company_name
        IF EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'suppliers' 
            AND column_name = 'company_name' 
            AND data_type = 'bytea'
        ) THEN
            -- Convert bytea to text using proper encoding
            ALTER TABLE suppliers ALTER COLUMN company_name TYPE VARCHAR(255) 
            USING CASE 
                WHEN company_name IS NULL THEN NULL 
                ELSE convert_from(company_name, 'UTF8')
            END;
        END IF;
        
        -- Fix contact_person
        IF EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'suppliers' 
            AND column_name = 'contact_person' 
            AND data_type = 'bytea'
        ) THEN
            ALTER TABLE suppliers ALTER COLUMN contact_person TYPE VARCHAR(255) 
            USING CASE 
                WHEN contact_person IS NULL THEN NULL 
                ELSE convert_from(contact_person, 'UTF8')
            END;
        END IF;
        
        -- Fix email
        IF EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'suppliers' 
            AND column_name = 'email' 
            AND data_type = 'bytea'
        ) THEN
            ALTER TABLE suppliers ALTER COLUMN email TYPE VARCHAR(255) 
            USING CASE 
                WHEN email IS NULL THEN NULL 
                ELSE convert_from(email, 'UTF8')
            END;
        END IF;
        
        -- Fix phone
        IF EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'suppliers' 
            AND column_name = 'phone' 
            AND data_type = 'bytea'
        ) THEN
            ALTER TABLE suppliers ALTER COLUMN phone TYPE VARCHAR(20) 
            USING CASE 
                WHEN phone IS NULL THEN NULL 
                ELSE convert_from(phone, 'UTF8')
            END;
        END IF;
        
        -- Fix fax
        IF EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'suppliers' 
            AND column_name = 'fax' 
            AND data_type = 'bytea'
        ) THEN
            ALTER TABLE suppliers ALTER COLUMN fax TYPE VARCHAR(20) 
            USING CASE 
                WHEN fax IS NULL THEN NULL 
                ELSE convert_from(fax, 'UTF8')
            END;
        END IF;
        
        -- Fix address
        IF EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'suppliers' 
            AND column_name = 'address' 
            AND data_type = 'bytea'
        ) THEN
            ALTER TABLE suppliers ALTER COLUMN address TYPE TEXT 
            USING CASE 
                WHEN address IS NULL THEN NULL 
                ELSE convert_from(address, 'UTF8')
            END;
        END IF;
        
        -- Fix tax_code
        IF EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'suppliers' 
            AND column_name = 'tax_code' 
            AND data_type = 'bytea'
        ) THEN
            ALTER TABLE suppliers ALTER COLUMN tax_code TYPE VARCHAR(50) 
            USING CASE 
                WHEN tax_code IS NULL THEN NULL 
                ELSE convert_from(tax_code, 'UTF8')
            END;
        END IF;
        
        -- Fix business_license
        IF EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'suppliers' 
            AND column_name = 'business_license' 
            AND data_type = 'bytea'
        ) THEN
            ALTER TABLE suppliers ALTER COLUMN business_license TYPE VARCHAR(100) 
            USING CASE 
                WHEN business_license IS NULL THEN NULL 
                ELSE convert_from(business_license, 'UTF8')
            END;
        END IF;
        
        -- Fix website
        IF EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'suppliers' 
            AND column_name = 'website' 
            AND data_type = 'bytea'
        ) THEN
            ALTER TABLE suppliers ALTER COLUMN website TYPE VARCHAR(255) 
            USING CASE 
                WHEN website IS NULL THEN NULL 
                ELSE convert_from(website, 'UTF8')
            END;
        END IF;
        
        -- Fix description
        IF EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'suppliers' 
            AND column_name = 'description' 
            AND data_type = 'bytea'
        ) THEN
            ALTER TABLE suppliers ALTER COLUMN description TYPE TEXT 
            USING CASE 
                WHEN description IS NULL THEN NULL 
                ELSE convert_from(description, 'UTF8')
            END;
        END IF;
        
        -- Fix status
        IF EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_name = 'suppliers' 
            AND column_name = 'status' 
            AND data_type = 'bytea'
        ) THEN
            ALTER TABLE suppliers ALTER COLUMN status TYPE VARCHAR(50) 
            USING CASE 
                WHEN status IS NULL THEN 'ACTIVE'
                ELSE convert_from(status, 'UTF8')
            END;
        END IF;
        
    ELSE
        -- Create table with proper types if it doesn't exist
        CREATE TABLE suppliers (
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
    END IF;
    
    -- Ensure all columns have correct NOT NULL constraints
    IF table_exists THEN
        -- Make sure required columns are not null
        ALTER TABLE suppliers ALTER COLUMN company_name SET NOT NULL;
        ALTER TABLE suppliers ALTER COLUMN contact_person SET NOT NULL;
        ALTER TABLE suppliers ALTER COLUMN email SET NOT NULL;
        ALTER TABLE suppliers ALTER COLUMN phone SET NOT NULL;
        ALTER TABLE suppliers ALTER COLUMN address SET NOT NULL;
        ALTER TABLE suppliers ALTER COLUMN status SET NOT NULL;
        
        -- Set default for status if not already set
        ALTER TABLE suppliers ALTER COLUMN status SET DEFAULT 'ACTIVE';
        
        -- Ensure created_at has default
        ALTER TABLE suppliers ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;
    END IF;
    
END $$;

-- Ensure junction table exists with proper structure
CREATE TABLE IF NOT EXISTS supplier_spare_part_types (
    supplier_id      BIGINT       NOT NULL,
    spare_part_type  VARCHAR(255) NOT NULL,
    PRIMARY KEY (supplier_id, spare_part_type),
    FOREIGN KEY (supplier_id) REFERENCES suppliers (id) ON DELETE CASCADE
);

-- Create indexes for optimal query performance
CREATE INDEX IF NOT EXISTS idx_suppliers_company_name_lower ON suppliers(LOWER(company_name));
CREATE INDEX IF NOT EXISTS idx_suppliers_contact_person_lower ON suppliers(LOWER(contact_person));
CREATE INDEX IF NOT EXISTS idx_suppliers_email_lower ON suppliers(LOWER(email));
CREATE INDEX IF NOT EXISTS idx_suppliers_status ON suppliers(status);
CREATE INDEX IF NOT EXISTS idx_suppliers_created_at ON suppliers(created_at);
CREATE INDEX IF NOT EXISTS idx_supplier_spare_part_types_supplier_id ON supplier_spare_part_types(supplier_id);
CREATE INDEX IF NOT EXISTS idx_supplier_spare_part_types_type ON supplier_spare_part_types(spare_part_type);

-- Add constraint if not exists
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'chk_supplier_status' 
        AND table_name = 'suppliers'
    ) THEN
        ALTER TABLE suppliers ADD CONSTRAINT chk_supplier_status 
        CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'));
    END IF;
END $$;