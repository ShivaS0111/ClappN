package biz.craftline.server.feature.employeemanagement.domain.service;

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

    public List<Employee> getAllEmployees() {
        return employeeRepository.findAll().stream()
            .map(EmployeeMapper::toDomain)
            .collect(Collectors.toList());
    }

    public Optional<Employee> getEmployeeById(Long id) {
        return employeeRepository.findById(id).map(EmployeeMapper::toDomain);
    }

    public List<Employee> getEmployeesByStoreId(Long storeId) {
        return employeeRepository.findByStoreId(storeId).stream()
            .map(EmployeeMapper::toDomain)
            .collect(Collectors.toList());
    }

    public List<Employee> getEmployeesByBusinessId(Long businessId) {
        return employeeRepository.findByBusinessId(businessId).stream()
            .map(EmployeeMapper::toDomain)
            .collect(Collectors.toList());
    }

    public Employee createEmployee(Employee employee) {
        EmployeeEntity entity = EmployeeMapper.toEntity(employee);
        EmployeeEntity saved = employeeRepository.save(entity);
        return EmployeeMapper.toDomain(saved);
    }

    public void deleteEmployee(Long id) {
        employeeRepository.deleteById(id);
    }
}
