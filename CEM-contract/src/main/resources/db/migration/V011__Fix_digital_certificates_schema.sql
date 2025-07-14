-- Fix digital_certificates table schema to match entity
-- Add alias column if it doesn't exist
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'digital_certificates' AND column_name = 'alias') THEN
        ALTER TABLE digital_certificates ADD COLUMN alias VARCHAR(255);
    END IF;
END $$;

-- Set default values for existing records if alias column was just added
UPDATE digital_certificates 
SET alias = 'cert_' || id || '_' || EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::bigint
WHERE alias IS NULL;

-- Make alias NOT NULL after setting default values
ALTER TABLE digital_certificates ALTER COLUMN alias SET NOT NULL;

-- Remove certificate_name column if it exists
DO $$ 
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name = 'digital_certificates' AND column_name = 'certificate_name') THEN
        ALTER TABLE digital_certificates DROP COLUMN certificate_name;
    END IF;
END $$;

-- Add comment
COMMENT ON COLUMN digital_certificates.alias IS 'Unique alias/identifier for the certificate'; 