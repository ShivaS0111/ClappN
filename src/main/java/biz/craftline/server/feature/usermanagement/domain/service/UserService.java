package biz.craftline.server.feature.usermanagement.domain.service;

import biz.craftline.server.feature.usermanagement.domain.model.AuthUser;
import biz.craftline.server.feature.usermanagement.domain.model.User;
import biz.craftline.server.feature.usermanagement.api.mapper.UserMapper;
import biz.craftline.server.feature.usermanagement.infra.entity.UserEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.RoleEntity;
import biz.craftline.server.feature.usermanagement.infra.repository.UserRepository;
import biz.craftline.server.feature.usermanagement.infra.repository.RoleRepository;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {
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

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = getUserByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        return new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of();
            }

            @Override
            public String getPassword() {
                return user.getPassword();
            }

            @Override
            public String getUsername() {
                return user.getEmail();
            }

            @Override
            public boolean isAccountNonExpired() {
                return false;
            }

            @Override
            public boolean isAccountNonLocked() {
                return false;
            }

            @Override
            public boolean isCredentialsNonExpired() {
                return false;
            }

            @Override
            public boolean isEnabled() {
                return user.isEnabled();
            }
        };
    }

    private User createUser(User user) {
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

    public AuthUser getAuthUserByEmail(
            @NotBlank(message = "Username is required")
            @Email(message = "Username must be a valid email")
            String username) {
        return userRepository.findByEmail(username).map(UserMapper::toAuthUser)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

    }
}
