-- Align existing student records with Student.java. Existing data is preserved.
BEGIN;
ALTER TABLE students ADD COLUMN IF NOT EXISTS date_of_birth date;
ALTER TABLE students ADD COLUMN IF NOT EXISTS gender varchar(10);
-- Student profiles may be created before an academic class is assigned.
ALTER TABLE students ALTER COLUMN class_id DROP NOT NULL;
COMMIT;
