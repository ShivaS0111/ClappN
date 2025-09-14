package biz.craftline.server.feature.ordermanagement.infra.repository;

import biz.craftline.server.feature.ordermanagement.infra.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    // Custom query methods if needed
}