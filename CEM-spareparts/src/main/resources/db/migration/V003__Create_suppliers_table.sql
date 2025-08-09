-- Create suppliers table
CREATE TABLE suppliers
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_name     VARCHAR(255) NOT NULL,
    contact_person   VARCHAR(255) NOT NULL,
    email            VARCHAR(255) NOT NULL,
    phone            VARCHAR(20)  NOT NULL,
    fax              VARCHAR(20),
    address          TEXT         NOT NULL,
    tax_code         VARCHAR(50),
    business_license VARCHAR(100),
    website          VARCHAR(255),
    description      TEXT,
    status           VARCHAR(50)  NOT NULL DEFAULT 'ACTIVE',
    created_at       DATETIME     NOT NULL,
    updated_at       DATETIME,
    CONSTRAINT chk_supplier_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'SUSPENDED'))
);

-- Create junction table for supplier spare part types
CREATE TABLE supplier_spare_part_types
(
    supplier_id      BIGINT       NOT NULL,
    spare_part_type  VARCHAR(255) NOT NULL,
    PRIMARY KEY (supplier_id, spare_part_type),
    FOREIGN KEY (supplier_id) REFERENCES suppliers (id) ON DELETE CASCADE
);