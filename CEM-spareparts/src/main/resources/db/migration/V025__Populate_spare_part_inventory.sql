-- Migration: Populate spare_part_inventory table with initial data
-- This migration populates the spare_part_inventory table with default inventory records
-- for all existing spare parts that don't already have inventory records

-- Insert default inventory records for spare parts that don't have inventory yet
INSERT INTO spare_part_inventory (spare_part_id, quantity_in_stock, minimum_stock_level, maximum_stock_level, reorder_point, unit_cost, warehouse_location, notes, created_by, created_at, updated_at)
SELECT 
    sp.id as spare_part_id,
    0 as quantity_in_stock, -- Start with 0 stock
    5 as minimum_stock_level, -- Default minimum stock level
    100 as maximum_stock_level, -- Default maximum stock level
    10 as reorder_point, -- Default reorder point
    NULL as unit_cost, -- No cost information initially
    'Main Warehouse' as warehouse_location, -- Default warehouse location
    'Initial inventory record created by migration' as notes,
    'system' as created_by,
    CURRENT_TIMESTAMP as created_at,
    CURRENT_TIMESTAMP as updated_at
FROM spare_parts sp
WHERE sp.id NOT IN (
    SELECT spi.spare_part_id 
    FROM spare_part_inventory spi
);

-- Update existing inventory records to have default values if they're missing
UPDATE spare_part_inventory 
SET 
    minimum_stock_level = COALESCE(minimum_stock_level, 5),
    maximum_stock_level = COALESCE(maximum_stock_level, 100),
    reorder_point = COALESCE(reorder_point, 10),
    warehouse_location = COALESCE(warehouse_location, 'Main Warehouse'),
    updated_at = CURRENT_TIMESTAMP
WHERE 
    minimum_stock_level IS NULL 
    OR maximum_stock_level IS NULL 
    OR reorder_point IS NULL 
    OR warehouse_location IS NULL;


