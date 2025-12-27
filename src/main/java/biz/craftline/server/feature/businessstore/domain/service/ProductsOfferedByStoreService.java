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

    StoreOfferedProduct findById(Long id);
}

