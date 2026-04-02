package biz.craftline.server.config.security;

import biz.craftline.server.feature.businessstore.infra.entity.StoreEntity;
import biz.craftline.server.feature.businessstore.infra.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import biz.craftline.server.feature.usermanagement.infra.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Central service for accessing the current user's identity, roles, permissions,
 * and data scope (storeIds/businessIds) from the SecurityContext.
 *
 * All scope data is extracted from the JWT via {@link ScopedAuthenticationToken},
 * avoiding extra DB queries on every request.
 *
 * Scope Rules:
 * - SYSTEM_ADMIN → unrestricted access to all data
 * - Business-level roles (BUSINESS_OWNER/ADMIN/MANAGER) with NO storeIds assigned → all stores under their business(es)
 * - Business-level roles with storeIds assigned → only those specific stores
 * - Store-level roles → only their assigned storeIds
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityContextService {

    private static final String ROLE_SYSTEM_ADMIN = "SYSTEM_ADMIN";
    private static final Set<String> BUSINESS_LEVEL_ROLES = Set.of(
            "BUSINESS_OWNER", "BUSINESS_ADMIN", "BUSINESS_MANAGER"
    );

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    // ─── Identity ────────────────────────────────────────────────────

    /** Get the username (email) of the current authenticated user. */
    public String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("No authenticated user in security context");
        }
        return auth.getName();
    }

    /** Get the database user ID of the current authenticated user. */
    public Long getCurrentUserId() {
        String username = getCurrentUsername();
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found in database"))
                .getId();
    }

    // ─── Token-based scope accessors (zero DB queries) ───────────────

    /** Get the ScopedAuthenticationToken or throw. */
    private ScopedAuthenticationToken getScopedToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof ScopedAuthenticationToken scoped) {
            return scoped;
        }
        throw new AccessDeniedException("Authentication token does not contain scope data");
    }

    /** Roles carried in the JWT. */
    public List<String> getCurrentUserRoles() {
        return getScopedToken().getRoles();
    }

    /** Store IDs directly assigned to the user (from Employee records). */
    public List<Long> getCurrentUserStoreIds() {
        return getScopedToken().getStoreIds();
    }

    /** Business IDs directly assigned to the user (from Employee records). */
    public List<Long> getCurrentUserBusinessIds() {
        return getScopedToken().getBusinessIds();
    }

    /** Permissions (authorities) from the JWT. */
    public List<String> getCurrentUserPermissions() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }

    // ─── Role checks ────────────────────────────────────────────────

    /** True if the user has the SYSTEM_ADMIN role. */
    public boolean isSystemAdmin() {
        return getCurrentUserRoles().contains(ROLE_SYSTEM_ADMIN);
    }

    /** True if the user holds any business-level role (BUSINESS_OWNER/ADMIN/MANAGER). */
    public boolean isBusinessLevel() {
        return getCurrentUserRoles().stream().anyMatch(BUSINESS_LEVEL_ROLES::contains);
    }

    /** True if the user has a specific role. */
    public boolean hasRole(String roleName) {
        return getCurrentUserRoles().contains(roleName);
    }

    /** True if the user has a specific permission/authority. */
    public boolean hasPermission(String permission) {
        return getCurrentUserPermissions().contains(permission);
    }

    // ─── Scope resolution ───────────────────────────────────────────

    /**
     * Resolve the full set of store IDs the user may access.
     *
     * Rules:
     *  - SYSTEM_ADMIN  → empty list (meaning "all" — callers must treat empty as unrestricted)
     *  - Business-level role with NO storeIds → expand each businessId → all stores under those businesses
     *  - Business-level role WITH storeIds → only those storeIds
     *  - Store-level role → only their storeIds
     *
     * Returns null for SYSTEM_ADMIN to differentiate "all" from "none".
     */
    public List<Long> getAccessibleStoreIds() {
        if (isSystemAdmin()) {
            return null; // null = unrestricted
        }

        List<Long> storeIds = getCurrentUserStoreIds();
        List<Long> businessIds = getCurrentUserBusinessIds();

        if (isBusinessLevel() && storeIds.isEmpty() && !businessIds.isEmpty()) {
            // Business-level user with no specific store assignment → all stores under their businesses
            List<Long> expandedStoreIds = new ArrayList<>();
            for (Long businessId : businessIds) {
                List<Long> businessStoreIds = storeRepository.findByBusinessId(businessId).stream()
                        .map(StoreEntity::getId)
                        .toList();
                expandedStoreIds.addAll(businessStoreIds);
            }
            return expandedStoreIds.stream().distinct().toList();
        }

        // Store-level role OR business-level with specific stores assigned
        return storeIds;
    }

    /**
     * Resolve the full set of business IDs the user may access.
     * Returns null for SYSTEM_ADMIN (unrestricted).
     */
    public List<Long> getAccessibleBusinessIds() {
        if (isSystemAdmin()) {
            return null; // null = unrestricted
        }
        return getCurrentUserBusinessIds();
    }

    // ─── Access validation (throws on violation) ─────────────────────

    /**
     * Validate that the current user can access data belonging to the given storeId.
     * @throws AccessDeniedException if the user does not have access
     */
    public void validateStoreAccess(Long storeId) {
        if (storeId == null) return;
        if (isSystemAdmin()) return;

        List<Long> accessibleStoreIds = getAccessibleStoreIds();
        if (accessibleStoreIds != null && !accessibleStoreIds.contains(storeId)) {
            log.warn("User {} attempted to access store {} without permission. Accessible stores: {}",
                    getCurrentUsername(), storeId, accessibleStoreIds);
            throw new AccessDeniedException("You do not have access to store: " + storeId);
        }
    }

    /**
     * Validate that the current user can access data belonging to the given businessId.
     * @throws AccessDeniedException if the user does not have access
     */
    public void validateBusinessAccess(Long businessId) {
        if (businessId == null) return;
        if (isSystemAdmin()) return;

        List<Long> accessibleBusinessIds = getAccessibleBusinessIds();
        if (accessibleBusinessIds != null && !accessibleBusinessIds.contains(businessId)) {
            log.warn("User {} attempted to access business {} without permission. Accessible businesses: {}",
                    getCurrentUsername(), businessId, accessibleBusinessIds);
            throw new AccessDeniedException("You do not have access to business: " + businessId);
        }
    }

    /**
     * Validate that the current user can access data for ANY of the given storeIds.
     * Useful for batch operations.
     */
    public void validateStoreAccessForAll(List<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) return;
        for (Long storeId : storeIds) {
            validateStoreAccess(storeId);
        }
    }
}

