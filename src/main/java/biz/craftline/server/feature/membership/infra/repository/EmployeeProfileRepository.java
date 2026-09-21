package biz.craftline.server.feature.membership.infra.repository;

import biz.craftline.server.feature.membership.infra.entity.EmployeeProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeProfileRepository extends JpaRepository<EmployeeProfileEntity, Long> {
    Optional<EmployeeProfileEntity> findByMembershipId(Long membershipId);

    Optional<EmployeeProfileEntity> findByUserIdAndMembershipId(Long userId, Long membershipId);
}
