-- Migration: Create spare_part_inventory table
-- This migration creates the spare_part_inventory table for managing spare part stock levels
-- and inventory information within the CEM-spareparts service

-- Create spare_part_inventory table
CREATE TABLE IF NOT EXISTS spare_part_inventory (
    id BIGSERIAL PRIMARY KEY,
    spare_part_id BIGINT NOT NULL,
    quantity_in_stock INTEGER NOT NULL DEFAULT 0,
    minimum_stock_level INTEGER NOT NULL DEFAULT 5,
    maximum_stock_level INTEGER NOT NULL DEFAULT 100,
    reorder_point INTEGER NOT NULL DEFAULT 10,
    unit_cost DECIMAL(10,2),
    warehouse_location VARCHAR(255) DEFAULT 'Main Warehouse',
    notes TEXT,
    created_by VARCHAR(100) NOT NULL DEFAULT 'system',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Constraints
    CONSTRAINT fk_spare_part_inventory_spare_part 
        FOREIGN KEY (spare_part_id) 
        REFERENCES spare_parts(id) 
        ON DELETE CASCADE,
    CONSTRAINT chk_quantity_in_stock_positive 
        CHECK (quantity_in_stock >= 0),
    CONSTRAINT chk_minimum_stock_level_positive 
        CHECK (minimum_stock_level >= 0),
    CONSTRAINT chk_maximum_stock_level_greater_than_minimum 
        CHECK (maximum_stock_level > minimum_stock_level),
    CONSTRAINT chk_reorder_point_between_min_max 
        CHECK (reorder_point >= minimum_stock_level AND reorder_point <= maximum_stock_level)
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_spare_part_inventory_spare_part_id 
    ON spare_part_inventory(spare_part_id);
CREATE INDEX IF NOT EXISTS idx_spare_part_inventory_quantity 
    ON spare_part_inventory(quantity_in_stock);
CREATE INDEX IF NOT EXISTS idx_spare_part_inventory_low_stock 
    ON spare_part_inventory(quantity_in_stock, minimum_stock_level);
CREATE INDEX IF NOT EXISTS idx_spare_part_inventory_warehouse 
    ON spare_part_inventory(warehouse_location);
CREATE INDEX IF NOT EXISTS idx_spare_part_inventory_created_at 
    ON spare_part_inventory(created_at);

-- Create unique constraint to ensure one inventory record per spare part
CREATE UNIQUE INDEX IF NOT EXISTS idx_spare_part_inventory_unique_spare_part 
    ON spare_part_inventory(spare_part_id);

-- Add comments for documentation
COMMENT ON TABLE spare_part_inventory IS 'Stores inventory information for spare parts including stock levels, reorder points, and warehouse locations';
COMMENT ON COLUMN spare_part_inventory.id IS 'Primary key for the inventory record';
COMMENT ON COLUMN spare_part_inventory.spare_part_id IS 'Foreign key reference to the spare part';
COMMENT ON COLUMN spare_part_inventory.quantity_in_stock IS 'Current quantity available in stock';
COMMENT ON COLUMN spare_part_inventory.minimum_stock_level IS 'Minimum stock level before reordering is needed';
COMMENT ON COLUMN spare_part_inventory.maximum_stock_level IS 'Maximum stock level for storage capacity';
COMMENT ON COLUMN spare_part_inventory.reorder_point IS 'Stock level at which reordering should be triggered';
COMMENT ON COLUMN spare_part_inventory.unit_cost IS 'Cost per unit of the spare part';
COMMENT ON COLUMN spare_part_inventory.warehouse_location IS 'Physical location of the spare part in the warehouse';
COMMENT ON COLUMN spare_part_inventory.notes IS 'Additional notes about the inventory record';
COMMENT ON COLUMN spare_part_inventory.created_by IS 'User or system that created the record';
COMMENT ON COLUMN spare_part_inventory.created_at IS 'Timestamp when the record was created';
COMMENT ON COLUMN spare_part_inventory.updated_at IS 'Timestamp when the record was last updated';
