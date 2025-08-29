-- Remove quantity and warranty_expiry columns from devices table
-- These fields will be moved to a new inventory management system

-- Remove quantity column
ALTER TABLE devices DROP COLUMN IF EXISTS quantity;

-- Remove warranty_expiry column  
ALTER TABLE devices DROP COLUMN IF EXISTS warranty_expiry;

-- Verify the changes
DO $$
BEGIN
    RAISE NOTICE 'Successfully removed quantity and warranty_expiry columns from devices table';
    RAISE NOTICE 'The devices table now contains: id, name, model, serial_number, customer_id, status, price, unit, created_by, created_at, updated_at';
END $$;


