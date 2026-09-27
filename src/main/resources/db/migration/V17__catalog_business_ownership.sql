-- Tenant ownership on master catalog (nullable = platform template).
-- Relaxes global unique names so different businesses can reuse product/service names.

ALTER TABLE business_product
    ADD COLUMN business_id BIGINT NULL;

ALTER TABLE business_service
    ADD COLUMN business_id BIGINT NULL;

CREATE INDEX idx_business_product_business_id ON business_product (business_id);
CREATE INDEX idx_business_service_business_id ON business_service (business_id);

-- Drop single-column unique indexes created by Hibernate unique=true (names typically match column).
-- If an environment uses a different UK name, adjust manually once.
SET @db := DATABASE();

SET @bp_uk := (
    SELECT INDEX_NAME FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'business_product'
      AND COLUMN_NAME = 'name' AND NON_UNIQUE = 0
    LIMIT 1
);
SET @sql := IF(@bp_uk IS NOT NULL,
    CONCAT('ALTER TABLE business_product DROP INDEX `', @bp_uk, '`'),
    'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @bs_uk := (
    SELECT INDEX_NAME FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'business_service'
      AND COLUMN_NAME = 'service_name' AND NON_UNIQUE = 0
    LIMIT 1
);
SET @sql2 := IF(@bs_uk IS NOT NULL,
    CONCAT('ALTER TABLE business_service DROP INDEX `', @bs_uk, '`'),
    'SELECT 1');
PREPARE stmt2 FROM @sql2; EXECUTE stmt2; DEALLOCATE PREPARE stmt2;
