-- Remove quantity_in_stock and supplier columns from spare_parts table
ALTER TABLE spare_parts 
DROP COLUMN quantity_in_stock,
DROP COLUMN supplier;