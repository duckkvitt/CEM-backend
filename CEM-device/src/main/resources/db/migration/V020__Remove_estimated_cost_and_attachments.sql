-- Remove columns estimated_cost and attachments as per requirements
-- Tasks table: drop estimated_cost only (keep actual_cost)
ALTER TABLE tasks
  DROP COLUMN IF EXISTS estimated_cost;

-- Service requests table: drop attachments and estimated/actual cost
ALTER TABLE service_requests
  DROP COLUMN IF EXISTS attachments,
  DROP COLUMN IF EXISTS estimated_cost,
  DROP COLUMN IF EXISTS actual_cost;


