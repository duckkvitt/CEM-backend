CREATE TABLE IF NOT EXISTS spare_part_usage (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    spare_part_id BIGINT NOT NULL,
    spare_part_name VARCHAR(255) NOT NULL,
    spare_part_code VARCHAR(255),
    quantity_used INT NOT NULL,
    unit_price NUMERIC(19,2) NOT NULL DEFAULT 0,
    total_cost NUMERIC(19,2) NOT NULL DEFAULT 0,
    notes TEXT,
    used_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW() NOT NULL,
    created_by VARCHAR(255) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_spare_part_usage_task_id ON spare_part_usage(task_id);


