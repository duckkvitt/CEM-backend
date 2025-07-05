-- Add new columns as nullable
ALTER TABLE customers ADD COLUMN company_name VARCHAR(255);
ALTER TABLE customers ADD COLUMN company_tax_code VARCHAR(50);
ALTER TABLE customers ADD COLUMN company_address TEXT;
ALTER TABLE customers ADD COLUMN legal_representative VARCHAR(255);
ALTER TABLE customers ADD COLUMN title VARCHAR(255);
ALTER TABLE customers ADD COLUMN identity_number VARCHAR(50);
ALTER TABLE customers ADD COLUMN identity_issue_date DATE;
ALTER TABLE customers ADD COLUMN identity_issue_place VARCHAR(255);
ALTER TABLE customers ADD COLUMN fax VARCHAR(20);

-- Update existing rows with default values
UPDATE customers SET company_name = 'N/A' WHERE company_name IS NULL;
UPDATE customers SET company_tax_code = 'N/A' WHERE company_tax_code IS NULL;
UPDATE customers SET company_address = 'N/A' WHERE company_address IS NULL;
UPDATE customers SET legal_representative = 'N/A' WHERE legal_representative IS NULL;
UPDATE customers SET title = 'N/A' WHERE title IS NULL;
UPDATE customers SET identity_number = 'N/A' WHERE identity_number IS NULL;
UPDATE customers SET identity_issue_date = '1970-01-01' WHERE identity_issue_date IS NULL;
UPDATE customers SET identity_issue_place = 'N/A' WHERE identity_issue_place IS NULL;
UPDATE customers SET phone = 'N/A' WHERE phone IS NULL;

-- Alter columns to be NOT NULL
ALTER TABLE customers ALTER COLUMN company_name SET NOT NULL;
ALTER TABLE customers ALTER COLUMN company_tax_code SET NOT NULL;
ALTER TABLE customers ALTER COLUMN company_address SET NOT NULL;
ALTER TABLE customers ALTER COLUMN legal_representative SET NOT NULL;
ALTER TABLE customers ALTER COLUMN title SET NOT NULL;
ALTER TABLE customers ALTER COLUMN identity_number SET NOT NULL;
ALTER TABLE customers ALTER COLUMN identity_issue_date SET NOT NULL;
ALTER TABLE customers ALTER COLUMN identity_issue_place SET NOT NULL;
ALTER TABLE customers ALTER COLUMN phone SET NOT NULL; 