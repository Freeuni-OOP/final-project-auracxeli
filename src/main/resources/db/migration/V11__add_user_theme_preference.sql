-- Persist each user's theme choice so it follows them across sessions, browsers and devices.
ALTER TABLE users
    ADD COLUMN theme_preference VARCHAR(10) NOT NULL DEFAULT 'LIGHT';

ALTER TABLE users
    ADD CONSTRAINT chk_theme_preference CHECK (theme_preference IN ('LIGHT', 'DARK'));
