package biz.craftline.server.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Canonical roles and permissions aligned with:
 * - db/role-permissions-data.sql
 * - @RequirePermission annotations on controllers
 * - SecurityContextService / UserScopeResolver role checks
 */
public final class RbacSeedData {

    private RbacSeedData() {}

    public static final String[] ROLES = {
            "SYSTEM_ADMIN",
            "BUSINESS_OWNER",
            "BUSINESS_ADMIN",
            "BUSINESS_MANAGER",
            "STORE_OWNER",
            "STORE_MANAGER",
            "ASSISTANT_MANAGER",
            "SHIFT_SUPERVISOR",
            "SALES_ASSOCIATE",
            "CASHIER",
            "INVENTORY_STAFF",
            "CUSTOMER_SERVICE_REP",
            "STOCK_KEEPER",
            "VISUAL_MERCHANDISER",
            "SECURITY_STAFF",
            "CLEANING_STAFF",
            "INVENTORY_MANAGER",
            "SALES_MANAGER",
            "CUSTOMER_SERVICE",
            "FINANCE_MANAGER",
            "MARKETING_MANAGER",
            "SECURITY_OFFICER",
            "MAINTENANCE_STAFF",
            "CUSTOMER",
            "GUEST",
            "VENDOR_ADMIN",
            "VENDOR_MANAGER",
            "APPROVAL_MANAGER",
            "CONTENT_MANAGER",
            "ANALYTICS_MANAGER",
            "SUPPORT_MANAGER",
            "WAREHOUSE_MANAGER",
            "LOGISTICS_MANAGER",
            "QUALITY_ASSURANCE",
            "RETURNS_MANAGER"
    };

    /** Legacy PascalCase names → SCREAMING_SNAKE (for migration / rename). */
    public static final Map<String, String> LEGACY_ROLE_RENAMES = Map.ofEntries(
            Map.entry("SystemAdmin", "SYSTEM_ADMIN"),
            Map.entry("BusinessOwner", "BUSINESS_OWNER"),
            Map.entry("BusinessAdmin", "BUSINESS_ADMIN"),
            Map.entry("BusinessManager", "BUSINESS_MANAGER"),
            Map.entry("StoreOwner", "STORE_OWNER"),
            Map.entry("StoreManager", "STORE_MANAGER"),
            Map.entry("AssistantManager", "ASSISTANT_MANAGER"),
            Map.entry("ShiftSupervisor", "SHIFT_SUPERVISOR"),
            Map.entry("SalesAssociate", "SALES_ASSOCIATE"),
            Map.entry("Cashier", "CASHIER"),
            Map.entry("InventoryStaff", "INVENTORY_STAFF"),
            Map.entry("CustomerServiceRep", "CUSTOMER_SERVICE_REP"),
            Map.entry("StockKeeper", "STOCK_KEEPER"),
            Map.entry("VisualMerchandiser", "VISUAL_MERCHANDISER"),
            Map.entry("SecurityStaff", "SECURITY_STAFF"),
            Map.entry("CleaningStaff", "CLEANING_STAFF"),
            Map.entry("InventoryManager", "INVENTORY_MANAGER"),
            Map.entry("SalesManager", "SALES_MANAGER"),
            Map.entry("CustomerService", "CUSTOMER_SERVICE"),
            Map.entry("FinanceManager", "FINANCE_MANAGER"),
            Map.entry("MarketingManager", "MARKETING_MANAGER"),
            Map.entry("SecurityOfficer", "SECURITY_OFFICER"),
            Map.entry("MaintenanceStaff", "MAINTENANCE_STAFF"),
            Map.entry("Customer", "CUSTOMER"),
            Map.entry("Guest", "GUEST"),
            Map.entry("VendorAdmin", "VENDOR_ADMIN"),
            Map.entry("VendorManager", "VENDOR_MANAGER"),
            Map.entry("ApprovalManager", "APPROVAL_MANAGER"),
            Map.entry("ContentManager", "CONTENT_MANAGER"),
            Map.entry("AnalyticsManager", "ANALYTICS_MANAGER"),
            Map.entry("SupportManager", "SUPPORT_MANAGER"),
            Map.entry("WarehouseManager", "WAREHOUSE_MANAGER"),
            Map.entry("LogisticsManager", "LOGISTICS_MANAGER"),
            Map.entry("QualityAssurance", "QUALITY_ASSURANCE"),
            Map.entry("ReturnsManager", "RETURNS_MANAGER")
    );

