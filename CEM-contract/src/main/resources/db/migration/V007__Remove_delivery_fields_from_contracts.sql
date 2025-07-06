-- Remove delivery fields from contracts table since they are now managed by contract_delivery_schedules table
ALTER TABLE contracts DROP COLUMN IF EXISTS delivery_time;
ALTER TABLE contracts DROP COLUMN IF EXISTS delivery_location;
ALTER TABLE contracts DROP COLUMN IF EXISTS delivery_method;

-- Add comment to document the change
COMMENT ON TABLE contracts IS 'Delivery information moved to contract_delivery_schedules table for Article 3'; 