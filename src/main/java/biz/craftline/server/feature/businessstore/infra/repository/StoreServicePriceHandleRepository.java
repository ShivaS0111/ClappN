package biz.craftline.server.feature.businessstore.infra.repository;

import biz.craftline.server.feature.businessstore.infra.entity.StoreServicePriceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreServicePriceHandleRepository extends JpaRepository<StoreServicePriceEntity, Long> {

    List<StoreServicePriceEntity> findAllByServiceId(Long serviceId);
}
