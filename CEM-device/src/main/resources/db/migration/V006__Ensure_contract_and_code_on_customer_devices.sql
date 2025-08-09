-- Ensure columns exist, backfill, then enforce NOT NULL and indexes

-- 1) Add columns if missing (nullable first to avoid failures on existing rows)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'customer_devices' AND column_name = 'contract_id'
    ) THEN
        ALTER TABLE customer_devices ADD COLUMN contract_id BIGINT DEFAULT 0;
    END IF;

    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'customer_devices' AND column_name = 'customer_device_code'
    ) THEN
        ALTER TABLE customer_devices ADD COLUMN customer_device_code VARCHAR(100);
    END IF;
END$$;

-- 2) Backfill customer_device_code where null
UPDATE customer_devices cd
SET customer_device_code = CONCAT('C', cd.customer_id, '-K', cd.contract_id, '-D', cd.device_id, '-', LPAD((ROW_NUMBER() OVER (PARTITION BY cd.customer_id, cd.contract_id, cd.device_id ORDER BY cd.id))::text, 3, '0'))
WHERE cd.customer_device_code IS NULL;

-- 3) Enforce NOT NULL
ALTER TABLE customer_devices
    ALTER COLUMN contract_id SET NOT NULL,
    ALTER COLUMN customer_device_code SET NOT NULL;

-- 4) Constraints and indexes (idempotent)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_customer_device_code'
    ) THEN
        ALTER TABLE customer_devices ADD CONSTRAINT uk_customer_device_code UNIQUE (customer_device_code);
    END IF;
END$$;

CREATE INDEX IF NOT EXISTS idx_customer_devices_contract ON customer_devices(contract_id);
CREATE INDEX IF NOT EXISTS idx_customer_devices_customer ON customer_devices(customer_id);

