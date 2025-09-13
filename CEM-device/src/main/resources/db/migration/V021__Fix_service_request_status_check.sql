-- Ensure status columns allow all expected enum values, including REJECTED

-- Fix constraint on service_requests.status
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.table_constraints tc
        WHERE tc.table_name = 'service_requests'
          AND tc.constraint_name = 'service_requests_status_check'
    ) THEN
        EXECUTE 'ALTER TABLE service_requests DROP CONSTRAINT service_requests_status_check';
    END IF;
END $$;

ALTER TABLE service_requests
ADD CONSTRAINT service_requests_status_check CHECK (status IN (
    'PENDING', 'APPROVED', 'REJECTED', 'IN_PROGRESS', 'COMPLETED'
));

-- Fix constraint on service_request_history.status
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.table_constraints tc
        WHERE tc.table_name = 'service_request_history'
          AND tc.constraint_name = 'service_request_history_status_check'
    ) THEN
        EXECUTE 'ALTER TABLE service_request_history DROP CONSTRAINT service_request_history_status_check';
    END IF;
END $$;

ALTER TABLE service_request_history
ADD CONSTRAINT service_request_history_status_check CHECK (status IN (
    'PENDING', 'APPROVED', 'REJECTED', 'IN_PROGRESS', 'COMPLETED'
));



