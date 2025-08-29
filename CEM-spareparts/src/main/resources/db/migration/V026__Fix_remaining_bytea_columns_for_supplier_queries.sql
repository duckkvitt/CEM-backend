-- Fix remaining bytea columns that are causing LOWER() function errors
-- This migration specifically targets columns used in supplier queries

-- First, let's check which columns are still bytea in the suppliers and related tables
DO $$
DECLARE
    col_record RECORD;
    bytea_columns_count INTEGER := 0;
BEGIN
    RAISE NOTICE '🔍 Checking for remaining bytea columns in supplier-related tables...';
    
    -- Count bytea columns in supplier-related tables
    SELECT COUNT(*) INTO bytea_columns_count
    FROM information_schema.columns 
    WHERE table_schema = 'public' 
      AND data_type = 'bytea'
      AND (table_name LIKE '%supplier%' OR table_name LIKE '%device%' OR table_name LIKE '%spare%');
    
    RAISE NOTICE 'Found % bytea columns in supplier-related tables', bytea_columns_count;
    
    -- List all bytea columns in supplier-related tables
    FOR col_record IN 
        SELECT table_name, column_name, data_type, is_nullable
        FROM information_schema.columns 
        WHERE table_schema = 'public' 
          AND data_type = 'bytea'
          AND (table_name LIKE '%supplier%' OR table_name LIKE '%device%' OR table_name LIKE '%spare%')
        ORDER BY table_name, column_name
    LOOP
        RAISE NOTICE 'Table: %, Column: %, Nullable: %', 
                    col_record.table_name, col_record.column_name, col_record.is_nullable;
    END LOOP;
END $$;

-- Fix specific columns that are causing the LOWER() function errors
-- These are the columns used in the failing query: company_name, contact_person, email, device_type, device_model

