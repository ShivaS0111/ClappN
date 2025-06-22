package biz.craftline.server.feature.businessstore.infra.repository;

import biz.craftline.server.feature.businessstore.infra.entity.StoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreRepository extends JpaRepository<StoreEntity, Long> {
    void deleteBusinessTypeById(Long id);

    List<StoreEntity> searchStoreByStoreName(String keyword);

    List<StoreEntity> findByBusinessId(long businessId);
}