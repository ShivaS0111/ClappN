package biz.craftline.server.feature.businessstore.infra.repository;

import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedProduct;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedProductEntity;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductsOfferedByStoreRepository extends JpaRepository<StoreOfferedProductEntity, Long> {

    void deleteStoreProductById(Long id);

    Optional<List<StoreOfferedProductEntity>> findProductsByStoreId(Long id);

}
