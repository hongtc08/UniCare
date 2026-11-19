-- Add emergency intake tracking fields to medical_checkups table.
BEGIN;
ALTER TABLE medical_checkups ADD COLUMN IF NOT EXISTS emergency_level VARCHAR(20);
ALTER TABLE medical_checkups ADD COLUMN IF NOT EXISTS emergency_type VARCHAR(50);
ALTER TABLE medical_checkups ADD COLUMN IF NOT EXISTS reporter_contact VARCHAR(150);
ALTER TABLE medical_checkups ADD COLUMN IF NOT EXISTS current_step VARCHAR(30);
COMMIT;
