package biz.craftline.server.config;

import biz.craftline.server.config.security.RequirePermission;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Keeps seed catalog aligned with controller {@code @RequirePermission} usage.
 */
class RbacSeedDataConsistencyTest {

    private static final String CONTROLLER_BASE = "biz.craftline.server.feature";

    @Test
    void everySeededRolePermissionExistsInPermissionCatalog() {
        Set<String> catalog = Set.of(RbacSeedData.PERMISSIONS);
        RbacSeedData.rolePermissionMap().forEach((role, perms) -> {
            for (String p : perms) {
                assertTrue(catalog.contains(p),
                        () -> "Role " + role + " references unknown permission: " + p);
            }
        });
    }

    @Test
    void everyControllerRequirePermissionExistsInCatalog() throws Exception {
        Set<String> catalog = Set.of(RbacSeedData.PERMISSIONS);
        Set<String> used = scanControllerPermissions();
        assertFalse(used.isEmpty(), "Expected to discover @RequirePermission annotations");

        Set<String> missing = used.stream()
                .filter(p -> !catalog.contains(p))
                .collect(Collectors.toCollection(TreeSet::new));
        assertTrue(missing.isEmpty(),
                () -> "Controller permissions missing from RbacSeedData.PERMISSIONS: " + missing);
    }

    @Test
    void operationalRolesCoverCorePosPermissions() {
        var map = RbacSeedData.rolePermissionMap();
        assertTrue(map.get("CASHIER").containsAll(List.of(
                "order.create", "order.read", "payment.create", "payment.read", "payment.refund",
                "invoice.create", "invoice.read", "customer.read")));
        assertTrue(map.get("STORE_MANAGER").containsAll(List.of(
                "order.update", "inventory.read", "user.read", "store.metrics", "payment.refund")));
        assertTrue(map.get("INVENTORY_STAFF").containsAll(List.of(
                "inventory.read", "inventory.create", "inventory.update", "inventory.adjust")));
        assertFalse(map.get("CASHIER").contains("user.delete"));
        assertFalse(map.get("CASHIER").contains("user.permissions"));
        assertFalse(map.get("INVENTORY_STAFF").contains("payment.refund"));
    }

    @Test
    void rolesAndLegacyRenamesStayAligned() {
        Set<String> roles = new HashSet<>(Arrays.asList(RbacSeedData.ROLES));
        RbacSeedData.LEGACY_ROLE_RENAMES.values().forEach(modern ->
                assertTrue(roles.contains(modern),
                        () -> "Legacy rename target missing from ROLES: " + modern));
    }

    private Set<String> scanControllerPermissions() throws Exception {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(Controller.class));

        Set<String> used = new HashSet<>();
        for (BeanDefinition bd : scanner.findCandidateComponents(CONTROLLER_BASE)) {
            Class<?> clazz = Class.forName(bd.getBeanClassName());
            RequirePermission classAnn = clazz.getAnnotation(RequirePermission.class);
            if (classAnn != null) {
                used.add(classAnn.value());
            }
            for (Method method : clazz.getDeclaredMethods()) {
                RequirePermission methodAnn = method.getAnnotation(RequirePermission.class);
                if (methodAnn != null) {
                    used.add(methodAnn.value());
                }
            }
        }
        return used;
    }
}
