package biz.craftline.server.config.security;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Set;

/**
 * Per-request authorization scope resolved from the database (not JWT claims).
 * Built once per request and reused by all services.
 */
@Getter
@Builder
public class UserScopeContext {

    private final Long userId;
    private final String email;
    private final List<String> roles;
    private final Set<String> permissions;

    /** Full capability — null = unrestricted (SYSTEM_ADMIN). Used for access checks. */
    private final List<Long> accessibleStoreIds;

    /** Full capability — null = unrestricted (SYSTEM_ADMIN). Used for access checks. */
    private final List<Long> accessibleBusinessIds;

    /**
     * Store IDs for list queries this request (active store / business narrowing applied).
     * null = unrestricted.
     */
    private final List<Long> effectiveStoreIds;

    /**
     * Business IDs for list queries this request.
     * null = unrestricted.
     */
    private final List<Long> effectiveBusinessIds;

    private final Long activeStoreId;
    private final Long activeBusinessId;
    private final boolean unrestricted;

    public boolean hasPermission(String permission) {
        if (permission == null || permission.isBlank()) {
            return false;
        }
        return permissions != null && permissions.contains(permission);
    }

    public boolean hasRole(String roleName) {
        return roles != null && roles.stream().anyMatch(r -> r.equalsIgnoreCase(roleName));
    }

    public boolean canAccessStore(Long storeId) {
        if (storeId == null || unrestricted) {
            return true;
        }
        return accessibleStoreIds != null && accessibleStoreIds.contains(storeId);
    }

    public boolean canAccessBusiness(Long businessId) {
        if (businessId == null || unrestricted) {
            return true;
        }
        return accessibleBusinessIds != null && accessibleBusinessIds.contains(businessId);
    }
}
