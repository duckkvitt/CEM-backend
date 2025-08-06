-- Create service_requests table
CREATE TABLE IF NOT EXISTS service_requests (
    id BIGSERIAL PRIMARY KEY,
    request_id VARCHAR(50) UNIQUE NOT NULL,
    customer_id BIGINT NOT NULL,
    device_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    description TEXT NOT NULL,
    preferred_date_time TIMESTAMP,
    attachments TEXT, -- JSON array of Google Drive file IDs
    staff_notes TEXT,
    customer_comments TEXT,
    estimated_cost DECIMAL(15,2),
    actual_cost DECIMAL(15,2),
    completed_at TIMESTAMP,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_service_requests_customer_device FOREIGN KEY (device_id) REFERENCES customer_devices(id) ON DELETE CASCADE
);

-- Create service_request_history table
CREATE TABLE IF NOT EXISTS service_request_history (
    id BIGSERIAL PRIMARY KEY,
    service_request_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    comment TEXT,
    updated_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_service_request_history_service_request FOREIGN KEY (service_request_id) REFERENCES service_requests(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_service_requests_customer_id ON service_requests(customer_id);
CREATE INDEX IF NOT EXISTS idx_service_requests_device_id ON service_requests(device_id);
CREATE INDEX IF NOT EXISTS idx_service_requests_type ON service_requests(type);
CREATE INDEX IF NOT EXISTS idx_service_requests_status ON service_requests(status);
CREATE INDEX IF NOT EXISTS idx_service_requests_request_id ON service_requests(request_id);
CREATE INDEX IF NOT EXISTS idx_service_requests_created_at ON service_requests(created_at);

CREATE INDEX IF NOT EXISTS idx_service_request_history_service_request_id ON service_request_history(service_request_id);
CREATE INDEX IF NOT EXISTS idx_service_request_history_created_at ON service_request_history(created_at);

-- Add comments for documentation
COMMENT ON TABLE service_requests IS 'Service requests for maintenance and warranty';
COMMENT ON COLUMN service_requests.request_id IS 'Auto-generated unique request ID';
COMMENT ON COLUMN service_requests.type IS 'Type of service request (MAINTENANCE or WARRANTY)';
COMMENT ON COLUMN service_requests.status IS 'Current status of the service request';
COMMENT ON COLUMN service_requests.attachments IS 'JSON array of Google Drive file IDs for uploaded documents/photos';
COMMENT ON COLUMN service_requests.staff_notes IS 'Internal notes from staff members';
COMMENT ON COLUMN service_requests.customer_comments IS 'Additional comments from customer';

COMMENT ON TABLE service_request_history IS 'History of status changes and comments for service requests';
COMMENT ON COLUMN service_request_history.status IS 'Status at the time of this history entry';
COMMENT ON COLUMN service_request_history.comment IS 'Comment or note associated with this history entry'; 