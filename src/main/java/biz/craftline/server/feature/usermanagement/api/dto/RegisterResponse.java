package biz.craftline.server.feature.usermanagement.api.dto;

public record RegisterResponse(Long userId, String email, String fullName, String message) {

}