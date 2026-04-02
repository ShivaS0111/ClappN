package biz.craftline.server.feature.employeemanagement.domain.service;

import biz.craftline.server.config.security.SecurityContextService;
import biz.craftline.server.feature.employeemanagement.domain.model.Employee;
import biz.craftline.server.feature.employeemanagement.api.mapper.EmployeeMapper;
import biz.craftline.server.feature.employeemanagement.infra.entity.EmployeeEntity;
import biz.craftline.server.feature.employeemanagement.infra.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private SecurityContextService securityContextService;

    public List<Employee> getAllEmployees() {
        List<Long> accessibleStoreIds = securityContextService.getAccessibleStoreIds();
        if (accessibleStoreIds == null) {
            // SYSTEM_ADMIN — unrestricted
            return employeeRepository.findAll().stream()
                .map(EmployeeMapper::toDomain)
                .collect(Collectors.toList());
        }
        if (accessibleStoreIds.isEmpty()) {
            // Fall back to business-level filtering
            List<Long> accessibleBusinessIds = securityContextService.getAccessibleBusinessIds();
            if (accessibleBusinessIds != null && !accessibleBusinessIds.isEmpty()) {
                return employeeRepository.findByBusinessIdIn(accessibleBusinessIds).stream()
                    .map(EmployeeMapper::toDomain)
                    .collect(Collectors.toList());
            }
            return List.of();
        }
        return employeeRepository.findByStoreIdIn(accessibleStoreIds).stream()
            .map(EmployeeMapper::toDomain)
            .collect(Collectors.toList());
    }

    public Optional<Employee> getEmployeeById(Long id) {
        Optional<Employee> employee = employeeRepository.findById(id).map(EmployeeMapper::toDomain);
        employee.ifPresent(e -> {
            if (e.getStoreId() != null) {
                securityContextService.validateStoreAccess(e.getStoreId());
            } else if (e.getBusinessId() != null) {
                securityContextService.validateBusinessAccess(e.getBusinessId());
            }
        });
        return employee;
    }

    public List<Employee> getEmployeesByStoreId(Long storeId) {
        securityContextService.validateStoreAccess(storeId);
        return employeeRepository.findByStoreId(storeId).stream()
            .map(EmployeeMapper::toDomain)
            .collect(Collectors.toList());
    }

    public List<Employee> getEmployeesByBusinessId(Long businessId) {
        securityContextService.validateBusinessAccess(businessId);
        return employeeRepository.findByBusinessId(businessId).stream()
            .map(EmployeeMapper::toDomain)
            .collect(Collectors.toList());
    }

    public Employee createEmployee(Employee employee) {
        // Validate scope for the target store/business
        if (employee.getStoreId() != null) {
            securityContextService.validateStoreAccess(employee.getStoreId());
        }
        if (employee.getBusinessId() != null) {
            securityContextService.validateBusinessAccess(employee.getBusinessId());
        }
        EmployeeEntity entity = EmployeeMapper.toEntity(employee);
        EmployeeEntity saved = employeeRepository.save(entity);
        return EmployeeMapper.toDomain(saved);
    }

    public void deleteEmployee(Long id) {
        employeeRepository.findById(id).ifPresent(entity -> {
            if (entity.getStoreId() != null) {
                securityContextService.validateStoreAccess(entity.getStoreId());
            } else if (entity.getBusinessId() != null) {
                securityContextService.validateBusinessAccess(entity.getBusinessId());
            }
        });
        employeeRepository.deleteById(id);
    }
}
