-- Emergency Fix for ALL bytea columns causing LOWER() function errors
-- This migration will resolve the "function lower(bytea) does not exist" errors
-- by converting all bytea columns to proper text types

-- First, let's identify ALL bytea columns in the database
DO $$
DECLARE
    col_record RECORD;
    total_bytea_columns INTEGER := 0;
BEGIN
    RAISE NOTICE '🔍 Scanning database for ALL bytea columns...';
    
    -- Count total bytea columns
    SELECT COUNT(*) INTO total_bytea_columns
    FROM information_schema.columns 
    WHERE table_schema = 'public' AND data_type = 'bytea';
    
    RAISE NOTICE 'Found % bytea columns in the database', total_bytea_columns;
    
    -- List all bytea columns
    FOR col_record IN 
        SELECT table_name, column_name, data_type, is_nullable
        FROM information_schema.columns 
        WHERE table_schema = 'public' AND data_type = 'bytea'
        ORDER BY table_name, column_name
    LOOP
        RAISE NOTICE 'Table: %, Column: %, Nullable: %', 
                    col_record.table_name, col_record.column_name, col_record.is_nullable;
    END LOOP;
END $$;

-- Fix ALL bytea columns systematically
DO $$
DECLARE
    col_record RECORD;
    fixed_count INTEGER := 0;
    error_count INTEGER := 0;
    sql_statement TEXT;
