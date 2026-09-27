package biz.craftline.server.feature.businesstype.application.service;

import biz.craftline.server.config.security.SecurityContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Resolves and enforces tenant ownership on business_product / business_service rows.
 * null businessId = platform template (SYSTEM_ADMIN write; readable by all).
 */
@Component
@RequiredArgsConstructor
public class CatalogBusinessOwnership {

    private final SecurityContextService securityContextService;

    /**
     * For create: non-admins must supply (or auto-pick) a businessId; admins may leave null (template).
     */
    public Long resolveBusinessIdForCreate(Long requestedBusinessId) {
        if (securityContextService.isSystemAdmin()) {
            if (requestedBusinessId != null) {
                securityContextService.validateBusinessAccess(requestedBusinessId);
            }
            return requestedBusinessId;
        }
        Long businessId = requestedBusinessId;
        if (businessId == null) {
            List<Long> accessible = securityContextService.getAccessibleBusinessIds();
            if (accessible != null && accessible.size() == 1) {
                businessId = accessible.get(0);
            }
        }
        if (businessId == null) {
            throw new IllegalArgumentException("businessId is required to create a business catalog item");
        }
        securityContextService.validateBusinessAccess(businessId);
        return businessId;
    }

    public void assertCanRead(Long catalogBusinessId) {
        if (catalogBusinessId == null) {
            return; // platform template
        }
        if (securityContextService.isSystemAdmin()) {
            return;
        }
        securityContextService.validateBusinessAccess(catalogBusinessId);
    }

    public void assertCanMutate(Long catalogBusinessId) {
        if (securityContextService.isSystemAdmin()) {
            return;
        }
        if (catalogBusinessId == null) {
            throw new AccessDeniedException("Only SYSTEM_ADMIN can modify platform catalog templates");
        }
        securityContextService.validateBusinessAccess(catalogBusinessId);
    }

    public boolean isVisibleToCaller(Long catalogBusinessId) {
        if (catalogBusinessId == null || securityContextService.isSystemAdmin()) {
            return true;
        }
        List<Long> accessible = securityContextService.getAccessibleBusinessIds();
        if (accessible == null) {
            return true;
        }
        return accessible.contains(catalogBusinessId);
    }

    public void assertUniqueName(String label, String name, boolean duplicate) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(label + " name is required");
        }
        if (duplicate) {
            throw new IllegalArgumentException(
                    "A " + label.toLowerCase() + " named '" + name.trim()
                            + "' already exists for this business catalog");
        }
    }
}
