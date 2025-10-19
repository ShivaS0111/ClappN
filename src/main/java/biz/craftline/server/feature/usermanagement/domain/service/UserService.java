package biz.craftline.server.feature.usermanagement.domain.service;

import biz.craftline.server.feature.usermanagement.domain.model.User;
import biz.craftline.server.feature.usermanagement.api.mapper.UserMapper;
import biz.craftline.server.feature.usermanagement.infra.entity.UserEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.RoleEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.PermissionEntity;
import biz.craftline.server.feature.usermanagement.infra.repository.UserRepository;
import biz.craftline.server.feature.usermanagement.infra.repository.RoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    // RBACService is not injected here to avoid potential circular dependency during authentication
    // (permission resolution should happen outside of UserDetailsService/loadUserByUsername)

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

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("loadUserByUsername called for {}", username);
        try {
            // Load user entity with simple query - NO roles or permissions
            UserEntity userEntity = userRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

            log.debug("Loaded userEntity id={} email={}", userEntity.getId(), userEntity.getEmail());

            // Create minimal authorities - just a basic user role to avoid any lazy loading
            List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));

            log.debug("Built {} authorities for user {}", authorities.size(), username);

            // Return Spring Security's User with minimal data
            return org.springframework.security.core.userdetails.User.withUsername(userEntity.getEmail())
                    .password(userEntity.getPassword())
                    .authorities(authorities)
                    .accountExpired(false)
                    .build();

        } catch (Throwable t) {
            // Catching Throwable to log Errors (like StackOverflowError) for debugging
            log.error("Error in loadUserByUsername for {}: ", username, t);
            throw new RuntimeException("Failed to load user: " + username, t);
        }
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

    public User createUserWithHashedPassword(User newUser) {
        // Here you would hash the password before saving
        // For example, using BCryptPasswordEncoder
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String hashedPassword = encoder.encode(newUser.getPassword());
        newUser.setPassword(hashedPassword);
        return createUser(newUser);
    }
}
