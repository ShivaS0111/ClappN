-- Migration: Add GST, discount, coupon, and item name fields to orders
-- Supports per-item GST/discount and bill-level discount/coupon for POS

-- Order-level pricing breakdown
ALTER TABLE orders ADD COLUMN IF NOT EXISTS subtotal DECIMAL(38,2);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS total_gst DECIMAL(38,2);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS total_discount DECIMAL(38,2);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS bill_discount DECIMAL(38,2);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS bill_discount_type VARCHAR(20);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS coupon_code VARCHAR(100);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS notes TEXT;

-- Order item-level pricing details
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS item_name VARCHAR(255);
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS gst_percentage DOUBLE;
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS gst_amount DOUBLE;
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS sgst_percentage DOUBLE;
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS cgst_percentage DOUBLE;
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS discount_amount DOUBLE;
ALTER TABLE order_items ADD COLUMN IF NOT EXISTS discount_type VARCHAR(20);

