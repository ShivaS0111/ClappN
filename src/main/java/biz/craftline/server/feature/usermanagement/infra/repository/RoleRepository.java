package biz.craftline.server.feature.usermanagement.infra.repository;

import biz.craftline.server.feature.usermanagement.infra.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByName(String name);
}

