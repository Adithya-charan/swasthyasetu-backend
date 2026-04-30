-- MySQL-compatible data seeding
-- Hibernate handles table creation, this script populates them.

INSERT IGNORE INTO users (id, full_name, email, password_hash, role, phone, is_active, account_status, created_at, updated_at)
VALUES 
('d50b4d45-5d93-4e8f-8f83-e18e5b6140b0', 'Admin User', 'admin@swasthyasetu.com', '$2a$10$o3f.A6Yg.Q.hK0.y1F2o/.GQQO9q2C1xON4iP4T1z5QvJ.U8MInG', 'ADMIN', '9999999999', true, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('c7ab2bf2-1fc5-4c6e-826c-d23dd88d9cb6', 'Dr. Smith', 'smith@swasthyasetu.com', '$2a$10$o3f.A6Yg.Q.hK0.y1F2o/.GQQO9q2C1xON4iP4T1z5QvJ.U8MInG', 'DOCTOR', '8888888888', true, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('a2c8f8b9-8e7c-473d-8d48-39e2e690fd1f', 'John Doe', 'patient@swasthyasetu.com', '$2a$10$o3f.A6Yg.Q.hK0.y1F2o/.GQQO9q2C1xON4iP4T1z5QvJ.U8MInG', 'PATIENT', '7777777777', true, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT IGNORE INTO doctor_profiles (id, specialization, qualifications, experience_years, license_number, consultation_fee, bio, verification_status)
VALUES 
('c7ab2bf2-1fc5-4c6e-826c-d23dd88d9cb6', 'Cardiologist', 'MBBS, MD', 10, 'LIC-12345', 500.00, 'Experienced Cardiologist', 'VERIFIED');

INSERT IGNORE INTO medicines (id, name, generic_name, manufacturer, category, stock_quantity, unit_price, reorder_level, created_at, updated_at)
VALUES 
(UUID(), 'Paracetamol 500mg', 'Paracetamol', 'PharmaCorp', 'Painkiller', 1000, 2.50, 100, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(UUID(), 'Amoxicillin 250mg', 'Amoxicillin', 'HealthInc', 'Antibiotic', 500, 5.00, 50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
