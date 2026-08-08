package biz.craftline.server.config.security;

import biz.craftline.server.feature.businessstore.infra.entity.StoreEntity;
import biz.craftline.server.feature.businessstore.infra.repository.StoreRepository;
import biz.craftline.server.feature.employeemanagement.infra.entity.EmployeeEntity;
import biz.craftline.server.feature.employeemanagement.infra.repository.EmployeeRepository;
import biz.craftline.server.feature.usermanagement.domain.service.RBACService;
import biz.craftline.server.feature.usermanagement.infra.entity.RoleEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.UserEntity;
import biz.craftline.server.feature.usermanagement.infra.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Loads fresh permissions and store/business scope from the database once per request.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserScopeResolver {

    private static final String ROLE_SYSTEM_ADMIN = "SYSTEM_ADMIN";
    private static final Set<String> BUSINESS_LEVEL_ROLES = Set.of(
            "BUSINESS_OWNER", "BUSINESS_ADMIN", "BUSINESS_MANAGER"
    );

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final StoreRepository storeRepository;
    private final RBACService rbacService;

    @Transactional(readOnly = true)
    public UserScopeContext resolve(String email, Long activeStoreId, Long activeBusinessId) {
        UserEntity user = userRepository.findByEmailWithRolesAndPermissions(email)
                .or(() -> userRepository.findByEmail(email))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        List<String> roles = user.getRoles() == null ? List.of() :
                user.getRoles().stream().map(RoleEntity::getName).filter(Objects::nonNull).toList();

        List<String> permissionList = rbacService.getUserPermissions(email);
        Set<String> permissions = permissionList != null ? new HashSet<>(permissionList) : Set.of();

        boolean unrestricted = roles.stream().anyMatch(ROLE_SYSTEM_ADMIN::equalsIgnoreCase);

        List<Long> accessibleStoreIds;
        List<Long> accessibleBusinessIds;

        if (unrestricted) {
            accessibleStoreIds = null;
            accessibleBusinessIds = null;
        } else {
            List<EmployeeEntity> employees = employeeRepository.findByUserId(user.getId());
            List<Long> assignedStoreIds = employees.stream()
                    .map(EmployeeEntity::getStoreId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();
            List<Long> assignedBusinessIds = employees.stream()
                    .map(EmployeeEntity::getBusinessId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            accessibleBusinessIds = assignedBusinessIds;

            boolean businessLevel = roles.stream().anyMatch(BUSINESS_LEVEL_ROLES::contains);
            if (businessLevel && assignedStoreIds.isEmpty() && !assignedBusinessIds.isEmpty()) {
                accessibleStoreIds = storeRepository.findByBusinessIdIn(assignedBusinessIds).stream()
                        .map(StoreEntity::getId)
                        .distinct()
                        .toList();
            } else {
                accessibleStoreIds = assignedStoreIds;
            }
        }

        if (activeStoreId != null && !unrestricted) {
            if (accessibleStoreIds == null || !accessibleStoreIds.contains(activeStoreId)) {
                throw new AccessDeniedException("You do not have access to store: " + activeStoreId);
            }
        }

        if (activeBusinessId != null && !unrestricted) {
            if (accessibleBusinessIds == null || !accessibleBusinessIds.contains(activeBusinessId)) {
                throw new AccessDeniedException("You do not have access to business: " + activeBusinessId);
            }
        }

        List<Long> effectiveStoreIds = accessibleStoreIds;
        List<Long> effectiveBusinessIds = accessibleBusinessIds;

        if (!unrestricted) {
            if (activeStoreId != null) {
                effectiveStoreIds = List.of(activeStoreId);
            } else if (activeBusinessId != null && accessibleStoreIds != null) {
                Set<Long> storesInBusiness = storeRepository.findByBusinessId(activeBusinessId).stream()
                        .map(StoreEntity::getId)
                        .collect(java.util.stream.Collectors.toSet());
                effectiveStoreIds = accessibleStoreIds.stream()
                        .filter(storesInBusiness::contains)
                        .toList();
            }

            if (activeBusinessId != null) {
                effectiveBusinessIds = List.of(activeBusinessId);
            }
        }

        log.debug("Resolved request scope for {}: unrestricted={}, stores={}, businesses={}, effectiveStores={}, activeStore={}, activeBusiness={}",
                email, unrestricted, accessibleStoreIds, accessibleBusinessIds, effectiveStoreIds, activeStoreId, activeBusinessId);

        return UserScopeContext.builder()
                .userId(user.getId())
                .email(email)
                .roles(roles)
                .permissions(permissions)
                .accessibleStoreIds(accessibleStoreIds)
                .accessibleBusinessIds(accessibleBusinessIds)
                .effectiveStoreIds(effectiveStoreIds)
                .effectiveBusinessIds(effectiveBusinessIds)
                .activeStoreId(activeStoreId)
                .activeBusinessId(activeBusinessId)
                .unrestricted(unrestricted)
                .build();
    }
}
