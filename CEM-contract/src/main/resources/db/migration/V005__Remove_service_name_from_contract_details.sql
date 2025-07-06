-- Remove service_name column from contract_details table
-- Device ID is sufficient to identify what the contract detail is about
ALTER TABLE contract_details DROP COLUMN IF EXISTS service_name;

-- Add comment to document the change
COMMENT ON TABLE contract_details IS 'Contract details now use device_id to identify items instead of service_name'; 