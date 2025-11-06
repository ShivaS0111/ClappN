package biz.craftline.server.feature.businessstore.infra.repository;

import biz.craftline.server.feature.businessstore.infra.entity.ProductLotEntity;
import biz.craftline.server.feature.businessstore.infra.entity.ProductLotTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductLotTransactionRepository extends JpaRepository<ProductLotTransaction, Long> {

    List<ProductLotTransaction> findByProductLot(ProductLotEntity productLot);

    List<ProductLotTransaction> findByCorrelationId(String correlationId);
}
