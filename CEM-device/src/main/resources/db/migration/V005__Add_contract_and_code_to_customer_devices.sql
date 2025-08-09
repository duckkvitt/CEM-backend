-- Add contract_id and customer_device_code to customer_devices; add indexes and unique constraint
ALTER TABLE customer_devices
    ADD COLUMN IF NOT EXISTS contract_id BIGINT DEFAULT 0,
    ADD COLUMN IF NOT EXISTS customer_device_code VARCHAR(100);

-- Backfill codes for existing rows if null
UPDATE customer_devices cd
SET customer_device_code = CONCAT('C', cd.customer_id, '-K', cd.contract_id, '-D', cd.device_id, '-', LPAD((ROW_NUMBER() OVER (PARTITION BY cd.customer_id, cd.contract_id, cd.device_id ORDER BY cd.id))::text, 3, '0'))
WHERE cd.customer_device_code IS NULL;

-- Ensure columns are NOT NULL after backfill
ALTER TABLE customer_devices
    ALTER COLUMN contract_id SET NOT NULL,
    ALTER COLUMN customer_device_code SET NOT NULL;

-- Add unique constraint and indexes
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
