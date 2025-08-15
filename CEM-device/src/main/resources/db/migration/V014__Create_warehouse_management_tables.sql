-- Create warehouse management tables for devices

-- Device inventory tracking
CREATE TABLE device_inventory (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL,
    quantity_in_stock INTEGER NOT NULL DEFAULT 0,
    minimum_stock_level INTEGER DEFAULT 0,
    maximum_stock_level INTEGER DEFAULT 1000,
    last_restocked_at TIMESTAMP,
    last_updated_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_device_inventory_device FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE,
    CONSTRAINT uk_device_inventory_device UNIQUE (device_id),
    CONSTRAINT chk_device_inventory_quantity CHECK (quantity_in_stock >= 0)
);

-- Import requests for devices
CREATE TABLE device_import_requests (
    id BIGSERIAL PRIMARY KEY,
    request_number VARCHAR(50) NOT NULL UNIQUE,
    device_id BIGINT NOT NULL,
    supplier_id BIGINT,
    requested_quantity INTEGER NOT NULL,
    unit_price DECIMAL(15,2),
    total_amount DECIMAL(15,2),
    request_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    request_reason TEXT,
    requested_by VARCHAR(255) NOT NULL,
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_by VARCHAR(255),
    reviewed_at TIMESTAMP,
    approval_status VARCHAR(50),
    approval_reason TEXT,
    expected_delivery_date DATE,
    actual_delivery_date DATE,
    invoice_number VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_device_import_device FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE,
    CONSTRAINT chk_device_import_quantity CHECK (requested_quantity > 0),
    CONSTRAINT chk_device_import_status CHECK (request_status IN ('PENDING', 'APPROVED', 'REJECTED', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_device_import_approval CHECK (approval_status IN ('APPROVED', 'REJECTED'))
);

-- Device inventory transactions (for audit trail)
CREATE TABLE device_inventory_transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_number VARCHAR(50) NOT NULL UNIQUE,
    device_id BIGINT NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    quantity_change INTEGER NOT NULL,
    quantity_before INTEGER NOT NULL,
    quantity_after INTEGER NOT NULL,
    reference_type VARCHAR(50),
    reference_id BIGINT,
    transaction_reason TEXT,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_device_transaction_device FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE,
    CONSTRAINT chk_device_transaction_type CHECK (transaction_type IN ('IMPORT', 'EXPORT', 'ADJUSTMENT', 'TRANSFER')),
    CONSTRAINT chk_device_reference_type CHECK (reference_type IN ('IMPORT_REQUEST', 'CONTRACT', 'ADJUSTMENT', 'TRANSFER'))
);

-- Supplier-Device relationship (junction table)
CREATE TABLE supplier_devices (
    supplier_id BIGINT NOT NULL,
    device_id BIGINT NOT NULL,
    is_primary_supplier BOOLEAN DEFAULT FALSE,
    unit_price DECIMAL(15,2),
    minimum_order_quantity INTEGER DEFAULT 1,
    lead_time_days INTEGER DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (supplier_id, device_id),
    CONSTRAINT fk_supplier_devices_device FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_device_inventory_device ON device_inventory(device_id);
CREATE INDEX idx_device_import_requests_device ON device_import_requests(device_id);
CREATE INDEX idx_device_import_requests_status ON device_import_requests(request_status);
CREATE INDEX idx_device_import_requests_requested_by ON device_import_requests(requested_by);
CREATE INDEX idx_device_inventory_transactions_device ON device_inventory_transactions(device_id);
CREATE INDEX idx_device_inventory_transactions_type ON device_inventory_transactions(transaction_type);
CREATE INDEX idx_device_inventory_transactions_created_at ON device_inventory_transactions(created_at);
CREATE INDEX idx_supplier_devices_device ON supplier_devices(device_id);
CREATE INDEX idx_supplier_devices_supplier ON supplier_devices(supplier_id);

-- Create triggers to automatically update updated_at timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_device_inventory_updated_at BEFORE UPDATE ON device_inventory FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_device_import_requests_updated_at BEFORE UPDATE ON device_import_requests FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_supplier_devices_updated_at BEFORE UPDATE ON supplier_devices FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Create function to generate device import request numbers
CREATE OR REPLACE FUNCTION generate_device_import_request_number()
RETURNS VARCHAR(50) AS $$
DECLARE
    next_num INTEGER;
    request_number VARCHAR(50);
BEGIN
    -- Get the next sequence number
    SELECT COALESCE(MAX(CAST(SUBSTRING(request_number FROM 5) AS INTEGER)), 0) + 1
    INTO next_num
    FROM device_import_requests
    WHERE request_number LIKE 'DIR-%';
    
    -- Format as DIR-YYYY-NNNN
    request_number := 'DIR-' || TO_CHAR(CURRENT_DATE, 'YYYY') || '-' || LPAD(next_num::TEXT, 4, '0');
    
    RETURN request_number;
END;
$$ LANGUAGE plpgsql;

-- Create function to generate transaction numbers
CREATE OR REPLACE FUNCTION generate_device_transaction_number()
RETURNS VARCHAR(50) AS $$
DECLARE
    next_num INTEGER;
    transaction_number VARCHAR(50);
BEGIN
    -- Get the next sequence number for today
    SELECT COALESCE(MAX(CAST(SUBSTRING(transaction_number FROM 9) AS INTEGER)), 0) + 1
    INTO next_num
    FROM device_inventory_transactions
    WHERE transaction_number LIKE 'DT-' || TO_CHAR(CURRENT_DATE, 'YYYY-MM-DD') || '-%';
    
    -- Format as DT-YYYY-MM-DD-NNN
    transaction_number := 'DT-' || TO_CHAR(CURRENT_DATE, 'YYYY-MM-DD') || '-' || LPAD(next_num::TEXT, 3, '0');
    
    RETURN transaction_number;
END;
$$ LANGUAGE plpgsql;

-- Initialize inventory for existing devices with default values
INSERT INTO device_inventory (device_id, quantity_in_stock, minimum_stock_level, maximum_stock_level, last_updated_by)
SELECT 
    id, 
    COALESCE(quantity, 0),
    5, -- default minimum
    100, -- default maximum  
    'SYSTEM_MIGRATION'
FROM devices
WHERE id NOT IN (SELECT device_id FROM device_inventory);
