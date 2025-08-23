-- Fix encoding issues in warehouse-related tables
-- This migration ensures all text columns are properly typed as text/varchar, not bytea

-- First, let's check what columns actually exist and their current types
DO $$
DECLARE
    col_record RECORD;
BEGIN
    RAISE NOTICE 'Checking column types for spare_parts_inventory_transactions table...';
    
    FOR col_record IN 
        SELECT column_name, data_type, is_nullable
        FROM information_schema.columns 
        WHERE table_name = 'spare_parts_inventory_transactions'
        ORDER BY column_name
    LOOP
        RAISE NOTICE 'Column: %, Type: %, Nullable: %', col_record.column_name, col_record.data_type, col_record.is_nullable;
    END LOOP;
END $$;

-- Fix spare_parts_inventory_transactions table - direct approach
-- Convert bytea columns to proper text types

-- Fix transaction_number column
ALTER TABLE spare_parts_inventory_transactions 
ALTER COLUMN transaction_number TYPE VARCHAR(50) 
USING CASE 
    WHEN transaction_number IS NULL THEN NULL 
    ELSE convert_from(transaction_number, 'UTF8')
END;

-- Fix transaction_type column  
ALTER TABLE spare_parts_inventory_transactions 
ALTER COLUMN transaction_type TYPE VARCHAR(50) 
USING CASE 
    WHEN transaction_type IS NULL THEN NULL 
    ELSE convert_from(transaction_type, 'UTF8')
END;

-- Fix reference_type column
ALTER TABLE spare_parts_inventory_transactions 
ALTER COLUMN reference_type TYPE VARCHAR(50) 
USING CASE 
    WHEN reference_type IS NULL THEN NULL 
    ELSE convert_from(reference_type, 'UTF8')
END;

-- Fix transaction_reason column
ALTER TABLE spare_parts_inventory_transactions 
ALTER COLUMN transaction_reason TYPE TEXT 
USING CASE 
    WHEN transaction_reason IS NULL THEN NULL 
    ELSE convert_from(transaction_reason, 'UTF8')
END;

-- Fix created_by column
ALTER TABLE spare_parts_inventory_transactions 
ALTER COLUMN created_by TYPE VARCHAR(255) 
USING CASE 
    WHEN created_by IS NULL THEN NULL 
    ELSE convert_from(created_by, 'UTF8')
END;

-- Fix spare_parts_inventory table
ALTER TABLE spare_parts_inventory 
ALTER COLUMN last_updated_by TYPE VARCHAR(255) 
USING CASE 
    WHEN last_updated_by IS NULL THEN NULL 
    ELSE convert_from(last_updated_by, 'UTF8')
END;

-- Fix spare_parts_import_requests table
ALTER TABLE spare_parts_import_requests 
ALTER COLUMN request_number TYPE VARCHAR(50) 
USING CASE 
    WHEN request_number IS NULL THEN NULL 
    ELSE convert_from(request_number, 'UTF8')
END;

ALTER TABLE spare_parts_import_requests 
ALTER COLUMN request_status TYPE VARCHAR(50) 
USING CASE 
    WHEN request_status IS NULL THEN 'PENDING'
    ELSE convert_from(request_status, 'UTF8')
END;

ALTER TABLE spare_parts_import_requests 
ALTER COLUMN approval_status TYPE VARCHAR(50) 
USING CASE 
    WHEN approval_status IS NULL THEN NULL 
    ELSE convert_from(approval_status, 'UTF8')
END;

ALTER TABLE spare_parts_import_requests 
ALTER COLUMN request_reason TYPE TEXT 
USING CASE 
    WHEN request_reason IS NULL THEN NULL 
    ELSE convert_from(request_reason, 'UTF8')
END;

ALTER TABLE spare_parts_import_requests 
ALTER COLUMN approval_reason TYPE TEXT 
USING CASE 
    WHEN approval_reason IS NULL THEN NULL 
    ELSE convert_from(approval_reason, 'UTF8')
END;

