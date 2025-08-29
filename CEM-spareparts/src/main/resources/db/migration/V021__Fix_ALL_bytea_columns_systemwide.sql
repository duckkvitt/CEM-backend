-- Fix ALL bytea columns across the entire database
-- This migration systematically converts all bytea columns to proper text types

-- First, let's identify ALL bytea columns in the database
DO $$
DECLARE
    col_record RECORD;
    total_bytea_columns INTEGER := 0;
BEGIN
    RAISE NOTICE 'Scanning database for ALL bytea columns...';
    
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
    RAISE NOTICE 'Starting to fix ALL bytea columns...';
    
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
            ELSE
                -- Default case - convert to VARCHAR(255)
                sql_statement := format('ALTER TABLE %I ALTER COLUMN %I TYPE VARCHAR(255) USING convert_from(%I, ''UTF8'')', 
                                      col_record.table_name, col_record.column_name, col_record.column_name);
            END IF;
            
            -- Execute the ALTER statement
            EXECUTE sql_statement;
            
            fixed_count := fixed_count + 1;
            RAISE NOTICE 'Successfully fixed column % in table %', col_record.column_name, col_record.table_name;
            
        EXCEPTION WHEN OTHERS THEN
            error_count := error_count + 1;
            RAISE NOTICE 'Error fixing column % in table %: %', col_record.column_name, col_record.table_name, SQLERRM;
        END;
    END LOOP;
    
    RAISE NOTICE 'Migration completed. Fixed: % columns, Errors: % columns', fixed_count, error_count;
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
        RAISE NOTICE 'SUCCESS: All bytea columns have been converted to proper text types!';
    ELSE
        RAISE NOTICE 'WARNING: % bytea columns still remain. Manual intervention may be required.', remaining_bytea_count;
        
        -- List any remaining bytea columns
        FOR col_record IN 
            SELECT table_name, column_name, data_type
            FROM information_schema.columns 
            WHERE table_schema = 'public' AND data_type = 'bytea'
            ORDER BY table_name, column_name
        LOOP
            RAISE NOTICE 'Remaining bytea column: Table: %, Column: %', 
                        col_record.table_name, col_record.column_name;
        END LOOP;
    END IF;
END $$;

-- Refresh statistics for all tables to improve query performance
DO $$
DECLARE
    table_record RECORD;
BEGIN
    RAISE NOTICE 'Refreshing table statistics...';
    
    FOR table_record IN 
        SELECT table_name 
        FROM information_schema.tables 
        WHERE table_schema = 'public' AND table_type = 'BASE TABLE'
    LOOP
        EXECUTE format('ANALYZE %I', table_record.table_name);
    END LOOP;
    
    RAISE NOTICE 'All table statistics have been refreshed';
END $$;
