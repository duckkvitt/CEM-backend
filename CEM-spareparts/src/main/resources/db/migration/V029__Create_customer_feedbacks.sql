-- Create table for customer feedback on completed service requests
CREATE TABLE IF NOT EXISTS customer_feedbacks (
    id BIGSERIAL PRIMARY KEY,
    service_request_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    device_id BIGINT NOT NULL,
    service_type VARCHAR(50) NOT NULL,
    star_rating INTEGER NOT NULL CHECK (star_rating BETWEEN 1 AND 5),
    comment TEXT,
    technician_id BIGINT,
    submitted_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW() NOT NULL,
    created_by VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW() NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_feedback_service_request
        FOREIGN KEY (service_request_id) REFERENCES service_requests(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_feedback_service_request_id ON customer_feedbacks(service_request_id);
CREATE INDEX IF NOT EXISTS idx_feedback_customer_id ON customer_feedbacks(customer_id);
CREATE INDEX IF NOT EXISTS idx_feedback_star_rating ON customer_feedbacks(star_rating);
CREATE INDEX IF NOT EXISTS idx_feedback_submitted_at ON customer_feedbacks(submitted_at);
CREATE INDEX IF NOT EXISTS idx_feedback_service_type ON customer_feedbacks(service_type);


