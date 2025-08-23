-- Remove quantity_in_stock column from spare_parts table
-- This column was moved to spare_parts_inventory table for better inventory management
-- The column is no longer used in the SparePart entity and causes constraint violations

-- Check if the column exists before trying to remove it
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'spare_parts' 
        AND column_name = 'quantity_in_stock'
    ) THEN
        ALTER TABLE spare_parts DROP COLUMN quantity_in_stock;
        RAISE NOTICE 'Successfully removed quantity_in_stock column from spare_parts table';
    ELSE
        RAISE NOTICE 'quantity_in_stock column does not exist in spare_parts table';
    END IF;
END $$;

-- Verify the table structure
-- The spare_parts table should now only contain:
-- id, part_name, part_code, description, compatible_devices, unit_of_measurement, status, created_at, updated_at