    public static final String[] PERMISSIONS = {
            // Store
            "store.create", "store.read", "store.update", "store.delete", "store.metrics", "store.settings",
            // Product / service catalog
            "product.create", "product.read", "product.update", "product.delete", "product.inventory",
            "service.create", "service.read", "service.update", "service.delete",
            // Business
            "business.create", "business.read", "business.update", "business.delete",
            // Vendor
            "vendor.create", "vendor.read", "vendor.update", "vendor.delete",
            "vendor.approve", "vendor.suspend", "vendor.onboarding", "vendor.analytics",
            "vendor.commission.view", "vendor.commission.manage",
            // Category / brand / address / file (controllers)
            "category.create", "category.read", "category.update", "category.delete",
            "brand.create", "brand.read", "brand.update", "brand.delete",
            "address.create", "address.read", "address.update", "address.delete",
            "file.upload", "file.delete",
            // Coupons / offers / packages
            "coupon.create", "coupon.read", "coupon.update", "coupon.delete",
            "offer.create", "offer.read", "offer.update", "offer.delete",
            "package.create", "package.read", "package.update", "package.delete",
            // Approvals / reports
            "approval.create", "approval.read", "approval.update", "approval.delete",
            "approval.vendor", "approval.product", "approval.service",
            "approval.discount", "approval.refund", "approval.vendor_payout",
            "report.create", "report.read", "report.update", "report.schedule",
            "price_history.read", "price_history.update",
            // Store offered
            "store_product.create", "store_product.read", "store_product.update", "store_product.delete",
            "store_service.create", "store_service.read", "store_service.update", "store_service.delete",
            // Users
            "user.create", "user.read", "user.update", "user.delete", "user.permissions",
            // Orders / bookings / invoices / payments / pricing
            "order.create", "order.read", "order.update", "order.cancel", "order.refund", "order.delete",
            "booking.create", "booking.read", "booking.update", "booking.delete",
            "invoice.create", "invoice.read", "invoice.update", "invoice.delete",
            "payment.create", "payment.read", "payment.update", "payment.refund",
            "pricing.create", "pricing.read", "pricing.update", "pricing.delete",
            // Finance / reporting / marketing / system
            "finance.view", "finance.pricing", "finance.reports", "finance.coupons",
            "reports.sales", "reports.user", "reports.inventory", "reports.system",
            "marketing.campaigns", "marketing.packages", "marketing.analytics",
            "system.settings", "system.logs", "system.backup", "system.maintenance",
            "security.access", "security.audit", "security.monitor",
            // Content / analytics / customer
            "content.create", "content.read", "content.update", "content.delete", "content.publish", "content.review",
            "analytics.dashboard", "analytics.sales", "analytics.customer", "analytics.inventory", "analytics.vendor", "analytics.export",
            "customer.create", "customer.read", "customer.update", "customer.delete", "customer.preferences", "customer.segments",
            // Warehouse / shipping / inventory
            "warehouse.create", "warehouse.read", "warehouse.update", "warehouse.delete", "warehouse.operations", "warehouse.transfers",
            "shipping.create", "shipping.read", "shipping.update", "shipping.cancel", "shipping.track", "shipping.rates",
            "inventory.create", "inventory.read", "inventory.update", "inventory.delete",
            "inventory.adjust", "inventory.audit", "inventory.forecast", "inventory.alerts", "inventory.reorder",
            // Returns / quality / notifications / disputes
            "returns.create", "returns.read", "returns.approve", "returns.process", "returns.refund", "returns.reshelve",
            "quality.inspect", "quality.report", "quality.escalate", "quality.standards",
            "notification.create", "notification.read", "notification.broadcast", "notification.templates",
            "dispute.view", "dispute.create", "dispute.resolve", "dispute.escalate",
            // Subscriptions / reviews / tax / bulk / payout
            "subscription.create", "subscription.read", "subscription.manage", "subscription.cancel",
            "review.moderate", "review.respond", "review.hide", "review.analytics",
            "tax.manage", "tax.reports", "compliance.view", "compliance.audit",
            "bulk.import", "bulk.export", "bulk.update", "bulk.delete",
            "payout.process", "payout.view", "payout.dispute", "payout.reconcile"
    };

