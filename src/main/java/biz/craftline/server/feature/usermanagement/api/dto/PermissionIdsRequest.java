package biz.craftline.server.feature.usermanagement.api.dto;

import lombok.Data;
import java.util.List;

@Data
public class PermissionIdsRequest {
    private List<Long> permissionIds;
}

