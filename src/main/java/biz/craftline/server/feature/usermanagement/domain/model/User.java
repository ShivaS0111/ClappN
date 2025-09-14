package biz.craftline.server.feature.usermanagement.domain.model;

import lombok.Data;

@Data
public class User {
    private Long id;
    private String fullName;
    private String email;
    private String password;
    private boolean enabled;
    private boolean isAccountNonExpired;
    private boolean isAccountNonLocked;
    private boolean isCredentialsNonExpired;
    // Add other fields as needed (address, roles, etc.)
    // Getters and setters
}

