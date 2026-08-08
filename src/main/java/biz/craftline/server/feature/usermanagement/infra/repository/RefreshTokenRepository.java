package biz.craftline.server.feature.usermanagement.infra.repository;

import biz.craftline.server.feature.usermanagement.infra.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
    Optional<RefreshTokenEntity> findByToken(String token);
    void deleteByToken(String token);
    java.util.List<RefreshTokenEntity> findAllByUsername(String username);
    void deleteAllByUsername(String username);
    // find non-revoked token by token string
    Optional<RefreshTokenEntity> findByTokenAndRevokedFalse(String token);
}

