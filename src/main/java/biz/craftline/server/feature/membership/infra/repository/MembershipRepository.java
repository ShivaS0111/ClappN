package biz.craftline.server.feature.membership.infra.repository;

import biz.craftline.server.feature.membership.infra.entity.MembershipEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<MembershipEntity, Long> {

    Optional<MembershipEntity> findByUserIdAndBusinessId(Long userId, Long businessId);

    List<MembershipEntity> findByUserId(Long userId);

    List<MembershipEntity> findByUserIdAndStatus(Long userId, String status);

    List<MembershipEntity> findByBusinessId(Long businessId);

    List<MembershipEntity> findByBusinessIdIn(Collection<Long> businessIds);

    @Query("SELECT DISTINCT m FROM MembershipEntity m JOIN m.storeScopes s WHERE s IN :storeIds")
    List<MembershipEntity> findByStoreScopeIn(@Param("storeIds") Collection<Long> storeIds);
}
