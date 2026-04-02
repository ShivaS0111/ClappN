-- Migration: Allow NULL customer_id in orders table for POS walk-in orders
-- POS walk-in customers don't have a registered customer record
ALTER TABLE orders MODIFY COLUMN customer_id BIGINT NULL;

