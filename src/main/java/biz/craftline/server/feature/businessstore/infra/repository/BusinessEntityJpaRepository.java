package biz.craftline.server.feature.businessstore.infra.repository;

import biz.craftline.server.feature.businessstore.infra.entity.BusinessEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BusinessEntityJpaRepository extends JpaRepository<BusinessEntity, Long>{

    void deleteBusinessTypeById(Long id);

    Optional<BusinessEntity> findById(Long id);
}