-- Create technician profiles table to store additional technician information
CREATE TABLE technician_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    phone VARCHAR(20),
    location VARCHAR(100),
    skills TEXT, -- Comma-separated skills
    specializations TEXT, -- Comma-separated specializations
    certifications TEXT, -- Comma-separated certifications
    experience_years INTEGER DEFAULT 0,
    hourly_rate DECIMAL(10,2),
    max_concurrent_tasks INTEGER DEFAULT 8,
    working_hours_start TIME DEFAULT '08:00:00',
    working_hours_end TIME DEFAULT '17:00:00',
    working_days VARCHAR(20) DEFAULT 'MON,TUE,WED,THU,FRI', -- Comma-separated days
    emergency_contact_name VARCHAR(100),
    emergency_contact_phone VARCHAR(20),
    notes TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add indexes for performance
CREATE INDEX idx_technician_profiles_user_id ON technician_profiles(user_id);
CREATE INDEX idx_technician_profiles_location ON technician_profiles(location);
CREATE INDEX idx_technician_profiles_active ON technician_profiles(is_active);

-- Insert default profiles for existing technicians
-- This will be populated by the application when technicians first login
INSERT INTO technician_profiles (user_id, phone, location, skills, specializations, max_concurrent_tasks)
VALUES 
(6, '+1-555-0001', 'Downtown', 'Electrical,HVAC,Plumbing', 'Electrical Systems,Climate Control', 8),
(7, '+1-555-0002', 'Uptown', 'Electronics,Networking,Software', 'Network Infrastructure,System Integration', 6)
ON CONFLICT (user_id) DO NOTHING;
