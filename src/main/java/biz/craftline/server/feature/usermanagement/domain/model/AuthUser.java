package biz.craftline.server.feature.usermanagement.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class AuthUser extends User {
    List<String> roles;
    List<String> permissions;
    List<String> allowedPermissions;
    List<String> deniedPermissions;
}

