-- Robustly drop ANY CHECK constraints on status columns, then recreate with full enum list

-- Helper DO block to drop all CHECK constraints on a given table.column
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
    ) LOOP
        EXECUTE format('ALTER TABLE public.service_requests DROP CONSTRAINT IF EXISTS %I', r.conname);
    END LOOP;
END $$;

ALTER TABLE public.service_requests
ADD CONSTRAINT service_requests_status_check CHECK (status IN ('PENDING','APPROVED','REJECTED','IN_PROGRESS','COMPLETED'));

-- service_request_history
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
    ) LOOP
        EXECUTE format('ALTER TABLE public.service_request_history DROP CONSTRAINT IF EXISTS %I', r.conname);
    END LOOP;
END $$;

ALTER TABLE public.service_request_history
ADD CONSTRAINT service_request_history_status_check CHECK (status IN ('PENDING','APPROVED','REJECTED','IN_PROGRESS','COMPLETED'));



