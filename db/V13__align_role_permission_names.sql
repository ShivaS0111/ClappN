-- Align legacy PascalCase role names with SCREAMING_SNAKE used by
-- SecurityContextService, UserScopeResolver, and @RequirePermission controllers.
-- Safe if canonical names already exist (UPDATE affects 0 rows).

UPDATE `role` SET `name` = 'SYSTEM_ADMIN' WHERE `name` = 'SystemAdmin'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'SYSTEM_ADMIN') t);
UPDATE `role` SET `name` = 'BUSINESS_OWNER' WHERE `name` = 'BusinessOwner'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'BUSINESS_OWNER') t);
UPDATE `role` SET `name` = 'BUSINESS_ADMIN' WHERE `name` = 'BusinessAdmin'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'BUSINESS_ADMIN') t);
UPDATE `role` SET `name` = 'BUSINESS_MANAGER' WHERE `name` = 'BusinessManager'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'BUSINESS_MANAGER') t);
UPDATE `role` SET `name` = 'STORE_OWNER' WHERE `name` = 'StoreOwner'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'STORE_OWNER') t);
UPDATE `role` SET `name` = 'STORE_MANAGER' WHERE `name` = 'StoreManager'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'STORE_MANAGER') t);
UPDATE `role` SET `name` = 'ASSISTANT_MANAGER' WHERE `name` = 'AssistantManager'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'ASSISTANT_MANAGER') t);
UPDATE `role` SET `name` = 'SHIFT_SUPERVISOR' WHERE `name` = 'ShiftSupervisor'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'SHIFT_SUPERVISOR') t);
UPDATE `role` SET `name` = 'SALES_ASSOCIATE' WHERE `name` = 'SalesAssociate'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'SALES_ASSOCIATE') t);
UPDATE `role` SET `name` = 'CASHIER' WHERE `name` = 'Cashier'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'CASHIER') t);
UPDATE `role` SET `name` = 'INVENTORY_STAFF' WHERE `name` = 'InventoryStaff'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'INVENTORY_STAFF') t);
UPDATE `role` SET `name` = 'CUSTOMER_SERVICE_REP' WHERE `name` = 'CustomerServiceRep'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'CUSTOMER_SERVICE_REP') t);
UPDATE `role` SET `name` = 'STOCK_KEEPER' WHERE `name` = 'StockKeeper'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'STOCK_KEEPER') t);
UPDATE `role` SET `name` = 'VISUAL_MERCHANDISER' WHERE `name` = 'VisualMerchandiser'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'VISUAL_MERCHANDISER') t);
UPDATE `role` SET `name` = 'SECURITY_STAFF' WHERE `name` = 'SecurityStaff'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'SECURITY_STAFF') t);
UPDATE `role` SET `name` = 'CLEANING_STAFF' WHERE `name` = 'CleaningStaff'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'CLEANING_STAFF') t);
UPDATE `role` SET `name` = 'INVENTORY_MANAGER' WHERE `name` = 'InventoryManager'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'INVENTORY_MANAGER') t);
UPDATE `role` SET `name` = 'SALES_MANAGER' WHERE `name` = 'SalesManager'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'SALES_MANAGER') t);
UPDATE `role` SET `name` = 'CUSTOMER_SERVICE' WHERE `name` = 'CustomerService'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'CUSTOMER_SERVICE') t);
UPDATE `role` SET `name` = 'FINANCE_MANAGER' WHERE `name` = 'FinanceManager'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'FINANCE_MANAGER') t);
UPDATE `role` SET `name` = 'MARKETING_MANAGER' WHERE `name` = 'MarketingManager'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'MARKETING_MANAGER') t);
UPDATE `role` SET `name` = 'SECURITY_OFFICER' WHERE `name` = 'SecurityOfficer'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'SECURITY_OFFICER') t);
UPDATE `role` SET `name` = 'MAINTENANCE_STAFF' WHERE `name` = 'MaintenanceStaff'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'MAINTENANCE_STAFF') t);
UPDATE `role` SET `name` = 'VENDOR_ADMIN' WHERE `name` = 'VendorAdmin'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'VENDOR_ADMIN') t);
UPDATE `role` SET `name` = 'VENDOR_MANAGER' WHERE `name` = 'VendorManager'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'VENDOR_MANAGER') t);
UPDATE `role` SET `name` = 'APPROVAL_MANAGER' WHERE `name` = 'ApprovalManager'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'APPROVAL_MANAGER') t);
UPDATE `role` SET `name` = 'CONTENT_MANAGER' WHERE `name` = 'ContentManager'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'CONTENT_MANAGER') t);
UPDATE `role` SET `name` = 'ANALYTICS_MANAGER' WHERE `name` = 'AnalyticsManager'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'ANALYTICS_MANAGER') t);
UPDATE `role` SET `name` = 'SUPPORT_MANAGER' WHERE `name` = 'SupportManager'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'SUPPORT_MANAGER') t);
UPDATE `role` SET `name` = 'WAREHOUSE_MANAGER' WHERE `name` = 'WarehouseManager'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'WAREHOUSE_MANAGER') t);
UPDATE `role` SET `name` = 'LOGISTICS_MANAGER' WHERE `name` = 'LogisticsManager'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'LOGISTICS_MANAGER') t);
UPDATE `role` SET `name` = 'QUALITY_ASSURANCE' WHERE `name` = 'QualityAssurance'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'QUALITY_ASSURANCE') t);
UPDATE `role` SET `name` = 'RETURNS_MANAGER' WHERE `name` = 'ReturnsManager'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'RETURNS_MANAGER') t);
-- Customer / Guest: only rename when exact PascalCase (and not already SCREAMING)
UPDATE `role` SET `name` = 'CUSTOMER' WHERE `name` = 'Customer'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'CUSTOMER') t);
UPDATE `role` SET `name` = 'GUEST' WHERE `name` = 'Guest'
  AND NOT EXISTS (SELECT 1 FROM (SELECT `id` FROM `role` WHERE `name` = 'GUEST') t);

