package biz.craftline.server.feature.businessstore.infra.repository;

import biz.craftline.server.feature.businessstore.infra.entity.BusinessEntity;
import biz.craftline.server.feature.businesstype.infra.entity.BusinessTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessEntityJpaRepository extends JpaRepository<BusinessEntity, Long>{

    void deleteBusinessTypeById(Long id);

    Optional<BusinessEntity> findById(Long id);



    @Query("SELECT c FROM business  c WHERE (LOWER(c.businessName) LIKE LOWER(CONCAT('%', :keyword, '%')) )")
    List<BusinessEntity> findByNameContaining(String keyword);
}