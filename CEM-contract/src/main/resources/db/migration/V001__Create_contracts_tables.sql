-- Create contracts table
CREATE TABLE IF NOT EXISTS contracts (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    staff_id BIGINT NOT NULL, -- userId from authentication service
    contract_number VARCHAR(50) UNIQUE NOT NULL, -- Auto-generated contract number
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'UNSIGNED', -- UNSIGNED, PAPER_SIGNED, DIGITALLY_SIGNED
    file_path VARCHAR(500), -- Path to contract document
    digital_signed BOOLEAN NOT NULL DEFAULT false,
    paper_confirmed BOOLEAN NOT NULL DEFAULT false,
    signed_at TIMESTAMP, -- When the contract was signed
    signed_by VARCHAR(255), -- Who signed the contract
    total_value DECIMAL(15,2), -- Total contract value
    start_date DATE, -- Contract start date
    end_date DATE, -- Contract end date
    is_hidden BOOLEAN NOT NULL DEFAULT false,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create contract_details table
CREATE TABLE IF NOT EXISTS contract_details (
    id BIGSERIAL PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    work_code VARCHAR(100) NOT NULL, -- Work/Service code
    device_id BIGINT, -- Reference to device (optional)
    service_name VARCHAR(255) NOT NULL,
    description TEXT,
    quantity INTEGER NOT NULL DEFAULT 1,
    unit_price DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    total_price DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    warranty_months INTEGER DEFAULT 0, -- Warranty period in months
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    CONSTRAINT fk_contract_details_contract FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE
);

-- Create contract_signatures table for tracking signature events
CREATE TABLE IF NOT EXISTS contract_signatures (
    id BIGSERIAL PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    signer_type VARCHAR(50) NOT NULL, -- STAFF, CUSTOMER
    signer_id BIGINT, -- User ID or Customer ID
    signer_name VARCHAR(255) NOT NULL,
    signer_email VARCHAR(255) NOT NULL,
    signature_type VARCHAR(50) NOT NULL, -- DIGITAL, PAPER
    signature_data TEXT, -- Base64 encoded signature or file path
    ip_address VARCHAR(45), -- IP address when signed
    user_agent TEXT, -- Browser info when signed
    signed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_contract_signatures_contract FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE
);

-- Create contract_history table for audit trail
CREATE TABLE IF NOT EXISTS contract_history (
    id BIGSERIAL PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    action VARCHAR(100) NOT NULL, -- CREATED, UPDATED, SIGNED, HIDDEN, RESTORED
    old_status VARCHAR(50),
    new_status VARCHAR(50),
    changed_by VARCHAR(255) NOT NULL,
    change_reason VARCHAR(500),
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_contract_history_contract FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_contracts_customer_id ON contracts(customer_id);
CREATE INDEX IF NOT EXISTS idx_contracts_staff_id ON contracts(staff_id);
CREATE INDEX IF NOT EXISTS idx_contracts_status ON contracts(status);
CREATE INDEX IF NOT EXISTS idx_contracts_contract_number ON contracts(contract_number);
CREATE INDEX IF NOT EXISTS idx_contracts_created_at ON contracts(created_at);
CREATE INDEX IF NOT EXISTS idx_contracts_signed_at ON contracts(signed_at);
CREATE INDEX IF NOT EXISTS idx_contracts_is_hidden ON contracts(is_hidden);

CREATE INDEX IF NOT EXISTS idx_contract_details_contract_id ON contract_details(contract_id);
CREATE INDEX IF NOT EXISTS idx_contract_details_device_id ON contract_details(device_id);
CREATE INDEX IF NOT EXISTS idx_contract_details_work_code ON contract_details(work_code);

CREATE INDEX IF NOT EXISTS idx_contract_signatures_contract_id ON contract_signatures(contract_id);
CREATE INDEX IF NOT EXISTS idx_contract_signatures_signer_id ON contract_signatures(signer_id);
CREATE INDEX IF NOT EXISTS idx_contract_signatures_signed_at ON contract_signatures(signed_at);

CREATE INDEX IF NOT EXISTS idx_contract_history_contract_id ON contract_history(contract_id);
CREATE INDEX IF NOT EXISTS idx_contract_history_changed_at ON contract_history(changed_at);

-- Create sequence for contract numbers
CREATE SEQUENCE IF NOT EXISTS contract_number_seq START 1000; 