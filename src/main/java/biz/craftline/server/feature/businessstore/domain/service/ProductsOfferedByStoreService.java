package biz.craftline.server.feature.businessstore.domain.service;

import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedProduct;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductsOfferedByStoreService {
    // This should be replaced with repository/database logic
    private final List<StoreOfferedProduct> products = new ArrayList<>();

    public Optional<List<StoreOfferedProduct>> findProductsByStoreId(Long storeId) {
        List<StoreOfferedProduct> result = new ArrayList<>();
        for (StoreOfferedProduct product : products) {
        }
        return Optional.of(result);
    }

    public StoreOfferedProduct save(StoreOfferedProduct product) {
        // Simulate ID assignment
        product.setId((long) (products.size() + 1));
        products.add(product);
        return product;
    }
}

