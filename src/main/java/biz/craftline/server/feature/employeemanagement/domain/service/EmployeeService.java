package biz.craftline.server.feature.employeemanagement.domain.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.businessstore.infra.entity.StoreEntity;
import biz.craftline.server.feature.businessstore.infra.repository.StoreRepository;
import biz.craftline.server.feature.employeemanagement.api.mapper.EmployeeMapper;
import biz.craftline.server.feature.employeemanagement.domain.model.Employee;
import biz.craftline.server.feature.membership.infra.entity.EmployeeProfileEntity;
import biz.craftline.server.feature.membership.infra.entity.MembershipEntity;
import biz.craftline.server.feature.membership.infra.repository.MembershipRepository;
import biz.craftline.server.feature.usermanagement.infra.entity.RoleEntity;
import biz.craftline.server.feature.usermanagement.infra.repository.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Employee API facade over Membership (tenancy/roles/scopes) + EmployeeProfile (HR).
 * Response id = membership id.
 */
@Service
public class EmployeeService {

    @Autowired
    private MembershipRepository membershipRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private StoreRepository storeRepository;

    @Autowired
    private SecurityContextService securityContextService;

    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees() {
        List<Long> accessibleStoreIds = securityContextService.getAccessibleStoreIds();
        if (accessibleStoreIds == null) {
            return membershipRepository.findAll().stream()
                    .map(EmployeeMapper::fromMembership)
                    .collect(Collectors.toList());
        }
        if (accessibleStoreIds.isEmpty()) {
            List<Long> accessibleBusinessIds = securityContextService.getAccessibleBusinessIds();
            if (accessibleBusinessIds != null && !accessibleBusinessIds.isEmpty()) {
                return membershipRepository.findByBusinessIdIn(accessibleBusinessIds).stream()
                        .map(EmployeeMapper::fromMembership)
                        .collect(Collectors.toList());
            }
            return List.of();
        }

        Set<Long> seen = new HashSet<>();
        List<Employee> byStore = membershipRepository.findByStoreScopeIn(accessibleStoreIds).stream()
                .filter(m -> seen.add(m.getId()))
                .map(EmployeeMapper::fromMembership)
                .collect(Collectors.toList());

        List<Long> accessibleBusinessIds = securityContextService.getAccessibleBusinessIds();
        if (accessibleBusinessIds != null && !accessibleBusinessIds.isEmpty()) {
            membershipRepository.findByBusinessIdIn(accessibleBusinessIds).stream()
                    .filter(m -> m.getStoreScopes() == null || m.getStoreScopes().isEmpty())
                    .filter(m -> seen.add(m.getId()))
                    .map(EmployeeMapper::fromMembership)
                    .forEach(byStore::add);
        }
        return byStore;
    }

    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeById(Long id) {
        Optional<MembershipEntity> membership = membershipRepository.findById(id);
        membership.ifPresent(this::assertCanAccessMembership);
        return membership.map(EmployeeMapper::fromMembership);
    }

    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByStoreId(Long storeId) {
        securityContextService.validateStoreAccess(storeId);
        return membershipRepository.findByStoreScopeIn(List.of(storeId)).stream()
                .map(EmployeeMapper::fromMembership)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Employee> getEmployeesByBusinessId(Long businessId) {
        securityContextService.validateBusinessAccess(businessId);
        return membershipRepository.findByBusinessId(businessId).stream()
                .map(EmployeeMapper::fromMembership)
                .collect(Collectors.toList());
    }

    @Transactional
    public Employee createEmployee(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee request is required");
        }
        Long businessId = resolveBusinessId(employee);
        if (businessId == null) {
            throw new IllegalArgumentException("businessId or storeId is required");
        }
        if (employee.getUserId() == null) {
            throw new IllegalArgumentException("userId is required");
        }

        securityContextService.validateBusinessAccess(businessId);
        if (employee.getStoreId() != null) {
            securityContextService.validateStoreAccess(employee.getStoreId());
        }

        MembershipEntity membership = membershipRepository
                .findByUserIdAndBusinessId(employee.getUserId(), businessId)
                .orElseGet(() -> MembershipEntity.builder()
                        .userId(employee.getUserId())
                        .businessId(businessId)
                        .status(MembershipEntity.STATUS_ACTIVE)
                        .roles(new HashSet<>())
                        .storeScopes(new HashSet<>())
                        .build());

        if (employee.getStatus() != null && !employee.getStatus().isBlank()) {
            membership.setStatus(employee.getStatus());
        } else if (membership.getStatus() == null) {
            membership.setStatus(MembershipEntity.STATUS_ACTIVE);
        }

        applyRole(membership, employee.getRoleId());
        applyStoreScope(membership, employee.getStoreId());
        applyProfile(membership, employee);

        MembershipEntity saved = membershipRepository.save(membership);
        return EmployeeMapper.fromMembership(saved);
    }

