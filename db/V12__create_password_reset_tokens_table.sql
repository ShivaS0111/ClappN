-- Password reset tokens (persisted; replaces in-memory map)
CREATE TABLE IF NOT EXISTS password_reset_tokens (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  token VARCHAR(100) NOT NULL UNIQUE,
  email VARCHAR(255) NOT NULL,
  used BOOLEAN NOT NULL DEFAULT FALSE,
  expires_at TIMESTAMP NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_password_reset_token (token),
  INDEX idx_password_reset_email (email)
);
