package biz.craftline.server.feature.addressmanagement.infra.repository;

import biz.craftline.server.feature.addressmanagement.infra.entity.SubRegionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubRegionRepository extends JpaRepository<SubRegionEntity, Long> {
    boolean existsByName(String name);
    SubRegionEntity findByName(String name);
}

