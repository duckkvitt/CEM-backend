-- Create devices table
CREATE TABLE IF NOT EXISTS devices (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    model VARCHAR(255),
    serial_number VARCHAR(255) UNIQUE,
    customer_id BIGINT,
    warranty_expiry DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create customer_devices table  
CREATE TABLE IF NOT EXISTS customer_devices (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    device_id BIGINT NOT NULL,
    purchase_date DATE,
    warranty_start DATE,
    warranty_end DATE,
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    note TEXT,
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE
);

-- Create device_notes table
CREATE TABLE IF NOT EXISTS device_notes (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL,
    note TEXT NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_devices_customer_id ON devices(customer_id);
CREATE INDEX IF NOT EXISTS idx_devices_serial_number ON devices(serial_number);
CREATE INDEX IF NOT EXISTS idx_devices_status ON devices(status);
CREATE INDEX IF NOT EXISTS idx_devices_created_at ON devices(created_at);

CREATE INDEX IF NOT EXISTS idx_customer_devices_customer_id ON customer_devices(customer_id);
CREATE INDEX IF NOT EXISTS idx_customer_devices_device_id ON customer_devices(device_id);
CREATE INDEX IF NOT EXISTS idx_customer_devices_status ON customer_devices(status);
CREATE INDEX IF NOT EXISTS idx_customer_devices_created_at ON customer_devices(created_at);

CREATE INDEX IF NOT EXISTS idx_device_notes_device_id ON device_notes(device_id);
CREATE INDEX IF NOT EXISTS idx_device_notes_created_by ON device_notes(created_by);
CREATE INDEX IF NOT EXISTS idx_device_notes_created_at ON device_notes(created_at); 