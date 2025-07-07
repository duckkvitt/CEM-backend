CREATE TABLE spare_parts
(
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    part_name             VARCHAR(255) NOT NULL,
    part_code             VARCHAR(255) NOT NULL UNIQUE,
    description           TEXT,
    compatible_devices    VARCHAR(255),
    quantity_in_stock     INT          NOT NULL,
    unit_of_measurement   VARCHAR(255) NOT NULL,
    supplier              VARCHAR(255),
    status                VARCHAR(50)  NOT NULL,
    created_at            DATETIME     NOT NULL,
    updated_at            DATETIME,
    CONSTRAINT chk_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
); 