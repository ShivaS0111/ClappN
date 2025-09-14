package biz.craftline.server.feature.usermanagement.api.dto;

import lombok.Data;

@Data
public class UserCreateRequest {
    private String fullName;
    private String email;
    private String password;
    // Add other fields as needed (address, etc.)
    // Getters and setters
}

