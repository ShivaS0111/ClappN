package biz.craftline.server.feature.ordermanagement.infra.repository;


import biz.craftline.server.feature.ordermanagement.infra.entity.OrderAllocatedLotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderAllocatedLotRepository extends JpaRepository<OrderAllocatedLotEntity, Long> {
    List<OrderAllocatedLotEntity> findByOrderItemId(Long orderItemId);
}
