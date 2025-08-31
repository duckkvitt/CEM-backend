-- Migration: Ensure all spare parts have inventory records
-- This migration creates default inventory records for any spare parts that don't have them
-- This prevents race conditions when multiple requests try to create inventory simultaneously

-- Insert default inventory records for spare parts that don't have them
INSERT INTO spare_part_inventory (spare_part_id, quantity_in_stock, minimum_stock_level, maximum_stock_level, reorder_point, warehouse_location, created_by, created_at, updated_at)
SELECT 
    sp.id,
    0, -- Default quantity in stock
    5, -- Default minimum stock level
    100, -- Default maximum stock level
    10, -- Default reorder point
    'Main Warehouse', -- Default warehouse location
    'System', -- Default created by
    CURRENT_TIMESTAMP, -- Created at
    CURRENT_TIMESTAMP -- Updated at
FROM spare_parts sp
WHERE NOT EXISTS (
    SELECT 1 FROM spare_part_inventory spi WHERE spi.spare_part_id = sp.id
);

-- Log the results
DO $$
DECLARE
    inserted_count INTEGER;
    total_spare_parts INTEGER;
BEGIN
    -- Get count of inserted records
    GET DIAGNOSTICS inserted_count = ROW_COUNT;
    
    -- Get total count of spare parts
    SELECT COUNT(*) INTO total_spare_parts FROM spare_parts;
    
    RAISE NOTICE 'Migration completed successfully:';
    RAISE NOTICE '- Total spare parts: %', total_spare_parts;
    RAISE NOTICE '- New inventory records created: %', inserted_count;
    RAISE NOTICE '- Existing inventory records: %', (total_spare_parts - inserted_count);
    
    -- Verify the results
    IF inserted_count > 0 THEN
        RAISE NOTICE 'All spare parts now have inventory records';
    ELSE
        RAISE NOTICE 'No new inventory records were needed - all spare parts already have inventory';
    END IF;
END $$;

-- Verify the constraint is working properly
DO $$
DECLARE
    duplicate_count INTEGER;
BEGIN
    -- Check for any duplicate spare_part_id entries
    SELECT COUNT(*) INTO duplicate_count
    FROM (
        SELECT spare_part_id, COUNT(*) as cnt
        FROM spare_part_inventory
        GROUP BY spare_part_id
        HAVING COUNT(*) > 1
    ) duplicates;
    
    IF duplicate_count > 0 THEN
        RAISE EXCEPTION 'Found % duplicate spare_part_id entries in spare_part_inventory table', duplicate_count;
    ELSE
        RAISE NOTICE 'Unique constraint verification passed - no duplicate spare_part_id entries found';
    END IF;
END $$;
