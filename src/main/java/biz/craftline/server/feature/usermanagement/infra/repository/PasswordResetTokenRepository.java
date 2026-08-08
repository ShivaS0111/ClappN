package biz.craftline.server.feature.usermanagement.infra.repository;

import biz.craftline.server.feature.usermanagement.infra.entity.PasswordResetTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetTokenEntity, Long> {

    Optional<PasswordResetTokenEntity> findByTokenAndUsedFalse(String token);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("UPDATE PasswordResetTokenEntity t SET t.used = true WHERE t.email = :email AND t.used = false")
    void invalidateAllForEmail(@Param("email") String email);
}
