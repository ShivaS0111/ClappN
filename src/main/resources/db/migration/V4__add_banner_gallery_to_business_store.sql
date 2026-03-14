-- Add banner and gallery fields to business table
ALTER TABLE `business` ADD COLUMN `banner_url` VARCHAR(500) NULL;
ALTER TABLE `business` ADD COLUMN `gallery_urls` JSON NULL COMMENT 'JSON array of gallery image URLs';

-- Add banner and gallery fields to store table
ALTER TABLE `store` ADD COLUMN `banner_url` VARCHAR(500) NULL;
ALTER TABLE `store` ADD COLUMN `gallery_urls` JSON NULL COMMENT 'JSON array of gallery image URLs';