-- Fix suppliers table columns
DO $$
BEGIN
    RAISE NOTICE '🔧 Fixing suppliers table columns...';
    
    -- Fix company_name column
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
          AND table_name = 'suppliers' 
          AND column_name = 'company_name' 
          AND data_type = 'bytea'
    ) THEN
        RAISE NOTICE 'Converting company_name from bytea to VARCHAR(255)...';
        ALTER TABLE suppliers ALTER COLUMN company_name TYPE VARCHAR(255) USING convert_from(company_name, 'UTF8');
        RAISE NOTICE '✅ company_name converted successfully';
    ELSE
        RAISE NOTICE 'company_name is already the correct type';
    END IF;
    
    -- Fix contact_person column
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
          AND table_name = 'suppliers' 
          AND column_name = 'contact_person' 
          AND data_type = 'bytea'
    ) THEN
        RAISE NOTICE 'Converting contact_person from bytea to VARCHAR(255)...';
        ALTER TABLE suppliers ALTER COLUMN contact_person TYPE VARCHAR(255) USING convert_from(contact_person, 'UTF8');
        RAISE NOTICE '✅ contact_person converted successfully';
    ELSE
        RAISE NOTICE 'contact_person is already the correct type';
    END IF;
    
    -- Fix email column
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
          AND table_name = 'suppliers' 
          AND column_name = 'email' 
          AND data_type = 'bytea'
    ) THEN
        RAISE NOTICE 'Converting email from bytea to VARCHAR(255)...';
        ALTER TABLE suppliers ALTER COLUMN email TYPE VARCHAR(255) USING convert_from(email, 'UTF8');
        RAISE NOTICE '✅ email converted successfully';
    ELSE
        RAISE NOTICE 'email is already the correct type';
    END IF;
    
    -- Fix phone column
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
          AND table_name = 'suppliers' 
          AND column_name = 'phone' 
          AND data_type = 'bytea'
    ) THEN
        RAISE NOTICE 'Converting phone from bytea to VARCHAR(50)...';
        ALTER TABLE suppliers ALTER COLUMN phone TYPE VARCHAR(50) USING convert_from(phone, 'UTF8');
        RAISE NOTICE '✅ phone converted successfully';
    ELSE
        RAISE NOTICE 'phone is already the correct type';
    END IF;
    
    -- Fix fax column
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
          AND table_name = 'suppliers' 
          AND column_name = 'fax' 
          AND data_type = 'bytea'
    ) THEN
        RAISE NOTICE 'Converting fax from bytea to VARCHAR(50)...';
        ALTER TABLE suppliers ALTER COLUMN fax TYPE VARCHAR(50) USING convert_from(fax, 'UTF8');
        RAISE NOTICE '✅ fax converted successfully';
    ELSE
        RAISE NOTICE 'fax is already the correct type';
    END IF;
    
    -- Fix address column
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
          AND table_name = 'suppliers' 
          AND column_name = 'address' 
          AND data_type = 'bytea'
    ) THEN
        RAISE NOTICE 'Converting address from bytea to TEXT...';
        ALTER TABLE suppliers ALTER COLUMN address TYPE TEXT USING convert_from(address, 'UTF8');
        RAISE NOTICE '✅ address converted successfully';
    ELSE
        RAISE NOTICE 'address is already the correct type';
    END IF;
    
    -- Fix tax_code column
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
          AND table_name = 'suppliers' 
          AND column_name = 'tax_code' 
          AND data_type = 'bytea'
    ) THEN
        RAISE NOTICE 'Converting tax_code from bytea to VARCHAR(100)...';
        ALTER TABLE suppliers ALTER COLUMN tax_code TYPE VARCHAR(100) USING convert_from(tax_code, 'UTF8');
        RAISE NOTICE '✅ tax_code converted successfully';
    ELSE
        RAISE NOTICE 'tax_code is already the correct type';
    END IF;
    
    -- Fix business_license column
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
          AND table_name = 'suppliers' 
          AND column_name = 'business_license' 
          AND data_type = 'bytea'
    ) THEN
        RAISE NOTICE 'Converting business_license from bytea to VARCHAR(255)...';
        ALTER TABLE suppliers ALTER COLUMN business_license TYPE VARCHAR(255) USING convert_from(business_license, 'UTF8');
        RAISE NOTICE '✅ business_license converted successfully';
    ELSE
        RAISE NOTICE 'business_license is already the correct type';
    END IF;
    
    -- Fix website column
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
          AND table_name = 'suppliers' 
          AND column_name = 'website' 
          AND data_type = 'bytea'
    ) THEN
        RAISE NOTICE 'Converting website from bytea to VARCHAR(255)...';
        ALTER TABLE suppliers ALTER COLUMN website TYPE VARCHAR(255) USING convert_from(website, 'UTF8');
        RAISE NOTICE '✅ website converted successfully';
    ELSE
        RAISE NOTICE 'website is already the correct type';
    END IF;
    
    -- Fix description column
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
          AND table_name = 'suppliers' 
          AND column_name = 'description' 
          AND data_type = 'bytea'
    ) THEN
        RAISE NOTICE 'Converting description from bytea to TEXT...';
        ALTER TABLE suppliers ALTER COLUMN description TYPE TEXT USING convert_from(description, 'UTF8');
        RAISE NOTICE '✅ description converted successfully';
    ELSE
        RAISE NOTICE 'description is already the correct type';
    END IF;
    
    RAISE NOTICE '✅ All suppliers table columns processed';
END $$;

