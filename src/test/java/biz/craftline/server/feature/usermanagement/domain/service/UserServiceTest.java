package biz.craftline.server.feature.usermanagement.domain.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.config.security.UserScopeContext;
import biz.craftline.server.config.security.UserScopeContextHolder;
import biz.craftline.server.feature.membership.infra.entity.MembershipEntity;
import biz.craftline.server.feature.membership.infra.repository.MembershipRepository;
import biz.craftline.server.feature.usermanagement.domain.model.AuthUser;
import biz.craftline.server.feature.usermanagement.domain.model.User;
import biz.craftline.server.feature.usermanagement.infra.entity.PermissionEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.RoleEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.UserEntity;
import biz.craftline.server.feature.usermanagement.infra.repository.RoleRepository;
import biz.craftline.server.feature.usermanagement.infra.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private MembershipRepository membershipRepository;
    @Mock private SecurityContextService securityContextService;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        UserScopeContextHolder.clear();
        lenient().when(securityContextService.getAccessibleStoreIds()).thenReturn(null);
        lenient().when(securityContextService.getAccessibleBusinessIds()).thenReturn(null);
    }

    @AfterEach
    void tearDown() {
        UserScopeContextHolder.clear();
    }

    private UserEntity userEntity(Long id, String email) {
        UserEntity e = new UserEntity();
        e.setId(id);
        e.setFullName("Test User");
        e.setEmail(email);
        e.setPassword(new BCryptPasswordEncoder().encode("secret123"));
        e.setEnabled(true);
        e.setVerified(1);
        e.setRoles(new HashSet<>());
        e.setAllowedPermissions(new HashSet<>());
        e.setDeniedPermissions(new HashSet<>());
        return e;
    }

    @Test
    void getAllUsers_systemAdmin_returnsAll() {
        when(userRepository.findAll()).thenReturn(List.of(userEntity(1L, "a@b.com")));
        List<User> users = userService.getAllUsers();
        assertEquals(1, users.size());
        assertEquals("a@b.com", users.get(0).getEmail());
    }

    @Test
    void getAllUsers_scoped_includesSelfAndMembershipUsers() {
        when(securityContextService.getAccessibleStoreIds()).thenReturn(List.of(5L));
        when(securityContextService.getAccessibleBusinessIds()).thenReturn(List.of());
        when(securityContextService.getCurrentUserId()).thenReturn(99L);

        MembershipEntity m = MembershipEntity.builder().id(1L).userId(10L).businessId(1L)
                .storeScopes(Set.of(5L)).build();
        when(membershipRepository.findByStoreScopeIn(List.of(5L))).thenReturn(List.of(m));
        when(userRepository.findAllById(any())).thenReturn(List.of(
                userEntity(10L, "m@b.com"), userEntity(99L, "self@b.com")));

        List<User> users = userService.getAllUsers();
        assertEquals(2, users.size());
    }

    @Test
    void getUserById_present() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity(1L, "a@b.com")));
        Optional<User> user = userService.getUserById(1L);
        assertTrue(user.isPresent());
    }

    @Test
    void getUserByEmail_present() {
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(userEntity(1L, "a@b.com")));
        assertTrue(userService.getUserByEmail("a@b.com").isPresent());
    }

    @Test
    void assertCanViewUser_allowsSelf() {
        UserScopeContextHolder.set(UserScopeContext.builder()
                .userId(1L).email("a@b.com").roles(List.of()).permissions(Set.of())
                .accessibleStoreIds(List.of(1L)).accessibleBusinessIds(List.of(1L))
                .unrestricted(false).build());
        when(securityContextService.isSystemAdmin()).thenReturn(false);
        when(securityContextService.getCurrentUserId()).thenReturn(1L);

        User u = new User();
        u.setId(1L);
        assertDoesNotThrow(() -> userService.assertCanViewUser(u));
    }

    @Test
    void assertCanViewUser_deniesWithoutOverlap() {
        UserScopeContextHolder.set(UserScopeContext.builder()
                .userId(1L).email("a@b.com").roles(List.of()).permissions(Set.of())
                .accessibleStoreIds(List.of(1L)).accessibleBusinessIds(List.of(1L))
                .unrestricted(false).build());
        when(securityContextService.isSystemAdmin()).thenReturn(false);
        when(securityContextService.getCurrentUserId()).thenReturn(1L);
        when(securityContextService.getAccessibleStoreIds()).thenReturn(List.of(1L));
        when(securityContextService.getAccessibleBusinessIds()).thenReturn(List.of(1L));
        when(membershipRepository.findByUserId(2L)).thenReturn(List.of(
                MembershipEntity.builder().userId(2L).businessId(9L).storeScopes(Set.of(9L)).build()));

        User u = new User();
        u.setId(2L);
        assertThrows(AccessDeniedException.class, () -> userService.assertCanViewUser(u));
    }

    @Test
    void isCurrentUserSystemAdmin_delegates() {
        when(securityContextService.isSystemAdmin()).thenReturn(true);
        assertTrue(userService.isCurrentUserSystemAdmin());
    }

    @Test
    void loadUserByUsername_success() {
        when(userRepository.findByEmailWithRolesAndPermissions("a@b.com"))
                .thenReturn(Optional.of(userEntity(1L, "a@b.com")));
        UserDetails details = userService.loadUserByUsername("a@b.com");
        assertEquals("a@b.com", details.getUsername());
        assertTrue(details.isEnabled());
    }

    @Test
    void loadUserByUsername_notFound() {
        when(userRepository.findByEmailWithRolesAndPermissions("missing@b.com"))
                .thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("missing@b.com"));
    }

    @Test
    void updateUser_success() {
        UserEntity existing = userEntity(1L, "a@b.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        User updates = new User();
        updates.setFullName("New Name");
        updates.setEmail("new@b.com");
        updates.setEnabled(true);
        User result = userService.updateUser(1L, updates);
        assertEquals("New Name", result.getFullName());
        assertEquals("new@b.com", result.getEmail());
    }

    @Test
    void updateUser_notFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.updateUser(1L, new User()));
    }

    @Test
    void deleteUser_delegates() {
        userService.deleteUser(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void assignRole_success() {
        UserEntity user = userEntity(1L, "a@b.com");
        RoleEntity role = new RoleEntity();
        role.setId(2L);
        role.setName("MANAGER");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(2L)).thenReturn(Optional.of(role));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.assignRole(1L, 2L);
        assertEquals(1L, result.getId());
        assertTrue(user.getRoles().contains(role));
    }

    @Test
    void createUserWithHashedPassword_hashes() {
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> {
            UserEntity e = inv.getArgument(0);
            e.setId(5L);
            return e;
        });
        User newUser = new User();
        newUser.setFullName("Ada");
        newUser.setEmail("ada@b.com");
        newUser.setPassword("plainpass");
        newUser.setVerified(1);

        User saved = userService.createUserWithHashedPassword(newUser);
        assertEquals(5L, saved.getId());
        assertNotEquals("plainpass", saved.getPassword());
        assertTrue(new BCryptPasswordEncoder().matches("plainpass", saved.getPassword()));
    }

    @Test
    void updatePassword_success() {
        UserEntity user = userEntity(1L, "a@b.com");
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.updatePassword("a@b.com", "newpass1");
        assertTrue(new BCryptPasswordEncoder().matches("newpass1", user.getPassword()));
    }

    @Test
    void changePassword_wrongCurrent_throws() {
        UserEntity user = userEntity(1L, "a@b.com");
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        assertThrows(BadCredentialsException.class,
                () -> userService.changePassword("a@b.com", "wrong", "newpass1"));
    }

    @Test
    void changePassword_success() {
        UserEntity user = userEntity(1L, "a@b.com");
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.changePassword("a@b.com", "secret123", "brandnew1");
        assertTrue(new BCryptPasswordEncoder().matches("brandnew1", user.getPassword()));
    }

    @Test
    void getAuthUserByEmail_populatesMembershipScopes() {
        UserEntity user = userEntity(1L, "a@b.com");
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        MembershipEntity m = MembershipEntity.builder()
                .id(1L).userId(1L).businessId(20L)
                .status(MembershipEntity.STATUS_ACTIVE)
                .storeScopes(Set.of(30L, 31L))
                .build();
        when(membershipRepository.findByUserIdAndStatus(1L, MembershipEntity.STATUS_ACTIVE))
                .thenReturn(List.of(m));

        AuthUser auth = userService.getAuthUserByEmail("a@b.com");
        assertEquals(List.of(20L), auth.getBusinessIds());
        assertTrue(auth.getStoreIds().containsAll(List.of(30L, 31L)));
    }

    @Test
    void findUserIdByIdOrEmail_byIdOnly() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity(1L, "a@b.com")));
        assertTrue(userService.findUserIdByIdOrEmail(1L, null).isPresent());
    }

    @Test
    void findUserIdByIdOrEmail_emptyWhenBothMissing() {
        assertTrue(userService.findUserIdByIdOrEmail(null, "  ").isEmpty());
    }

    @Test
    void findUser_delegates() {
        when(userRepository.findUser(1L, "a@b.com")).thenReturn(Optional.of(userEntity(1L, "a@b.com")));
        assertTrue(userService.findUser(1L, "a@b.com").isPresent());
    }

    @Test
    void getAllUsers_emptyVisible_returnsEmpty() {
        when(securityContextService.getAccessibleStoreIds()).thenReturn(List.of(5L));
        when(securityContextService.getAccessibleBusinessIds()).thenReturn(List.of());
        when(membershipRepository.findByStoreScopeIn(List.of(5L))).thenReturn(List.of());
        when(securityContextService.getCurrentUserId()).thenThrow(new RuntimeException("no auth"));

        assertTrue(userService.getAllUsers().isEmpty());
    }

    @Test
    void getAllUsers_byBusinessMembership() {
        when(securityContextService.getAccessibleStoreIds()).thenReturn(List.of());
        when(securityContextService.getAccessibleBusinessIds()).thenReturn(List.of(3L));
        when(securityContextService.getCurrentUserId()).thenReturn(1L);
        MembershipEntity m = MembershipEntity.builder().userId(10L).businessId(3L).build();
        when(membershipRepository.findByBusinessIdIn(List.of(3L))).thenReturn(List.of(m));
        when(userRepository.findAllById(any())).thenReturn(List.of(userEntity(10L, "m@b.com")));

        assertEquals(1, userService.getAllUsers().size());
    }

    @Test
    void assertCanViewUser_allowsBusinessOverlap() {
        UserScopeContextHolder.set(UserScopeContext.builder()
                .userId(1L).email("a@b.com").roles(List.of()).permissions(Set.of())
                .accessibleStoreIds(List.of()).accessibleBusinessIds(List.of(3L))
                .unrestricted(false).build());
        when(securityContextService.isSystemAdmin()).thenReturn(false);
        when(securityContextService.getCurrentUserId()).thenReturn(1L);
        when(securityContextService.getAccessibleStoreIds()).thenReturn(List.of());
        when(securityContextService.getAccessibleBusinessIds()).thenReturn(List.of(3L));
        when(membershipRepository.findByUserId(2L)).thenReturn(List.of(
                MembershipEntity.builder().userId(2L).businessId(3L).build()));

        User u = new User();
        u.setId(2L);
        assertDoesNotThrow(() -> userService.assertCanViewUser(u));
    }

    @Test
    void assertCanViewUser_skipsWhenNoScope() {
        User u = new User();
        u.setId(2L);
        assertDoesNotThrow(() -> userService.assertCanViewUser(u));
    }

    @Test
    void assignRole_userNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.assignRole(1L, 2L));
    }

    @Test
    void assignRole_roleNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(userEntity(1L, "a@b.com")));
        when(roleRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.assignRole(1L, 2L));
    }

    @Test
    void updatePassword_userNotFound() {
        when(userRepository.findByEmail("missing@b.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class,
                () -> userService.updatePassword("missing@b.com", "new"));
    }

    @Test
    void getAuthUserByEmail_notFound() {
        when(userRepository.findByEmail("missing@b.com")).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.getAuthUserByEmail("missing@b.com"));
    }

    @Test
    void findUserIdByIdOrEmail_byEmailOnly() {
        when(userRepository.findByEmail("a@b.com")).thenReturn(Optional.of(userEntity(1L, "a@b.com")));
        assertTrue(userService.findUserIdByIdOrEmail(null, "a@b.com").isPresent());
    }

    @Test
    void findUserIdByIdOrEmail_byIdAndEmail() {
        when(userRepository.findByIdOrEmail(1L, "a@b.com")).thenReturn(Optional.of(userEntity(1L, "a@b.com")));
        assertTrue(userService.findUserIdByIdOrEmail(1L, "a@b.com").isPresent());
    }

    @Test
    void loadUserByUsername_includesAuthorities() {
        UserEntity entity = userEntity(1L, "a@b.com");
        RoleEntity role = new RoleEntity();
        role.setName("MANAGER");
        PermissionEntity perm = new PermissionEntity();
        perm.setName("order.read");
        role.setPermissions(Set.of(perm));
        entity.getRoles().add(role);
        when(userRepository.findByEmailWithRolesAndPermissions("a@b.com")).thenReturn(Optional.of(entity));

        UserDetails details = userService.loadUserByUsername("a@b.com");
        assertFalse(details.getAuthorities().isEmpty());
    }
}
