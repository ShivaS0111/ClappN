package biz.craftline.server.feature.membership.domain.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.membership.api.mapper.MembershipMapper;
import biz.craftline.server.feature.membership.domain.model.Membership;
import biz.craftline.server.feature.membership.infra.entity.EmployeeProfileHistoryEntity;
import biz.craftline.server.feature.membership.infra.entity.MembershipEntity;
import biz.craftline.server.feature.membership.infra.entity.MembershipRoleAssignmentHistoryEntity;
import biz.craftline.server.feature.membership.infra.entity.MembershipStatusHistoryEntity;
import biz.craftline.server.feature.membership.infra.repository.MembershipRepository;
import biz.craftline.server.feature.usermanagement.infra.entity.RoleEntity;
import biz.craftline.server.feature.usermanagement.infra.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tenancy membership API: user ↔ business link with roles and store scopes.
 * Employee HR fields stay on {@code /api/employees}; this surface manages access only.
 */
@Service
@RequiredArgsConstructor
public class MembershipService {

    private final MembershipRepository membershipRepository;
    private final RoleRepository roleRepository;
    private final SecurityContextService securityContextService;
    private final MembershipHistoryService membershipHistoryService;

    @Transactional(readOnly = true)
    public List<Membership> listAccessible() {
        List<Long> storeIds = securityContextService.getAccessibleStoreIds();
        if (storeIds == null) {
            return membershipRepository.findAll().stream()
                    .map(MembershipMapper::fromEntity)
                    .collect(Collectors.toList());
        }
        Set<Long> seen = new HashSet<>();
        List<Membership> result = new java.util.ArrayList<>();
        List<Long> businessIds = securityContextService.getAccessibleBusinessIds();
        if (businessIds != null && !businessIds.isEmpty()) {
            membershipRepository.findByBusinessIdIn(businessIds).stream()
                    .filter(m -> seen.add(m.getId()))
                    .map(MembershipMapper::fromEntity)
                    .forEach(result::add);
        }
        if (!storeIds.isEmpty()) {
            membershipRepository.findByStoreScopeIn(storeIds).stream()
                    .filter(m -> seen.add(m.getId()))
                    .map(MembershipMapper::fromEntity)
                    .forEach(result::add);
        }
        return result;
    }

    @Transactional(readOnly = true)
    public Membership getById(Long id) {
        MembershipEntity entity = membershipRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Membership not found: " + id));
        assertCanAccess(entity);
        return MembershipMapper.fromEntity(entity);
    }