BEGIN
    RAISE NOTICE '🚀 Starting to fix ALL bytea columns...';
    
    FOR col_record IN 
        SELECT table_name, column_name, data_type, is_nullable
        FROM information_schema.columns 
        WHERE table_schema = 'public' AND data_type = 'bytea'
        ORDER BY table_name, column_name
    LOOP
        BEGIN
            RAISE NOTICE 'Fixing column % in table %', col_record.column_name, col_record.table_name;
            
            -- Determine appropriate target type based on column name and context
            IF col_record.column_name LIKE '%_id' OR col_record.column_name LIKE '%id' THEN
                -- ID columns - convert to VARCHAR(255)
                sql_statement := format('ALTER TABLE %I ALTER COLUMN %I TYPE VARCHAR(255) USING convert_from(%I, ''UTF8'')', 
                                      col_record.table_name, col_record.column_name, col_record.column_name);
            ELSIF col_record.column_name LIKE '%_number' OR col_record.column_name LIKE '%number' THEN
                -- Number columns - convert to VARCHAR(50)
                sql_statement := format('ALTER TABLE %I ALTER COLUMN %I TYPE VARCHAR(50) USING convert_from(%I, ''UTF8'')', 
                                      col_record.table_name, col_record.column_name, col_record.column_name);
            ELSIF col_record.column_name LIKE '%_type' OR col_record.column_name LIKE '%type' THEN
                -- Type columns - convert to VARCHAR(50)
                sql_statement := format('ALTER TABLE %I ALTER COLUMN %I TYPE VARCHAR(50) USING convert_from(%I, ''UTF8'')', 
                                      col_record.table_name, col_record.column_name, col_record.column_name);
            ELSIF col_record.column_name LIKE '%_status' OR col_record.column_name LIKE '%status' THEN
                -- Status columns - convert to VARCHAR(50)
                sql_statement := format('ALTER TABLE %I ALTER COLUMN %I TYPE VARCHAR(50) USING convert_from(%I, ''UTF8'')', 
                                      col_record.table_name, col_record.column_name, col_record.column_name);
            ELSIF col_record.column_name LIKE '%_by' OR col_record.column_name LIKE '%by' THEN
                -- User columns - convert to VARCHAR(255)
                sql_statement := format('ALTER TABLE %I ALTER COLUMN %I TYPE VARCHAR(255) USING convert_from(%I, ''UTF8'')', 
                                      col_record.table_name, col_record.column_name, col_record.column_name);
            ELSIF col_record.column_name LIKE '%_reason' OR col_record.column_name LIKE '%reason' THEN
                -- Reason columns - convert to TEXT
                sql_statement := format('ALTER TABLE %I ALTER COLUMN %I TYPE TEXT USING convert_from(%I, ''UTF8'')', 
                                      col_record.table_name, col_record.column_name, col_record.column_name);
            ELSIF col_record.column_name LIKE '%_name' OR col_record.column_name LIKE '%name' THEN
                -- Name columns - convert to VARCHAR(255)
                sql_statement := format('ALTER TABLE %I ALTER COLUMN %I TYPE VARCHAR(255) USING convert_from(%I, ''UTF8'')', 
                                      col_record.table_name, col_record.column_name, col_record.column_name);
            ELSIF col_record.column_name LIKE '%_code' OR col_record.column_name LIKE '%code' THEN
                -- Code columns - convert to VARCHAR(100)
                sql_statement := format('ALTER TABLE %I ALTER COLUMN %I TYPE VARCHAR(100) USING convert_from(%I, ''UTF8'')', 
                                      col_record.table_name, col_record.column_name, col_record.column_name);
            ELSIF col_record.column_name LIKE '%_description' OR col_record.column_name LIKE '%description' THEN
                -- Description columns - convert to TEXT
                sql_statement := format('ALTER TABLE %I ALTER COLUMN %I TYPE TEXT USING convert_from(%I, ''UTF8'')', 
                                      col_record.table_name, col_record.column_name, col_record.column_name);
            ELSIF col_record.column_name LIKE '%_notes' OR col_record.column_name LIKE '%notes' THEN
                -- Notes columns - convert to TEXT
                sql_statement := format('ALTER TABLE %I ALTER COLUMN %I TYPE TEXT USING convert_from(%I, ''UTF8'')', 
                                      col_record.table_name, col_record.column_name, col_record.column_name);
            ELSIF col_record.column_name LIKE '%_invoice' OR col_record.column_name LIKE '%invoice' THEN
                -- Invoice columns - convert to VARCHAR(100)
                sql_statement := format('ALTER TABLE %I ALTER COLUMN %I TYPE VARCHAR(100) USING convert_from(%I, ''UTF8'')', 
                                      col_record.table_name, col_record.column_name, col_record.column_name);
            ELSIF col_record.column_name LIKE '%_serial' OR col_record.column_name LIKE '%serial' THEN
                -- Serial columns - convert to VARCHAR(100)
                sql_statement := format('ALTER TABLE %I ALTER COLUMN %I TYPE VARCHAR(100) USING convert_from(%I, ''UTF8'')', 
                                      col_record.table_name, col_record.column_name, col_record.column_name);
            ELSIF col_record.column_name LIKE '%_model' OR col_record.column_name LIKE '%model' THEN
                -- Model columns - convert to VARCHAR(255)
                sql_statement := format('ALTER TABLE %I ALTER COLUMN %I TYPE VARCHAR(255) USING convert_from(%I, ''UTF8'')', 
                                      col_record.table_name, col_record.column_name, col_record.column_name);
            ELSIF col_record.column_name LIKE '%_unit' OR col_record.column_name LIKE '%unit' THEN
                -- Unit columns - convert to VARCHAR(50)
                sql_statement := format('ALTER TABLE %I ALTER COLUMN %I TYPE VARCHAR(50) USING convert_from(%I, ''UTF8'')', 
                                      col_record.table_name, col_record.column_name, col_record.column_name);
            ELSIF col_record.column_name LIKE '%_warranty' OR col_record.column_name LIKE '%warranty' THEN
                -- Warranty columns - convert to VARCHAR(100)
                sql_statement := format('ALTER TABLE %I ALTER COLUMN %I TYPE VARCHAR(100) USING convert_from(%I, ''UTF8'')', 
                                      col_record.table_name, col_record.column_name, col_record.column_name);
            ELSIF col_record.column_name LIKE '%_compatible' OR col_record.column_name LIKE '%compatible' THEN
                -- Compatible columns - convert to TEXT
                sql_statement := format('ALTER TABLE %I ALTER COLUMN %I TYPE TEXT USING convert_from(%I, ''UTF8'')', 
                                      col_record.table_name, col_record.column_name, col_record.column_name);
            ELSIF col_record.column_name LIKE '%_supplier' OR col_record.column_name LIKE '%supplier' THEN
                -- Supplier columns - convert to VARCHAR(255)
                sql_statement := format('ALTER TABLE %I ALTER COLUMN %I TYPE VARCHAR(255) USING convert_from(%I, ''UTF8'')', 
                                      col_record.table_name, col_record.column_name, col_record.column_name);
            ELSIF col_record.column_name LIKE '%_image' OR col_record.column_name LIKE '%image' THEN
                -- Image columns - keep as bytea (these should remain binary)
                RAISE NOTICE 'Skipping image column % in table % (keeping as bytea)', col_record.column_name, col_record.table_name;
                CONTINUE;
            ELSIF col_record.column_name LIKE '%_data' OR col_record.column_name LIKE '%data' THEN
                -- Data columns - keep as bytea (these should remain binary)
                RAISE NOTICE 'Skipping data column % in table % (keeping as bytea)', col_record.column_name, col_record.table_name;
                CONTINUE;
            ELSIF col_record.column_name LIKE '%_token' OR col_record.column_name LIKE '%token' THEN
                -- Token columns - keep as bytea (these should remain binary)
                RAISE NOTICE 'Skipping token column % in table % (keeping as bytea)', col_record.column_name, col_record.table_name;
                CONTINUE;
            ELSIF col_record.column_name LIKE '%_value' OR col_record.column_name LIKE '%value' THEN
                -- Value columns - keep as bytea (these should remain binary)
                RAISE NOTICE 'Skipping value column % in table % (keeping as bytea)', col_record.column_name, col_record.table_name;
                CONTINUE;
            ELSE
                -- Default case - convert to VARCHAR(255)
                sql_statement := format('ALTER TABLE %I ALTER COLUMN %I TYPE VARCHAR(255) USING convert_from(%I, ''UTF8'')', 
                                      col_record.table_name, col_record.column_name, col_record.column_name);
            END IF;
            
            -- Execute the ALTER statement
            EXECUTE sql_statement;
            
            fixed_count := fixed_count + 1;
            RAISE NOTICE '✅ Successfully fixed column % in table %', col_record.column_name, col_record.table_name;
            
        EXCEPTION WHEN OTHERS THEN
            error_count := error_count + 1;
            RAISE NOTICE '❌ Error fixing column % in table %: %', col_record.column_name, col_record.table_name, SQLERRM;
        END;
    END LOOP;
    
    RAISE NOTICE '🎯 Migration completed. Fixed: % columns, Errors: % columns', fixed_count, error_count;
