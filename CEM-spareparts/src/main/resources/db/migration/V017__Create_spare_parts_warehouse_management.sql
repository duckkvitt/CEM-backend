-- Create warehouse management tables for spare parts

-- Spare parts inventory tracking
CREATE TABLE spare_parts_inventory (
    id BIGSERIAL PRIMARY KEY,
    spare_part_id BIGINT NOT NULL,
    quantity_in_stock INTEGER NOT NULL DEFAULT 0,
    minimum_stock_level INTEGER DEFAULT 0,
    maximum_stock_level INTEGER DEFAULT 1000,
    last_restocked_at TIMESTAMP,
    last_updated_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_spare_parts_inventory_spare_part FOREIGN KEY (spare_part_id) REFERENCES spare_parts(id) ON DELETE CASCADE,
    CONSTRAINT uk_spare_parts_inventory_spare_part UNIQUE (spare_part_id),
    CONSTRAINT chk_spare_parts_inventory_quantity CHECK (quantity_in_stock >= 0)
);

-- Import requests for spare parts
CREATE TABLE spare_parts_import_requests (
    id BIGSERIAL PRIMARY KEY,
    request_number VARCHAR(50) NOT NULL UNIQUE,
    spare_part_id BIGINT NOT NULL,
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
    CONSTRAINT fk_spare_parts_import_spare_part FOREIGN KEY (spare_part_id) REFERENCES spare_parts(id) ON DELETE CASCADE,
    CONSTRAINT fk_spare_parts_import_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE SET NULL,
    CONSTRAINT chk_spare_parts_import_quantity CHECK (requested_quantity > 0),
    CONSTRAINT chk_spare_parts_import_status CHECK (request_status IN ('PENDING', 'APPROVED', 'REJECTED', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT chk_spare_parts_import_approval CHECK (approval_status IN ('APPROVED', 'REJECTED'))
);

-- Export/Usage requests for spare parts (from technicians during tasks)
CREATE TABLE spare_parts_export_requests (
    id BIGSERIAL PRIMARY KEY,
    request_number VARCHAR(50) NOT NULL UNIQUE,
    spare_part_id BIGINT NOT NULL,
    task_id BIGINT,
    requested_quantity INTEGER NOT NULL,
    request_reason TEXT NOT NULL,
    request_status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    requested_by VARCHAR(255) NOT NULL, -- technician
    requested_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_by VARCHAR(255), -- manager
    reviewed_at TIMESTAMP,
    approval_status VARCHAR(50),
    approval_reason TEXT,
    issued_quantity INTEGER DEFAULT 0,
    issued_at TIMESTAMP,
    issued_by VARCHAR(255),
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_spare_parts_export_spare_part FOREIGN KEY (spare_part_id) REFERENCES spare_parts(id) ON DELETE CASCADE,
    CONSTRAINT chk_spare_parts_export_quantity CHECK (requested_quantity > 0),
    CONSTRAINT chk_spare_parts_export_issued_quantity CHECK (issued_quantity >= 0),
    CONSTRAINT chk_spare_parts_export_status CHECK (request_status IN ('PENDING', 'APPROVED', 'REJECTED', 'ISSUED', 'CANCELLED')),
    CONSTRAINT chk_spare_parts_export_approval CHECK (approval_status IN ('APPROVED', 'REJECTED'))
);

-- Spare parts inventory transactions (for audit trail)
CREATE TABLE spare_parts_inventory_transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_number VARCHAR(50) NOT NULL UNIQUE,
    spare_part_id BIGINT NOT NULL,
    transaction_type VARCHAR(50) NOT NULL,
    quantity_change INTEGER NOT NULL,
    quantity_before INTEGER NOT NULL,
    quantity_after INTEGER NOT NULL,
    reference_type VARCHAR(50),
    reference_id BIGINT,
    transaction_reason TEXT,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_spare_parts_transaction_spare_part FOREIGN KEY (spare_part_id) REFERENCES spare_parts(id) ON DELETE CASCADE,
    CONSTRAINT chk_spare_parts_transaction_type CHECK (transaction_type IN ('IMPORT', 'EXPORT', 'ADJUSTMENT', 'TRANSFER')),
    CONSTRAINT chk_spare_parts_reference_type CHECK (reference_type IN ('IMPORT_REQUEST', 'EXPORT_REQUEST', 'ADJUSTMENT', 'TRANSFER'))
);

-- Create indexes for performance
CREATE INDEX idx_spare_parts_inventory_spare_part ON spare_parts_inventory(spare_part_id);
CREATE INDEX idx_spare_parts_import_requests_spare_part ON spare_parts_import_requests(spare_part_id);
CREATE INDEX idx_spare_parts_import_requests_supplier ON spare_parts_import_requests(supplier_id);
CREATE INDEX idx_spare_parts_import_requests_status ON spare_parts_import_requests(request_status);
CREATE INDEX idx_spare_parts_import_requests_requested_by ON spare_parts_import_requests(requested_by);
CREATE INDEX idx_spare_parts_export_requests_spare_part ON spare_parts_export_requests(spare_part_id);
CREATE INDEX idx_spare_parts_export_requests_task ON spare_parts_export_requests(task_id);
CREATE INDEX idx_spare_parts_export_requests_status ON spare_parts_export_requests(request_status);
CREATE INDEX idx_spare_parts_export_requests_requested_by ON spare_parts_export_requests(requested_by);
CREATE INDEX idx_spare_parts_transactions_spare_part ON spare_parts_inventory_transactions(spare_part_id);
CREATE INDEX idx_spare_parts_transactions_type ON spare_parts_inventory_transactions(transaction_type);
CREATE INDEX idx_spare_parts_transactions_created_at ON spare_parts_inventory_transactions(created_at);

-- Create triggers to automatically update updated_at timestamps
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_spare_parts_inventory_updated_at BEFORE UPDATE ON spare_parts_inventory FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_spare_parts_import_requests_updated_at BEFORE UPDATE ON spare_parts_import_requests FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
CREATE TRIGGER update_spare_parts_export_requests_updated_at BEFORE UPDATE ON spare_parts_export_requests FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Create function to generate spare parts import request numbers
CREATE OR REPLACE FUNCTION generate_spare_parts_import_request_number()
RETURNS VARCHAR(50) AS $$
DECLARE
    next_num INTEGER;
    request_number VARCHAR(50);
BEGIN
    -- Get the next sequence number
    SELECT COALESCE(MAX(CAST(SUBSTRING(request_number FROM 6) AS INTEGER)), 0) + 1
    INTO next_num
    FROM spare_parts_import_requests
    WHERE request_number LIKE 'SPIR-%';
    
    -- Format as SPIR-YYYY-NNNN
    request_number := 'SPIR-' || TO_CHAR(CURRENT_DATE, 'YYYY') || '-' || LPAD(next_num::TEXT, 4, '0');
    
    RETURN request_number;
END;
$$ LANGUAGE plpgsql;

-- Create function to generate spare parts export request numbers
CREATE OR REPLACE FUNCTION generate_spare_parts_export_request_number()
RETURNS VARCHAR(50) AS $$
DECLARE
    next_num INTEGER;
    request_number VARCHAR(50);
BEGIN
    -- Get the next sequence number
    SELECT COALESCE(MAX(CAST(SUBSTRING(request_number FROM 6) AS INTEGER)), 0) + 1
    INTO next_num
    FROM spare_parts_export_requests
    WHERE request_number LIKE 'SPER-%';
    
    -- Format as SPER-YYYY-NNNN
    request_number := 'SPER-' || TO_CHAR(CURRENT_DATE, 'YYYY') || '-' || LPAD(next_num::TEXT, 4, '0');
    
    RETURN request_number;
END;
$$ LANGUAGE plpgsql;

-- Create function to generate transaction numbers
CREATE OR REPLACE FUNCTION generate_spare_parts_transaction_number()
RETURNS VARCHAR(50) AS $$
DECLARE
    next_num INTEGER;
    transaction_number VARCHAR(50);
BEGIN
    -- Get the next sequence number for today
    SELECT COALESCE(MAX(CAST(SUBSTRING(transaction_number FROM 10) AS INTEGER)), 0) + 1
    INTO next_num
    FROM spare_parts_inventory_transactions
    WHERE transaction_number LIKE 'SPT-' || TO_CHAR(CURRENT_DATE, 'YYYY-MM-DD') || '-%';
    
    -- Format as SPT-YYYY-MM-DD-NNN
    transaction_number := 'SPT-' || TO_CHAR(CURRENT_DATE, 'YYYY-MM-DD') || '-' || LPAD(next_num::TEXT, 3, '0');
    
    RETURN transaction_number;
END;
$$ LANGUAGE plpgsql;

-- Initialize inventory for existing spare parts with default values
INSERT INTO spare_parts_inventory (spare_part_id, quantity_in_stock, minimum_stock_level, maximum_stock_level, last_updated_by)
SELECT 
    id, 
    0, -- start with 0 stock
    10, -- default minimum
    500, -- default maximum  
    'SYSTEM_MIGRATION'
FROM spare_parts
WHERE id NOT IN (SELECT spare_part_id FROM spare_parts_inventory WHERE spare_part_id IS NOT NULL);
