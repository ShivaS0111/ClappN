package biz.craftline.server.feature.businessstore.infra.repository;

import biz.craftline.server.feature.businessstore.domain.model.StoreOfferedProduct;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedProductEntity;
import biz.craftline.server.feature.businessstore.infra.entity.StoreOfferedServiceEntity;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductsOfferedByStoreRepository extends JpaRepository<StoreOfferedProductEntity, Long> {

    void deleteStoreProductById(Long id);

    Optional<List<StoreOfferedProductEntity>> findProductsByStoreId(Long id);

    @Query("SELECT bs FROM StoreOfferedProductEntity bs WHERE  (LOWER(bs.aliasName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(bs.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<StoreOfferedProductEntity> searchByKeyword(@Param("keyword") String keyword);

    @Query("""
    SELECT bs
    FROM StoreOfferedProductEntity bs
    WHERE bs.storeId = :storeId
      AND (
           LOWER(bs.aliasName) LIKE LOWER(CONCAT('%', :keyword, '%'))
        OR LOWER(bs.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
      )
    """)
    List<StoreOfferedProductEntity> searchByStoreIdAndKeyword(
            @Param("storeId") String storeId,
            @Param("keyword") String keyword
    );
}
