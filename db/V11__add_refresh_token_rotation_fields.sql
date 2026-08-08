-- Add columns to support refresh token rotation and reuse-detection
ALTER TABLE refresh_tokens
  ADD COLUMN revoked BOOLEAN NOT NULL DEFAULT FALSE,
  ADD COLUMN replaced_by VARCHAR(255) NULL;

