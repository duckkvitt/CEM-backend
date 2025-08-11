-- Create tasks table
CREATE TABLE IF NOT EXISTS tasks (
    id BIGSERIAL PRIMARY KEY,
    task_id VARCHAR(50) UNIQUE NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    priority VARCHAR(50) NOT NULL DEFAULT 'NORMAL',
    
    -- Relationships
    service_request_id BIGINT,
    customer_device_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    
    -- Assignment information
    assigned_technician_id BIGINT,
    assigned_by VARCHAR(255),
    assigned_at TIMESTAMP,
    
    -- Scheduling
    scheduled_date TIMESTAMP,
    estimated_duration_hours INTEGER,
    
    -- Location and service details
    service_location TEXT,
    customer_contact_info TEXT,
    
    -- Cost information
    estimated_cost DECIMAL(15,2),
    actual_cost DECIMAL(15,2),
    
    -- Notes and comments
    support_notes TEXT,
    techlead_notes TEXT,
    technician_notes TEXT,
    completion_notes TEXT,
    
    -- Rejection information
    rejection_reason TEXT,
    rejected_by VARCHAR(255),
    rejected_at TIMESTAMP,
    
    -- Timestamps
    completed_at TIMESTAMP,
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    -- Foreign key constraints
    CONSTRAINT fk_tasks_customer_device FOREIGN KEY (customer_device_id) REFERENCES customer_devices(id) ON DELETE CASCADE,
    CONSTRAINT fk_tasks_service_request FOREIGN KEY (service_request_id) REFERENCES service_requests(id) ON DELETE SET NULL
);

-- Create task_history table
CREATE TABLE IF NOT EXISTS task_history (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    comment TEXT,
    updated_by VARCHAR(255) NOT NULL,
    user_role VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_task_history_task FOREIGN KEY (task_id) REFERENCES tasks(id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_tasks_task_id ON tasks(task_id);
CREATE INDEX IF NOT EXISTS idx_tasks_customer_id ON tasks(customer_id);
CREATE INDEX IF NOT EXISTS idx_tasks_customer_device_id ON tasks(customer_device_id);
CREATE INDEX IF NOT EXISTS idx_tasks_service_request_id ON tasks(service_request_id);
CREATE INDEX IF NOT EXISTS idx_tasks_assigned_technician_id ON tasks(assigned_technician_id);
CREATE INDEX IF NOT EXISTS idx_tasks_status ON tasks(status);
CREATE INDEX IF NOT EXISTS idx_tasks_type ON tasks(type);
CREATE INDEX IF NOT EXISTS idx_tasks_priority ON tasks(priority);
CREATE INDEX IF NOT EXISTS idx_tasks_scheduled_date ON tasks(scheduled_date);
CREATE INDEX IF NOT EXISTS idx_tasks_created_at ON tasks(created_at);
CREATE INDEX IF NOT EXISTS idx_tasks_assigned_by ON tasks(assigned_by);

CREATE INDEX IF NOT EXISTS idx_task_history_task_id ON task_history(task_id);
CREATE INDEX IF NOT EXISTS idx_task_history_created_at ON task_history(created_at);
CREATE INDEX IF NOT EXISTS idx_task_history_updated_by ON task_history(updated_by);
