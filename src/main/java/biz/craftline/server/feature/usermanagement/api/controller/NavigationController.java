package biz.craftline.server.feature.usermanagement.api.controller;

import biz.craftline.server.feature.usermanagement.domain.service.RBACService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/navigation-config")
public class NavigationController {
    @Autowired
    private RBACService rbacService;

    // Static route config (should match frontend routes.json)
    private static final List<Map<String, Object>> ROUTES = List.of(
        Map.of("path", "/dashboard", "label", "Dashboard", "component", "DashboardPage", "requiredRoles", List.of("admin", "businessOwner", "storeManager", "storeAdmin", "storeEmployee")),
        Map.of("path", "/user-management", "label", "User Management", "component", "UserManagementPage", "requiredRoles", List.of("SystemAdmin", "BusinessOwner"), "requiredPermissions", List.of("user.read", "user.create", "user.update")),
        Map.of("path", "/privileges", "label", "Role Privileges", "component", "PrivilegesPage", "requiredRoles", List.of("SystemAdmin", "BusinessOwner"), "requiredPermissions", List.of("user.permissions")),
        Map.of("path", "/admin/users", "label", "User Management", "component", "AdminUsersPage", "requiredRoles", List.of("admin"), "requiredPermissions", List.of("manageUsers")),
        Map.of("path", "/business/reports", "label", "Business Reports", "component", "BusinessReportsPage", "requiredRoles", List.of("businessOwner")),
        Map.of("path", "/store/inventory", "label", "Inventory", "component", "InventoryPage", "requiredRoles", List.of("storeManager", "storeAdmin")),
        Map.of("path", "/store/orders", "label", "Orders", "component", "OrdersPage", "requiredRoles", List.of("storeAdmin", "storeEmployee")),
        Map.of("path", "/pos", "label", "Point of Sale", "component", "POSPage", "requiredRoles", List.of("storeAdmin", "storeEmployee", "cashier"), "requiredPermissions", List.of("pos.access")),
        Map.of("path", "/profile", "label", "My Profile", "component", "ProfilePage")
    );

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getNavigationConfig() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        String username = authentication.getName();
        List<String> userRoles = rbacService.getUserRoles(username);
        List<String> userPermissions = rbacService.getUserPermissions(username);

        List<Map<String, Object>> allowedRoutes = ROUTES.stream().filter(route -> {
            // Check roles
            List<String> requiredRoles = (List<String>) route.getOrDefault("requiredRoles", List.of());
            boolean roleAllowed = requiredRoles.isEmpty() || userRoles.stream().anyMatch(requiredRoles::contains);
            // Check permissions
            List<String> requiredPermissions = (List<String>) route.getOrDefault("requiredPermissions", List.of());
            boolean permAllowed = requiredPermissions.isEmpty() || requiredPermissions.stream().allMatch(userPermissions::contains);
            return roleAllowed && permAllowed;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(allowedRoutes);
    }
}

