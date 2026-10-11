package biz.craftline.server.feature.membership.infra.repository;

import biz.craftline.server.feature.membership.infra.entity.EmployeeProfileHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeProfileHistoryRepository extends JpaRepository<EmployeeProfileHistoryEntity, Long> {

    List<EmployeeProfileHistoryEntity> findByMembershipIdOrderByChangedAtAsc(Long membershipId);
}
