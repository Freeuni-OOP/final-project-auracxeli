-- V6__seed_admin_user.sql
-- I added the migration for default admin account here the password is admin123, already hashed btw
INSERT INTO users (username, email, password, role, created_at, is_active)
VALUES ('admin', 'admin@kartuli-games.local', '$2b$10$cATNVF/8UIyAFo.IbUkD6.H4j57ifU2zNopyB/8brYVmXcsGAqprC', 'ADMIN', NOW(), true);