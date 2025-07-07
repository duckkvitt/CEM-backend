-- Create contract_delivery_schedules table for "Điều 3. Thời gian, địa điểm, phương thức giao hàng"
CREATE TABLE IF NOT EXISTS contract_delivery_schedules (
    id BIGSERIAL PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    sequence_number INTEGER NOT NULL,
    item_name VARCHAR(500) NOT NULL,
    unit VARCHAR(50) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 1,
    delivery_time VARCHAR(255),
    delivery_location TEXT,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_delivery_schedules_contract FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE
);

-- Create index for better performance
CREATE INDEX IF NOT EXISTS idx_delivery_schedules_contract_id ON contract_delivery_schedules(contract_id);
CREATE INDEX IF NOT EXISTS idx_delivery_schedules_sequence ON contract_delivery_schedules(contract_id, sequence_number);

-- Add comment to document the table purpose
COMMENT ON TABLE contract_delivery_schedules IS 'Delivery schedule table for Article 3 of contracts'; 