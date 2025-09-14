package biz.craftline.server.feature.usermanagement.infra.repository;

import biz.craftline.server.feature.usermanagement.infra.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<PermissionEntity, Long> {
    Optional<PermissionEntity> findByName(String name);
}