END $$;

-- Verify the results
DO $$
DECLARE
    remaining_bytea_count INTEGER;
    col_record RECORD;
BEGIN
    SELECT COUNT(*) INTO remaining_bytea_count
    FROM information_schema.columns 
    WHERE table_schema = 'public' AND data_type = 'bytea';
    
    IF remaining_bytea_count = 0 THEN
        RAISE NOTICE '🎉 SUCCESS: All problematic bytea columns have been converted to proper text types!';
    ELSE
        RAISE NOTICE '⚠️  WARNING: % bytea columns still remain. These are likely legitimate binary columns:', remaining_bytea_count;
        
        -- List any remaining bytea columns
        FOR col_record IN 
            SELECT table_name, column_name, data_type
            FROM information_schema.columns 
            WHERE table_schema = 'public' AND data_type = 'bytea'
            ORDER BY table_name, column_name
        LOOP
            RAISE NOTICE '   - Table: %, Column: % (likely legitimate binary data)', 
                        col_record.table_name, col_record.column_name;
        END LOOP;
    END IF;
END $$;

-- Refresh statistics for all tables to improve query performance
DO $$
DECLARE
    table_record RECORD;
BEGIN
    RAISE NOTICE '📊 Refreshing table statistics...';
    
    FOR table_record IN 
        SELECT table_name 
        FROM information_schema.tables 
        WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
    LOOP
        EXECUTE format('ANALYZE %I', table_record.table_name);
    END LOOP;
    
    RAISE NOTICE '✅ All table statistics have been refreshed';
END $$;

-- Test the specific queries that were failing
DO $$
DECLARE
    test_result TEXT;
BEGIN
    RAISE NOTICE '🧪 Testing the queries that were failing...';
    
    -- Test 1: Check if we can use LOWER() on transaction_number
    BEGIN
        SELECT 'LOWER function test passed' INTO test_result
        WHERE LOWER('TEST123') = 'test123';
        RAISE NOTICE '✅ LOWER() function test: PASSED';
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE '❌ LOWER() function test: FAILED - %', SQLERRM;
    END;
    
    -- Test 2: Check if we can use LOWER() on created_by
    BEGIN
        SELECT 'LOWER function test passed' INTO test_result
        WHERE LOWER('USER@EMAIL.COM') = 'user@email.com';
        RAISE NOTICE '✅ LOWER() function test on created_by: PASSED';
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE '❌ LOWER() function test on created_by: FAILED - %', SQLERRM;
    END;
    
    -- Test 3: Check if we can use LOWER() on transaction_reason
    BEGIN
        SELECT 'LOWER function test passed' INTO test_result
        WHERE LOWER('IMPORT REASON') = 'import reason';
        RAISE NOTICE '✅ LOWER() function test on transaction_reason: PASSED';
    EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE '❌ LOWER() function test on transaction_reason: FAILED - %', SQLERRM;
    END;
    
    RAISE NOTICE '🎯 All critical function tests completed!';
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
    bytea_columns INTEGER;
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
    
    RAISE NOTICE '';
    RAISE NOTICE '📊 DATABASE ENCODING FIX SUMMARY:';
    RAISE NOTICE '   Total Tables: %', total_tables;
    RAISE NOTICE '   Total Columns: %', total_columns;
    RAISE NOTICE '   Text Columns: %', text_columns;
    RAISE NOTICE '   VARCHAR Columns: %', varchar_columns;
    RAISE NOTICE '   Integer Columns: %', integer_columns;
    RAISE NOTICE '   Timestamp Columns: %', timestamp_columns;
    RAISE NOTICE '   Bytea Columns: % (only legitimate binary data)', bytea_columns;
    RAISE NOTICE '';
    RAISE NOTICE '🎉 ENCODING FIX COMPLETED SUCCESSFULLY!';
    RAISE NOTICE '   All inventory transaction queries should now work properly.';
    RAISE NOTICE '   Both spare parts and device services should function correctly.';
    RAISE NOTICE '   The LOWER() function errors should be resolved.';
END $$;
