-- Business catalog amount = optional default sell price (store may override via store_item_price).
-- Not mandatory: allow NULL so "unset" is distinct from 0.

SET @db := DATABASE();

SET @sql := (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA=@db AND TABLE_NAME='business_product' AND COLUMN_NAME='amount')>0,
  'ALTER TABLE business_product MODIFY COLUMN amount FLOAT NULL',
  'SELECT 1'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (SELECT IF(
  (SELECT COUNT(*) FROM information_schema.COLUMNS
   WHERE TABLE_SCHEMA=@db AND TABLE_NAME='business_service' AND COLUMN_NAME='amount')>0,
  'ALTER TABLE business_service MODIFY COLUMN amount FLOAT NULL',
  'SELECT 1'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
