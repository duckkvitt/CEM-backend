-- Fix spare parts table column types for PostgreSQL compatibility
-- Ensure all text columns are properly typed as text/varchar, not bytea

-- First, check if spare_parts table exists, if not create it with proper types
CREATE TABLE IF NOT EXISTS spare_parts
(
    id                    BIGSERIAL PRIMARY KEY,
    part_name             VARCHAR(255) NOT NULL,
    part_code             VARCHAR(255) NOT NULL UNIQUE,
    description           TEXT,
    compatible_devices    VARCHAR(255),
    unit_of_measurement   VARCHAR(255) NOT NULL,
    status                VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP,
    CONSTRAINT chk_spare_part_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

-- If the table already exists but has wrong column types, fix them
DO $$
BEGIN
    -- Check and fix part_name column type if needed
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'spare_parts' 
        AND column_name = 'part_name' 
        AND data_type = 'bytea'
    ) THEN
        ALTER TABLE spare_parts ALTER COLUMN part_name TYPE VARCHAR(255) 
        USING CASE 
            WHEN part_name IS NULL THEN NULL 
            ELSE convert_from(part_name, 'UTF8')
        END;
    END IF;
    
    -- Check and fix part_code column type if needed
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'spare_parts' 
        AND column_name = 'part_code' 
        AND data_type = 'bytea'
    ) THEN
        ALTER TABLE spare_parts ALTER COLUMN part_code TYPE VARCHAR(255) 
        USING CASE 
            WHEN part_code IS NULL THEN NULL 
            ELSE convert_from(part_code, 'UTF8')
        END;
    END IF;
    
    -- Check and fix description column type if needed
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'spare_parts' 
        AND column_name = 'description' 
        AND data_type = 'bytea'
    ) THEN
        ALTER TABLE spare_parts ALTER COLUMN description TYPE TEXT 
        USING CASE 
            WHEN description IS NULL THEN NULL 
            ELSE convert_from(description, 'UTF8')
        END;
    END IF;
    
    -- Check and fix compatible_devices column type if needed
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'spare_parts' 
        AND column_name = 'compatible_devices' 
        AND data_type = 'bytea'
    ) THEN
        ALTER TABLE spare_parts ALTER COLUMN compatible_devices TYPE VARCHAR(255) 
        USING CASE 
            WHEN compatible_devices IS NULL THEN NULL 
            ELSE convert_from(compatible_devices, 'UTF8')
        END;
    END IF;
    
    -- Check and fix unit_of_measurement column type if needed
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'spare_parts' 
        AND column_name = 'unit_of_measurement' 
        AND data_type = 'bytea'
    ) THEN
        ALTER TABLE spare_parts ALTER COLUMN unit_of_measurement TYPE VARCHAR(255) 
        USING CASE 
            WHEN unit_of_measurement IS NULL THEN NULL 
            ELSE convert_from(unit_of_measurement, 'UTF8')
        END;
    END IF;
    
    -- Check and fix status column type if needed
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'spare_parts' 
        AND column_name = 'status' 
        AND data_type = 'bytea'
    ) THEN
        ALTER TABLE spare_parts ALTER COLUMN status TYPE VARCHAR(50) 
        USING CASE 
            WHEN status IS NULL THEN 'ACTIVE'
            ELSE convert_from(status, 'UTF8')
        END;
    END IF;
END $$;

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_spare_parts_part_name_lower ON spare_parts(LOWER(part_name));
CREATE INDEX IF NOT EXISTS idx_spare_parts_part_code_lower ON spare_parts(LOWER(part_code));
CREATE INDEX IF NOT EXISTS idx_spare_parts_description_lower ON spare_parts(LOWER(description));
CREATE INDEX IF NOT EXISTS idx_spare_parts_compatible_devices_lower ON spare_parts(LOWER(compatible_devices));
CREATE INDEX IF NOT EXISTS idx_spare_parts_status ON spare_parts(status);
CREATE INDEX IF NOT EXISTS idx_spare_parts_created_at ON spare_parts(created_at);

-- Refresh statistics for query planner
ANALYZE spare_parts;