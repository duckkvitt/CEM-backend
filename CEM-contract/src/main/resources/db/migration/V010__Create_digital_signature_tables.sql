-- Create digital certificates table
CREATE TABLE digital_certificates (
    id BIGSERIAL PRIMARY KEY,
    certificate_name VARCHAR(255) NOT NULL,
    subject_dn TEXT NOT NULL,
    issuer_dn TEXT NOT NULL,
    serial_number VARCHAR(100) NOT NULL,
    certificate_type VARCHAR(50) NOT NULL CHECK (certificate_type IN ('SELF_SIGNED', 'CA_ISSUED', 'ORGANIZATION', 'PERSONAL', 'CODE_SIGNING', 'DOCUMENT_SIGNING')),
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'REVOKED', 'EXPIRED', 'PENDING', 'SUSPENDED')),
    valid_from TIMESTAMP NOT NULL,
    valid_to TIMESTAMP NOT NULL,
    certificate_data BYTEA NOT NULL,
    private_key_data BYTEA,
    key_algorithm VARCHAR(50),
    key_size INTEGER,
    signature_algorithm VARCHAR(100),
    fingerprint_sha1 VARCHAR(64),
    fingerprint_sha256 VARCHAR(64),
    created_by VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    description TEXT,
    
    CONSTRAINT uk_digital_certificates_serial_issuer UNIQUE (serial_number, issuer_dn),
    CONSTRAINT uk_digital_certificates_fingerprint_sha256 UNIQUE (fingerprint_sha256)
);

-- Create digital signature records table
CREATE TABLE digital_signature_records (
    id BIGSERIAL PRIMARY KEY,
    contract_id BIGINT NOT NULL,
    certificate_id BIGINT,
    signer_type VARCHAR(50) NOT NULL CHECK (signer_type IN ('STAFF', 'CUSTOMER', 'MANAGER')),
    signer_id BIGINT,
    signer_name VARCHAR(255) NOT NULL,
    signer_email VARCHAR(255) NOT NULL,
    signature_field_name VARCHAR(100),
    signature_algorithm VARCHAR(50) NOT NULL CHECK (signature_algorithm IN ('SHA256_WITH_RSA', 'SHA256_WITH_ECDSA', 'SHA384_WITH_RSA', 'SHA384_WITH_ECDSA', 'SHA512_WITH_RSA', 'SHA512_WITH_ECDSA', 'RSASSA_PSS_SHA256', 'ED25519', 'ED448')),
    status VARCHAR(50) NOT NULL DEFAULT 'VALID' CHECK (status IN ('VALID', 'INVALID', 'PENDING_VERIFICATION', 'VERIFICATION_FAILED', 'CERTIFICATE_REVOKED', 'CERTIFICATE_EXPIRED', 'CERTIFICATE_INVALID', 'TIMESTAMP_INVALID', 'DOCUMENT_MODIFIED', 'CORRUPTED')),
    
    -- Signature appearance data (canvas drawing)
    signature_image_data BYTEA,
    signature_image_width INTEGER,
    signature_image_height INTEGER,
    
    -- PDF signature position
    page_number INTEGER,
    signature_x REAL,
    signature_y REAL,
    signature_width REAL,
    signature_height REAL,
    
    -- Cryptographic signature data
    signature_value BYTEA,
    signature_hash VARCHAR(128),
    hash_algorithm VARCHAR(50),
    
    -- Timestamp information
    timestamp_url VARCHAR(500),
    timestamp_token BYTEA,
    timestamp_verified BOOLEAN DEFAULT FALSE,
    
    -- Signature verification info
    signature_verified BOOLEAN DEFAULT FALSE,
    certificate_verified BOOLEAN DEFAULT FALSE,
    verification_errors TEXT,
    last_verified_at TIMESTAMP,
    
    -- Signature metadata
    reason VARCHAR(500),
    location VARCHAR(255),
    contact_info VARCHAR(255),
    
    -- Audit trail
    ip_address VARCHAR(45),
    user_agent TEXT,
    signed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_digital_signature_records_contract FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE,
    CONSTRAINT fk_digital_signature_records_certificate FOREIGN KEY (certificate_id) REFERENCES digital_certificates(id) ON DELETE SET NULL
);

-- Create indexes for performance
CREATE INDEX idx_digital_certificates_status ON digital_certificates(status);
CREATE INDEX idx_digital_certificates_valid_dates ON digital_certificates(valid_from, valid_to);
CREATE INDEX idx_digital_certificates_created_by ON digital_certificates(created_by);
CREATE INDEX idx_digital_certificates_type_status ON digital_certificates(certificate_type, status);

CREATE INDEX idx_digital_signature_records_contract ON digital_signature_records(contract_id);
CREATE INDEX idx_digital_signature_records_signer ON digital_signature_records(signer_id, signer_type);
CREATE INDEX idx_digital_signature_records_certificate ON digital_signature_records(certificate_id);
CREATE INDEX idx_digital_signature_records_status ON digital_signature_records(status);
CREATE INDEX idx_digital_signature_records_signed_at ON digital_signature_records(signed_at);
CREATE INDEX idx_digital_signature_records_verification ON digital_signature_records(signature_verified, certificate_verified);

-- Add comments for documentation
COMMENT ON TABLE digital_certificates IS 'Digital certificates for PAdES-compliant signatures';
COMMENT ON TABLE digital_signature_records IS 'Records of digital signatures applied to contracts';

COMMENT ON COLUMN digital_certificates.certificate_data IS 'X.509 certificate binary data';
COMMENT ON COLUMN digital_certificates.private_key_data IS 'Encrypted private key data (if stored)';
COMMENT ON COLUMN digital_certificates.fingerprint_sha256 IS 'SHA-256 fingerprint for certificate identification';

COMMENT ON COLUMN digital_signature_records.signature_image_data IS 'PNG image from canvas signature';
COMMENT ON COLUMN digital_signature_records.signature_value IS 'Cryptographic signature bytes';
COMMENT ON COLUMN digital_signature_records.timestamp_token IS 'RFC 3161 timestamp token';
COMMENT ON COLUMN digital_signature_records.verification_errors IS 'JSON array of verification errors'; 