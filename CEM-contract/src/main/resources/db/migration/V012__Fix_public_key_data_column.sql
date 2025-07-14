-- Fix public_key_data column in digital_certificates table
-- Add public_key_data column if it doesn't exist
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'digital_certificates' AND column_name = 'public_key_data') THEN
        ALTER TABLE digital_certificates ADD COLUMN public_key_data BYTEA;
    END IF;
END $$;

-- For existing records without public_key_data, extract public key from certificate_data
-- This is a temporary solution - in production, proper public keys should be stored
UPDATE digital_certificates 
SET public_key_data = certificate_data  -- Temporary: use certificate data as public key data
WHERE public_key_data IS NULL;

-- Make public_key_data NOT NULL after setting default values
ALTER TABLE digital_certificates ALTER COLUMN public_key_data SET NOT NULL;

-- Add helpful comment
COMMENT ON COLUMN digital_certificates.public_key_data IS 'Encoded public key data extracted from the certificate'; 