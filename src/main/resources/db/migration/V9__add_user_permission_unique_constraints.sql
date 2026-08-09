-- Unique constraints on user allow/deny permission rows (idempotent MySQL 8)

SET @db := DATABASE();

SET @sql := (SELECT IF((SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA=@db AND TABLE_NAME='user_allowed_permissions' AND CONSTRAINT_NAME='uq_user_allowed_user_permission')=0,
  'ALTER TABLE user_allowed_permissions ADD CONSTRAINT uq_user_allowed_user_permission UNIQUE (user_id, permission_id)',
  'SELECT 1'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (SELECT IF((SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE TABLE_SCHEMA=@db AND TABLE_NAME='user_denied_permissions' AND CONSTRAINT_NAME='uq_user_denied_user_permission')=0,
  'ALTER TABLE user_denied_permissions ADD CONSTRAINT uq_user_denied_user_permission UNIQUE (user_id, permission_id)',
  'SELECT 1'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