ALTER TABLE spare_parts_import_requests 
ALTER COLUMN requested_by TYPE VARCHAR(255) 
USING CASE 
    WHEN requested_by IS NULL THEN NULL 
    ELSE convert_from(requested_by, 'UTF8')
END;

ALTER TABLE spare_parts_import_requests 
ALTER COLUMN reviewed_by TYPE VARCHAR(255) 
USING CASE 
    WHEN reviewed_by IS NULL THEN NULL 
    ELSE convert_from(reviewed_by, 'UTF8')
END;

ALTER TABLE spare_parts_import_requests 
ALTER COLUMN invoice_number TYPE VARCHAR(100) 
USING CASE 
    WHEN invoice_number IS NULL THEN NULL 
    ELSE convert_from(invoice_number, 'UTF8')
END;

ALTER TABLE spare_parts_import_requests 
ALTER COLUMN notes TYPE TEXT 
USING CASE 
    WHEN notes IS NULL THEN NULL 
    ELSE convert_from(notes, 'UTF8')
END;

-- Fix spare_parts_export_requests table
ALTER TABLE spare_parts_export_requests 
ALTER COLUMN request_number TYPE VARCHAR(50) 
USING CASE 
    WHEN request_number IS NULL THEN NULL 
    ELSE convert_from(request_number, 'UTF8')
END;

ALTER TABLE spare_parts_export_requests 
ALTER COLUMN request_status TYPE VARCHAR(50) 
USING CASE 
    WHEN request_status IS NULL THEN 'PENDING'
    ELSE convert_from(request_status, 'UTF8')
END;

ALTER TABLE spare_parts_export_requests 
ALTER COLUMN approval_status TYPE VARCHAR(50) 
USING CASE 
    WHEN approval_status IS NULL THEN NULL 
    ELSE convert_from(approval_status, 'UTF8')
END;

ALTER TABLE spare_parts_export_requests 
ALTER COLUMN request_reason TYPE TEXT 
USING CASE 
    WHEN request_reason IS NULL THEN NULL 
    ELSE convert_from(request_reason, 'UTF8')
END;

ALTER TABLE spare_parts_export_requests 
ALTER COLUMN approval_reason TYPE TEXT 
USING CASE 
    WHEN approval_reason IS NULL THEN NULL 
    ELSE convert_from(approval_reason, 'UTF8')
END;

ALTER TABLE spare_parts_export_requests 
ALTER COLUMN requested_by TYPE VARCHAR(255) 
USING CASE 
    WHEN requested_by IS NULL THEN NULL 
    ELSE convert_from(requested_by, 'UTF8')
END;

ALTER TABLE spare_parts_export_requests 
ALTER COLUMN reviewed_by TYPE VARCHAR(255) 
USING CASE 
    WHEN reviewed_by IS NULL THEN NULL 
    ELSE convert_from(reviewed_by, 'UTF8')
END;

ALTER TABLE spare_parts_export_requests 
ALTER COLUMN issued_by TYPE VARCHAR(255) 
USING CASE 
    WHEN issued_by IS NULL THEN NULL 
    ELSE convert_from(issued_by, 'UTF8')
END;

ALTER TABLE spare_parts_export_requests 
ALTER COLUMN notes TYPE TEXT 
USING CASE 
    WHEN notes IS NULL THEN NULL 
    ELSE convert_from(notes, 'UTF8')
END;

-- Verify the changes
DO $$
DECLARE
    col_record RECORD;
BEGIN
    RAISE NOTICE 'Verifying column types after migration...';
    
    FOR col_record IN 
        SELECT column_name, data_type, is_nullable
        FROM information_schema.columns 
        WHERE table_name = 'spare_parts_inventory_transactions'
        ORDER BY column_name
    LOOP
        RAISE NOTICE 'Column: %, Type: %, Nullable: %', col_record.column_name, col_record.data_type, col_record.is_nullable;
    END LOOP;
END $$;

-- Refresh statistics for query planner
ANALYZE spare_parts_inventory_transactions;
ANALYZE spare_parts_inventory;
ANALYZE spare_parts_import_requests;
ANALYZE spare_parts_export_requests;
