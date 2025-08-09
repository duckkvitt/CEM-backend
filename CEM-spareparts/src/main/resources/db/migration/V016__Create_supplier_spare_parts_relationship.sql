-- Create supplier_spare_parts join table for many-to-many relationship
CREATE TABLE IF NOT EXISTS supplier_spare_parts
(
    supplier_id   BIGINT NOT NULL,
    spare_part_id BIGINT NOT NULL,
    PRIMARY KEY (supplier_id, spare_part_id),
    FOREIGN KEY (supplier_id) REFERENCES suppliers (id) ON DELETE CASCADE,
    FOREIGN KEY (spare_part_id) REFERENCES spare_parts (id) ON DELETE CASCADE
);

-- Migrate existing data from supplier_spare_part_types to the new relationship
-- This will attempt to match spare part types with actual spare part names
DO $$
DECLARE
    supplier_record RECORD;
    spare_part_record RECORD;
    spare_part_type_record RECORD;
BEGIN
    -- For each supplier with spare part types
    FOR supplier_record IN 
        SELECT DISTINCT s.id as supplier_id
        FROM suppliers s
        WHERE EXISTS (
            SELECT 1 FROM supplier_spare_part_types sspt 
            WHERE sspt.supplier_id = s.id
        )
    LOOP
        -- For each spare part type of this supplier
        FOR spare_part_type_record IN
            SELECT spare_part_type
            FROM supplier_spare_part_types
            WHERE supplier_id = supplier_record.supplier_id
        LOOP
            -- Try to find matching spare parts by name (case-insensitive partial match)
            FOR spare_part_record IN
                SELECT id
                FROM spare_parts
                WHERE LOWER(part_name) LIKE '%' || LOWER(spare_part_type_record.spare_part_type) || '%'
                   OR LOWER(spare_part_type_record.spare_part_type) LIKE '%' || LOWER(part_name) || '%'
                   AND status = 'ACTIVE'
            LOOP
                -- Insert the relationship if it doesn't already exist
                INSERT INTO supplier_spare_parts (supplier_id, spare_part_id)
                VALUES (supplier_record.supplier_id, spare_part_record.id)
                ON CONFLICT (supplier_id, spare_part_id) DO NOTHING;
                
                RAISE NOTICE 'Linked supplier % with spare part % (type: %)', 
                    supplier_record.supplier_id, spare_part_record.id, spare_part_type_record.spare_part_type;
            END LOOP;
        END LOOP;
    END LOOP;
END $$;

-- Create index for better performance on the join table
CREATE INDEX IF NOT EXISTS idx_supplier_spare_parts_supplier_id ON supplier_spare_parts (supplier_id);
CREATE INDEX IF NOT EXISTS idx_supplier_spare_parts_spare_part_id ON supplier_spare_parts (spare_part_id);

-- Note: We keep the old supplier_spare_part_types table for now as a backup
-- It can be manually dropped later after verifying the migration worked correctly
-- Uncomment the following line after verification:
-- DROP TABLE IF EXISTS supplier_spare_part_types;

COMMIT;