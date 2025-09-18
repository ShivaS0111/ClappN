package biz.craftline.server.feature.addressmanagement.infra.repository;

import biz.craftline.server.feature.addressmanagement.infra.entity.AreaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AreaRepository extends JpaRepository<AreaEntity, Long> {
    boolean existsByName(String name);
    AreaEntity findByName(String name);
}

