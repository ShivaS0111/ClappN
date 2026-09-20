package biz.craftline.server.feature.usermanagement.api.controller;

import biz.craftline.server.config.security.RequirePermission;
import biz.craftline.server.feature.usermanagement.api.dto.UserDto;
import biz.craftline.server.feature.usermanagement.api.dto.UserCreateRequest;
import biz.craftline.server.feature.usermanagement.api.dto.UserUpdateRequest;
import biz.craftline.server.feature.usermanagement.api.mapper.UserMapper;
import biz.craftline.server.feature.usermanagement.domain.model.User;
import biz.craftline.server.feature.usermanagement.domain.service.UserService;
import biz.craftline.server.util.APIResponse;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    @RequirePermission("user.read")
    public ResponseEntity<APIResponse<List<UserDto>>> getAllUsers() {
        return  APIResponse.success(userService.getAllUsers().stream()
            .map(UserMapper::toDto)
            .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    @RequirePermission("user.read")
    public ResponseEntity<APIResponse<UserDto>> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
        return APIResponse.success( UserMapper.toDto(user));
    }

    @GetMapping("/email/{email}")
    @RequirePermission("user.read")
    public ResponseEntity<APIResponse<UserDto>> getUserByEmail(@PathVariable String email) {
        User user = userService.getUserByEmail(email).orElseThrow(() -> new EntityNotFoundException("User not found"));
        userService.assertCanViewUser(user);
        return APIResponse.success(UserMapper.toDto(user));
    }

    @PostMapping
    @RequirePermission("user.create")
    public ResponseEntity<APIResponse<UserDto>> createUser(@RequestBody UserCreateRequest request) {
        // Direct user creation is SYSTEM_ADMIN only; business users must use employee APIs
        if (!userService.isCurrentUserSystemAdmin()) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Only SYSTEM_ADMIN can create users via this endpoint. Use employee APIs instead.");
        }
        try {
            User existingUser = userService.getUserByEmail(request.getEmail()).orElse(null);
            if (existingUser != null) {
                throw new RuntimeException("User with this email already exists");
            }

            User user = UserMapper.toDomain(request);
            User created = userService.createUserWithHashedPassword(user);
            return APIResponse.success(UserMapper.toDto(created));
        } catch (RuntimeException ex) {
            throw ex;
        }
    }

    @PutMapping("/{id}")
    @RequirePermission("user.update")
    public ResponseEntity<APIResponse<UserDto>> updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        User user = userService.getUserById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
        UserMapper.updateDomain(user, request);
        User updated = userService.updateUser(id, user);
        return APIResponse.success(UserMapper.toDto(updated));
    }

    @DeleteMapping("/{id}")
    @RequirePermission("user.delete")
    public void deleteUser(@PathVariable Long id) {
        User user = userService.getUserById(id).orElseThrow(() -> new EntityNotFoundException("User not found"));
        userService.assertCanViewUser(user);
        userService.deleteUser(id);
    }

    @PostMapping("/{userId}/roles/{roleId}")
    @RequirePermission("user.permissions")
    public ResponseEntity<APIResponse<UserDto>> assignRole(@PathVariable Long userId, @PathVariable Long roleId) {
        User user = userService.getUserById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        userService.assertCanViewUser(user);
        User updated = userService.assignRole(userId, roleId);
        return APIResponse.success(UserMapper.toDto(updated));
    }
}
