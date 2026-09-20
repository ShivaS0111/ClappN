package biz.craftline.server.feature.employeemanagement.api.controller;

import biz.craftline.server.config.mail.MailService;
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

    @Autowired
    private MailService mailService;

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
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        if (request.getBusinessId() == null && request.getStoreId() == null) {
            throw new IllegalArgumentException("businessId or storeId is required");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            String first = request.getFirstName() != null ? request.getFirstName() : "";
            String last = request.getLastName() != null ? request.getLastName() : "";
            String sur = request.getSurName() != null ? request.getSurName() : "";
            request.setName((first + " " + last + " " + sur).trim());
        }
        if (request.getUserId() == null || request.getUserId() <= 0) {
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                throw new IllegalArgumentException("userId or email is required");
            }
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
            try {
                mailService.sendText(
                        request.getEmail(),
                        "Your Clapp account",
                        "Hello " + request.getName() + ",\n\n"
                                + "An account was created for you.\n"
                                + "Email: " + request.getEmail() + "\n"
                                + "Temporary password: " + tempPassword + "\n\n"
                                + "Please sign in and change your password."
                );
            } catch (Exception mailEx) {
                log.warn("Failed to send employee invite email to {}: {}", request.getEmail(), mailEx.getMessage());
            }
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

    @PutMapping("/{id}")
    @RequirePermission("user.update")
    public ResponseEntity<APIResponse<EmployeeResponse>> updateEmployee(
            @PathVariable Long id,
            @RequestBody EmployeeRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            String first = request.getFirstName() != null ? request.getFirstName() : "";
            String last = request.getLastName() != null ? request.getLastName() : "";
            String sur = request.getSurName() != null ? request.getSurName() : "";
            request.setName((first + " " + last + " " + sur).trim());
        }
        Employee updated = employeeService.updateEmployee(id, EmployeeMapper.toDomain(request));
        return APIResponse.ok(EmployeeMapper.toResponse(updated));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("user.delete")
    public void deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
    }
}
