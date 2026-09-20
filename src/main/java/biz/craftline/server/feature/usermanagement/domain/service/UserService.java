package biz.craftline.server.feature.usermanagement.domain.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.membership.infra.entity.MembershipEntity;
import biz.craftline.server.feature.membership.infra.repository.MembershipRepository;
import biz.craftline.server.feature.usermanagement.domain.model.AuthUser;
import biz.craftline.server.feature.usermanagement.domain.model.User;
import biz.craftline.server.feature.usermanagement.api.mapper.UserMapper;
import biz.craftline.server.feature.usermanagement.infra.entity.UserEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.RoleEntity;
import biz.craftline.server.feature.usermanagement.infra.repository.UserRepository;
import biz.craftline.server.feature.usermanagement.infra.repository.RoleRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private MembershipRepository membershipRepository;
    @Autowired
    private SecurityContextService securityContextService;

    public List<User> getAllUsers() {
        List<Long> accessibleStoreIds = securityContextService.getAccessibleStoreIds();
        List<Long> accessibleBusinessIds = securityContextService.getAccessibleBusinessIds();

        // SYSTEM_ADMIN — unrestricted
        if (accessibleStoreIds == null && accessibleBusinessIds == null) {
            return userRepository.findAll().stream()
                    .map(UserMapper::toDomain)
                    .collect(Collectors.toList());
        }

        Set<Long> visibleUserIds = new HashSet<>();
        if (accessibleStoreIds != null && !accessibleStoreIds.isEmpty()) {
            membershipRepository.findByStoreScopeIn(accessibleStoreIds).stream()
                    .map(MembershipEntity::getUserId)
                    .filter(Objects::nonNull)
                    .forEach(visibleUserIds::add);
        }
        if (accessibleBusinessIds != null && !accessibleBusinessIds.isEmpty()) {
            membershipRepository.findByBusinessIdIn(accessibleBusinessIds).stream()
                    .map(MembershipEntity::getUserId)
                    .filter(Objects::nonNull)
                    .forEach(visibleUserIds::add);
        }

        // Always include self
        try {
            visibleUserIds.add(securityContextService.getCurrentUserId());
        } catch (Exception ignored) {
            // no authenticated context
        }

        if (visibleUserIds.isEmpty()) {
            return List.of();
        }

        return userRepository.findAllById(visibleUserIds).stream()
                .map(UserMapper::toDomain)
                .collect(Collectors.toList());
    }

    public Optional<User> getUserById(Long id) {
        Optional<User> user = userRepository.findById(id).map(UserMapper::toDomain);
        user.ifPresent(this::assertCanViewUser);
        return user;
    }

    public Optional<User> getUserByEmail(String email) {
        // Used by auth and internal flows — do not enforce API scope here.
        // Controllers that expose this must call assertCanViewUser.
        return userRepository.findByEmail(email).map(UserMapper::toDomain);
    }

    /**
     * Enforce that the current request may view the given user (store/business overlap or self).
     */
    public void assertCanViewUser(User user) {
        if (!biz.craftline.server.config.security.UserScopeContextHolder.isPresent()) {
            return;
        }
        if (securityContextService.isSystemAdmin()) {
            return;
        }
        if (Objects.equals(user.getId(), securityContextService.getCurrentUserId())) {
            return;
        }

        List<Long> accessibleStoreIds = securityContextService.getAccessibleStoreIds();
        List<Long> accessibleBusinessIds = securityContextService.getAccessibleBusinessIds();

        List<MembershipEntity> targetMemberships = membershipRepository.findByUserId(user.getId());
        boolean allowed = targetMemberships.stream().anyMatch(m -> {
            if (accessibleBusinessIds != null && m.getBusinessId() != null
                    && accessibleBusinessIds.contains(m.getBusinessId())) {
                return true;
            }
            if (accessibleStoreIds != null && m.getStoreScopes() != null) {
                return m.getStoreScopes().stream().anyMatch(accessibleStoreIds::contains);
            }
            return false;
        });

        if (!allowed) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not have access to user: " + user.getId());
        }
    }

    public boolean isCurrentUserSystemAdmin() {
        return securityContextService.isSystemAdmin();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Use fetch-join query to load roles, permissions and user-specific overrides in one round-trip
        AuthUser user = userRepository.findByEmailWithRolesAndPermissions(username)
                .map(UserMapper::toAuthUser)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));
        log.info("Loaded user: {}", user.getEmail());

        return new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return user.getPermissions().stream().map(p->
                        (GrantedAuthority) () -> p).collect(Collectors.toList());
            }

            @Override
            public String getPassword() {
                return user.getPassword();
            }

            @Override
            public String getUsername() {
                return user.getEmail();
            }

            @Override
            public boolean isAccountNonExpired() {
                return true;
            }

            @Override
            public boolean isAccountNonLocked() {
                return true;
            }

            @Override
            public boolean isCredentialsNonExpired() {
                return true;
            }

            @Override
            public boolean isEnabled() {
                return user.isEnabled();
            }
        };
    }

    private User createUser(User user) {
        UserEntity entity = UserMapper.toEntity(user);
        UserEntity saved = userRepository.save(entity);
        return UserMapper.toDomain(saved);
    }

    public User updateUser(Long id, User userDetails) {
        return userRepository.findById(id).map(userEntity -> {
            userEntity.setFullName(userDetails.getFullName());
            userEntity.setEmail(userDetails.getEmail());
            // Password changes must go through updatePassword / changePassword (hashed)
            userEntity.setEnabled(userDetails.isEnabled());
            userEntity.setAccountNonLocked(userDetails.isAccountNonLocked());
            userEntity.setAccountNonExpired(userDetails.isAccountNonExpired());
            userEntity.setCredentialsNonExpired(userDetails.isCredentialsNonExpired());
            UserEntity updated = userRepository.save(userEntity);
            return UserMapper.toDomain(updated);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User assignRole(Long userId, Long roleId) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        RoleEntity role = roleRepository.findById(roleId).orElseThrow(() -> new RuntimeException("Role not found"));
        userEntity.getRoles().add(role);
        UserEntity updated = userRepository.save(userEntity);
        return UserMapper.toDomain(updated);
    }

    public User createUserWithHashedPassword(User newUser) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode(newUser.getPassword());
        newUser.setPassword(hashedPassword);
        return createUser(newUser);
    }

    /**
     * Update password for an existing user (hashed). Does not create a new user.
     */
    public void updatePassword(String email, String rawPassword) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        user.setPassword(encoder.encode(rawPassword));
        userRepository.save(user);
    }

    /**
     * Change password for the current user after verifying the old password.
     */
    public void changePassword(String email, String currentRawPassword, String newRawPassword) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(currentRawPassword, user.getPassword())) {
            throw new org.springframework.security.authentication.BadCredentialsException("Current password is incorrect");
        }
        user.setPassword(encoder.encode(newRawPassword));
        userRepository.save(user);
    }

    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public AuthUser getAuthUserByEmail(
            @NotBlank(message = "Username is required")
            @Email(message = "Username must be a valid email")
            String username) {
        AuthUser authUser = userRepository.findByEmail(username).map(UserMapper::toAuthUser)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        // Populate storeIds and businessIds from Membership
        List<MembershipEntity> memberships = membershipRepository
                .findByUserIdAndStatus(authUser.getId(), MembershipEntity.STATUS_ACTIVE);
        List<Long> storeIds = memberships.stream()
                .flatMap(m -> m.getStoreScopes() == null ? java.util.stream.Stream.<Long>empty() : m.getStoreScopes().stream())
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        // Expand business-level empty scopes for auth payload convenience
        List<Long> businessIds = memberships.stream()
                .map(MembershipEntity::getBusinessId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        authUser.setStoreIds(storeIds);
        authUser.setBusinessIds(businessIds);

        return authUser;
    }

    public Optional<User> findUser(Long userId, String email) {
        return userRepository.findUser(userId, email)
                .map(UserMapper::toDomain);
    }

    public Optional<User> findUserIdByIdOrEmail(Long userId, String email) {

        boolean emailValid = email != null && !email.isBlank();

        if (userId != null && emailValid) {
            return userRepository.findByIdOrEmail(userId, email).map(UserMapper::toDomain);
        }
        if (userId != null) {
            return userRepository.findById(userId).map(UserMapper::toDomain);
        }
        if (emailValid) {
            return userRepository.findByEmail(email).map(UserMapper::toDomain);
        }
        return Optional.empty();
    }
}
