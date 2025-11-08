package biz.craftline.server.feature.inventorymanagement.infra.repository;

import biz.craftline.server.feature.inventorymanagement.infra.entity.ProductLotEntity;
import biz.craftline.server.feature.inventorymanagement.infra.entity.ProductLotTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductLotTransactionRepository extends JpaRepository<ProductLotTransactionEntity, Long> {

    List<ProductLotTransactionEntity> findByProductLot(ProductLotEntity productLot);

    List<ProductLotTransactionEntity> findByCorrelationId(String correlationId);

}
