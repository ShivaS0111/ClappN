package biz.craftline.server.feature.usermanagement.api.controller;

import biz.craftline.server.feature.usermanagement.domain.service.RBACService;
import biz.craftline.server.feature.usermanagement.domain.service.UserPermissionManagementService;
import biz.craftline.server.util.APIResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
@Slf4j
public class PermissionDemoController {

    private final RBACService rbacService;
    private final UserPermissionManagementService userPermissionService;

    @GetMapping("/current-user")
    public ResponseEntity<APIResponse<Map<String, Object>>> getCurrentUserPermissions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        Map<String, Object> result = new HashMap<>();
        result.put("username", username);
        result.put("effectivePermissions", rbacService.getUserPermissions(username));
        result.put("authorities", auth.getAuthorities().stream().map(Object::toString).toList());
        result.put("roleBasedPermissions", rbacService.getUserRoles(username));

        return APIResponse.success(result);
    }

    @GetMapping("/test/create-user")
    @PreAuthorize("hasAuthority('CREATE_USER')")
    public ResponseEntity<APIResponse<String>> testCreateUserPermission() {
        return APIResponse.success("✅ You have CREATE_USER authority!");
    }

    @GetMapping("/test/delete-user")
    @PreAuthorize("hasAuthority('DELETE_USER')")
    public ResponseEntity<APIResponse<String>> testDeleteUserPermission() {
        return APIResponse.success("✅ You have DELETE_USER authority!");
    }

    @GetMapping("/test/admin-access")
    @PreAuthorize("hasAuthority('ADMIN_ACCESS')")
    public ResponseEntity<APIResponse<String>> testAdminPermission() {
        return APIResponse.success("✅ You have ADMIN_ACCESS authority!");
    }

    @GetMapping("/test/custom-annotation")
    //@RequirePermission("VIEW_REPORTS")
    public ResponseEntity<APIResponse<String>> testCustomAnnotation() {
        return APIResponse.success("✅ Custom @RequirePermission annotation works!");
    }

    @GetMapping("/test/multiple-authorities")
    @PreAuthorize("hasAnyAuthority('CREATE_USER', 'UPDATE_USER', 'USER_MANAGEMENT')")
    public ResponseEntity<APIResponse<String>> testMultipleAuthorities() {
        return APIResponse.success("✅ You have one of the required authorities!");
    }

    @GetMapping("/test/hierarchy-demo/{permission}")
    public ResponseEntity<APIResponse<Map<String, Object>>> demonstrateHierarchy(@PathVariable String permission) {
        String username = rbacService.getCurrentUsername();
        Long userId = rbacService.getUserId(username);

        Map<String, Object> demo = new HashMap<>();
        demo.put("permission", permission);
        demo.put("username", username);
        demo.put("hasPermission", rbacService.currentUserHasPermission(permission));

        // Get detailed breakdown
        demo.put("userAllowedPermissions", userPermissionService.getUserAllowedPermissions(userId)
            .stream().map(p -> p.getPermission().getName()).toList());
        demo.put("userDeniedPermissions", userPermissionService.getUserDeniedPermissions(userId)
            .stream().map(p -> p.getPermission().getName()).toList());
        demo.put("roleBasedPermissions", rbacService.getUserRoles(username));
        demo.put("effectivePermissions", rbacService.getUserPermissions(username));

        return APIResponse.success(demo);
    }

    /*
    @PostMapping("/simulate/scenario")
    @PreAuthorize("hasAuthority('MANAGE_USER_PERMISSIONS')")
    public ResponseEntity<APIResponse<String>> simulatePermissionScenario(
            @RequestParam String targetUser,
            @RequestParam String permission,
            @RequestParam String action) { // grant, deny, remove

        String currentUser = rbacService.getCurrentUsername();
        String reason = "API simulation test";
        boolean success = false;

        switch (action.toLowerCase()) {
            case "grant":
                success = userPermissionService.grantPermissionToUser(targetUser, permission, currentUser, reason);
                break;
            case "deny":
                success = userPermissionService.denyPermissionToUser(targetUser, permission, currentUser, reason);
                break;
            case "remove":
                success = userPermissionService.removeUserPermissionOverride(targetUser, permission);
                break;
            default:
                return APIResponse.error("Invalid action. Use: grant, deny, or remove", HttpStatus.BAD_REQUEST);
        }

        if (success) {
            return APIResponse.success(
                String.format("Successfully %sed permission '%s' for user '%s'", action, permission, targetUser));
        } else {
            return APIResponse.error(
                String.format("Failed to %s permission '%s' for user '%s'", action, permission, targetUser) , HttpStatus.BAD_REQUEST);
        }
    }
     */
}
