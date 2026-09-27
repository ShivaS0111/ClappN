package biz.craftline.server.feature.businessstore.infra.repository;

import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedProductEntity;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ServicesOfferedByStoreRepository extends JpaRepository<StoreOfferedServiceEntity, Long> {

    void deleteStoreServiceById(Long id);

    boolean existsByStoreIdAndBusinessServiceId(Long storeId, Long businessServiceId);

    Optional<StoreOfferedServiceEntity> findByStoreIdAndBusinessServiceId(Long storeId, Long businessServiceId);

    void deleteByStoreIdAndBusinessServiceId(Long storeId, Long businessServiceId);

    Optional<List<StoreOfferedServiceEntity>> findByBusinessId(Long businessId);
    Optional<List<StoreOfferedServiceEntity>> findByStoreId(Long id);

    @Query("SELECT bs FROM StoreOfferedServiceEntity bs WHERE  (LOWER(bs.aliasName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(bs.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<StoreOfferedServiceEntity> searchByKeyword(@Param("keyword") String keyword);

    @Query("""
    SELECT bs
    FROM StoreOfferedServiceEntity bs
    WHERE bs.storeId = :storeId
      AND (
           LOWER(bs.aliasName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(bs.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
      )
    """)
    List<StoreOfferedServiceEntity> searchByStoreIdAndKeyword(
            @Param("storeId") String storeId,
            @Param("keyword") String keyword
    );
}
