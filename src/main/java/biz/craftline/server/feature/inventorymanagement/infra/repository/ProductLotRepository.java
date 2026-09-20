package biz.craftline.server.feature.inventorymanagement.infra.repository;

import biz.craftline.server.feature.inventorymanagement.infra.entity.ProductLotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductLotRepository extends JpaRepository<ProductLotEntity, Long> {

    List<ProductLotEntity> findByProductId(Long productId);

    /** active is an int flag (1 = active), not boolean */
    List<ProductLotEntity> findByActive(int active);

    List<ProductLotEntity> findByProductIdAndActive(Long productId, int active);

    List<ProductLotEntity> findByStoreIdAndProductIdAndActive(Long storeId, Long productId, int active);

    List<ProductLotEntity> findByStoreIdIn(List<Long> storeIds);

    List<ProductLotEntity> findByStoreIdInAndActive(List<Long> storeIds, int active);
}
