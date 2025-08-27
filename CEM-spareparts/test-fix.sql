-- Test script to verify the bytea column fix
-- Run this after applying the migration V026

-- 1. Check current column types in supplier-related tables
SELECT 
    table_name,
    column_name,
    data_type,
    is_nullable,
    character_maximum_length
FROM information_schema.columns 
WHERE table_schema = 'public' 
  AND (table_name LIKE '%supplier%' OR table_name LIKE '%device%' OR table_name LIKE '%spare%')
  AND data_type IN ('bytea', 'text', 'character varying')
ORDER BY table_name, column_name;

-- 2. Test LOWER() function on key columns
SELECT 'Testing LOWER() function on key columns...' as test_info;

-- Test company_name
SELECT 
    'company_name' as column_name,
    CASE 
        WHEN LOWER('TEST COMPANY') = 'test company' THEN 'PASSED'
        ELSE 'FAILED'
    END as test_result;

-- Test contact_person
SELECT 
    'contact_person' as column_name,
    CASE 
        WHEN LOWER('JOHN DOE') = 'john doe' THEN 'PASSED'
        ELSE 'FAILED'
    END as test_result;

-- Test email
SELECT 
    'email' as column_name,
    CASE 
        WHEN LOWER('TEST@EMAIL.COM') = 'test@email.com' THEN 'PASSED'
        ELSE 'FAILED'
    END as test_result;

-- Test device_type
SELECT 
    'device_type' as column_name,
    CASE 
        WHEN LOWER('LAPTOP') = 'laptop' THEN 'PASSED'
        ELSE 'FAILED'
    END as test_result;

-- Test device_model
SELECT 
    'device_model' as column_name,
    CASE 
        WHEN LOWER('MACBOOK PRO') = 'macbook pro' THEN 'PASSED'
        ELSE 'FAILED'
    END as test_result;

-- 3. Test the specific query that was failing
SELECT 'Testing the specific query that was failing...' as test_info;

-- Test the supplier query with LOWER() function
SELECT 
    COUNT(*) as supplier_count
FROM suppliers s
WHERE LOWER(s.company_name) LIKE '%test%';

-- Test the full supplier device type query
SELECT 
    COUNT(*) as device_type_count
FROM supplier_device_types sdt
LEFT JOIN suppliers s ON s.id = sdt.supplier_id
WHERE (NULL IS NULL OR s.id = 1)
  AND (NULL IS NULL OR sdt.is_active = true)
  AND (NULL IS NULL OR 
       LOWER(COALESCE(sdt.device_type, '')) LIKE '%test%' OR
       LOWER(COALESCE(sdt.device_model, '')) LIKE '%test%' OR
       LOWER(COALESCE(s.company_name, '')) LIKE '%test%');

-- 4. Test with actual data (if any exists)
SELECT 'Testing with actual data...' as test_info;

-- Check if there are any suppliers
SELECT COUNT(*) as total_suppliers FROM suppliers;

-- Check if there are any supplier device types
SELECT COUNT(*) as total_device_types FROM supplier_device_types;

-- Test a simple search query
SELECT 
    sdt.id,
    sdt.device_type,
    sdt.device_model,
    s.company_name,
    sdt.is_active
FROM supplier_device_types sdt
LEFT JOIN suppliers s ON s.id = sdt.supplier_id
WHERE sdt.is_active = true
LIMIT 5;

-- 5. Test the COALESCE function with LOWER
SELECT 'Testing COALESCE with LOWER...' as test_info;

SELECT 
    'COALESCE test' as test_name,
    CASE 
        WHEN LOWER(COALESCE('TEST', '')) = 'test' THEN 'PASSED'
        ELSE 'FAILED'
    END as test_result;

-- 6. Final verification
SELECT 'Final verification...' as test_info;

-- Count remaining bytea columns in supplier-related tables
SELECT 
    COUNT(*) as remaining_bytea_columns
FROM information_schema.columns 
WHERE table_schema = 'public' 
  AND data_type = 'bytea'
  AND (table_name LIKE '%supplier%' OR table_name LIKE '%device%' OR table_name LIKE '%spare%');

-- Summary report
SELECT 
    'SUMMARY REPORT' as report_type,
    CASE 
        WHEN COUNT(*) = 0 THEN 'SUCCESS: All supplier-related bytea columns have been fixed!'
        ELSE 'WARNING: ' || COUNT(*) || ' bytea columns still remain in supplier-related tables'
    END as status
FROM information_schema.columns 
WHERE table_schema = 'public' 
  AND data_type = 'bytea'
  AND (table_name LIKE '%supplier%' OR table_name LIKE '%device%' OR table_name LIKE '%spare%');
