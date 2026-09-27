package biz.craftline.server.feature.businessstore.application.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.businessstore.domain.model.Store;
import biz.craftline.server.feature.businessstore.domain.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Shared rules when assigning a master catalog item to a store offering.
 */
@Component
@RequiredArgsConstructor
public class StoreOfferingAssignSupport {

    private final StoreService storeService;
    private final SecurityContextService securityContextService;

    /**
     * Validates store access, resolves/fills businessId from the store, and checks business access.
     *
     * @return store's owning business id (never null)
     */
    public Long resolveAndValidateStoreBusiness(Long storeId, Long requestedBusinessId) {
        if (storeId == null) {
            throw new IllegalArgumentException("storeId is required");
        }
        securityContextService.validateStoreAccess(storeId);

        Store store = storeService.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found: " + storeId));

        Long storeBusinessId = store.getBusiness() != null ? store.getBusiness().getId() : null;
        if (storeBusinessId == null) {
            throw new IllegalArgumentException("Store " + storeId + " has no owning business");
        }

        if (requestedBusinessId != null && !requestedBusinessId.equals(storeBusinessId)) {
            throw new IllegalArgumentException(
                    "businessId " + requestedBusinessId + " does not match store's business " + storeBusinessId);
        }

        securityContextService.validateBusinessAccess(storeBusinessId);
        return storeBusinessId;
    }

    public Long storeBusinessTypeId(Long storeId) {
        Store store = storeService.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found: " + storeId));
        return store.getBusinessType();
    }

    public void assertBusinessTypeCompatible(Long storeBusinessTypeId, Long catalogBusinessTypeId, String label) {
        if (storeBusinessTypeId == null || catalogBusinessTypeId == null) {
            return;
        }
        if (!storeBusinessTypeId.equals(catalogBusinessTypeId)) {
            throw new IllegalArgumentException(
                    label + " businessType " + catalogBusinessTypeId
                            + " is not compatible with store businessType " + storeBusinessTypeId);
        }
    }
}
