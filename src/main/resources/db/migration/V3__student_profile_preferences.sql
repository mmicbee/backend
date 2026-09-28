-- Migration V3: Add bio and notification preferences to student table for Issue #307
ALTER TABLE student ADD COLUMN IF NOT EXISTS bio TEXT;
ALTER TABLE student ADD COLUMN IF NOT EXISTS notify_course_updates BOOLEAN DEFAULT TRUE;
ALTER TABLE student ADD COLUMN IF NOT EXISTS notify_announcements BOOLEAN DEFAULT TRUE;
ALTER TABLE student ADD COLUMN IF NOT EXISTS notify_marketing BOOLEAN DEFAULT FALSE;
