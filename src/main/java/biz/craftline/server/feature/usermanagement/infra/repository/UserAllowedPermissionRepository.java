package biz.craftline.server.feature.usermanagement.infra.repository;

import biz.craftline.server.feature.usermanagement.infra.entity.UserAllowedPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAllowedPermissionRepository extends JpaRepository<UserAllowedPermissionEntity, Long> {
    List<UserAllowedPermissionEntity> findByUserId(Long id);
}