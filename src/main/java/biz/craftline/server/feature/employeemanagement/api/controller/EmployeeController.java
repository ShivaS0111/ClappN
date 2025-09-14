package biz.craftline.server.feature.employeemanagement.api.controller;

import biz.craftline.server.feature.employeemanagement.api.dto.EmployeeRequest;
import biz.craftline.server.feature.employeemanagement.api.dto.EmployeeResponse;
import biz.craftline.server.feature.employeemanagement.api.mapper.EmployeeMapper;
import biz.craftline.server.feature.employeemanagement.domain.model.Employee;
import biz.craftline.server.feature.employeemanagement.domain.service.EmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @GetMapping
    public List<EmployeeResponse> getAllEmployees() {
        return employeeService.getAllEmployees().stream().map(EmployeeMapper::toResponse).collect(Collectors.toList());
    }

    @PostMapping
    public EmployeeResponse createEmployee(@RequestBody EmployeeRequest request) {
        Employee employee = EmployeeMapper.toDomain(request);
        Employee created = employeeService.createEmployee(employee);
        return EmployeeMapper.toResponse(created);
    }

    @DeleteMapping("/{id}")
    public void deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
    }
}
