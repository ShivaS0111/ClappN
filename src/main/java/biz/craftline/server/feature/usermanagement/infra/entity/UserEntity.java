package biz.craftline.server.feature.usermanagement.infra.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.sql.Date;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Entity representing a user in the system.
 */
@Data
@Entity(name = "user")
public class UserEntity implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    /** Full name of the user */
    @NotNull(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    @Column(nullable = false, name = "full_name")
    private String fullName;

    /** Email address of the user */
    @NotNull(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(unique = true, length = 100, nullable = false)
    private String email;

    /** Hashed password of the user */
    @NotNull(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Column(nullable = false)
    private String password;

    @CreationTimestamp
    @Column(updatable = false, name = "created_at")
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    @JsonManagedReference
    private Set<RoleEntity> roles = Set.of();

    // User-specific permissions that override role permissions
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserAllowedPermissionEntity> allowedPermissions = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<UserDeniedPermissionEntity> deniedPermissions = new HashSet<>();

    // Account status fields
    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private boolean accountNonLocked = true;

    @Column(nullable = false)
    private boolean accountNonExpired = true;

    @Column(nullable = false)
    private boolean credentialsNonExpired = true;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Implement hierarchical permission system as Spring Security authorities
        // Priority: 1. User Denied > 2. User Allowed > 3. Role-based permissions

        Set<String> effectivePermissions = new HashSet<>();

        // Start with role-based permissions (Priority 3 - lowest)
        roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .forEach(permission -> effectivePermissions.add(permission.getName()));

        // Add user-specific allowed permissions (Priority 2)
        allowedPermissions
                .forEach(userPermission -> effectivePermissions.add(userPermission.getPermission().getName()));

        // Remove user-specific denied permissions (Priority 1 - highest)
        deniedPermissions
                .forEach(userPermission -> effectivePermissions.remove(userPermission.getPermission().getName()));

        // Convert permissions to Spring Security authorities
        return effectivePermissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    public UserEntity addRole(RoleEntity role) {
        roles.add(role);
        return this;
    }

    public void deleteRole(RoleEntity role) {
        roles.remove(role);
    }
}