package biz.craftline.server.feature.usermanagement.domain.service;

import biz.craftline.server.feature.usermanagement.domain.model.User;
import biz.craftline.server.feature.usermanagement.api.mapper.UserMapper;
import biz.craftline.server.feature.usermanagement.infra.entity.UserEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.RoleEntity;
import biz.craftline.server.feature.usermanagement.infra.repository.UserRepository;
import biz.craftline.server.feature.usermanagement.infra.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll().stream()
            .map(UserMapper::toDomain)
            .collect(Collectors.toList());
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id).map(UserMapper::toDomain);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email).map(UserMapper::toDomain);
    }

    public User createUser(User user) {
        UserEntity entity = UserMapper.toEntity(user);
        UserEntity saved = userRepository.save(entity);
        return UserMapper.toDomain(saved);
    }

    public User updateUser(Long id, User userDetails) {
        return userRepository.findById(id).map(userEntity -> {
            userEntity.setFullName(userDetails.getFullName());
            userEntity.setEmail(userDetails.getEmail());
            userEntity.setPassword(userDetails.getPassword());
            userEntity.setEnabled(userDetails.isEnabled());
            userEntity.setAccountNonLocked(userDetails.isAccountNonLocked());
            userEntity.setAccountNonExpired(userDetails.isAccountNonExpired());
            userEntity.setCredentialsNonExpired(userDetails.isCredentialsNonExpired());
           // userEntity.setAddress(userDetails.getAddress());
            UserEntity updated = userRepository.save(userEntity);
            return UserMapper.toDomain(updated);
        }).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User assignRole(Long userId, Long roleId) {
        UserEntity userEntity = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        RoleEntity role = roleRepository.findById(roleId).orElseThrow(() -> new RuntimeException("Role not found"));
        userEntity.getRoles().add(role);
        UserEntity updated = userRepository.save(userEntity);
        return UserMapper.toDomain(updated);
    }
}
