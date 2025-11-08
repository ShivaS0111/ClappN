package biz.craftline.server.feature.inventorymanagement.infra.repository;

import biz.craftline.server.feature.inventorymanagement.infra.entity.ProductLotEntity;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductLotRepository extends JpaRepository<ProductLotEntity, Long> {

    List<ProductLotEntity> findByProductId(Long productId);

    List<ProductLotEntity> findByActiveTrue();

}