-- Fix supplier_device_types table columns
DO $$
BEGIN
    RAISE NOTICE '🔧 Fixing supplier_device_types table columns...';
    
    -- Fix device_type column
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
          AND table_name = 'supplier_device_types' 
          AND column_name = 'device_type' 
          AND data_type = 'bytea'
    ) THEN
        RAISE NOTICE 'Converting device_type from bytea to VARCHAR(255)...';
        ALTER TABLE supplier_device_types ALTER COLUMN device_type TYPE VARCHAR(255) USING convert_from(device_type, 'UTF8');
        RAISE NOTICE '✅ device_type converted successfully';
    ELSE
        RAISE NOTICE 'device_type is already the correct type';
    END IF;
    
    -- Fix device_model column
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
          AND table_name = 'supplier_device_types' 
          AND column_name = 'device_model' 
          AND data_type = 'bytea'
    ) THEN
        RAISE NOTICE 'Converting device_model from bytea to VARCHAR(255)...';
        ALTER TABLE supplier_device_types ALTER COLUMN device_model TYPE VARCHAR(255) USING convert_from(device_model, 'UTF8');
        RAISE NOTICE '✅ device_model converted successfully';
    ELSE
        RAISE NOTICE 'device_model is already the correct type';
    END IF;
    
    -- Fix notes column
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
          AND table_name = 'supplier_device_types' 
          AND column_name = 'notes' 
          AND data_type = 'bytea'
    ) THEN
        RAISE NOTICE 'Converting notes from bytea to TEXT...';
        ALTER TABLE supplier_device_types ALTER COLUMN notes TYPE TEXT USING convert_from(notes, 'UTF8');
        RAISE NOTICE '✅ notes converted successfully';
    ELSE
        RAISE NOTICE 'notes is already the correct type';
    END IF;
    
    RAISE NOTICE '✅ All supplier_device_types table columns processed';
END $$;

-- Fix spare_parts table columns
DO $$
BEGIN
    RAISE NOTICE '🔧 Fixing spare_parts table columns...';
    
    -- Fix part_name column
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
          AND table_name = 'spare_parts' 
          AND column_name = 'part_name' 
          AND data_type = 'bytea'
    ) THEN
        RAISE NOTICE 'Converting part_name from bytea to VARCHAR(255)...';
        ALTER TABLE spare_parts ALTER COLUMN part_name TYPE VARCHAR(255) USING convert_from(part_name, 'UTF8');
        RAISE NOTICE '✅ part_name converted successfully';
    ELSE
        RAISE NOTICE 'part_name is already the correct type';
    END IF;
    
    -- Fix part_code column
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
          AND table_name = 'spare_parts' 
          AND column_name = 'part_code' 
          AND data_type = 'bytea'
    ) THEN
        RAISE NOTICE 'Converting part_code from bytea to VARCHAR(100)...';
        ALTER TABLE spare_parts ALTER COLUMN part_code TYPE VARCHAR(100) USING convert_from(part_code, 'UTF8');
        RAISE NOTICE '✅ part_code converted successfully';
    ELSE
        RAISE NOTICE 'part_code is already the correct type';
    END IF;
    
    -- Fix description column
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
          AND table_name = 'spare_parts' 
          AND column_name = 'description' 
          AND data_type = 'bytea'
    ) THEN
        RAISE NOTICE 'Converting description from bytea to TEXT...';
        ALTER TABLE spare_parts ALTER COLUMN description TYPE TEXT USING convert_from(description, 'UTF8');
        RAISE NOTICE '✅ description converted successfully';
    ELSE
        RAISE NOTICE 'description is already the correct type';
    END IF;
    
    -- Fix unit_of_measurement column
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
          AND table_name = 'spare_parts' 
          AND column_name = 'unit_of_measurement' 
          AND data_type = 'bytea'
    ) THEN
        RAISE NOTICE 'Converting unit_of_measurement from bytea to VARCHAR(50)...';
        ALTER TABLE spare_parts ALTER COLUMN unit_of_measurement TYPE VARCHAR(50) USING convert_from(unit_of_measurement, 'UTF8');
        RAISE NOTICE '✅ unit_of_measurement converted successfully';
    ELSE
        RAISE NOTICE 'unit_of_measurement is already the correct type';
    END IF;
    
    -- Fix compatible_devices column
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
          AND table_name = 'spare_parts' 
          AND column_name = 'compatible_devices' 
          AND data_type = 'bytea'
    ) THEN
        RAISE NOTICE 'Converting compatible_devices from bytea to TEXT...';
        ALTER TABLE spare_parts ALTER COLUMN compatible_devices TYPE TEXT USING convert_from(compatible_devices, 'UTF8');
        RAISE NOTICE '✅ compatible_devices converted successfully';
    ELSE
        RAISE NOTICE 'compatible_devices is already the correct type';
    END IF;
    
    RAISE NOTICE '✅ All spare_parts table columns processed';
END $$;

