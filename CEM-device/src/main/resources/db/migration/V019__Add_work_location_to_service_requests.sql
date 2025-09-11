-- Add work_location column to service_requests to store customer's preferred work location
ALTER TABLE service_requests
ADD COLUMN IF NOT EXISTS work_location TEXT;

COMMENT ON COLUMN service_requests.work_location IS 'Customer-provided work location for servicing the device';

