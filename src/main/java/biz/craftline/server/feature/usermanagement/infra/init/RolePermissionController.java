package biz.craftline.server.feature.usermanagement.infra.init;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/roles-permissions")
@RequiredArgsConstructor
@Slf4j
public class RolePermissionController {

    private final RolePermissionService rolePermissionService;

    // Allow access to admins; this can be adjusted to your project's authority naming
    @PreAuthorize("hasAuthority('ADMIN_ACCESS') or hasAuthority('ADMIN')")
    @PostMapping("/init")
    public ResponseEntity<String> initialize() {
        try {
            rolePermissionService.initializeFromResource();
            return ResponseEntity.ok("Role/permission initialization triggered successfully.");
        } catch (Exception ex) {
            log.error("Failed to initialize roles/permissions", ex);
            return ResponseEntity.status(500).body("Initialization failed: " + ex.getMessage());
        }
    }
}

