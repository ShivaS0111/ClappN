package biz.craftline.server.feature.usermanagement.domain.service;

import biz.craftline.server.feature.usermanagement.infra.entity.RoleEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.PermissionEntity;
import org.springframework.stereotype.Service;
import java.util.Set;

@Service
public class RBACService {
    public boolean hasPermission(RoleEntity role, String permissionName) {
        Set<PermissionEntity> permissions = role.getPermissions();
        if (permissions == null) return false;
        return permissions.stream().anyMatch(p -> p.getName().equalsIgnoreCase(permissionName));
    }
}

