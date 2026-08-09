-- Enhance booking_details for org → store → service-booking flow (idempotent MySQL 8)

SET @db := DATABASE();

SET @sql := (SELECT IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='booking_details' AND COLUMN_NAME='store_id')=0,
  'ALTER TABLE booking_details ADD COLUMN store_id BIGINT', 'SELECT 1'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (SELECT IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='booking_details' AND COLUMN_NAME='customer_id')=0,
  'ALTER TABLE booking_details ADD COLUMN customer_id BIGINT', 'SELECT 1'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (SELECT IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='booking_details' AND COLUMN_NAME='service_id')=0,
  'ALTER TABLE booking_details ADD COLUMN service_id BIGINT', 'SELECT 1'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (SELECT IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='booking_details' AND COLUMN_NAME='order_item_id')=0,
  'ALTER TABLE booking_details ADD COLUMN order_item_id BIGINT', 'SELECT 1'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (SELECT IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='booking_details' AND COLUMN_NAME='notes')=0,
  'ALTER TABLE booking_details ADD COLUMN notes TEXT', 'SELECT 1'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (SELECT IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='booking_details' AND COLUMN_NAME='duration_minutes')=0,
  'ALTER TABLE booking_details ADD COLUMN duration_minutes INT DEFAULT 30', 'SELECT 1'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (SELECT IF((SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='booking_details' AND INDEX_NAME='idx_booking_store_id')=0,
  'CREATE INDEX idx_booking_store_id ON booking_details(store_id)', 'SELECT 1'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (SELECT IF((SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='booking_details' AND INDEX_NAME='idx_booking_customer_id')=0,
  'CREATE INDEX idx_booking_customer_id ON booking_details(customer_id)', 'SELECT 1'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (SELECT IF((SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='booking_details' AND INDEX_NAME='idx_booking_service_id')=0,
  'CREATE INDEX idx_booking_service_id ON booking_details(service_id)', 'SELECT 1'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (SELECT IF((SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='booking_details' AND INDEX_NAME='idx_booking_staff_id')=0,
  'CREATE INDEX idx_booking_staff_id ON booking_details(staff_id)', 'SELECT 1'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (SELECT IF((SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='booking_details' AND INDEX_NAME='idx_booking_status')=0,
  'CREATE INDEX idx_booking_status ON booking_details(booking_status)', 'SELECT 1'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;

SET @sql := (SELECT IF((SELECT COUNT(*) FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=@db AND TABLE_NAME='booking_details' AND INDEX_NAME='idx_booking_appointment_date')=0,
  'CREATE INDEX idx_booking_appointment_date ON booking_details(appointment_date)', 'SELECT 1'));
PREPARE s FROM @sql; EXECUTE s; DEALLOCATE PREPARE s;
