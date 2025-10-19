package biz.craftline.server.feature.usermanagement.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class LoginResponse {
    private final String accessToken;
    private final String tokenType;
    private final List<String> permissions;
    private final String refreshToken;

    public LoginResponse(String accessToken, String tokenType, List<String> permissions, String refreshToken) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.permissions = permissions;
        this.refreshToken = refreshToken;
    }

}