    /**
     * Role → permission names for seed. SYSTEM_ADMIN is handled separately (all permissions).
     * Mirrors db/role-permissions-data.sql plus controller-required API permissions.
     */
    public static Map<String, List<String>> rolePermissionMap() {
        Map<String, List<String>> map = new LinkedHashMap<>();

        map.put("BUSINESS_OWNER", List.of(
                "business.read", "business.update",
                "store.read", "store.create", "store.update", "store.metrics",
                "product.read", "product.create", "product.update", "product.inventory",
                "service.read", "service.create", "service.update",
                "category.read", "brand.read", "brand.create",
                "address.read", "address.create", "address.update",
                "file.upload",
                "user.read", "user.create", "user.update", "user.permissions",
                "order.read", "order.create", "order.update",
                "booking.read", "booking.create", "booking.update",
                "invoice.read", "invoice.create",
                "payment.read", "payment.create",
                "pricing.read", "pricing.create", "pricing.update",
                "store_product.create", "store_product.read", "store_product.update", "store_product.delete",
                "store_service.create", "store_service.read", "store_service.update", "store_service.delete",
                "package.create", "package.read", "package.update", "package.delete",
                "inventory.read", "inventory.create", "inventory.update", "inventory.adjust", "inventory.alerts",
                "customer.read", "customer.create", "customer.update",
                "finance.view", "finance.pricing", "finance.reports",
                "reports.sales", "reports.inventory",
                "marketing.campaigns", "marketing.packages", "marketing.analytics",
                "analytics.dashboard", "analytics.sales", "analytics.vendor",
                "vendor.read", "vendor.commission.view",
                "payout.view",
                "bulk.import", "bulk.export"
        ));

        map.put("BUSINESS_ADMIN", List.of(
                "store.read", "store.update", "store.metrics",
                "product.read", "product.create", "product.update",
                "service.read", "service.create", "service.update",
                "category.read", "brand.read",
                "address.read", "address.create", "address.update",
                "file.upload",
                "user.read", "user.create", "user.update",
                "order.read", "order.create", "order.update",
                "booking.read", "booking.create", "booking.update",
                "invoice.read", "invoice.create",
                "payment.read", "payment.create",
                "pricing.read", "pricing.update",
                "store_product.read", "store_product.update",
                "store_service.read", "store_service.update",
                "inventory.read", "inventory.update", "inventory.adjust",
                "customer.read", "customer.create", "customer.update",
                "finance.view",
                "reports.sales",
                "analytics.dashboard", "analytics.sales",
                "vendor.read",
                "bulk.import", "bulk.export"
        ));

        map.put("BUSINESS_MANAGER", List.of(
                "store.read", "store.update", "store.metrics",
                "product.read", "product.create", "product.update",
                "service.read", "service.update",
                "category.read", "brand.read",
                "address.read",
                "user.read",
                "order.read", "order.create", "order.update",
                "booking.read", "booking.create",
                "invoice.read",
                "payment.read",
                "pricing.read",
                "store_product.read", "store_service.read",
                "inventory.read",
                "customer.read", "customer.create",
                "reports.sales",
                "analytics.dashboard", "analytics.sales"
        ));

        map.put("STORE_OWNER", List.of(
                "store.read", "store.update", "store.delete", "store.metrics", "store.settings",
                "product.read", "product.create", "product.update", "product.delete", "product.inventory",
                "service.read", "service.create", "service.update", "service.delete",
                "category.read", "brand.read", "brand.create",
                "address.read", "address.create", "address.update", "address.delete",
                "file.upload", "file.delete",
                "order.read", "order.create", "order.update", "order.cancel", "order.refund", "order.delete",
                "booking.read", "booking.create", "booking.update", "booking.delete",
                "invoice.read", "invoice.create",
                "payment.read", "payment.create", "payment.refund",
                "pricing.read", "pricing.create", "pricing.update",
                "finance.view", "finance.pricing", "finance.reports",
                "user.read", "user.create", "user.update", "user.delete", "user.permissions",
                "reports.sales", "reports.inventory", "reports.user",
                "analytics.dashboard", "analytics.sales", "analytics.inventory",
                "inventory.create", "inventory.read", "inventory.update", "inventory.delete",
                "inventory.adjust", "inventory.audit", "inventory.forecast", "inventory.alerts",
                "marketing.campaigns",
                "coupon.create", "coupon.read", "coupon.update",
                "store_product.create", "store_product.read", "store_product.update", "store_product.delete",
                "store_service.create", "store_service.read", "store_service.update", "store_service.delete",
                "package.create", "package.read", "package.update", "package.delete",
                "customer.create", "customer.read", "customer.update", "customer.delete",
                "bulk.import", "bulk.export", "bulk.update",
                "warehouse.read", "warehouse.operations",
                "approval.discount", "approval.refund",
                "returns.read", "returns.approve",
                "payout.view"
        ));

        map.put("STORE_MANAGER", List.of(
                "store.read", "store.update", "store.metrics",
                "product.read", "product.update", "product.inventory",
                "service.read", "service.update",
                "category.read", "brand.read",
                "address.read", "address.create", "address.update",
                "file.upload",
                "order.read", "order.create", "order.update", "order.cancel",
                "booking.read", "booking.create", "booking.update",
                "invoice.read", "invoice.create",
                "payment.read", "payment.create", "payment.refund",
                "pricing.read", "pricing.update",
                "finance.view",
                "user.read", "user.create", "user.update",
                "reports.sales", "reports.inventory",
                "analytics.dashboard", "analytics.sales",
                "inventory.create", "inventory.read", "inventory.update",
                "inventory.adjust", "inventory.alerts", "inventory.audit",
                "store_product.read", "store_product.update",
                "store_service.read", "store_service.update",
                "package.create", "package.read", "package.update", "package.delete",
                "customer.create", "customer.read", "customer.update",
                "marketing.campaigns",
                "bulk.import", "bulk.export",
                "warehouse.read", "warehouse.operations",
                "returns.read", "returns.approve"
        ));

        map.put("ASSISTANT_MANAGER", List.of(
                "store.read", "store.update",
                "product.read", "product.update", "product.inventory",
                "service.read", "service.update",
                "order.read", "order.create", "order.update",
                "booking.read", "booking.create",
                "invoice.read",
                "payment.read", "payment.create",
                "pricing.read",
                "user.read", "user.create", "user.update",
                "reports.sales",
                "inventory.read", "inventory.update", "inventory.adjust", "inventory.alerts",
                "store_product.read", "store_product.update",
                "store_service.read", "store_service.update",
                "customer.read", "customer.create",
                "analytics.dashboard",
                "returns.read"
        ));

        map.put("SHIFT_SUPERVISOR", List.of(
                "store.read",
                "order.read", "order.update",
                "booking.read",
                "payment.read",
                "user.read",
                "product.read", "product.inventory",
                "inventory.read", "inventory.alerts",
                "customer.read",
                "reports.sales",
                "analytics.dashboard"
        ));

        map.put("SALES_ASSOCIATE", List.of(
                "store.read",
                "product.read",
                "order.create", "order.read",
                "booking.read", "booking.create",
                "customer.read", "customer.create",
                "store_product.read",
                "payment.create", "payment.read"
        ));

        map.put("CASHIER", List.of(
                "store.read",
                "order.create", "order.read",
                "product.read",
                "finance.view",
                "store_product.read",
                "payment.create", "payment.read", "payment.refund",
                "invoice.read", "invoice.create",
                "customer.read", "customer.create",
                "pricing.read"
        ));

        map.put("INVENTORY_STAFF", List.of(
                "store.read",
                "product.read", "product.inventory",
                "inventory.create", "inventory.read", "inventory.update",
                "inventory.adjust", "inventory.alerts",
                "reports.inventory",
                "warehouse.read",
                "store_product.read"
        ));

        map.put("INVENTORY_MANAGER", List.of(
                "product.read", "product.update", "product.inventory",
                "inventory.create", "inventory.read", "inventory.update", "inventory.delete",
                "inventory.adjust", "inventory.audit", "inventory.forecast", "inventory.alerts", "inventory.reorder",
                "reports.inventory",
                "warehouse.read", "warehouse.operations",
                "bulk.import", "bulk.export",
                "analytics.inventory",
                "store_product.read", "store_product.update"
        ));

        map.put("CUSTOMER_SERVICE_REP", List.of(
                "store.read",
                "order.read", "order.update", "order.cancel", "order.refund",
                "booking.read", "booking.update",
                "product.read",
                "customer.read", "customer.update",
                "returns.create", "returns.read", "returns.approve",
                "dispute.view", "dispute.resolve",
                "notification.create",
                "store_product.read"
        ));

        map.put("FINANCE_MANAGER", List.of(
                "finance.view", "finance.pricing", "finance.reports", "finance.coupons",
                "reports.sales",
                "analytics.dashboard", "analytics.sales",
                "order.read",
                "invoice.read", "invoice.create",
                "payment.read", "payment.refund",
                "pricing.read", "pricing.update",
                "payout.view", "payout.process",
                "vendor.commission.view",
                "tax.manage", "tax.reports"
        ));

        map.put("CUSTOMER", List.of(
                "product.read",
                "order.read", "order.create",
                "booking.read", "booking.create",
                "customer.read", "customer.preferences",
                "review.respond",
                "dispute.create",
                "returns.create"
        ));

        return map;
    }
}
