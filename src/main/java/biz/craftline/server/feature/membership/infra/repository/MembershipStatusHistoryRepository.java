package biz.craftline.server.feature.membership.infra.repository;

import biz.craftline.server.feature.membership.infra.entity.MembershipStatusHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MembershipStatusHistoryRepository extends JpaRepository<MembershipStatusHistoryEntity, Long> {

    List<MembershipStatusHistoryEntity> findByMembershipIdOrderByChangedAtAsc(Long membershipId);
}
