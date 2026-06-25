-- V6__seed_admin_user.sql
-- I added the migration for default admin account here the password is admin123, already hashed btw
INSERT INTO users (username, email, password, role, created_at, is_active)
VALUES
    ('admin', 'admin@gmail.com', '$2b$10$vXIG.VYdfPsb4O0b2ytnOexZ6iOERDVJNS1vN5I/4Nyqnr9L9mADm', 'ADMIN', UTC_TIMESTAMP(), true),
    ('admin1', 'admin1@gmail.com', '$2b$10$Vi1SS/FIPTKFxFnT3eDJBelhSfuXAG/zXM/.RmFwuScNxuJ25IlrG', 'ADMIN', UTC_TIMESTAMP(), true);