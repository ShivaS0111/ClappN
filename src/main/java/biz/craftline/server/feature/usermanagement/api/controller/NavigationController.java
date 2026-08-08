package biz.craftline.server.feature.usermanagement.api.controller;

import biz.craftline.server.config.security.UserScopeContext;
import biz.craftline.server.config.security.UserScopeContextHolder;
import biz.craftline.server.util.APIResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Returns navigation routes filtered by the current request's DB-resolved roles/permissions.
 */
@RestController
@RequestMapping("/api/navigation-config")
public class NavigationController {

    private static final List<Map<String, Object>> ROUTES = List.of(
        route("/dashboard", "Dashboard", "DashboardPage", List.of(), List.of()),
        route("/user-management", "User Management", "UserManagementPage",
                List.of("SYSTEM_ADMIN", "BUSINESS_OWNER"), List.of("user.read")),
        route("/privileges", "Role Privileges", "PrivilegesPage",
                List.of("SYSTEM_ADMIN", "BUSINESS_OWNER"), List.of("user.permissions")),
        route("/admin/users", "User Management", "AdminUsersPage",
                List.of("SYSTEM_ADMIN"), List.of("user.read")),
        route("/business/reports", "Business Reports", "BusinessReportsPage",
                List.of("BUSINESS_OWNER", "BUSINESS_ADMIN"), List.of()),
        route("/store/inventory", "Inventory", "InventoryPage",
                List.of("BUSINESS_OWNER", "BUSINESS_ADMIN", "STORE_OWNER", "STORE_MANAGER", "INVENTORY_MANAGER", "INVENTORY_STAFF"), List.of("inventory.read")),
        route("/store/orders", "Orders", "OrdersPage",
                List.of("BUSINESS_OWNER", "STORE_OWNER", "STORE_MANAGER", "SALES_ASSOCIATE", "CASHIER"), List.of("order.read")),
        route("/pos", "Point of Sale", "POSPage",
                List.of("BUSINESS_OWNER", "STORE_OWNER", "STORE_MANAGER", "SALES_ASSOCIATE", "CASHIER"), List.of("order.create")),
        route("/profile", "My Profile", "ProfilePage", List.of(), List.of())
    );

    private static Map<String, Object> route(String path, String label, String component,
                                             List<String> roles, List<String> permissions) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("path", path);
        map.put("label", label);
        map.put("component", component);
        map.put("requiredRoles", roles);
        map.put("requiredPermissions", permissions);
        return map;
    }

    @GetMapping
    public ResponseEntity<APIResponse<List<Map<String, Object>>>> getNavigationConfig() {
        UserScopeContext scope = UserScopeContextHolder.require();
        List<String> userRoles = scope.getRoles();
        Set<String> userPermissions = scope.getPermissions();

        List<Map<String, Object>> allowedRoutes = ROUTES.stream().filter(route -> {
            @SuppressWarnings("unchecked")
            List<String> requiredRoles = (List<String>) route.getOrDefault("requiredRoles", List.of());
            @SuppressWarnings("unchecked")
            List<String> requiredPermissions = (List<String>) route.getOrDefault("requiredPermissions", List.of());

            boolean roleAllowed = requiredRoles.isEmpty()
                    || userRoles.stream().anyMatch(r -> requiredRoles.stream().anyMatch(req -> req.equalsIgnoreCase(r)));
            boolean permAllowed = requiredPermissions.isEmpty()
                    || requiredPermissions.stream().allMatch(userPermissions::contains);
            return roleAllowed && permAllowed;
        }).collect(Collectors.toList());

        return APIResponse.success(allowedRoutes, "Navigation config");
    }
}
