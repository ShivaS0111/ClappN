package biz.craftline.server.feature.usermanagement.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class TokenValidationResponse {
    private final boolean valid;
    private final String username;
    private final List<String> permissions;

    public TokenValidationResponse(boolean valid, String username, List<String> permissions) {
        this.valid = valid;
        this.username = username;
        this.permissions = permissions;
    }

}