package biz.craftline.server.feature.businesstype.infra.repository;

import biz.craftline.server.feature.businesstype.domain.model.BusinessType;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BusinessTypeJpaRepository extends JpaRepository<BusinessTypeEntity, Long> {

    @Query("SELECT c FROM business_type  c WHERE (LOWER(c.businessName) LIKE LOWER(CONCAT('%', :keyword, '%')) )")
    List<BusinessTypeEntity> findByNameContaining(String keyword);
}
