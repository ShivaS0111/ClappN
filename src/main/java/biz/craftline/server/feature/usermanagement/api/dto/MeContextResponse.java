package biz.craftline.server.feature.usermanagement.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

/**
 * Current request identity + scope for the frontend (and debugging).
 */
@Data
@Builder
public class MeContextResponse {
    private Long userId;
    private String email;
    private List<String> roles;
    private Set<String> permissions;
    private List<Long> accessibleStoreIds;
    private List<Long> accessibleBusinessIds;
    private Long activeStoreId;
    private Long activeBusinessId;
    private boolean unrestricted;
}
