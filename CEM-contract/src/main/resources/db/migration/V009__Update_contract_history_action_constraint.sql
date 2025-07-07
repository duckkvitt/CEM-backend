-- Migration to update contract_history action constraint to match ContractAction enum
-- Drop existing action constraint if it exists
ALTER TABLE contract_history DROP CONSTRAINT IF EXISTS contract_history_action_check;

-- Add new action constraint with all enum values from ContractAction
ALTER TABLE contract_history ADD CONSTRAINT contract_history_action_check 
    CHECK (action IN ('CREATED', 'UPDATED', 'STATUS_CHANGED', 'SIGNED', 'HIDDEN', 'RESTORED', 'CANCELLED', 'FILE_UPLOADED', 'DIGITAL_SIGNATURE_ADDED')); 