-- Controller-aligned permissions missing from older seeds
INSERT IGNORE INTO `permission` (`name`) VALUES
('booking.create'), ('booking.read'), ('booking.update'), ('booking.delete'),
('invoice.create'), ('invoice.read'), ('invoice.update'), ('invoice.delete'),
('payment.create'), ('payment.read'), ('payment.update'), ('payment.refund'),
('pricing.create'), ('pricing.read'), ('pricing.update'), ('pricing.delete'),
('inventory.create'), ('inventory.read'), ('inventory.update'), ('inventory.delete'),
('address.create'), ('address.read'), ('address.update'), ('address.delete'),
('brand.create'), ('brand.read'), ('brand.update'), ('brand.delete'),
('file.upload'), ('file.delete'),
('order.delete'),
('store_product.create'), ('store_product.read'), ('store_product.update'), ('store_product.delete'),
('store_service.create'), ('store_service.read'), ('store_service.update'), ('store_service.delete'),
('customer.create'), ('customer.read'), ('customer.update'), ('customer.delete'),
('user.permissions');

-- Grant new API permissions to key operational roles (idempotent via INSERT IGNORE if unique)
INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id FROM `role` r CROSS JOIN `permission` p
WHERE r.name = 'SYSTEM_ADMIN';

INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id FROM `role` r
JOIN `permission` p ON p.name IN (
  'booking.read', 'booking.create', 'booking.update',
  'invoice.read', 'invoice.create',
  'payment.read', 'payment.create',
  'pricing.read', 'pricing.create', 'pricing.update',
  'inventory.read', 'inventory.create', 'inventory.update',
  'address.read', 'address.create', 'address.update',
  'brand.read', 'brand.create',
  'file.upload',
  'store_product.read', 'store_product.create', 'store_product.update',
  'store_service.read', 'store_service.create', 'store_service.update',
  'customer.read', 'customer.create', 'customer.update',
  'user.permissions', 'order.create', 'order.update'
)
WHERE r.name IN ('BUSINESS_OWNER', 'STORE_OWNER', 'STORE_MANAGER');

INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id FROM `role` r
JOIN `permission` p ON p.name IN (
  'payment.create', 'payment.read', 'invoice.read', 'invoice.create',
  'pricing.read', 'customer.read', 'customer.create'
)
WHERE r.name = 'CASHIER';

INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT r.id, p.id FROM `role` r
JOIN `permission` p ON p.name IN (
  'inventory.create', 'inventory.read', 'inventory.update'
)
WHERE r.name IN ('INVENTORY_STAFF', 'INVENTORY_MANAGER');