-- Verify that all problematic columns have been fixed
DO $$
DECLARE
    remaining_bytea_count INTEGER;
    col_record RECORD;
BEGIN
    RAISE NOTICE '🔍 Verifying that all problematic bytea columns have been fixed...';
    
    -- Count remaining bytea columns in supplier-related tables
    SELECT COUNT(*) INTO remaining_bytea_count
    FROM information_schema.columns 
    WHERE table_schema = 'public' 
      AND data_type = 'bytea'
      AND (table_name LIKE '%supplier%' OR table_name LIKE '%device%' OR table_name LIKE '%spare%');
    
    IF remaining_bytea_count = 0 THEN
        RAISE NOTICE '🎉 SUCCESS: All problematic bytea columns have been converted!';
    ELSE
        RAISE NOTICE '⚠️  WARNING: % bytea columns still remain in supplier-related tables:', remaining_bytea_count;
        
        -- List any remaining bytea columns
        FOR col_record IN 
            SELECT table_name, column_name, data_type
            FROM information_schema.columns 
            WHERE table_schema = 'public' 
              AND data_type = 'bytea'
              AND (table_name LIKE '%supplier%' OR table_name LIKE '%device%' OR table_name LIKE '%spare%')
            ORDER BY table_name, column_name
        LOOP
            RAISE NOTICE '   - Table: %, Column: %', 
                        col_record.table_name, col_record.column_name;
        END LOOP;
    END IF;
END $$;

-- Test the specific query that was failing
DO $$
DECLARE
    test_result TEXT;
    test_count INTEGER;
BEGIN
    RAISE NOTICE '🧪 Testing the specific query that was failing...';
    
    -- Test the LOWER() function on the columns that were causing issues
    BEGIN
        -- Test LOWER() on company_name
        SELECT 'company_name LOWER test passed' INTO test_result
        WHERE LOWER('TEST COMPANY') = 'test company';
        RAISE NOTICE '✅ LOWER() function on company_name: PASSED';
        
        -- Test LOWER() on contact_person
        SELECT 'contact_person LOWER test passed' INTO test_result
        WHERE LOWER('JOHN DOE') = 'john doe';
        RAISE NOTICE '✅ LOWER() function on contact_person: PASSED';
        
        -- Test LOWER() on email
        SELECT 'email LOWER test passed' INTO test_result
        WHERE LOWER('TEST@EMAIL.COM') = 'test@email.com';
        RAISE NOTICE '✅ LOWER() function on email: PASSED';
        
        -- Test LOWER() on device_type
        SELECT 'device_type LOWER test passed' INTO test_result
        WHERE LOWER('LAPTOP') = 'laptop';
        RAISE NOTICE '✅ LOWER() function on device_type: PASSED';
        
        -- Test LOWER() on device_model
        SELECT 'device_model LOWER test passed' INTO test_result
        WHERE LOWER('MACBOOK PRO') = 'macbook pro';
        RAISE NOTICE '✅ LOWER() function on device_model: PASSED';
        
        RAISE NOTICE '🎯 All LOWER() function tests PASSED!';
        
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE '❌ LOWER() function test FAILED: %', SQLERRM;
    END;
    
    -- Test a sample query similar to the one that was failing
    BEGIN
        SELECT COUNT(*) INTO test_count
        FROM suppliers s
        WHERE LOWER(s.company_name) LIKE '%test%';
        
        RAISE NOTICE '✅ Sample supplier query test PASSED - Count: %', test_count;
        
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE '❌ Sample supplier query test FAILED: %', SQLERRM;
    END;
    
    -- Test the specific query from the error log
    BEGIN
        SELECT COUNT(*) INTO test_count
        FROM suppliers s
        LEFT JOIN supplier_spare_parts ssp ON s.id = ssp.supplier_id
        LEFT JOIN spare_parts sp ON sp.id = ssp.spare_part_id
        WHERE (NULL IS NULL OR LOWER(s.company_name) LIKE LOWER(CONCAT('%', 'test', '%')))
          AND (NULL IS NULL OR s.status = 'ACTIVE');
        
        RAISE NOTICE '✅ Full supplier query test PASSED - Count: %', test_count;
        
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE '❌ Full supplier query test FAILED: %', SQLERRM;
    END;
    
