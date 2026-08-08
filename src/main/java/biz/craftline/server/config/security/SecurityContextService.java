package biz.craftline.server.config.security;

import biz.craftline.server.feature.usermanagement.infra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Facade for current-request identity and data scope.
 * Prefers {@link UserScopeContext} (DB-resolved once per request). Falls back to
 * {@link ScopedAuthenticationToken} only when context is unavailable (e.g. tests).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityContextService {

    private static final String ROLE_SYSTEM_ADMIN = "SYSTEM_ADMIN";

    private final UserRepository userRepository;

    private UserScopeContext scopeOrNull() {
        return UserScopeContextHolder.get();
    }

    public String getCurrentUsername() {
        UserScopeContext scope = scopeOrNull();
        if (scope != null) {
            return scope.getEmail();
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new AccessDeniedException("No authenticated user in security context");
        }
        return auth.getName();
    }

    public Long getCurrentUserId() {
        UserScopeContext scope = scopeOrNull();
        if (scope != null && scope.getUserId() != null) {
            return scope.getUserId();
        }
        String username = getCurrentUsername();
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new AccessDeniedException("Authenticated user not found in database"))
                .getId();
    }

    private ScopedAuthenticationToken getScopedToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof ScopedAuthenticationToken scoped) {
            return scoped;
        }
        throw new AccessDeniedException("Authentication token does not contain scope data");
    }

    public List<String> getCurrentUserRoles() {
        UserScopeContext scope = scopeOrNull();
        if (scope != null) {
            return scope.getRoles();
        }
        return getScopedToken().getRoles();
    }

    public List<Long> getCurrentUserStoreIds() {
        UserScopeContext scope = scopeOrNull();
        if (scope != null) {
            return scope.getAccessibleStoreIds() != null ? scope.getAccessibleStoreIds() : List.of();
        }
        return getScopedToken().getStoreIds();
    }

    public List<Long> getCurrentUserBusinessIds() {
        UserScopeContext scope = scopeOrNull();
        if (scope != null) {
            return scope.getAccessibleBusinessIds() != null ? scope.getAccessibleBusinessIds() : List.of();
        }
        return getScopedToken().getBusinessIds();
    }

    public List<String> getCurrentUserPermissions() {
        UserScopeContext scope = scopeOrNull();
        if (scope != null) {
            return scope.getPermissions().stream().toList();
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }

    public boolean isSystemAdmin() {
        UserScopeContext scope = scopeOrNull();
        if (scope != null) {
            return scope.isUnrestricted() || scope.hasRole(ROLE_SYSTEM_ADMIN);
        }
        return getCurrentUserRoles().contains(ROLE_SYSTEM_ADMIN);
    }

    public boolean isBusinessLevel() {
        UserScopeContext scope = scopeOrNull();
        if (scope != null) {
            return scope.getRoles().stream().anyMatch(r ->
                    r.equals("BUSINESS_OWNER") || r.equals("BUSINESS_ADMIN") || r.equals("BUSINESS_MANAGER"));
        }
        return getCurrentUserRoles().stream().anyMatch(r ->
                r.equals("BUSINESS_OWNER") || r.equals("BUSINESS_ADMIN") || r.equals("BUSINESS_MANAGER"));
    }

    public boolean hasRole(String roleName) {
        UserScopeContext scope = scopeOrNull();
        if (scope != null) {
            return scope.hasRole(roleName);
        }
        return getCurrentUserRoles().contains(roleName);
    }

    public boolean hasPermission(String permission) {
        UserScopeContext scope = scopeOrNull();
        if (scope != null) {
            return scope.hasPermission(permission);
        }
        return getCurrentUserPermissions().contains(permission);
    }

    /**
     * Store IDs the caller may access for this request.
     * null = unrestricted (SYSTEM_ADMIN). Empty = no access.
     * When X-Store-Id / X-Business-Id is present, returns the narrowed effective set.
     */
    public List<Long> getAccessibleStoreIds() {
        UserScopeContext scope = scopeOrNull();
        if (scope != null) {
            return scope.getEffectiveStoreIds();
        }
        if (isSystemAdmin()) {
            return null;
        }
        return getCurrentUserStoreIds();
    }

    /**
     * Business IDs the caller may access for this request.
     * null = unrestricted.
     */
    public List<Long> getAccessibleBusinessIds() {
        UserScopeContext scope = scopeOrNull();
        if (scope != null) {
            return scope.getEffectiveBusinessIds();
        }
        if (isSystemAdmin()) {
            return null;
        }
        return getCurrentUserBusinessIds();
    }

    public void validateStoreAccess(Long storeId) {
        if (storeId == null) return;
        UserScopeContext scope = scopeOrNull();
        if (scope != null) {
            if (!scope.canAccessStore(storeId)) {
                log.warn("User {} attempted to access store {} without permission. Accessible stores: {}",
                        scope.getEmail(), storeId, scope.getAccessibleStoreIds());
                throw new AccessDeniedException("You do not have access to store: " + storeId);
            }
            return;
        }
        if (isSystemAdmin()) return;
        List<Long> accessibleStoreIds = getAccessibleStoreIds();
        if (accessibleStoreIds != null && !accessibleStoreIds.contains(storeId)) {
            throw new AccessDeniedException("You do not have access to store: " + storeId);
        }
    }

    public void validateBusinessAccess(Long businessId) {
        if (businessId == null) return;
        UserScopeContext scope = scopeOrNull();
        if (scope != null) {
            if (!scope.canAccessBusiness(businessId)) {
                log.warn("User {} attempted to access business {} without permission. Accessible businesses: {}",
                        scope.getEmail(), businessId, scope.getAccessibleBusinessIds());
                throw new AccessDeniedException("You do not have access to business: " + businessId);
            }
            return;
        }
        if (isSystemAdmin()) return;
        List<Long> accessibleBusinessIds = getAccessibleBusinessIds();
        if (accessibleBusinessIds != null && !accessibleBusinessIds.contains(businessId)) {
            throw new AccessDeniedException("You do not have access to business: " + businessId);
        }
    }

    public void validateStoreAccessForAll(List<Long> storeIds) {
        if (storeIds == null || storeIds.isEmpty()) return;
        for (Long storeId : storeIds) {
            validateStoreAccess(storeId);
        }
    }
}
