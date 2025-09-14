package biz.craftline.server.feature.usermanagement.api.dto;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String fullName;
    private String email;
    private boolean enabled;
    private boolean isAccountNonExpired;
    private boolean isAccountNonLocked;
    private boolean isCredentialsNonExpired;
}
