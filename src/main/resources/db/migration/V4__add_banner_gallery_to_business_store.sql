-- Add banner/gallery columns on business and store (idempotent for MySQL 8)

SET @db := DATABASE();

SET @sql := (
  SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='business' AND COLUMN_NAME='banner_url') = 0,
    'ALTER TABLE `business` ADD COLUMN `banner_url` VARCHAR(500) NULL',
    'SELECT 1'
  )
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (
  SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='business' AND COLUMN_NAME='gallery_urls') = 0,
    'ALTER TABLE `business` ADD COLUMN `gallery_urls` JSON NULL COMMENT ''JSON array of gallery image URLs''',
    'SELECT 1'
  )
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (
  SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='store' AND COLUMN_NAME='banner_url') = 0,
    'ALTER TABLE `store` ADD COLUMN `banner_url` VARCHAR(500) NULL',
    'SELECT 1'
  )
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (
  SELECT IF(
    (SELECT COUNT(*) FROM information_schema.COLUMNS
      WHERE TABLE_SCHEMA=@db AND TABLE_NAME='store' AND COLUMN_NAME='gallery_urls') = 0,
    'ALTER TABLE `store` ADD COLUMN `gallery_urls` JSON NULL COMMENT ''JSON array of gallery image URLs''',
    'SELECT 1'
  )
);
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
