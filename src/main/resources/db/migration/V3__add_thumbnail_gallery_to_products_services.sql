-- Add thumbnail/gallery columns (idempotent for MySQL 8)

SET @db := DATABASE();

SET @sql := (
  SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='business_product' AND COLUMN_NAME='thumbnail_url') = 0,
    'ALTER TABLE `business_product` ADD COLUMN `thumbnail_url` VARCHAR(500) NULL',
    'SELECT 1'
  )
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (
  SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='business_product' AND COLUMN_NAME='gallery_urls') = 0,
    'ALTER TABLE `business_product` ADD COLUMN `gallery_urls` JSON NULL COMMENT ''JSON array of gallery image URLs''',
    'SELECT 1'
  )
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (
  SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='business_service' AND COLUMN_NAME='thumbnail_url') = 0,
    'ALTER TABLE `business_service` ADD COLUMN `thumbnail_url` VARCHAR(500) NULL',
    'SELECT 1'
  )
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (
  SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='business_service' AND COLUMN_NAME='gallery_urls') = 0,
    'ALTER TABLE `business_service` ADD COLUMN `gallery_urls` JSON NULL COMMENT ''JSON array of gallery image URLs''',
    'SELECT 1'
  )
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (
  SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='store_offered_product' AND COLUMN_NAME='thumbnail_url') = 0,
    'ALTER TABLE `store_offered_product` ADD COLUMN `thumbnail_url` VARCHAR(500) NULL',
    'SELECT 1'
  )
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (
  SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='store_offered_product' AND COLUMN_NAME='gallery_urls') = 0,
    'ALTER TABLE `store_offered_product` ADD COLUMN `gallery_urls` JSON NULL COMMENT ''JSON array of gallery image URLs''',
    'SELECT 1'
  )
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (
  SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='store_offered_service' AND COLUMN_NAME='thumbnail_url') = 0,
    'ALTER TABLE `store_offered_service` ADD COLUMN `thumbnail_url` VARCHAR(500) NULL',
    'SELECT 1'
  )
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (
  SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='store_offered_service' AND COLUMN_NAME='gallery_urls') = 0,
    'ALTER TABLE `store_offered_service` ADD COLUMN `gallery_urls` JSON NULL COMMENT ''JSON array of gallery image URLs''',
    'SELECT 1'
  )
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
