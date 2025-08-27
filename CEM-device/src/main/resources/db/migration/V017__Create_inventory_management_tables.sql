-- Create inventory management tables for devices and spare parts

-- Create device_inventory table
CREATE TABLE IF NOT EXISTS device_inventory (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL,
    quantity_in_stock INTEGER NOT NULL DEFAULT 0,
    minimum_stock_level INTEGER DEFAULT 0,
    maximum_stock_level INTEGER,
    reorder_point INTEGER,
    unit_cost DECIMAL(15,2),
    warehouse_location VARCHAR(100),
    notes TEXT,
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE
);

-- Create spare_part_inventory table
CREATE TABLE IF NOT EXISTS spare_part_inventory (
    id BIGSERIAL PRIMARY KEY,
    spare_part_id BIGINT NOT NULL,
    quantity_in_stock INTEGER NOT NULL DEFAULT 0,
    minimum_stock_level INTEGER DEFAULT 0,
    maximum_stock_level INTEGER,
    reorder_point INTEGER,
    unit_cost DECIMAL(15,2),
    warehouse_location VARCHAR(100),
    notes TEXT,
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (spare_part_id) REFERENCES spare_parts(id) ON DELETE CASCADE
);

-- Create inventory_transactions table
CREATE TABLE IF NOT EXISTS inventory_transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_number VARCHAR(50) NOT NULL UNIQUE,
    transaction_type VARCHAR(50) NOT NULL,
    item_type VARCHAR(50) NOT NULL,
    item_id BIGINT NOT NULL,
    item_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(15,2),
    total_amount DECIMAL(15,2),
    supplier_id BIGINT,
    supplier_name VARCHAR(255),
    reference_number VARCHAR(100),
    reference_type VARCHAR(50),
    reference_id BIGINT,
    transaction_reason TEXT,
    notes TEXT,
    warehouse_location VARCHAR(100),
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_device_inventory_device_id ON device_inventory(device_id);
CREATE INDEX IF NOT EXISTS idx_device_inventory_quantity ON device_inventory(quantity_in_stock);
CREATE INDEX IF NOT EXISTS idx_device_inventory_low_stock ON device_inventory(quantity_in_stock, minimum_stock_level);

CREATE INDEX IF NOT EXISTS idx_spare_part_inventory_spare_part_id ON spare_part_inventory(spare_part_id);
CREATE INDEX IF NOT EXISTS idx_spare_part_inventory_quantity ON spare_part_inventory(quantity_in_stock);
CREATE INDEX IF NOT EXISTS idx_spare_part_inventory_low_stock ON spare_part_inventory(quantity_in_stock, minimum_stock_level);

CREATE INDEX IF NOT EXISTS idx_inventory_transactions_transaction_number ON inventory_transactions(transaction_number);
CREATE INDEX IF NOT EXISTS idx_inventory_transactions_item_type ON inventory_transactions(item_type);
CREATE INDEX IF NOT EXISTS idx_inventory_transactions_item_id ON inventory_transactions(item_id);
CREATE INDEX IF NOT EXISTS idx_inventory_transactions_transaction_type ON inventory_transactions(transaction_type);
CREATE INDEX IF NOT EXISTS idx_inventory_transactions_created_at ON inventory_transactions(created_at);
CREATE INDEX IF NOT EXISTS idx_inventory_transactions_reference ON inventory_transactions(reference_type, reference_id);

-- Insert default inventory records for existing devices
INSERT INTO device_inventory (device_id, quantity_in_stock, minimum_stock_level, created_by, created_at)
SELECT id, 0, 0, COALESCE(created_by, 'SYSTEM'), CURRENT_TIMESTAMP
FROM devices
WHERE id NOT IN (SELECT device_id FROM device_inventory);

-- Insert default inventory records for existing spare parts
INSERT INTO spare_part_inventory (spare_part_id, quantity_in_stock, minimum_stock_level, created_by, created_at)
SELECT id, 0, 0, 'SYSTEM', CURRENT_TIMESTAMP
FROM spare_parts
WHERE id NOT IN (SELECT spare_part_id FROM spare_part_inventory);


