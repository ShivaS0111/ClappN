package biz.craftline.server.feature.usermanagement.api.controller;

import biz.craftline.server.feature.usermanagement.api.dto.UserDto;
import biz.craftline.server.feature.usermanagement.api.dto.UserCreateRequest;
import biz.craftline.server.feature.usermanagement.api.dto.UserUpdateRequest;
import biz.craftline.server.feature.usermanagement.api.mapper.UserMapper;
import biz.craftline.server.feature.usermanagement.domain.model.User;
import biz.craftline.server.feature.usermanagement.domain.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers().stream()
            .map(UserMapper::toDto)
            .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id).orElseThrow(() -> new RuntimeException("User not found"));
        return UserMapper.toDto(user);
    }

    @GetMapping("/email/{email}")
    public UserDto getUserByEmail(@PathVariable String email) {
        User user = userService.getUserByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        return UserMapper.toDto(user);
    }

    @PostMapping
    public UserDto createUser(@RequestBody UserCreateRequest request) {
        User user = UserMapper.toDomain(request);
        User created = userService.createUser(user);
        return UserMapper.toDto(created);
    }

    @PutMapping("/{id}")
    public UserDto updateUser(@PathVariable Long id, @RequestBody UserUpdateRequest request) {
        User user = userService.getUserById(id).orElseThrow(() -> new RuntimeException("User not found"));
        UserMapper.updateDomain(user, request);
        User updated = userService.updateUser(id, user);
        return UserMapper.toDto(updated);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @PostMapping("/{userId}/roles/{roleId}")
    public UserDto assignRole(@PathVariable Long userId, @PathVariable Long roleId) {
        User updated = userService.assignRole(userId, roleId);
        return UserMapper.toDto(updated);
    }
}
