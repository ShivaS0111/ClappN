-- Migration: Add new columns to booking_details table for complete booking flow
-- This supports the org → store → service-booking flow

ALTER TABLE booking_details ADD COLUMN IF NOT EXISTS store_id BIGINT;
ALTER TABLE booking_details ADD COLUMN IF NOT EXISTS customer_id BIGINT;
ALTER TABLE booking_details ADD COLUMN IF NOT EXISTS service_id BIGINT;
ALTER TABLE booking_details ADD COLUMN IF NOT EXISTS order_item_id BIGINT;
ALTER TABLE booking_details ADD COLUMN IF NOT EXISTS notes TEXT;
ALTER TABLE booking_details ADD COLUMN IF NOT EXISTS duration_minutes INT DEFAULT 30;

-- Add indexes for common queries
CREATE INDEX IF NOT EXISTS idx_booking_store_id ON booking_details(store_id);
CREATE INDEX IF NOT EXISTS idx_booking_customer_id ON booking_details(customer_id);
CREATE INDEX IF NOT EXISTS idx_booking_service_id ON booking_details(service_id);
CREATE INDEX IF NOT EXISTS idx_booking_staff_id ON booking_details(staff_id);
CREATE INDEX IF NOT EXISTS idx_booking_status ON booking_details(booking_status);
CREATE INDEX IF NOT EXISTS idx_booking_appointment_date ON booking_details(appointment_date);

