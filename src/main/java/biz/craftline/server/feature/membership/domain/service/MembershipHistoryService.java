package biz.craftline.server.feature.membership.domain.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.membership.infra.entity.EmployeeProfileEntity;
import biz.craftline.server.feature.membership.infra.entity.EmployeeProfileHistoryEntity;
import biz.craftline.server.feature.membership.infra.entity.MembershipRoleAssignmentHistoryEntity;
import biz.craftline.server.feature.membership.infra.entity.MembershipStatusHistoryEntity;
import biz.craftline.server.feature.membership.infra.repository.EmployeeProfileHistoryRepository;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Records append-only/effective-dated history whenever a membership's status,
 * role-at-scope assignments, or employee profile fields change.
 * <p>
 * Purely additive vs current-state tables used for authorization. Failures here
 * are logged and must not block primary create/update/deactivate flows.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MembershipHistoryService {

    private final MembershipStatusHistoryRepository statusHistoryRepository;
    private final MembershipRoleAssignmentHistoryRepository assignmentHistoryRepository;
    private final EmployeeProfileHistoryRepository profileHistoryRepository;
    private final SecurityContextService securityContextService;

    @Transactional
    public void recordStatusChange(Long membershipId, String oldStatus, String newStatus) {
        recordStatusChange(membershipId, oldStatus, newStatus, null);
    }

    @Transactional
    public void recordStatusChange(Long membershipId, String oldStatus, String newStatus, String reason) {
        if (membershipId == null || newStatus == null || newStatus.equalsIgnoreCase(oldStatus)) {
            return;
        }
        String trimmedReason = reason != null && !reason.isBlank() ? reason.trim() : null;
        if (trimmedReason != null && trimmedReason.length() > 512) {
            trimmedReason = trimmedReason.substring(0, 512);
        }
        String finalReason = trimmedReason;
        safely(() -> statusHistoryRepository.save(MembershipStatusHistoryEntity.builder()
                .membershipId(membershipId)
                .oldStatus(oldStatus)
                .newStatus(newStatus)
                .changedByUserId(currentUserIdOrNull())
                .reason(finalReason)
                .build()));
    }

    /**
     * Recomputes the full set of (role, scope) assignments implied by the current roles
     * and store scopes, diffs it against the currently-open history rows for this
     * membership, closes rows that no longer apply, and opens rows for new ones.
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

    /** Closes all open role-assignment history rows (used on deactivate / exit). */
    @Transactional
    public void closeOpenRoleAssignments(Long membershipId) {
        if (membershipId == null) {
            return;
        }
        Long actor = currentUserIdOrNull();
        Timestamp now = new Timestamp(System.currentTimeMillis());
        List<MembershipRoleAssignmentHistoryEntity> openRows =
                assignmentHistoryRepository.findByMembershipIdAndValidToIsNull(membershipId);
        for (MembershipRoleAssignmentHistoryEntity open : openRows) {
            safely(() -> {
                open.setValidTo(now);
                open.setChangedByUserId(actor);
                assignmentHistoryRepository.save(open);
            });
        }
    }

    /**
     * Diffs before/after profile field maps and appends one history row per changed field.
     * Keys should be stable API field names (employeeNumber, jobTitle, hireDate, …).
     */
    @Transactional
    public void recordProfileFieldChanges(Long membershipId,
                                          Map<String, String> before,
                                          Map<String, String> after) {
        if (membershipId == null || after == null) {
            return;
        }
        Map<String, String> oldMap = before != null ? before : Map.of();
        Long actor = currentUserIdOrNull();
        Set<String> keys = new HashSet<>();
        keys.addAll(oldMap.keySet());
        keys.addAll(after.keySet());
        for (String field : keys) {
            String oldVal = normalize(oldMap.get(field));
            String newVal = normalize(after.get(field));
            if (Objects.equals(oldVal, newVal)) {
                continue;
            }
            safely(() -> profileHistoryRepository.save(EmployeeProfileHistoryEntity.builder()
                    .membershipId(membershipId)
                    .fieldName(field)
                    .oldValue(truncate(oldVal))
                    .newValue(truncate(newVal))
                    .changedByUserId(actor)
                    .build()));
        }
    }

    public static Map<String, String> snapshotProfile(EmployeeProfileEntity profile) {
        Map<String, String> map = new LinkedHashMap<>();
        if (profile == null) {
            return map;
        }
        put(map, "employeeNumber", profile.getEmployeeNumber());
        put(map, "hireDate", profile.getHireDate());
        put(map, "leaveDate", profile.getLeaveDate());
        put(map, "jobTitle", profile.getJobTitle());
        put(map, "name", profile.getName());
        put(map, "firstName", profile.getFirstName());
        put(map, "lastName", profile.getLastName());
        put(map, "surName", profile.getSurName());
        put(map, "email", profile.getEmail());
        put(map, "phone", profile.getPhone());
        return map;
    }

    public static Map<String, String> withBusinessId(Map<String, String> profileFields, Long businessId) {
        Map<String, String> map = new LinkedHashMap<>(profileFields != null ? profileFields : Map.of());
        put(map, "businessId", businessId != null ? String.valueOf(businessId) : null);
        return map;
    }

    /**
     * Derives employment stints from status transitions: ACTIVE opens a period;
     * INACTIVE / SUSPENDED closes it.
     */
    public static List<EmploymentPeriod> deriveEmploymentPeriods(
            List<MembershipStatusHistoryEntity> statusHistory) {
        List<EmploymentPeriod> periods = new ArrayList<>();
        if (statusHistory == null || statusHistory.isEmpty()) {
            return periods;
        }
        Timestamp openStart = null;
        for (MembershipStatusHistoryEntity change : statusHistory) {
            String status = change.getNewStatus() != null ? change.getNewStatus().trim().toUpperCase() : "";
            Timestamp at = change.getChangedAt() != null
                    ? change.getChangedAt()
                    : new Timestamp(System.currentTimeMillis());
            if ("ACTIVE".equals(status)) {
                if (openStart == null) {
                    openStart = at;
                }
            } else if ("INACTIVE".equals(status) || "SUSPENDED".equals(status)) {
                if (openStart != null) {
                    periods.add(new EmploymentPeriod(openStart, at, "ACTIVE", status));
                    openStart = null;
                }
            }
        }
        if (openStart != null) {
            periods.add(new EmploymentPeriod(openStart, null, "ACTIVE", null));
        }
        return periods;
    }

    @Transactional(readOnly = true)
    public List<MembershipStatusHistoryEntity> getStatusHistory(Long membershipId) {
        return statusHistoryRepository.findByMembershipIdOrderByChangedAtAsc(membershipId);
    }

    @Transactional(readOnly = true)
    public List<MembershipRoleAssignmentHistoryEntity> getRoleAssignmentHistory(Long membershipId) {
        return assignmentHistoryRepository.findByMembershipIdOrderByValidFromAsc(membershipId);
    }

    @Transactional(readOnly = true)
    public List<EmployeeProfileHistoryEntity> getProfileHistory(Long membershipId) {
        return profileHistoryRepository.findByMembershipIdOrderByChangedAtAsc(membershipId);
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

    private static void put(Map<String, String> map, String key, String value) {
        map.put(key, normalize(value));
    }

    private static String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= 512 ? value : value.substring(0, 512);
    }

    private record Key(Long roleId, Long storeId) {
    }

    /** Derived employment stint from status history. */
    public record EmploymentPeriod(
            Timestamp startedAt,
            Timestamp endedAt,
            String startStatus,
            String endStatus) {
    }
}
