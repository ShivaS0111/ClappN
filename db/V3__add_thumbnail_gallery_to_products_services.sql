-- Add thumbnail and gallery fields to business_product table
ALTER TABLE `business_product` ADD COLUMN `thumbnail_url` VARCHAR(500) NULL;
ALTER TABLE `business_product` ADD COLUMN `gallery_urls` JSON NULL COMMENT 'JSON array of gallery image URLs';

-- Add thumbnail and gallery fields to business_service table
ALTER TABLE `business_service` ADD COLUMN `thumbnail_url` VARCHAR(500) NULL;
ALTER TABLE `business_service` ADD COLUMN `gallery_urls` JSON NULL COMMENT 'JSON array of gallery image URLs';

-- Add thumbnail and gallery fields to store_offered_product table
ALTER TABLE `store_offered_product` ADD COLUMN `thumbnail_url` VARCHAR(500) NULL;
ALTER TABLE `store_offered_product` ADD COLUMN `gallery_urls` JSON NULL COMMENT 'JSON array of gallery image URLs';

-- Add thumbnail and gallery fields to store_offered_service table
ALTER TABLE `store_offered_service` ADD COLUMN `thumbnail_url` VARCHAR(500) NULL;
ALTER TABLE `store_offered_service` ADD COLUMN `gallery_urls` JSON NULL COMMENT 'JSON array of gallery image URLs';

