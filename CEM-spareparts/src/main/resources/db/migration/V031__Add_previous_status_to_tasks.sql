-- Add previous_status column to tasks table for tracking status before assignment
-- This allows tasks to be returned to their previous status when rejected by technicians
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS previous_status VARCHAR(50);

-- Add comment to explain the column purpose
COMMENT ON COLUMN tasks.previous_status IS 'Stores the previous status before assignment, used for rollback when technician rejects task';

-- Add index for better query performance
CREATE INDEX IF NOT EXISTS idx_tasks_previous_status ON tasks(previous_status);
