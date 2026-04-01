package biz.craftline.server.feature.customermanagement.infra.repository;

import biz.craftline.server.feature.customermanagement.infra.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * JPA Repository for Customer entities.
 */
@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {
    
    List<CustomerEntity> findByStoreId(Long storeId);
    
    List<CustomerEntity> findByBusinessId(Long businessId);
    
    Optional<CustomerEntity> findByEmail(String email);
    
    List<CustomerEntity> findByStatus(int status);
    
    List<CustomerEntity> findByStoreIdAndStatus(Long storeId, int status);
}

