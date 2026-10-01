package biz.craftline.server.feature.membership.domain.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.membership.infra.entity.MembershipRoleAssignmentHistoryEntity;
import biz.craftline.server.feature.membership.infra.entity.MembershipStatusHistoryEntity;
import biz.craftline.server.feature.membership.infra.repository.MembershipRoleAssignmentHistoryRepository;
import biz.craftline.server.feature.membership.infra.repository.MembershipStatusHistoryRepository;
import biz.craftline.server.feature.usermanagement.infra.entity.RoleEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Records append-only/effective-dated history whenever a membership's status or
 * role-at-scope (business-wide or per-store) assignments change.
 * <p>
 * Purely additive: never mutates {@code membership}, {@code membership_role}, or
 * {@code membership_store_scope} (the current-state tables used for authorization),
 * and failures here must never block the primary create/update/deactivate flows.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MembershipHistoryService {

    private final MembershipStatusHistoryRepository statusHistoryRepository;
    private final MembershipRoleAssignmentHistoryRepository assignmentHistoryRepository;
    private final SecurityContextService securityContextService;

    @Transactional
    public void recordStatusChange(Long membershipId, String oldStatus, String newStatus) {
        if (membershipId == null || newStatus == null || newStatus.equalsIgnoreCase(oldStatus)) {
            return;
        }
        safely(() -> statusHistoryRepository.save(MembershipStatusHistoryEntity.builder()
                .membershipId(membershipId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedByUserId(currentUserIdOrNull())
                .build()));
    }

    /**
     * Recomputes the full set of (role, scope) assignments implied by the current roles
     * and store scopes, diffs it against the currently-open history rows for this
     * membership, closes rows that no longer apply, and opens rows for new ones.
     * <p>
     * Scope is BUSINESS (store_id null) when storeIds is empty, otherwise one STORE row
     * per (role, store) combination — matching how roles currently apply across all
     * scoped stores collectively in {@code MembershipEntity}.
     */
    @Transactional
    public void recordRoleAssignmentChanges(Long membershipId, Long userId, Long businessId,
                                             Set<RoleEntity> roles, Set<Long> storeIds) {
        if (membershipId == null) {
            return;
        }
        Set<RoleEntity> effectiveRoles = roles == null ? Set.of() : roles;
        Set<Long> effectiveStores = storeIds == null ? Set.of() : storeIds;

        List<MembershipRoleAssignmentHistoryEntity> openRows =
                assignmentHistoryRepository.findByMembershipIdAndValidToIsNull(membershipId);

        Set<Key> desired = new HashSet<>();
        if (effectiveStores.isEmpty()) {
            for (RoleEntity role : effectiveRoles) {
                if (role.getId() != null) {
                    desired.add(new Key(role.getId(), null));
                }
            }
        } else {
            for (RoleEntity role : effectiveRoles) {
                if (role.getId() == null) continue;
                for (Long storeId : effectiveStores) {
                    if (storeId != null) {
                        desired.add(new Key(role.getId(), storeId));
                    }
                }
            }
        }

        Long actor = currentUserIdOrNull();
        Timestamp now = new Timestamp(System.currentTimeMillis());

        List<Key> stillOpenKeys = new ArrayList<>();
        for (MembershipRoleAssignmentHistoryEntity open : openRows) {
            Key key = new Key(open.getRoleId(), open.getStoreId());
            if (desired.contains(key)) {
                stillOpenKeys.add(key);
            } else {
                safely(() -> {
                    open.setValidTo(now);
                    open.setChangedByUserId(actor);
                    assignmentHistoryRepository.save(open);
                });
            }
        }

        for (RoleEntity role : effectiveRoles) {
            if (role.getId() == null) continue;
            if (effectiveStores.isEmpty()) {
                Key key = new Key(role.getId(), null);
                if (!stillOpenKeys.contains(key)) {
                    openAssignment(membershipId, userId, businessId, null, role, actor, now);
                }
            } else {
                for (Long storeId : effectiveStores) {
                    if (storeId == null) continue;
                    Key key = new Key(role.getId(), storeId);
                    if (!stillOpenKeys.contains(key)) {
                        openAssignment(membershipId, userId, businessId, storeId, role, actor, now);
                    }
                }
            }
        }
    }

    private void openAssignment(Long membershipId, Long userId, Long businessId, Long storeId,
                                 RoleEntity role, Long actor, Timestamp validFrom) {
        safely(() -> assignmentHistoryRepository.save(MembershipRoleAssignmentHistoryEntity.builder()
                .scope(storeId == null
                        ? MembershipRoleAssignmentHistoryEntity.SCOPE_BUSINESS
                        : MembershipRoleAssignmentHistoryEntity.SCOPE_STORE)
                .userId(userId)
                .membershipId(membershipId)
                .businessId(businessId)
                .storeId(storeId)
                .roleId(role.getId())
                .roleName(role.getName())
                .validFrom(validFrom)
                .changedByUserId(actor)
                .build()));
    }

    @Transactional(readOnly = true)
    public List<MembershipStatusHistoryEntity> getStatusHistory(Long membershipId) {
        return statusHistoryRepository.findByMembershipIdOrderByChangedAtAsc(membershipId);
    }

    @Transactional(readOnly = true)
    public List<MembershipRoleAssignmentHistoryEntity> getRoleAssignmentHistory(Long membershipId) {
        return assignmentHistoryRepository.findByMembershipIdOrderByValidFromAsc(membershipId);
    }

    private Long currentUserIdOrNull() {
        try {
            return securityContextService.getCurrentUserId();
        } catch (Exception ex) {
            return null;
        }
    }

    private void safely(Runnable action) {
        try {
            action.run();
        } catch (Exception ex) {
            log.warn("Failed to record membership history: {}", ex.getMessage(), ex);
        }
    }

    private record Key(Long roleId, Long storeId) {
    }
}
