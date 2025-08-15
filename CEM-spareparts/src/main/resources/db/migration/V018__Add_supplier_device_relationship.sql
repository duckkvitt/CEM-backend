-- Add device support to suppliers
-- This enables suppliers to provide both spare parts and devices

-- Update the supplier_devices table to reference the correct supplier table
-- Note: The device table is in the device service, so we'll use supplier_id as BIGINT
-- The foreign key constraint will be managed at the application level due to microservice architecture

-- Table was created in device service migration, but we need to ensure suppliers can reference it
-- Create a view or reference table to track which devices each supplier provides

-- Track supplier-device relationships metadata in spare parts service
CREATE TABLE supplier_device_types (
    id BIGSERIAL PRIMARY KEY,
    supplier_id BIGINT NOT NULL,
    device_type VARCHAR(255) NOT NULL,
    device_model VARCHAR(255),
    unit_price DECIMAL(15,2),
    minimum_order_quantity INTEGER DEFAULT 1,
    lead_time_days INTEGER DEFAULT 0,
    notes TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_supplier_device_types_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id) ON DELETE CASCADE,
    CONSTRAINT uk_supplier_device_type UNIQUE (supplier_id, device_type, device_model)
);

-- Create index for performance
CREATE INDEX idx_supplier_device_types_supplier ON supplier_device_types(supplier_id);
CREATE INDEX idx_supplier_device_types_device_type ON supplier_device_types(device_type);
CREATE INDEX idx_supplier_device_types_active ON supplier_device_types(is_active);

-- Create trigger for updated_at
CREATE TRIGGER update_supplier_device_types_updated_at 
    BEFORE UPDATE ON supplier_device_types 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Add capability flags to suppliers to indicate what they can supply
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS supplies_devices BOOLEAN DEFAULT FALSE;
ALTER TABLE suppliers ADD COLUMN IF NOT EXISTS supplies_spare_parts BOOLEAN DEFAULT TRUE;

-- Update existing suppliers to have spare parts capability
UPDATE suppliers SET supplies_spare_parts = TRUE WHERE supplies_spare_parts IS NULL;

-- Create a view to easily see supplier capabilities
CREATE OR REPLACE VIEW supplier_capabilities AS
SELECT 
    s.id,
    s.company_name,
    s.contact_person,
    s.email,
    s.phone,
    s.supplies_devices,
    s.supplies_spare_parts,
    COUNT(DISTINCT sdt.id) as device_types_count,
    COUNT(DISTINCT ssp.spare_part_id) as spare_parts_count,
    s.status,
    s.created_at
FROM suppliers s
LEFT JOIN supplier_device_types sdt ON s.id = sdt.supplier_id AND sdt.is_active = TRUE
LEFT JOIN supplier_spare_parts ssp ON s.id = ssp.supplier_id
WHERE s.status = 'ACTIVE'
GROUP BY s.id, s.company_name, s.contact_person, s.email, s.phone, 
         s.supplies_devices, s.supplies_spare_parts, s.status, s.created_at
ORDER BY s.company_name;
