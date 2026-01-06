package biz.craftline.server.feature.businessstore.application.service;

import biz.craftline.server.enums.Item;
import biz.craftline.server.feature.businessstore.api.dto.ItemKey;
import biz.craftline.server.feature.businessstore.domain.model.Store;
import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedProduct;
import biz.craftline.server.feature.businessstore.domain.service.ProductsOfferedByStoreService;
import biz.craftline.server.feature.businessstore.domain.service.StoreItemPriceService;
import biz.craftline.server.feature.businessstore.domain.service.StoreService;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedProductEntity;
import biz.craftline.server.feature.businessstore.infra.mapper.StoreProductEntityMapper;
import biz.craftline.server.feature.businessstore.infra.repository.ProductsOfferedByStoreRepository;
import biz.craftline.server.feature.businessstore.infra.repository.StoreItemPriceHandleRepository;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessProductJpaRepository;
import biz.craftline.server.util.UserUtil;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@AllArgsConstructor
@Service
public class ProductsOfferedByStoreServiceImpl implements ProductsOfferedByStoreService {
    @Autowired
    StoreProductEntityMapper mapper;

    @Autowired
    ProductsOfferedByStoreRepository productsOfferedByStoreRepository;

    @Autowired
    BusinessProductJpaRepository businessProductJpaRepository;

    @Autowired
    StoreItemPriceService storeItemPriceService;

    @Autowired
    StoreService storeService;

    @Override
    public List<StoreOfferedProduct> findAll() {
        List<StoreOfferedProduct> list = productsOfferedByStoreRepository.findAll().stream().map(mapper::toDomain).toList();
        return findProductsLatestPrices(list);
    }

    @Override
    public void deleteStoreProductById(Long id) {
        productsOfferedByStoreRepository.deleteStoreProductById(id);
    }

    @Override
    public Optional<List<StoreOfferedProduct>> findProductsByStoreId(Long id) {
        List<StoreOfferedProductEntity> entities = productsOfferedByStoreRepository.findProductsByStoreId(id)
                .orElse( List.of());
        return Optional.of(entities.stream().map(mapper::toDomain).toList());
    }

    @Override
    public List<StoreOfferedProduct> searchProductByKeyword(String searchTerm) {
        List<StoreOfferedProductEntity> entities = productsOfferedByStoreRepository.searchByKeyword(searchTerm);
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<StoreOfferedProduct> searchProductByStoreIdAndKeyword(Long storeId, String searchTerm) {
        List<StoreOfferedProductEntity> entities = productsOfferedByStoreRepository.searchByStoreIdAndKeyword(storeId.toString(), searchTerm);
        return entities.stream().map(mapper::toDomain).toList();
    }

    @Override
    public StoreOfferedProduct save(StoreOfferedProduct domain) {
        long userId = UserUtil.getCurrentUserId();
        StoreOfferedProductEntity entity= mapper.toEntity(domain);
        //entity.setService(domain.getService());
        entity.setCreatedBy(userId);
        StoreOfferedProductEntity en = productsOfferedByStoreRepository.save(entity);
        return mapper.toDomain(en);
    }

    @Override
    public List<StoreOfferedProduct> save(List<StoreOfferedProduct> domains) {
        long userId = UserUtil.getCurrentUserId();
        List<StoreOfferedProductEntity> entities =domains.stream().map( domain-> {

            StoreOfferedProductEntity entity= mapper.toEntity(domain);
            //entity.setService(domain.getService());
            entity.setCreatedBy(userId);
            return  entity;
        }
        ).toList();
        List<StoreOfferedProductEntity> en = productsOfferedByStoreRepository.saveAll(entities);
        return en.stream().map(mapper::toDomain).toList();
    }

    @Override
    public StoreOfferedProduct findById(Long id) {
        Optional<StoreOfferedProductEntity>  service = productsOfferedByStoreRepository.findById(id);
        service.orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        return mapper.toDomain(service.get());
    }

    @Override
    public Optional<List<StoreOfferedProduct>> findProductsByBusinessId(Long businessId) {
        return Optional.of( productsOfferedByStoreRepository.findByBusinessId(businessId)
                .orElse(List.of())
                .stream().map(mapper::toDomain ).toList() );
    }

    public List<StoreOfferedProduct> findProductsLatestPrices(
            List<StoreOfferedProduct> products
    ) {
        if (products == null || products.isEmpty()) {
            return products;
        }

        // Build keys and index products at once
        Map<Long, StoreOfferedProduct> productMap = new HashMap<>(products.size());

        for (StoreOfferedProduct p : products) {
            productMap.put(
                     p.getId(),
                    p
            );
        }

        List<Long> keys = productMap.keySet().stream().toList();

        // Fetch latest prices
        storeItemPriceService
                .findLatestPricesForProductsInStores( keys, Item.ProductLot )
                .forEach(price -> {
                    StoreOfferedProduct product = productMap.get(price.getItemId());
                    if (product != null) {
                        product.setPrice(price);
                    }
                });

        return products;
    }


}
