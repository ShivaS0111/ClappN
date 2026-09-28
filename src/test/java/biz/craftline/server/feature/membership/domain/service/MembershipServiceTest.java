package biz.craftline.server.feature.membership.domain.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.membership.domain.model.Membership;
import biz.craftline.server.feature.membership.infra.entity.MembershipEntity;
import biz.craftline.server.feature.membership.infra.repository.MembershipRepository;
import biz.craftline.server.feature.usermanagement.infra.entity.RoleEntity;
import biz.craftline.server.feature.usermanagement.infra.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MembershipServiceTest {

    @Mock MembershipRepository membershipRepository;
    @Mock RoleRepository roleRepository;
    @Mock SecurityContextService securityContextService;

    @InjectMocks MembershipService membershipService;

    @BeforeEach
    void setUp() {
        lenient().when(securityContextService.isSystemAdmin()).thenReturn(true);
        lenient().when(securityContextService.getAccessibleStoreIds()).thenReturn(null);
        lenient().when(securityContextService.getAccessibleBusinessIds()).thenReturn(null);
    }

    private MembershipEntity entity(Long id, Long userId, Long businessId) {
        return MembershipEntity.builder()
                .id(id)
                .userId(userId)
                .businessId(businessId)
                .status(MembershipEntity.STATUS_ACTIVE)
                .roles(new HashSet<>())
                .storeScopes(new HashSet<>())
                .build();
    }

    @Test
    void create_success() {
        when(membershipRepository.findByUserIdAndBusinessId(10L, 2L)).thenReturn(Optional.empty());
        RoleEntity role = new RoleEntity();
        role.setId(5L);
        when(roleRepository.findById(5L)).thenReturn(Optional.of(role));
        when(membershipRepository.save(any(MembershipEntity.class))).thenAnswer(inv -> {
            MembershipEntity m = inv.getArgument(0);
            m.setId(100L);
            return m;
        });

        Membership req = new Membership();
        req.setUserId(10L);
        req.setBusinessId(2L);
        req.setRoleIds(Set.of(5L));
        req.setStoreIds(Set.of(7L));

        Membership created = membershipService.create(req);
        assertEquals(100L, created.getId());
        assertEquals(Set.of(5L), created.getRoleIds());
        assertEquals(Set.of(7L), created.getStoreIds());
        verify(securityContextService).validateBusinessAccess(2L);
        verify(securityContextService).validateStoreAccess(7L);
    }

    @Test
    void create_duplicate_throws() {
        when(membershipRepository.findByUserIdAndBusinessId(10L, 2L))
                .thenReturn(Optional.of(entity(1L, 10L, 2L)));
        Membership req = new Membership();
        req.setUserId(10L);
        req.setBusinessId(2L);
        assertThrows(IllegalArgumentException.class, () -> membershipService.create(req));
    }

    @Test
    void update_replacesRolesAndStores() {
        MembershipEntity m = entity(1L, 10L, 2L);
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(m));
        RoleEntity role = new RoleEntity();
        role.setId(9L);
        when(roleRepository.findById(9L)).thenReturn(Optional.of(role));
        when(membershipRepository.save(any(MembershipEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Membership updates = new Membership();
        updates.setRoleIds(Set.of(9L));
        updates.setStoreIds(Set.of(3L, 4L));
        updates.setStatus("SUSPENDED");

        Membership result = membershipService.update(1L, updates);
        assertEquals("SUSPENDED", result.getStatus());
        assertEquals(Set.of(9L), result.getRoleIds());
        assertEquals(Set.of(3L, 4L), result.getStoreIds());
    }

    @Test
    void deactivate_setsInactive() {
        MembershipEntity m = entity(1L, 10L, 2L);
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(m));
        when(membershipRepository.save(any(MembershipEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Membership result = membershipService.deactivate(1L);
        assertEquals(MembershipEntity.STATUS_INACTIVE, result.getStatus());
    }

    @Test
    void activate_setsActive() {
        MembershipEntity m = entity(1L, 10L, 2L);
        m.setStatus(MembershipEntity.STATUS_INACTIVE);
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(m));
        when(membershipRepository.save(any(MembershipEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Membership result = membershipService.activate(1L);
        assertEquals(MembershipEntity.STATUS_ACTIVE, result.getStatus());
    }

    @Test
    void getById_notFound() {
        when(membershipRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> membershipService.getById(99L));
    }

    @Test
    void getById_deniedForNonAdmin() {
        when(securityContextService.isSystemAdmin()).thenReturn(false);
        when(securityContextService.getAccessibleBusinessIds()).thenReturn(List.of(99L));
        when(securityContextService.getAccessibleStoreIds()).thenReturn(List.of(88L));
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(entity(1L, 10L, 2L)));

        assertThrows(AccessDeniedException.class, () -> membershipService.getById(1L));
    }

    @Test
    void listByBusiness_validatesScope() {
        when(membershipRepository.findByBusinessId(2L)).thenReturn(List.of(entity(1L, 10L, 2L)));
        List<Membership> list = membershipService.listByBusiness(2L);
        assertEquals(1, list.size());
        verify(securityContextService).validateBusinessAccess(2L);
    }

    @Test
    void update_cannotChangeBusinessId() {
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(entity(1L, 10L, 2L)));
        Membership updates = new Membership();
        updates.setBusinessId(99L);
        assertThrows(IllegalArgumentException.class, () -> membershipService.update(1L, updates));
    }
}
