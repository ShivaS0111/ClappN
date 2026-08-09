-- Allow NULL customer_id on orders for POS walk-in customers
ALTER TABLE orders MODIFY COLUMN customer_id BIGINT NULL;
