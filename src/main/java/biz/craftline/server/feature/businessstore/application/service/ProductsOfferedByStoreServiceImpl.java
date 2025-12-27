package biz.craftline.server.feature.businessstore.application.service;

import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedProduct;
import biz.craftline.server.feature.businessstore.domain.service.ProductsOfferedByStoreService;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedProductEntity;
import biz.craftline.server.feature.businessstore.infra.mapper.StoreProductEntityMapper;
import biz.craftline.server.feature.businessstore.infra.repository.ProductsOfferedByStoreRepository;
import biz.craftline.server.feature.businesstype.infra.repository.BusinessProductJpaRepository;
import biz.craftline.server.util.UserUtil;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class ProductsOfferedByStoreServiceImpl implements ProductsOfferedByStoreService {
    @Autowired
    StoreProductEntityMapper mapper;

    @Autowired
    ProductsOfferedByStoreRepository productsOfferedByStoreRepository;

    @Autowired
    BusinessProductJpaRepository businessProductJpaRepository;

    @Override
    public List<StoreOfferedProduct> findAll() {
        return productsOfferedByStoreRepository.findAll().stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteStoreProductById(Long id) {
        productsOfferedByStoreRepository.deleteStoreProductById(id);
    }

    @Override
    public Optional<List<StoreOfferedProduct>> findProductsByStoreId(Long id) {
        List<StoreOfferedProductEntity> entities = productsOfferedByStoreRepository.findProductsByStoreId(id)
                .orElseThrow(() -> new RuntimeException("Products not found with id: " + id));
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
}
