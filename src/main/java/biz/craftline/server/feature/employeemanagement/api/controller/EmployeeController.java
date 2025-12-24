package biz.craftline.server.feature.employeemanagement.api.controller;

import biz.craftline.server.feature.employeemanagement.api.dto.EmployeeRequest;
import biz.craftline.server.feature.employeemanagement.api.dto.EmployeeResponse;
import biz.craftline.server.feature.employeemanagement.api.mapper.EmployeeMapper;
import biz.craftline.server.feature.employeemanagement.domain.model.Employee;
import biz.craftline.server.feature.employeemanagement.domain.service.EmployeeService;
import biz.craftline.server.feature.usermanagement.domain.model.User;
import biz.craftline.server.feature.usermanagement.domain.service.UserService;
import biz.craftline.server.util.APIResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<APIResponse<List<EmployeeResponse>>> getAllEmployees() {
        List<EmployeeResponse> list = employeeService.getAllEmployees()
                .stream()
                .map(EmployeeMapper::toResponse).toList();
        return APIResponse.ok(list);
    }

    @GetMapping("/business/{businessId}")
    public ResponseEntity<APIResponse<List<EmployeeResponse>>> getAllEmployeesByBusiness(@PathVariable Long businessId) {
        List<EmployeeResponse> list = employeeService.getEmployeesByBusinessId(businessId)
                .stream()
                .map(EmployeeMapper::toResponse).collect(Collectors.toList());
        return APIResponse.ok(list);
    }

    @GetMapping("/store/{storeId}")
    public ResponseEntity<APIResponse<List<EmployeeResponse>>> getAllEmployeesByStore(@PathVariable Long storeId) {
        List<EmployeeResponse> list = employeeService.getEmployeesByStoreId(storeId)
                .stream()
                .map(EmployeeMapper::toResponse).collect(Collectors.toList());
        return APIResponse.ok(list);
    }

    @PostMapping
    public ResponseEntity<APIResponse<EmployeeResponse>> createEmployee(@RequestBody EmployeeRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            request.setName(request.getFirstName() + " " + request.getLastName() + " " + request.getSurName());
        }
        if (request.getUserId() == null || request.getUserId() <= 0) {
            User user = createUserIfNotExists(request);
            request.setUserId(user.getId());
        }
        Employee employee = EmployeeMapper.toDomain(request);
        Employee created = employeeService.createEmployee(employee);
        return APIResponse.ok(EmployeeMapper.toResponse(created));
    }

    private User createUserIfNotExists(EmployeeRequest request) {
        return userService.findUserIdByIdOrEmail(request.getUserId(), request.getEmail()).orElseGet(() -> {
            User newUser = new User();
            newUser.setFullName(request.getName());
            newUser.setEmail(request.getEmail());
            newUser.setPassword("clapp@123"); // In real scenarios, generate a secure password and send it to the user
            newUser.setEnabled(true);
            newUser.setAccountNonLocked(true);
            newUser.setAccountNonExpired(true);
            newUser.setCredentialsNonExpired(true);
            return userService.createUserWithHashedPassword(newUser);
        });
    }

    @DeleteMapping("/{id}")
    public void deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
    }
}