    @Transactional(readOnly = true)
    public List<Membership> listByBusiness(Long businessId) {
        securityContextService.validateBusinessAccess(businessId);
        return membershipRepository.findByBusinessId(businessId).stream()
                .map(MembershipMapper::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Membership> listByUser(Long userId) {
        return membershipRepository.findByUserId(userId).stream()
                .filter(this::canAccess)
                .map(MembershipMapper::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public Membership create(Membership request) {
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (request.getBusinessId() == null) {
            throw new IllegalArgumentException("businessId is required");
        }
        securityContextService.validateBusinessAccess(request.getBusinessId());
        validateStoreScopes(request.getStoreIds());

        membershipRepository.findByUserIdAndBusinessId(request.getUserId(), request.getBusinessId())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Membership already exists for user " + request.getUserId()
                                    + " and business " + request.getBusinessId());
                });

        MembershipEntity entity = MembershipEntity.builder()
                .userId(request.getUserId())
                .businessId(request.getBusinessId())
                .status(resolveStatus(request.getStatus(), MembershipEntity.STATUS_ACTIVE))
                .roles(new HashSet<>())
                .storeScopes(new HashSet<>())
                .build();

        applyRoles(entity, request.getRoleIds());
        applyStoreScopes(entity, request.getStoreIds());

        MembershipEntity saved = membershipRepository.save(entity);
        // Brand-new membership: previous status is null so history reflects the initial grant
        // instead of a no-op ACTIVE->ACTIVE entry.
        recordHistory(saved, null);
        return MembershipMapper.fromEntity(saved);
    }

    @Transactional
    public Membership update(Long id, Membership updates) {
        MembershipEntity entity = membershipRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Membership not found: " + id));
        assertCanAccess(entity);

        String previousStatus = entity.getStatus();

        if (updates.getBusinessId() != null
                && !Objects.equals(updates.getBusinessId(), entity.getBusinessId())) {
            throw new IllegalArgumentException("businessId cannot be changed");
        }
        if (updates.getUserId() != null
                && !Objects.equals(updates.getUserId(), entity.getUserId())) {
            throw new IllegalArgumentException("userId cannot be changed");
        }
        if (updates.getStatus() != null && !updates.getStatus().isBlank()) {
            entity.setStatus(updates.getStatus().trim().toUpperCase());
        }
        if (updates.getRoleIds() != null) {
            applyRoles(entity, updates.getRoleIds());
        }
        if (updates.getStoreIds() != null) {
            validateStoreScopes(updates.getStoreIds());
            applyStoreScopes(entity, updates.getStoreIds());
        }
        MembershipEntity saved = membershipRepository.save(entity);
        recordHistory(saved, previousStatus);
        return MembershipMapper.fromEntity(saved);
    }

    @Transactional
    public Membership deactivate(Long id) {
        return deactivate(id, null);
    }

    @Transactional
    public Membership deactivate(Long id, String reason) {
        MembershipEntity entity = membershipRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Membership not found: " + id));
        assertCanAccess(entity);
        String previousStatus = entity.getStatus();
        entity.setStatus(MembershipEntity.STATUS_INACTIVE);
        MembershipEntity saved = membershipRepository.save(entity);
        membershipHistoryService.recordStatusChange(
                saved.getId(), previousStatus, saved.getStatus(), reason);
        // Close access periods in history; current role rows on membership stay for rehire.
        membershipHistoryService.closeOpenRoleAssignments(saved.getId());
        return MembershipMapper.fromEntity(saved);
    }

    @Transactional
    public Membership activate(Long id) {
        return activate(id, null);
    }

    @Transactional
    public Membership activate(Long id, String reason) {
        MembershipEntity entity = membershipRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Membership not found: " + id));
        assertCanAccess(entity);
        String previousStatus = entity.getStatus();
        entity.setStatus(MembershipEntity.STATUS_ACTIVE);
        MembershipEntity saved = membershipRepository.save(entity);
        membershipHistoryService.recordStatusChange(
                saved.getId(), previousStatus, saved.getStatus(), reason);
        // Re-open role-assignment history from current membership roles/scopes.
        membershipHistoryService.recordRoleAssignmentChanges(
                saved.getId(), saved.getUserId(), saved.getBusinessId(),
                saved.getRoles(), saved.getStoreScopes());
        return MembershipMapper.fromEntity(saved);
    }

    /** Full change history (status + roles + profile + derived employment periods). */
    @Transactional(readOnly = true)
    public MembershipHistory getHistory(Long id) {
        MembershipEntity entity = membershipRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Membership not found: " + id));
        assertCanAccess(entity);
        List<MembershipStatusHistoryEntity> statusHistory =
                membershipHistoryService.getStatusHistory(id);
        return new MembershipHistory(
                id,
                statusHistory,
                membershipHistoryService.getRoleAssignmentHistory(id),
                membershipHistoryService.getProfileHistory(id),
                MembershipHistoryService.deriveEmploymentPeriods(statusHistory));
    }

    private void recordHistory(MembershipEntity saved, String previousStatus) {
        membershipHistoryService.recordStatusChange(saved.getId(), previousStatus, saved.getStatus());
        membershipHistoryService.recordRoleAssignmentChanges(
                saved.getId(), saved.getUserId(), saved.getBusinessId(),
                saved.getRoles(), saved.getStoreScopes());
    }

    /** Aggregate holder for membership/employee change history. */
    public record MembershipHistory(
            Long membershipId,
            List<MembershipStatusHistoryEntity> statusHistory,
            List<MembershipRoleAssignmentHistoryEntity> roleAssignmentHistory,
            List<EmployeeProfileHistoryEntity> profileHistory,
            List<MembershipHistoryService.EmploymentPeriod> employmentPeriods) {
    }

    private void applyRoles(MembershipEntity entity, Set<Long> roleIds) {
        if (roleIds == null) {
            return;
        }
        Set<RoleEntity> roles = new HashSet<>();
        for (Long roleId : roleIds) {
            if (roleId == null) continue;
            RoleEntity role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
            roles.add(role);
        }
        if (entity.getRoles() == null) {
            entity.setRoles(new HashSet<>());
        }
        entity.getRoles().clear();
        entity.getRoles().addAll(roles);
    }

    private void applyStoreScopes(MembershipEntity entity, Set<Long> storeIds) {
        if (storeIds == null) {
            return;
        }
        entity.setStoreScopes(new HashSet<>(storeIds));
    }

    private void validateStoreScopes(Set<Long> storeIds) {
        if (storeIds == null) {
            return;
        }
        for (Long storeId : storeIds) {
            if (storeId != null) {
                securityContextService.validateStoreAccess(storeId);
            }
        }
    }

    private static String resolveStatus(String status, String defaultStatus) {
        if (status == null || status.isBlank()) {
            return defaultStatus;
        }
        return status.trim().toUpperCase();
    }

    private boolean canAccess(MembershipEntity m) {
        try {
            assertCanAccess(m);
            return true;
        } catch (AccessDeniedException ex) {
            return false;
        }
    }

    private void assertCanAccess(MembershipEntity m) {
        if (securityContextService.isSystemAdmin()) {
            return;
        }
        if (m.getBusinessId() != null) {
            List<Long> businessIds = securityContextService.getAccessibleBusinessIds();
            if (businessIds == null || businessIds.contains(m.getBusinessId())) {
                return;
            }
        }
        List<Long> storeIds = securityContextService.getAccessibleStoreIds();
        if (storeIds == null) {
            return;
        }
        if (m.getStoreScopes() != null && m.getStoreScopes().stream().anyMatch(storeIds::contains)) {
            return;
        }
        throw new AccessDeniedException("You do not have access to membership: " + m.getId());
    }
}
