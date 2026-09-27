package biz.craftline.server.feature.businessstore.domain.service;

import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedProduct;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public interface ProductsOfferedByStoreService {

    void deleteStoreProductById(Long id);

    List<StoreOfferedProduct> findAll();

    List<StoreOfferedProduct>  searchProductByKeyword(String searchTerm);

    List<StoreOfferedProduct>  searchProductByStoreIdAndKeyword(Long storeId, String searchTerm);

    Optional<List<StoreOfferedProduct>> findProductsByStoreId(Long id);

    StoreOfferedProduct save(StoreOfferedProduct domain);

    List<StoreOfferedProduct> save(List<StoreOfferedProduct> domains);

    StoreOfferedProduct update(Long id, StoreOfferedProduct domain);

    StoreOfferedProduct findById(Long id);

    Optional<List<StoreOfferedProduct>> findProductsByBusinessId(Long businessId);

    /**
     * Assign a master catalog product to selected stores (or all stores of the business).
     */
    List<StoreOfferedProduct> assignToStores(Long businessId, Long businessProductId,
                                             List<Long> storeIds, boolean allStores,
                                             String aliasName, String description, int status);

    /** Remove catalog product from selected stores (or all stores of the business). Returns removed count. */
    int unassignFromStores(Long businessId, Long businessProductId, List<Long> storeIds, boolean allStores);
}

