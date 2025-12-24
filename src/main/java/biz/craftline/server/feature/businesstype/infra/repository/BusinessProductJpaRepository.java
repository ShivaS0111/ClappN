package biz.craftline.server.feature.businesstype.infra.repository;

import biz.craftline.server.feature.businesstype.infra.entity.BusinessProductEntity;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusinessProductJpaRepository extends JpaRepository<BusinessProductEntity, Long> {

    @Query("SELECT bs FROM business_product bs WHERE  (LOWER(bs.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(bs.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<BusinessProductEntity> searchByKeyword(@Param("keyword") String keyword);

    List<BusinessProductEntity> save(List<BusinessProductEntity> list);

    // Method 1: Using the business type's ID
    List<BusinessProductEntity> findByBusinessType_Id(Long businessTypeId);

}
