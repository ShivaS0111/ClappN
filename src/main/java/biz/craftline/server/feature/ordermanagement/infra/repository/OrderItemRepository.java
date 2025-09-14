package biz.craftline.server.feature.ordermanagement.infra.repository;

import biz.craftline.server.feature.ordermanagement.infra.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {
    // Custom query methods if needed
}

