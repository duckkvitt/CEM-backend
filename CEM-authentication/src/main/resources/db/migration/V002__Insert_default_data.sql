-- Create default roles
INSERT INTO roles (name, description, created_at, updated_at) VALUES 
    ('USER', 'Standard user with basic access', NOW(), NOW()),
    ('ADMIN', 'Administrator with full access', NOW(), NOW()),
    ('MODERATOR', 'Moderator with limited administrative access', NOW(), NOW()),
    ('SUPER_ADMIN', 'Super administrator with ultimate access', NOW(), NOW())
ON CONFLICT (name) DO NOTHING;

-- Create default super admin user
-- Password: AdminCEM@2024
INSERT INTO users (
    email, 
    password, 
    first_name, 
    last_name, 
    phone, 
    role_id, 
    status, 
    email_verified, 
    login_attempts, 
    created_by,
    created_at, 
    updated_at
) VALUES (
    'admin@cem.com',
    '$2a$12$rjbf8B6rWaX9XVHrj2ht0.Iu4fOyQm5k7EcVzEGC1mNxfhKn5u6Ri', -- AdminCEM@2024
    'CEM',
    'Administrator',
    '+84123456789',
    (SELECT id FROM roles WHERE name = 'SUPER_ADMIN'),
    'ACTIVE',
    true,
    0,
    'SYSTEM',
    NOW(),
    NOW()
) ON CONFLICT (email) DO NOTHING; 