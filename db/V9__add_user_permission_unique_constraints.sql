-- Add unique constraints to prevent duplicate allowed/denied permission rows per user
ALTER TABLE IF EXISTS user_allowed_permissions
  ADD CONSTRAINT IF NOT EXISTS uq_user_allowed_user_permission UNIQUE (user_id, permission_id);

ALTER TABLE IF EXISTS user_denied_permissions
  ADD CONSTRAINT IF NOT EXISTS uq_user_denied_user_permission UNIQUE (user_id, permission_id);

-- OPTIONAL: Consider application-level prevention of entries existing in both tables simultaneously.

