-- V2: phone number login + password reset OTP

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS phone_number VARCHAR(20) UNIQUE,
    ADD COLUMN IF NOT EXISTS reset_token  VARCHAR(6),
    ADD COLUMN IF NOT EXISTS reset_token_expires_at TIMESTAMPTZ;

ALTER TABLE doctors
    ADD COLUMN IF NOT EXISTS phone_number VARCHAR(20) UNIQUE,
    ADD COLUMN IF NOT EXISTS reset_token  VARCHAR(6),
    ADD COLUMN IF NOT EXISTS reset_token_expires_at TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_users_phone   ON users(phone_number)   WHERE phone_number IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_doctors_phone ON doctors(phone_number) WHERE phone_number IS NOT NULL;
