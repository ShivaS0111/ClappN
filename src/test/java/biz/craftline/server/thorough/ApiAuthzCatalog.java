package biz.craftline.server.thorough;

import java.util.ArrayList;
import java.util.List;

/**
 * Canonical authz probe catalog: one representative call per secured route family.
 * Paths use placeholders replaced at runtime: {storeId} {businessId} {productId}
 * {serviceId} {storeServiceId} {customerId} {packageId} {userId} {brandId} {orderId}
 */
public final class ApiAuthzCatalog {

    public record Probe(String method, String path, String permission, String note) {
        public boolean isMutating() {
            return !"GET".equalsIgnoreCase(method);
        }
    }

    private ApiAuthzCatalog() {}

    public static List<Probe> all() {
        List<Probe> p = new ArrayList<>();

        // Identity / me (authenticated only)
        p.add(new Probe("GET", "/api/me/context", null, "auth only"));
        p.add(new Probe("GET", "/api/navigation-config", null, "auth only"));

        // Business
        p.add(new Probe("GET", "/api/business", "business.read", null));
        p.add(new Probe("GET", "/api/business/list", "business.read", null));
        p.add(new Probe("GET", "/api/business/{businessId}", "business.read", null));
        p.add(new Probe("POST", "/api/business/search", "business.read", "body {}"));
        p.add(new Probe("POST", "/api/business", "business.create", "body {}"));
        p.add(new Probe("PUT", "/api/business/{businessId}", "business.update", "body {}"));
        p.add(new Probe("POST", "/api/business/update-status", "business.update", "body {}"));

        // Stores
        p.add(new Probe("GET", "/api/stores", "store.read", null));
        p.add(new Probe("GET", "/api/stores/list", "store.read", null));
        p.add(new Probe("GET", "/api/stores/{storeId}", "store.read", null));
        p.add(new Probe("GET", "/api/stores/{storeId}/details", "store.read", null));
        p.add(new Probe("GET", "/api/stores/list/{businessId}", "store.read", null));
        p.add(new Probe("GET", "/api/stores/{storeId}/metrics", "store.metrics", null));
        p.add(new Probe("POST", "/api/stores/search", "store.read", "body {}"));
        p.add(new Probe("POST", "/api/stores", "store.create", "body {}"));
        p.add(new Probe("PUT", "/api/stores/{storeId}", "store.update", "body {}"));
        p.add(new Probe("DELETE", "/api/stores/{storeId}", "store.delete", null));
        p.add(new Probe("POST", "/api/stores/update-status", "store.update", "body {}"));

        // Catalog templates
        p.add(new Probe("GET", "/api/business-product/list", "product.read", null));
        p.add(new Probe("GET", "/api/business-product/{productId}", "product.read", null));
        p.add(new Probe("POST", "/api/business-product/search", "product.read", "body {\"keyword\":\"thorough\"}"));
        p.add(new Probe("POST", "/api/business-product", "product.create", "body {}"));
        p.add(new Probe("PUT", "/api/business-product/{productId}", "product.update", "body {}"));
        p.add(new Probe("DELETE", "/api/business-product/999999001", "product.delete", null));

        p.add(new Probe("GET", "/api/business-service/list", "service.read", null));
        p.add(new Probe("GET", "/api/business-service/{serviceId}", "service.read", null));
        p.add(new Probe("POST", "/api/business-service/search", "service.read", "body {\"keyword\":\"thorough\"}"));
        p.add(new Probe("POST", "/api/business-service", "service.create", "body {}"));

        p.add(new Probe("GET", "/api/brands", "brand.read", null));
        p.add(new Probe("GET", "/api/brands/{brandId}", "brand.read", null));
        p.add(new Probe("POST", "/api/brands", "brand.create", "body {}"));
        p.add(new Probe("DELETE", "/api/brands/999999001", "brand.delete", null));

        p.add(new Probe("GET", "/api/categories/list", "category.read", null));
        p.add(new Probe("POST", "/api/categories/add", "category.create", "body {}"));

        p.add(new Probe("GET", "/api/business-type/list", "business.read", null));

        // Store offerings
        p.add(new Probe("GET", "/api/store-product", "store_product.read", null));
        p.add(new Probe("GET", "/api/store-product/store/{storeId}", "store_product.read", null));
        p.add(new Probe("POST", "/api/store-product/save", "store_product.create", "body {}"));

        p.add(new Probe("GET", "/api/store-service", "store_service.read", null));
        p.add(new Probe("GET", "/api/store-service/store/{storeId}", "store_service.read", null));
        p.add(new Probe("POST", "/api/store-service/save", "store_service.create", "body {}"));

        p.add(new Probe("GET", "/api/store-offered-packages/store/{storeId}", "package.read", null));
        p.add(new Probe("GET", "/api/store-offered-packages/{packageId}", "package.read", null));
        p.add(new Probe("POST", "/api/store-offered-packages", "package.create", "body {}"));
        p.add(new Probe("DELETE", "/api/store-offered-packages/999999001", "package.delete", null));

        // Pricing (legacy /store prefix) — use non-existent ids so missing price → 404, not seed mutation
        p.add(new Probe("GET", "/store/lot/999999001/price", "pricing.read", null));
        p.add(new Probe("GET", "/store/service/{storeServiceId}/price", "pricing.read", null));
        p.add(new Probe("POST", "/store/lot/price/add", "pricing.create", "body {}"));

        // People
        p.add(new Probe("GET", "/api/employees", "user.read", null));
        p.add(new Probe("GET", "/api/employees/business/{businessId}", "user.read", null));
        p.add(new Probe("GET", "/api/employees/store/{storeId}", "user.read", null));
        p.add(new Probe("POST", "/api/employees", "user.create", "body {}"));
        p.add(new Probe("DELETE", "/api/employees/999999", "user.delete", null));

        p.add(new Probe("GET", "/api/users", "user.read", null));
        p.add(new Probe("GET", "/api/users/999999001", "user.read", null));
        p.add(new Probe("POST", "/api/users", "user.create", "body {}"));
        p.add(new Probe("DELETE", "/api/users/999999", "user.delete", null));
        p.add(new Probe("POST", "/api/users/999999001/roles/1", "user.permissions", null));

        p.add(new Probe("GET", "/api/permissions", "user.permissions", null));
        p.add(new Probe("POST", "/api/permissions", "user.permissions", "body {}"));

        // Customers
        p.add(new Probe("GET", "/api/customers", "customer.read", null));
        p.add(new Probe("GET", "/api/customers/{customerId}", "customer.read", null));
        p.add(new Probe("POST", "/api/customers", "customer.create", "body {}"));
        p.add(new Probe("DELETE", "/api/customers/999999", "customer.delete", null));

        // Orders / bookings
        p.add(new Probe("GET", "/api/orders", "order.read", null));
        p.add(new Probe("GET", "/api/orders/store/{storeId}", "order.read", null));
        p.add(new Probe("GET", "/api/orders/customer/{customerId}", "order.read", null));
        p.add(new Probe("POST", "/api/orders/new", "order.create", "body {}"));
        p.add(new Probe("POST", "/api/orders/1/cancel", "order.update", null));

        p.add(new Probe("GET", "/api/booking-details", "booking.read", null));
        p.add(new Probe("GET", "/api/booking-details/store/{storeId}", "booking.read", null));
        p.add(new Probe("POST", "/api/booking-details", "booking.create", "body {}"));
        p.add(new Probe("DELETE", "/api/booking-details/999999", "booking.delete", null));

        p.add(new Probe("GET", "/api/delivery-info", "order.read", null));
        p.add(new Probe("GET", "/api/virtual-product-details", "product.read", null));

        // Invoices / payments
        p.add(new Probe("GET", "/api/invoices", "invoice.read", null));
        p.add(new Probe("GET", "/api/invoices/store/{storeId}", "invoice.read", null));
        p.add(new Probe("POST", "/api/invoices/generate", "invoice.create", "body {}"));

        p.add(new Probe("POST", "/api/payments/initiate", "payment.create", "body {}"));
        p.add(new Probe("GET", "/api/payments/status/test-pay", "payment.read", null));
        p.add(new Probe("POST", "/api/payments/test-pay/refund", "payment.refund", "body {}"));

        // Inventory
        p.add(new Probe("GET", "/api/store-inventory/{storeId}", "inventory.read", null));
        p.add(new Probe("GET", "/api/store-inventory/{storeId}/low-stock", "inventory.read", null));
        p.add(new Probe("POST", "/api/store-inventory/{storeId}/{productId}/add?quantity=1", "inventory.create", null));
        p.add(new Probe("POST", "/api/store-inventory/{storeId}/{productId}/sell?quantity=1", "inventory.update", null));

        p.add(new Probe("GET", "/api/product-lots/all/{productId}", "inventory.read", null));
        p.add(new Probe("POST", "/api/product-lots", "inventory.create", "body {}"));
        p.add(new Probe("DELETE", "/api/product-lots/999999", "inventory.delete", null));

        // Addresses / files
        p.add(new Probe("GET", "/api/addresses", "address.read", null));
        p.add(new Probe("POST", "/api/addresses", "address.create", "body {}"));
        p.add(new Probe("DELETE", "/api/addresses/999999", "address.delete", null));

        p.add(new Probe("POST", "/api/files/upload", "file.upload", "multipart skipped→expect authz gate"));
        p.add(new Probe("DELETE", "/api/files?url=x", "file.delete", null));

        // Auth privileged
        p.add(new Probe("POST", "/api/auth/revoke-all-refresh-tokens/thorough.cashier@clapp.test",
                "user.permissions", null));

        return List.copyOf(p);
    }
}
