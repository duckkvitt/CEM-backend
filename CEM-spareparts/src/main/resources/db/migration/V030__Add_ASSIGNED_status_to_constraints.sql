-- Add ASSIGNED status to service request constraints
-- This fixes the constraint violation when assigning tasks to technicians

-- Drop existing check constraints on service_requests table
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN (
        SELECT conname
        FROM pg_constraint c
        JOIN pg_class t ON t.oid = c.conrelid
        JOIN pg_namespace n ON n.oid = t.relnamespace
        WHERE c.contype = 'c'
          AND n.nspname = 'public'
          AND t.relname = 'service_requests'
          AND c.conname = 'service_requests_status_check'
    ) LOOP
        EXECUTE format('ALTER TABLE public.service_requests DROP CONSTRAINT IF EXISTS %I', r.conname);
    END LOOP;
END $$;

-- Drop existing check constraints on service_request_history table
DO $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN (
        SELECT conname
        FROM pg_constraint c
        JOIN pg_class t ON t.oid = c.conrelid
        JOIN pg_namespace n ON n.oid = t.relnamespace
        WHERE c.contype = 'c'
          AND n.nspname = 'public'
          AND t.relname = 'service_request_history'
          AND c.conname = 'service_request_history_status_check'
    ) LOOP
        EXECUTE format('ALTER TABLE public.service_request_history DROP CONSTRAINT IF EXISTS %I', r.conname);
    END LOOP;
END $$;

-- Recreate constraints with ASSIGNED status included
ALTER TABLE public.service_requests
ADD CONSTRAINT service_requests_status_check CHECK (status IN ('PENDING','APPROVED','ASSIGNED','REJECTED','IN_PROGRESS','COMPLETED'));

ALTER TABLE public.service_request_history
ADD CONSTRAINT service_request_history_status_check CHECK (status IN ('PENDING','APPROVED','ASSIGNED','REJECTED','IN_PROGRESS','COMPLETED'));
