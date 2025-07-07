-- Migration to update contract status constraint to match ContractStatus enum
-- Drop existing status constraint if it exists
ALTER TABLE contracts DROP CONSTRAINT IF EXISTS contracts_status_check;

-- Add new status constraint with updated enum values
ALTER TABLE contracts ADD CONSTRAINT contracts_status_check 
    CHECK (status IN ('DRAFT', 'PENDING_SELLER_SIGNATURE', 'PENDING_CUSTOMER_SIGNATURE', 'ACTIVE', 'REJECTED', 'CANCELLED', 'EXPIRED'));

-- Update any existing records with old status values to new ones
UPDATE contracts SET status = 'DRAFT' WHERE status = 'UNSIGNED';
UPDATE contracts SET status = 'ACTIVE' WHERE status = 'PAPER_SIGNED' OR status = 'DIGITALLY_SIGNED';

-- Update default value for status column
ALTER TABLE contracts ALTER COLUMN status SET DEFAULT 'DRAFT'; 