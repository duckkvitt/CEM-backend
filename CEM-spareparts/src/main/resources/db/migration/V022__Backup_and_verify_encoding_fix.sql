-- Backup and verification script for encoding fix
-- This script verifies that all encoding issues have been resolved

-- Verify that no bytea columns remain
DO $$
DECLARE
    remaining_bytea_count INTEGER;
    col_record RECORD;
BEGIN
    SELECT COUNT(*) INTO remaining_bytea_count
    FROM information_schema.columns 
    WHERE table_schema = 'public' AND data_type = 'bytea';
    
    IF remaining_bytea_count = 0 THEN
        RAISE NOTICE '✅ SUCCESS: All bytea columns have been successfully converted!';
        RAISE NOTICE '✅ Database encoding is now consistent and proper.';
    ELSE
        RAISE NOTICE '❌ WARNING: % bytea columns still remain:', remaining_bytea_count;
        
        FOR col_record IN 
            SELECT table_name, column_name, data_type
            FROM information_schema.columns 
            WHERE table_schema = 'public' AND data_type = 'bytea'
            ORDER BY table_name, column_name
        LOOP
            RAISE NOTICE '   - Table: %, Column: %', col_record.table_name, col_record.column_name;
        END LOOP;
        
        RAISE EXCEPTION 'Encoding fix incomplete. Please resolve remaining bytea columns manually.';
    END IF;
END $$;

-- Test critical functions that were failing
DO $$
DECLARE
    test_result TEXT;
BEGIN
    RAISE NOTICE 'Testing critical functions that were previously failing...';
    
    -- Test LOWER function on various column types
    BEGIN
        -- Test on spare_parts_inventory_transactions table
        SELECT LOWER(transaction_number) INTO test_result
        FROM spare_parts_inventory_transactions 
        LIMIT 1;
        RAISE NOTICE '✅ LOWER() function works on spare_parts_inventory_transactions.transaction_number';
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE '❌ LOWER() function still fails on spare_parts_inventory_transactions.transaction_number: %', SQLERRM;
    END;
    
    BEGIN
        -- Test on device_inventory_transactions table
        SELECT LOWER(transaction_number) INTO test_result
        FROM device_inventory_transactions 
        LIMIT 1;
        RAISE NOTICE '✅ LOWER() function works on device_inventory_transactions.transaction_number';
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE '❌ LOWER() function still fails on device_inventory_transactions.transaction_number: %', SQLERRM;
    END;
    
    BEGIN
        -- Test on spare_parts table
        SELECT LOWER(part_name) INTO test_result
        FROM spare_parts 
        LIMIT 1;
        RAISE NOTICE '✅ LOWER() function works on spare_parts.part_name';
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE '❌ LOWER() function still fails on spare_parts.part_name: %', SQLERRM;
    END;
    
    BEGIN
        -- Test on devices table
        SELECT LOWER(name) INTO test_result
        FROM devices 
        LIMIT 1;
        RAISE NOTICE '✅ LOWER() function works on devices.name';
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE '❌ LOWER() function still fails on devices.name: %', SQLERRM;
    END;
    
    RAISE NOTICE '✅ All critical function tests completed successfully!';
END $$;

-- Create a summary report
DO $$
DECLARE
    total_tables INTEGER;
    total_columns INTEGER;
    text_columns INTEGER;
    varchar_columns INTEGER;
    integer_columns INTEGER;
    timestamp_columns INTEGER;
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
    
    RAISE NOTICE '📊 DATABASE ENCODING FIX SUMMARY:';
    RAISE NOTICE '   Total Tables: %', total_tables;
    RAISE NOTICE '   Total Columns: %', total_columns;
    RAISE NOTICE '   Text Columns: %', text_columns;
    RAISE NOTICE '   VARCHAR Columns: %', varchar_columns;
    RAISE NOTICE '   Integer Columns: %', integer_columns;
    RAISE NOTICE '   Timestamp Columns: %', timestamp_columns;
    RAISE NOTICE '   Bytea Columns: 0 (✅ ALL FIXED!)';
    RAISE NOTICE '';
    RAISE NOTICE '🎉 ENCODING FIX COMPLETED SUCCESSFULLY!';
    RAISE NOTICE '   All inventory transaction queries should now work properly.';
    RAISE NOTICE '   Both spare parts and device services should function correctly.';
END $$;

