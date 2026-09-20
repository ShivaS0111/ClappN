package biz.craftline.server.config.security;

import biz.craftline.server.feature.businessstore.infra.entity.StoreEntity;
import biz.craftline.server.feature.businessstore.infra.repository.StoreRepository;
import biz.craftline.server.feature.membership.infra.entity.MembershipEntity;
import biz.craftline.server.feature.membership.infra.repository.MembershipRepository;
import biz.craftline.server.feature.usermanagement.infra.entity.PermissionEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.RoleEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.UserAllowedPermissionEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.UserDeniedPermissionEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.UserEntity;
import biz.craftline.server.feature.usermanagement.infra.repository.UserAllowedPermissionRepository;
import biz.craftline.server.feature.usermanagement.infra.repository.UserDeniedPermissionRepository;
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
import java.util.stream.Collectors;

/**
 * Loads permissions and store/business scope from Membership (+ platform User roles).
 * JWT is identity only; EmployeeProfile is HR data and is not used for access.
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
    private final MembershipRepository membershipRepository;
    private final StoreRepository storeRepository;
    private final UserAllowedPermissionRepository allowedPermissionRepository;
    private final UserDeniedPermissionRepository deniedPermissionRepository;

    @Transactional(readOnly = true)
    public UserScopeContext resolve(String email, Long activeStoreId, Long activeBusinessId) {
        UserEntity user = userRepository.findByEmailWithRolesAndPermissions(email)
                .or(() -> userRepository.findByEmail(email))
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        List<MembershipEntity> memberships = membershipRepository
                .findByUserIdAndStatus(user.getId(), MembershipEntity.STATUS_ACTIVE);

        boolean unrestricted = user.getRoles() != null && user.getRoles().stream()
                .map(RoleEntity::getName)
                .filter(Objects::nonNull)
                .anyMatch(ROLE_SYSTEM_ADMIN::equalsIgnoreCase);

        // Memberships relevant to this request (optional active business / store filter for roles)
        List<MembershipEntity> roleMemberships = memberships;
        if (!unrestricted && activeBusinessId != null) {
            roleMemberships = memberships.stream()
                    .filter(m -> Objects.equals(m.getBusinessId(), activeBusinessId))
                    .toList();
            if (roleMemberships.isEmpty()) {
                throw new AccessDeniedException("You do not have access to business: " + activeBusinessId);
            }
        }

        Set<String> roleNames = new HashSet<>();
        if (user.getRoles() != null) {
            user.getRoles().stream()
                    .map(RoleEntity::getName)
                    .filter(Objects::nonNull)
                    .forEach(roleNames::add);
        }
        for (MembershipEntity m : roleMemberships) {
            if (m.getRoles() != null) {
                m.getRoles().stream()
                        .map(RoleEntity::getName)
                        .filter(Objects::nonNull)
                        .forEach(roleNames::add);
            }
        }

        Set<String> permissions = buildPermissions(user, roleMemberships);

        List<Long> accessibleStoreIds;
        List<Long> accessibleBusinessIds;

        if (unrestricted) {
            accessibleStoreIds = null;
            accessibleBusinessIds = null;
        } else {
            accessibleBusinessIds = memberships.stream()
                    .map(MembershipEntity::getBusinessId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .toList();

            Set<Long> storeIds = new HashSet<>();
            for (MembershipEntity m : memberships) {
                Set<Long> scopes = m.getStoreScopes() != null ? m.getStoreScopes() : Set.of();
                boolean businessLevel = m.getRoles() != null && m.getRoles().stream()
                        .map(RoleEntity::getName)
                        .filter(Objects::nonNull)
                        .anyMatch(BUSINESS_LEVEL_ROLES::contains);

                if (!scopes.isEmpty()) {
                    storeIds.addAll(scopes);
                } else if (businessLevel) {
                    storeRepository.findByBusinessId(m.getBusinessId()).stream()
                            .map(StoreEntity::getId)
                            .forEach(storeIds::add);
                }
            }
            accessibleStoreIds = storeIds.stream().distinct().toList();
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
                        .collect(Collectors.toSet());
                effectiveStoreIds = accessibleStoreIds.stream()
                        .filter(storesInBusiness::contains)
                        .toList();
            }

            if (activeBusinessId != null) {
                effectiveBusinessIds = List.of(activeBusinessId);
            }
        }

        log.debug("Resolved membership scope for {}: unrestricted={}, stores={}, businesses={}, roles={}",
                email, unrestricted, accessibleStoreIds, accessibleBusinessIds, roleNames);

        return UserScopeContext.builder()
                .userId(user.getId())
                .email(email)
                .roles(roleNames.stream().sorted().toList())
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

    private Set<String> buildPermissions(UserEntity user, List<MembershipEntity> memberships) {
        Set<String> permissions = new HashSet<>();

        if (user.getRoles() != null) {
            user.getRoles().stream()
                    .filter(Objects::nonNull)
                    .flatMap(r -> r.getPermissions() == null ? java.util.stream.Stream.empty() : r.getPermissions().stream())
                    .map(PermissionEntity::getName)
                    .filter(Objects::nonNull)
                    .forEach(permissions::add);
        }

        for (MembershipEntity m : memberships) {
            if (m.getRoles() == null) continue;
            m.getRoles().stream()
                    .filter(Objects::nonNull)
                    .flatMap(r -> r.getPermissions() == null ? java.util.stream.Stream.empty() : r.getPermissions().stream())
                    .map(PermissionEntity::getName)
                    .filter(Objects::nonNull)
                    .forEach(permissions::add);
        }

        List<UserAllowedPermissionEntity> allowed = allowedPermissionRepository.findByUserId(user.getId());
        if (allowed != null) {
            allowed.stream()
                    .map(UserAllowedPermissionEntity::getPermission)
                    .filter(Objects::nonNull)
                    .map(PermissionEntity::getName)
                    .forEach(permissions::add);
        }

        List<UserDeniedPermissionEntity> denied = deniedPermissionRepository.findByUserId(user.getId());
        if (denied != null) {
            denied.stream()
                    .map(UserDeniedPermissionEntity::getPermission)
                    .filter(Objects::nonNull)
                    .map(PermissionEntity::getName)
                    .forEach(permissions::remove);
        }

        return permissions;
    }
}
