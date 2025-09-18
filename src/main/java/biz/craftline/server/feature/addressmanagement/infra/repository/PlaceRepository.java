package biz.craftline.server.feature.addressmanagement.infra.repository;

import biz.craftline.server.feature.addressmanagement.infra.entity.PlaceEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<PlaceEntity, Long> {
    boolean existsByName(String name);
    PlaceEntity findByName(String name);
}

