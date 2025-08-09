-- Final fix: Ensure contract_id column exists in customer_devices table
-- This migration is idempotent and safe to run multiple times

DO $$
BEGIN
    -- Check if contract_id column exists, if not add it
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'customer_devices' 
        AND column_name = 'contract_id'
    ) THEN
        -- Add contract_id column as nullable first
        ALTER TABLE customer_devices ADD COLUMN contract_id BIGINT DEFAULT 0;
        
        -- Set all existing rows to have contract_id = 0 as default
        UPDATE customer_devices SET contract_id = 0 WHERE contract_id IS NULL;
        
        -- Now make it NOT NULL
        ALTER TABLE customer_devices ALTER COLUMN contract_id SET NOT NULL;
        
        RAISE NOTICE 'Added contract_id column to customer_devices table';
    ELSE
        RAISE NOTICE 'contract_id column already exists in customer_devices table';
    END IF;

    -- Check if customer_device_code column exists, if not add it
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'customer_devices' 
        AND column_name = 'customer_device_code'
    ) THEN
        -- Add customer_device_code column as nullable first
        ALTER TABLE customer_devices ADD COLUMN customer_device_code VARCHAR(100);
        
        -- Generate codes for existing rows
        UPDATE customer_devices cd
        SET customer_device_code = CONCAT('C', cd.customer_id, '-K', cd.contract_id, '-D', cd.device_id, '-', LPAD((ROW_NUMBER() OVER (PARTITION BY cd.customer_id, cd.contract_id, cd.device_id ORDER BY cd.id))::text, 3, '0'))
        WHERE cd.customer_device_code IS NULL;
        
        -- Now make it NOT NULL
        ALTER TABLE customer_devices ALTER COLUMN customer_device_code SET NOT NULL;
        
        RAISE NOTICE 'Added customer_device_code column to customer_devices table';
    ELSE
        RAISE NOTICE 'customer_device_code column already exists in customer_devices table';
    END IF;

    -- Ensure indexes exist
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes 
        WHERE tablename = 'customer_devices' 
        AND indexname = 'idx_customer_devices_contract'
    ) THEN
        CREATE INDEX idx_customer_devices_contract ON customer_devices(contract_id);
        RAISE NOTICE 'Created index idx_customer_devices_contract';
    END IF;

    -- Ensure unique constraint exists for customer_device_code
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'uk_customer_device_code'
    ) THEN
        ALTER TABLE customer_devices ADD CONSTRAINT uk_customer_device_code UNIQUE (customer_device_code);
        RAISE NOTICE 'Created unique constraint uk_customer_device_code';
    END IF;

END$$;