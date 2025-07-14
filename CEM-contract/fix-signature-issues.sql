-- Fix Digital Signature Issues
-- Step 1: Check if digital_certificates table exists
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'digital_certificates') THEN
        RAISE NOTICE 'Creating digital_certificates table...';
        
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
    ELSE
        RAISE NOTICE 'digital_certificates table already exists';
    END IF;
END $$;

-- Step 2: Check if digital_signature_records table exists
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'digital_signature_records') THEN
        RAISE NOTICE 'Creating digital_signature_records table...';
        
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
            
            signature_image_data BYTEA,
            signature_image_width INTEGER,
            signature_image_height INTEGER,
            
            page_number INTEGER,
            signature_x REAL,
            signature_y REAL,
            signature_width REAL,
            signature_height REAL,
            
            signature_value BYTEA,
            signature_hash VARCHAR(128),
            hash_algorithm VARCHAR(50),
            
            timestamp_url VARCHAR(500),
            timestamp_token BYTEA,
            timestamp_verified BOOLEAN DEFAULT FALSE,
            
            signature_verified BOOLEAN DEFAULT FALSE,
            certificate_verified BOOLEAN DEFAULT FALSE,
            verification_errors TEXT,
            last_verified_at TIMESTAMP,
            
            reason VARCHAR(500),
            location VARCHAR(255),
            contact_info VARCHAR(255),
            
            ip_address VARCHAR(45),
            user_agent TEXT,
            signed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
            
            CONSTRAINT fk_digital_signature_records_contract FOREIGN KEY (contract_id) REFERENCES contracts(id) ON DELETE CASCADE,
            CONSTRAINT fk_digital_signature_records_certificate FOREIGN KEY (certificate_id) REFERENCES digital_certificates(id) ON DELETE SET NULL
        );
    ELSE
        RAISE NOTICE 'digital_signature_records table already exists';
    END IF;
END $$;

-- Step 3: Insert test certificate data
INSERT INTO digital_certificates (
    certificate_name,
    subject_dn,
    issuer_dn,
    serial_number,
    certificate_type,
    status,
    valid_from,
    valid_to,
    certificate_data,
    private_key_data,
    key_algorithm,
    key_size,
    signature_algorithm,
    fingerprint_sha1,
    fingerprint_sha256,
    created_by,
    description
) VALUES (
    'CEM Test Signing Certificate',
    'CN=CEM Test Certificate, O=CEM Contract System, EmailAddress=test@cemcontract.com',
    'CN=CEM Test Certificate, O=CEM Contract System, EmailAddress=test@cemcontract.com',
    '12345678901234567890',
    'SELF_SIGNED',
    'ACTIVE',
    CURRENT_TIMESTAMP - INTERVAL '1 day',
    CURRENT_TIMESTAMP + INTERVAL '365 days',
    decode('', 'base64'), -- Will be populated by Java code
    decode('', 'base64'), -- Will be populated by Java code
    'RSA',
    2048,
    'SHA256withRSA',
    'test_sha1_fingerprint',
    'test_sha256_fingerprint_unique',
    'system',
    'Test certificate for CEM digital signature functionality'
) ON CONFLICT (fingerprint_sha256) DO NOTHING;

-- Step 4: Show current tables and data
SELECT 'Tables created successfully' as status;
SELECT COUNT(*) as certificate_count FROM digital_certificates; 