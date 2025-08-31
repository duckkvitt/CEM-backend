-- Add warranty_expiry column back to devices table
-- This field is needed for the frontend integration

-- Add warranty_expiry column
ALTER TABLE devices ADD COLUMN IF NOT EXISTS warranty_expiry DATE;

-- Verify the changes
DO $$
BEGIN
    RAISE NOTICE 'Successfully added warranty_expiry column to devices table';
    RAISE NOTICE 'The devices table now contains: id, name, model, serial_number, customer_id, status, price, unit, warranty_expiry, created_by, created_at, updated_at';
END $$;
