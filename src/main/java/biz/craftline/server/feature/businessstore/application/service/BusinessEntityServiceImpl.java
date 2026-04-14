package biz.craftline.server.feature.businessstore.application.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.businessstore.domain.model.Business;
import biz.craftline.server.feature.businessstore.domain.service.BusinessEntityService;
import biz.craftline.server.feature.businessstore.infra.entity.BusinessEntity;
import biz.craftline.server.feature.businessstore.infra.mapper.BusinessEntityMapper;
import biz.craftline.server.feature.businessstore.infra.repository.BusinessEntityJpaRepository;
import biz.craftline.server.feature.employeemanagement.api.mapper.EmployeeMapper;
import biz.craftline.server.feature.employeemanagement.domain.model.Employee;
import biz.craftline.server.feature.employeemanagement.domain.service.EmployeeService;
import biz.craftline.server.feature.usermanagement.domain.model.User;
import biz.craftline.server.feature.usermanagement.domain.service.RoleService;
import biz.craftline.server.feature.usermanagement.domain.service.UserService;
import biz.craftline.server.feature.usermanagement.infra.entity.RoleEntity;
import biz.craftline.server.feature.usermanagement.infra.repository.RoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
@Repository
public class BusinessEntityServiceImpl implements BusinessEntityService {

    @Autowired
    BusinessEntityMapper mapper;

    @Autowired
    BusinessEntityJpaRepository businessEntityRepository;

    @Autowired
    SecurityContextService securityContextService;

    @Autowired
    private UserService userService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private RoleService roleService;

    @Override
    public List<Business> findAll() {
        List<Long> accessibleBusinessIds = securityContextService.getAccessibleBusinessIds();
        if (accessibleBusinessIds == null) {
            // SYSTEM_ADMIN — unrestricted
            return businessEntityRepository.findAll().stream().map(mapper::toDomain).collect(Collectors.toList());
        }
        if (accessibleBusinessIds.isEmpty()) {
            return List.of();
        }
        return businessEntityRepository.findAllById(accessibleBusinessIds).stream()
                .map(mapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteBusinessTypeById(Long id) {
        securityContextService.validateBusinessAccess(id);
        businessEntityRepository.deleteBusinessTypeById(id);
    }

    @Override
    public Optional<Business> findById(Long id) {
        securityContextService.validateBusinessAccess(id);
        return businessEntityRepository.findById(id).map(businessEntity -> mapper.toDomain(businessEntity));
    }

    @Override
    public Business save(Business business) {
        if (business.getId() != null) {
            securityContextService.validateBusinessAccess(business.getId());
        }
        BusinessEntity entity = mapper.toEntity(business);
        return mapper.toDomain(businessEntityRepository.save(entity));
    }

    @Override
    public List<Business> search(String keyword) {
        List<Business> results = businessEntityRepository.findByNameContaining(keyword.toLowerCase())
                .stream().map(mapper::toDomain).collect(Collectors.toList());
        List<Long> accessibleBusinessIds = securityContextService.getAccessibleBusinessIds();
        if (accessibleBusinessIds == null) {
            return results; // SYSTEM_ADMIN
        }
        return results.stream()
                .filter(b -> accessibleBusinessIds.contains(b.getId()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Business createBusinessWithOwner(Business business, String ownerName, String ownerEmail, String ownerPhone, String ownerPassword) {
        // Save business
        Business savedBusiness = save(business);

        // Check if user already exists
        userService.getUserByEmail(ownerEmail).ifPresent(u -> {
            throw new RuntimeException("Owner email already exists: " + ownerEmail);
        });

        User ownerUser = new User();
        ownerUser.setFullName(ownerName);
        ownerUser.setEmail(ownerEmail);
        ownerUser.setPassword(ownerPassword);
        ownerUser.setEnabled(true);
        ownerUser.setAccountNonExpired(true);
        ownerUser.setAccountNonLocked(true);
        ownerUser.setCredentialsNonExpired(true);
        User savedOwner = userService.createUserWithHashedPassword(ownerUser);

        // Assign BusinessOwner role
        Long ownerRoleId = roleService.getRoleByName("BUSINESS_OWNER")
                .orElseThrow(() -> new RuntimeException("BusinessOwner role not found")).getId();
        userService.assignRole(savedOwner.getId(), ownerRoleId);

        // Create Employee record
        Employee ownerEmployee = new Employee();
        ownerEmployee.setUserId(savedOwner.getId());
        ownerEmployee.setRoleId(ownerRoleId);
        ownerEmployee.setBusinessId(savedBusiness.getId());
        ownerEmployee.setName(ownerName);
        ownerEmployee.setEmail(ownerEmail);
        ownerEmployee.setPhone(ownerPhone);
        ownerEmployee.setEmployeeCode("OWNER-" + savedBusiness.getId());
        employeeService.createEmployee(ownerEmployee);

        return savedBusiness;
    }
}
