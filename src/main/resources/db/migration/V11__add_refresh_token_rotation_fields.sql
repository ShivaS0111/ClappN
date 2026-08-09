-- Refresh token rotation + reuse detection (idempotent MySQL 8)

SET @db := DATABASE();

SET @sql := (SELECT IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='refresh_tokens' AND COLUMN_NAME='revoked')=0,
  'ALTER TABLE refresh_tokens ADD COLUMN revoked BOOLEAN NOT NULL DEFAULT FALSE', 'SELECT 1'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (SELECT IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='refresh_tokens' AND COLUMN_NAME='replaced_by')=0,
  'ALTER TABLE refresh_tokens ADD COLUMN replaced_by VARCHAR(255) NULL', 'SELECT 1'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