    @Transactional
    public Employee updateEmployee(Long id, Employee updates) {
        MembershipEntity membership = membershipRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee (membership) not found: " + id));
        assertCanAccessMembership(membership);

        if (updates.getBusinessId() != null
                && !Objects.equals(updates.getBusinessId(), membership.getBusinessId())) {
            securityContextService.validateBusinessAccess(updates.getBusinessId());
            membership.setBusinessId(updates.getBusinessId());
        }
        if (updates.getStatus() != null && !updates.getStatus().isBlank()) {
            membership.setStatus(updates.getStatus());
        }
        if (updates.getRoleId() != null) {
            applyRole(membership, updates.getRoleId());
        }
        if (updates.getStoreId() != null) {
            securityContextService.validateStoreAccess(updates.getStoreId());
            applyStoreScope(membership, updates.getStoreId());
        }
        applyProfile(membership, updates);

        return EmployeeMapper.fromMembership(membershipRepository.save(membership));
    }

    @Transactional
    public void deleteEmployee(Long id) {
        MembershipEntity membership = membershipRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee (membership) not found: " + id));
        assertCanAccessMembership(membership);
        membershipRepository.delete(membership);
    }

    private void assertCanAccessMembership(MembershipEntity m) {
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

    private Long resolveBusinessId(Employee employee) {
        if (employee.getBusinessId() != null) {
            return employee.getBusinessId();
        }
        if (employee.getStoreId() == null) {
            return null;
        }
        return storeRepository.findById(employee.getStoreId())
                .map(StoreEntity::getBusiness)
                .map(b -> b != null ? b.getId() : null)
                .orElse(null);
    }

    private void applyRole(MembershipEntity membership, Long roleId) {
        if (roleId == null) {
            return;
        }
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        if (membership.getRoles() == null) {
            membership.setRoles(new HashSet<>());
        }
        // Single primary role for API compatibility (replace set)
        membership.getRoles().clear();
        membership.getRoles().add(role);
    }

    private void applyStoreScope(MembershipEntity membership, Long storeId) {
        if (storeId == null) {
            return;
        }
        if (membership.getStoreScopes() == null) {
            membership.setStoreScopes(new HashSet<>());
        }
        membership.getStoreScopes().add(storeId);
    }

    private void applyProfile(MembershipEntity membership, Employee employee) {
        EmployeeProfileEntity profile = membership.getProfile();
        if (profile == null) {
            profile = EmployeeProfileEntity.builder()
                    .userId(employee.getUserId() != null ? employee.getUserId() : membership.getUserId())
                    .membership(membership)
                    .build();
            membership.setProfile(profile);
        }
        if (employee.getUserId() != null) {
            profile.setUserId(employee.getUserId());
        } else if (profile.getUserId() == null) {
            profile.setUserId(membership.getUserId());
        }
        if (employee.getEmployeeCode() != null) {
            profile.setEmployeeNumber(employee.getEmployeeCode());
        }
        if (employee.getJoinDate() != null) {
            profile.setHireDate(employee.getJoinDate());
        }
        if (employee.getJobTitle() != null) {
            profile.setJobTitle(employee.getJobTitle());
        }
        if (employee.getName() != null) {
            profile.setName(employee.getName());
        }
        if (employee.getFirstName() != null) {
            profile.setFirstName(employee.getFirstName());
        }
        if (employee.getLastName() != null) {
            profile.setLastName(employee.getLastName());
        }
        if (employee.getSurName() != null) {
            profile.setSurName(employee.getSurName());
        }
        if (employee.getEmail() != null) {
            profile.setEmail(employee.getEmail());
        }
        if (employee.getPhone() != null) {
            profile.setPhone(employee.getPhone());
        }
        if (employee.getLeaveDate() != null) {
            profile.setLeaveDate(employee.getLeaveDate());
        }
    }
}
