-- Add rejection and approval fields to service_requests table
ALTER TABLE service_requests 
ADD COLUMN IF NOT EXISTS rejection_reason TEXT,
ADD COLUMN IF NOT EXISTS rejected_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS rejected_at TIMESTAMP,
ADD COLUMN IF NOT EXISTS approved_by VARCHAR(255),
ADD COLUMN IF NOT EXISTS approved_at TIMESTAMP;

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_service_requests_rejected_by ON service_requests(rejected_by);
CREATE INDEX IF NOT EXISTS idx_service_requests_approved_by ON service_requests(approved_by);
