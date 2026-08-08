package biz.craftline.server.feature.businessstore.infra.repository;

import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedPackageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StoreOfferedPackageRepository extends JpaRepository<StoreOfferedPackageEntity, Long> {
    List<StoreOfferedPackageEntity> findByStoreId(Long storeId);
}
