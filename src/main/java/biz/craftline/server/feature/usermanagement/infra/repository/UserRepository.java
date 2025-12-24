package biz.craftline.server.feature.usermanagement.infra.repository;

import biz.craftline.server.feature.usermanagement.infra.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);

    @Query("""
        SELECT u FROM user u
        WHERE (:userId IS NULL OR u.id = :userId)
          AND (:email IS NULL OR u.email = :email)
    """)
    Optional<UserEntity> findUser(Long userId, String email);

    Optional<UserEntity> findByIdOrEmail(Long userId, String email);
}

