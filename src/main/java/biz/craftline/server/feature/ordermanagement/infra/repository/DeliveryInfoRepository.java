package biz.craftline.server.feature.ordermanagement.infra.repository;

import biz.craftline.server.feature.ordermanagement.infra.entity.DeliveryInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeliveryInfoRepository extends JpaRepository<DeliveryInfoEntity, Long> {
    // Custom query methods if needed
}