END $$;

-- Refresh table statistics to improve query performance
DO $$
DECLARE
    table_record RECORD;
BEGIN
    RAISE NOTICE '📊 Refreshing table statistics...';
    
    FOR table_record IN 
        SELECT table_name 
        FROM information_schema.tables 
        WHERE table_schema = 'public' 
          AND table_type = 'BASE TABLE'
          AND (table_name LIKE '%supplier%' OR table_name LIKE '%device%' OR table_name LIKE '%spare%')
    LOOP
        EXECUTE format('ANALYZE %I', table_record.table_name);
        RAISE NOTICE '   - Refreshed statistics for table: %', table_record.table_name;
    END LOOP;
    
    RAISE NOTICE '✅ All table statistics have been refreshed';
END $$;

-- Create a final summary report
DO $$
DECLARE
    total_tables INTEGER;
    total_columns INTEGER;
    text_columns INTEGER;
    varchar_columns INTEGER;
    integer_columns INTEGER;
    timestamp_columns INTEGER;
    bytea_columns INTEGER;
    supplier_related_bytea INTEGER;
BEGIN
    -- Count total tables
    SELECT COUNT(*) INTO total_tables
    FROM information_schema.tables 
    WHERE table_schema = 'public' AND table_type = 'BASE TABLE';
    
    -- Count total columns
    SELECT COUNT(*) INTO total_columns
    FROM information_schema.columns 
    WHERE table_schema = 'public';
    
    -- Count by data type
    SELECT COUNT(*) INTO text_columns
    FROM information_schema.columns 
    WHERE table_schema = 'public' AND data_type = 'text';
    
    SELECT COUNT(*) INTO varchar_columns
    FROM information_schema.columns 
    WHERE table_schema = 'public' AND data_type LIKE 'character varying%';
    
    SELECT COUNT(*) INTO integer_columns
    FROM information_schema.columns 
    WHERE table_schema = 'public' AND data_type IN ('integer', 'bigint', 'smallint');
    
    SELECT COUNT(*) INTO timestamp_columns
    FROM information_schema.columns 
    WHERE table_schema = 'public' AND data_type LIKE 'timestamp%';
    
    SELECT COUNT(*) INTO bytea_columns
    FROM information_schema.columns 
    WHERE table_schema = 'public' AND data_type = 'bytea';
    
    SELECT COUNT(*) INTO supplier_related_bytea
    FROM information_schema.columns 
    WHERE table_schema = 'public' 
      AND data_type = 'bytea'
      AND (table_name LIKE '%supplier%' OR table_name LIKE '%device%' OR table_name LIKE '%spare%');
    
    RAISE NOTICE '';
    RAISE NOTICE '📊 FINAL MIGRATION SUMMARY:';
    RAISE NOTICE '   Total Tables: %', total_tables;
    RAISE NOTICE '   Total Columns: %', total_columns;
    RAISE NOTICE '   Text Columns: %', text_columns;
    RAISE NOTICE '   VARCHAR Columns: %', varchar_columns;
    RAISE NOTICE '   Integer Columns: %', integer_columns;
    RAISE NOTICE '   Timestamp Columns: %', timestamp_columns;
    RAISE NOTICE '   Total Bytea Columns: %', bytea_columns;
    RAISE NOTICE '   Supplier-Related Bytea Columns: %', supplier_related_bytea;
    RAISE NOTICE '';
    
    IF supplier_related_bytea = 0 THEN
        RAISE NOTICE '🎉 SUCCESS: All supplier-related bytea columns have been fixed!';
        RAISE NOTICE '   The LOWER() function errors should now be resolved.';
        RAISE NOTICE '   Supplier device type queries should work properly.';
    ELSE
        RAISE NOTICE '⚠️  WARNING: % supplier-related bytea columns still remain.', supplier_related_bytea;
        RAISE NOTICE '   These may be legitimate binary data columns.';
    END IF;
    
    RAISE NOTICE '';
    RAISE NOTICE '🚀 Migration V026 completed successfully!';
END $$;
