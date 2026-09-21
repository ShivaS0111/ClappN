package biz.craftline.server.feature.employeemanagement.domain.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.businessstore.infra.entity.BusinessEntity;
import biz.craftline.server.feature.businessstore.infra.entity.StoreEntity;
import biz.craftline.server.feature.businessstore.infra.repository.StoreRepository;
import biz.craftline.server.feature.employeemanagement.domain.model.Employee;
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
class EmployeeServiceTest {

    @Mock private MembershipRepository membershipRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private StoreRepository storeRepository;
    @Mock private SecurityContextService securityContextService;

    @InjectMocks
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        lenient().when(securityContextService.getAccessibleStoreIds()).thenReturn(null);
        lenient().when(securityContextService.isSystemAdmin()).thenReturn(true);
        lenient().doNothing().when(securityContextService).validateStoreAccess(anyLong());
        lenient().doNothing().when(securityContextService).validateBusinessAccess(anyLong());
    }

    private MembershipEntity membership(Long id, Long userId, Long businessId) {
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
    void getAllEmployees_unrestricted_returnsAll() {
        when(membershipRepository.findAll()).thenReturn(List.of(membership(1L, 10L, 2L)));
        List<Employee> result = employeeService.getAllEmployees();
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void getAllEmployees_byStoreScope() {
        when(securityContextService.getAccessibleStoreIds()).thenReturn(List.of(5L));
        when(securityContextService.getAccessibleBusinessIds()).thenReturn(List.of());
        when(membershipRepository.findByStoreScopeIn(List.of(5L)))
                .thenReturn(List.of(membership(1L, 10L, 2L)));

        List<Employee> result = employeeService.getAllEmployees();
        assertEquals(1, result.size());
    }

    @Test
    void getAllEmployees_emptyStores_fallsBackToBusiness() {
        when(securityContextService.getAccessibleStoreIds()).thenReturn(List.of());
        when(securityContextService.getAccessibleBusinessIds()).thenReturn(List.of(3L));
        when(membershipRepository.findByBusinessIdIn(List.of(3L)))
                .thenReturn(List.of(membership(2L, 11L, 3L)));

        assertEquals(1, employeeService.getAllEmployees().size());
    }

    @Test
    void getEmployeeById_present() {
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(membership(1L, 10L, 2L)));
        Optional<Employee> result = employeeService.getEmployeeById(1L);
        assertTrue(result.isPresent());
        assertEquals(10L, result.get().getUserId());
    }

    @Test
    void getEmployeesByStoreId_validatesStore() {
        when(membershipRepository.findByStoreScopeIn(List.of(5L))).thenReturn(List.of());
        employeeService.getEmployeesByStoreId(5L);
        verify(securityContextService).validateStoreAccess(5L);
    }

    @Test
    void getEmployeesByBusinessId_validatesBusiness() {
        when(membershipRepository.findByBusinessId(2L)).thenReturn(List.of());
        employeeService.getEmployeesByBusinessId(2L);
        verify(securityContextService).validateBusinessAccess(2L);
    }

    @Test
    void createEmployee_requiresUserId() {
        Employee e = new Employee();
        e.setBusinessId(1L);
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(e));
    }

    @Test
    void createEmployee_requiresBusinessOrStore() {
        Employee e = new Employee();
        e.setUserId(9L);
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(e));
    }

    @Test
    void createEmployee_newMembership_saves() {
        Employee e = new Employee();
        e.setUserId(9L);
        e.setBusinessId(2L);
        e.setName("Ada");
        e.setJobTitle("Cashier");

        when(membershipRepository.findByUserIdAndBusinessId(9L, 2L)).thenReturn(Optional.empty());
        when(membershipRepository.save(any(MembershipEntity.class))).thenAnswer(inv -> {
            MembershipEntity m = inv.getArgument(0);
            m.setId(100L);
            return m;
        });

        Employee created = employeeService.createEmployee(e);
        assertEquals(100L, created.getId());
        assertEquals("Ada", created.getName());
        verify(securityContextService).validateBusinessAccess(2L);
    }

    @Test
    void createEmployee_withRoleAndStore() {
        Employee e = new Employee();
        e.setUserId(9L);
        e.setBusinessId(2L);
        e.setRoleId(4L);
        e.setStoreId(5L);

        RoleEntity role = new RoleEntity();
        role.setId(4L);
        when(roleRepository.findById(4L)).thenReturn(Optional.of(role));
        when(membershipRepository.findByUserIdAndBusinessId(9L, 2L)).thenReturn(Optional.empty());
        when(membershipRepository.save(any(MembershipEntity.class))).thenAnswer(inv -> {
            MembershipEntity m = inv.getArgument(0);
            m.setId(101L);
            return m;
        });

        Employee created = employeeService.createEmployee(e);
        assertEquals(4L, created.getRoleId());
        assertEquals(5L, created.getStoreId());
        verify(securityContextService).validateStoreAccess(5L);
    }

    @Test
    void updateEmployee_notFound() {
        when(membershipRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> employeeService.updateEmployee(1L, new Employee()));
    }

    @Test
    void updateEmployee_updatesStatusAndProfile() {
        MembershipEntity m = membership(1L, 9L, 2L);
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(m));
        when(membershipRepository.save(any(MembershipEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Employee updates = new Employee();
        updates.setStatus("INACTIVE");
        updates.setEmail("a@b.com");
        Employee result = employeeService.updateEmployee(1L, updates);
        assertEquals("INACTIVE", result.getStatus());
        assertEquals("a@b.com", result.getEmail());
    }

    @Test
    void deleteEmployee_success() {
        MembershipEntity m = membership(1L, 9L, 2L);
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(m));
        employeeService.deleteEmployee(1L);
        verify(membershipRepository).delete(m);
    }

    @Test
    void assertCanAccessMembership_deniedForNonAdmin() {
        when(securityContextService.isSystemAdmin()).thenReturn(false);
        when(securityContextService.getAccessibleBusinessIds()).thenReturn(List.of(99L));
        when(securityContextService.getAccessibleStoreIds()).thenReturn(List.of(88L));
        MembershipEntity m = membership(1L, 9L, 2L);
        m.setStoreScopes(Set.of(1L));
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(m));

        assertThrows(AccessDeniedException.class, () -> employeeService.getEmployeeById(1L));
    }

    @Test
    void createEmployee_nullRequest_throws() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(null));
    }

    @Test
    void getAllEmployees_emptyStoresAndBusiness_returnsEmpty() {
        when(securityContextService.getAccessibleStoreIds()).thenReturn(List.of());
        when(securityContextService.getAccessibleBusinessIds()).thenReturn(List.of());
        assertTrue(employeeService.getAllEmployees().isEmpty());
    }

    @Test
    void getAllEmployees_mergesBusinessLevelMemberships() {
        when(securityContextService.getAccessibleStoreIds()).thenReturn(List.of(5L));
        when(securityContextService.getAccessibleBusinessIds()).thenReturn(List.of(3L));
        MembershipEntity storeMember = membership(1L, 10L, 2L);
        storeMember.setStoreScopes(Set.of(5L));
        MembershipEntity businessMember = membership(2L, 11L, 3L);
        businessMember.setStoreScopes(Set.of());
        when(membershipRepository.findByStoreScopeIn(List.of(5L))).thenReturn(List.of(storeMember));
        when(membershipRepository.findByBusinessIdIn(List.of(3L))).thenReturn(List.of(businessMember));

        List<Employee> result = employeeService.getAllEmployees();
        assertEquals(2, result.size());
    }

    @Test
    void createEmployee_resolvesBusinessFromStore() {
        Employee e = new Employee();
        e.setUserId(9L);
        e.setStoreId(7L);
        StoreEntity store = new StoreEntity();
        BusinessEntity business = new BusinessEntity();
        business.setId(4L);
        store.setBusiness(business);
        when(storeRepository.findById(7L)).thenReturn(Optional.of(store));
        when(membershipRepository.findByUserIdAndBusinessId(9L, 4L)).thenReturn(Optional.empty());
        when(membershipRepository.save(any(MembershipEntity.class))).thenAnswer(inv -> {
            MembershipEntity m = inv.getArgument(0);
            m.setId(200L);
            return m;
        });

        Employee created = employeeService.createEmployee(e);
        assertEquals(200L, created.getId());
        verify(securityContextService).validateBusinessAccess(4L);
        verify(securityContextService).validateStoreAccess(7L);
    }

    @Test
    void createEmployee_existingMembership_updates() {
        Employee e = new Employee();
        e.setUserId(9L);
        e.setBusinessId(2L);
        e.setStatus("INACTIVE");
        MembershipEntity existing = membership(50L, 9L, 2L);
        when(membershipRepository.findByUserIdAndBusinessId(9L, 2L)).thenReturn(Optional.of(existing));
        when(membershipRepository.save(any(MembershipEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Employee result = employeeService.createEmployee(e);
        assertEquals("INACTIVE", result.getStatus());
        assertEquals(50L, result.getId());
    }

    @Test
    void createEmployee_invalidRole_throws() {
        Employee e = new Employee();
        e.setUserId(9L);
        e.setBusinessId(2L);
        e.setRoleId(99L);
        when(membershipRepository.findByUserIdAndBusinessId(9L, 2L)).thenReturn(Optional.empty());
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(e));
    }

    @Test
    void updateEmployee_changesBusinessId() {
        MembershipEntity m = membership(1L, 9L, 2L);
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(m));
        when(membershipRepository.save(any(MembershipEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Employee updates = new Employee();
        updates.setBusinessId(5L);
        employeeService.updateEmployee(1L, updates);
        assertEquals(5L, m.getBusinessId());
        verify(securityContextService).validateBusinessAccess(5L);
    }

    @Test
    void assertCanAccessMembership_allowedViaBusiness() {
        when(securityContextService.isSystemAdmin()).thenReturn(false);
        when(securityContextService.getAccessibleBusinessIds()).thenReturn(List.of(2L));
        MembershipEntity m = membership(1L, 9L, 2L);
        when(membershipRepository.findById(1L)).thenReturn(Optional.of(m));

        assertTrue(employeeService.getEmployeeById(1L).isPresent());
    }
}
