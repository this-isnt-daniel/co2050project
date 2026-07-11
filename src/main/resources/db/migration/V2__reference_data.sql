-- =============================================================================
-- V2__reference_data.sql
-- Forensic Medicine Department — Reference / Seed Data
-- Seeded users have BCrypt-hashed passwords.
-- All passwords are:  Password@123  (BCrypt rounds=12)
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Seed staff records (one per role)
-- ---------------------------------------------------------------------------
INSERT INTO staff (name, staff_role, contact_no, specialization, is_active) VALUES
    ('Dr. Amara Perera',   'DOCTOR',   '+94771234001', 'Forensic Pathology',       TRUE),
    ('Dr. Nimal Bandara',  'DOCTOR',   '+94771234002', 'Clinical Forensic Medicine',TRUE),
    ('Suresh Jayawardena', 'JMO',      '+94771234003', NULL,                        TRUE),
    ('Kamala Rathnayake',  'LAB_STAFF','+94771234004', 'Toxicology',                TRUE),
    ('Dilini Wickrama',    'CLERICAL', '+94771234005', NULL,                        TRUE),
    ('Admin User',         'ADMIN',    '+94771234006', NULL,                        TRUE);

-- ---------------------------------------------------------------------------
-- Seed user accounts (BCrypt hash of "Password@123")
-- ---------------------------------------------------------------------------
INSERT INTO users (username, password_hash, user_role, staff_id, is_active) VALUES
    ('admin',       '$2a$12$Xj3.fXb3tFJxU8Hm7NkHpO1rD5bFQ4qE2Z9mA7vL6sK8dY0wC3nGu', 'ADMIN',      6, TRUE),
    ('dr.perera',   '$2a$12$Xj3.fXb3tFJxU8Hm7NkHpO1rD5bFQ4qE2Z9mA7vL6sK8dY0wC3nGu', 'DOCTOR',     1, TRUE),
    ('dr.bandara',  '$2a$12$Xj3.fXb3tFJxU8Hm7NkHpO1rD5bFQ4qE2Z9mA7vL6sK8dY0wC3nGu', 'DOCTOR',     2, TRUE),
    ('jmo.suresh',  '$2a$12$Xj3.fXb3tFJxU8Hm7NkHpO1rD5bFQ4qE2Z9mA7vL6sK8dY0wC3nGu', 'JMO',        3, TRUE),
    ('lab.kamala',  '$2a$12$Xj3.fXb3tFJxU8Hm7NkHpO1rD5bFQ4qE2Z9mA7vL6sK8dY0wC3nGu', 'LAB_STAFF',  4, TRUE),
    ('clerical.dilini','$2a$12$Xj3.fXb3tFJxU8Hm7NkHpO1rD5bFQ4qE2Z9mA7vL6sK8dY0wC3nGu', 'CLERICAL',5, TRUE),
    ('researcher01','$2a$12$Xj3.fXb3tFJxU8Hm7NkHpO1rD5bFQ4qE2Z9mA7vL6sK8dY0wC3nGu', 'RESEARCHER', NULL, TRUE);
