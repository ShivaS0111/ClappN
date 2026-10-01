package biz.craftline.server.feature.membership.infra.repository;

import biz.craftline.server.feature.membership.infra.entity.MembershipRoleAssignmentHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MembershipRoleAssignmentHistoryRepository
        extends JpaRepository<MembershipRoleAssignmentHistoryEntity, Long> {

    List<MembershipRoleAssignmentHistoryEntity> findByMembershipIdOrderByValidFromAsc(Long membershipId);

    /** Currently-in-effect rows for a membership (valid_to IS NULL) — used to compute diffs on update. */
    List<MembershipRoleAssignmentHistoryEntity> findByMembershipIdAndValidToIsNull(Long membershipId);
}
