-- Increase length of phone fields in technician_profiles table
ALTER TABLE technician_profiles 
ALTER COLUMN phone TYPE VARCHAR(50),
ALTER COLUMN emergency_contact_phone TYPE VARCHAR(50);
