package biz.craftline.server.feature.businessstore.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Assign a business catalog product/service to one, many, or all stores under a business.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignCatalogToStoresRequest {

    private Long businessId;

    /** Master catalog id (business_product.id or business_service.id). */
    private Long catalogItemId;

    /**
     * Target stores. If null/empty and {@code allStores} is true, assign to every store under businessId.
     */
    private List<Long> storeIds;

    /** When true, ignore storeIds and assign to all stores of the business. */
    private boolean allStores;

    private String aliasName;
    private String description;
    private int status;
}
