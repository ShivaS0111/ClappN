package biz.craftline.server.feature.employeemanagement.api.controller;

import biz.craftline.server.config.security.RequirePermission;
import biz.craftline.server.feature.employeemanagement.api.dto.EmployeeRequest;
import biz.craftline.server.feature.employeemanagement.api.dto.EmployeeResponse;
import biz.craftline.server.feature.employeemanagement.api.mapper.EmployeeMapper;
import biz.craftline.server.feature.employeemanagement.domain.model.Employee;
import biz.craftline.server.feature.employeemanagement.domain.service.EmployeeService;
import biz.craftline.server.feature.usermanagement.domain.model.User;
import biz.craftline.server.feature.usermanagement.domain.service.UserService;
import biz.craftline.server.util.APIResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private UserService userService;

    @GetMapping
    @RequirePermission("user.read")
    public ResponseEntity<APIResponse<List<EmployeeResponse>>> getAllEmployees() {
        List<EmployeeResponse> list = employeeService.getAllEmployees()
                .stream()
                .map(EmployeeMapper::toResponse).toList();
        return APIResponse.ok(list);
    }

    @GetMapping("/business/{businessId}")
    @RequirePermission("user.read")
    public ResponseEntity<APIResponse<List<EmployeeResponse>>> getAllEmployeesByBusiness(@PathVariable Long businessId) {
        List<EmployeeResponse> list = employeeService.getEmployeesByBusinessId(businessId)
                .stream()
                .map(EmployeeMapper::toResponse).collect(Collectors.toList());
        return APIResponse.ok(list);
    }

    @GetMapping("/store/{storeId}")
    @RequirePermission("user.read")
    public ResponseEntity<APIResponse<List<EmployeeResponse>>> getAllEmployeesByStore(@PathVariable Long storeId) {
        List<EmployeeResponse> list = employeeService.getEmployeesByStoreId(storeId)
                .stream()
                .map(EmployeeMapper::toResponse).collect(Collectors.toList());
        return APIResponse.ok(list);
    }

    @PostMapping
    @RequirePermission("user.create")
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

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*";

    private User createUserIfNotExists(EmployeeRequest request) {
        return userService.findUserIdByIdOrEmail(request.getUserId(), request.getEmail()).orElseGet(() -> {
            String tempPassword = generateSecurePassword(12);
            User newUser = new User();
            newUser.setFullName(request.getName());
            newUser.setEmail(request.getEmail());
            newUser.setPassword(tempPassword);
            newUser.setEnabled(true);
            newUser.setVerified(1);
            newUser.setAccountNonLocked(true);
            newUser.setAccountNonExpired(true);
            newUser.setCredentialsNonExpired(true);
            User created = userService.createUserWithHashedPassword(newUser);
            // TODO: Send temporary password to employee via email service
            log.info("Created user account for employee: {}. Temporary password must be sent via email.", request.getEmail());
            return created;
        });
    }

    private String generateSecurePassword(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(PASSWORD_CHARS.charAt(SECURE_RANDOM.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }

    @DeleteMapping("/{id}")
    @RequirePermission("user.delete")
    public void deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
    }
}
