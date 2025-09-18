package biz.craftline.server.feature.addressmanagement.infra.repository;

import biz.craftline.server.feature.addressmanagement.infra.entity.LandmarkEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LandmarkRepository extends JpaRepository<LandmarkEntity, Long> {
    boolean existsByName(String name);
    LandmarkEntity findByName(String name);
}

