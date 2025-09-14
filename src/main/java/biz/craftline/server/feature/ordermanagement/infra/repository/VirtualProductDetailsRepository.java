package biz.craftline.server.feature.ordermanagement.infra.repository;

import biz.craftline.server.feature.ordermanagement.infra.entity.VirtualProductDetailsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VirtualProductDetailsRepository extends JpaRepository<VirtualProductDetailsEntity, Long> {
    // Custom query methods if needed
}

