package biz.craftline.server.feature.usermanagement.api.controller;

import biz.craftline.server.config.security.JwtTokenProvider;
import biz.craftline.server.feature.usermanagement.domain.service.UserService;
import biz.craftline.server.feature.usermanagement.domain.service.RBACService;
import biz.craftline.server.feature.usermanagement.domain.model.User;
import biz.craftline.server.feature.usermanagement.api.dto.*;
import biz.craftline.server.util.APIResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final RBACService rbacService;
    private final PasswordEncoder passwordEncoder;

    // In-memory storage for refresh tokens and reset tokens (use Redis in production)
    private final ConcurrentHashMap<String, RefreshTokenInfo> refreshTokenStore = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PasswordResetInfo> passwordResetStore = new ConcurrentHashMap<>();

    @PostMapping("/register")
    public ResponseEntity<APIResponse<RegisterResponse>> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // Check if user already exists
            Optional<User> existingUser = userService.getUserByEmail(registerRequest.getEmail());
            if (existingUser.isPresent()) {
                return APIResponse.error("User with this email already exists", HttpStatus.BAD_REQUEST);
            }

            // Create new user
            User newUser = new User();
            newUser.setFullName(registerRequest.getFullName());
            newUser.setEmail(registerRequest.getEmail());
            newUser.setPassword(registerRequest.getPassword());
            newUser.setEnabled(true);
            newUser.setAccountNonLocked(true);
            newUser.setAccountNonExpired(true);
            newUser.setCredentialsNonExpired(true);

            User savedUser = userService.createUserWithHashedPassword(newUser);

            log.info("New user registered: {}", savedUser.getEmail());

            RegisterResponse response = new RegisterResponse(
                    savedUser.getId(),
                    savedUser.getEmail(),
                    savedUser.getFullName(),
                    "User registered successfully"
            );

            return APIResponse.success(response);

        } catch (Exception e) {
            log.error("Registration failed for email: {}", registerRequest.getEmail(), e);
            return APIResponse.error("Registration failed: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<APIResponse<LoginResponse>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // Get user permissions
            List<String> permissions = rbacService.getUserPermissions(loginRequest.getUsername());

            String jwt = tokenProvider.generateTokenWithPermissions(loginRequest.getUsername(), permissions);
            String refreshToken = generateRefreshToken(loginRequest.getUsername());

            LoginResponse response = new LoginResponse(jwt, "Bearer", permissions, refreshToken);

            log.info("User {} authenticated successfully with {} permissions",
                    loginRequest.getUsername(), permissions.size());

            return APIResponse.success(response);

        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user: {}", loginRequest.getUsername());
            return APIResponse.error("Invalid username or password", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<APIResponse<RefreshResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshRequest) {
        try {
            String refreshToken = refreshRequest.getRefreshToken();
            RefreshTokenInfo tokenInfo = refreshTokenStore.get(refreshToken);

            if (tokenInfo == null || tokenInfo.isExpired()) {
                refreshTokenStore.remove(refreshToken);
                return APIResponse.unauthorised("Invalid or expired refresh token");
            }

            // Generate new access token
            List<String> permissions = rbacService.getUserPermissions(tokenInfo.username());
            String newAccessToken = tokenProvider.generateTokenWithPermissions(tokenInfo.username(), permissions);

            RefreshResponse response = new RefreshResponse(newAccessToken, "Bearer");

            log.info("Token refreshed for user: {}", tokenInfo.username());
            return APIResponse.success(response);

        } catch (Exception e) {
            log.error("Token refresh failed", e);
            return APIResponse.error("Token refresh failed", HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<APIResponse<String>> logout(@Valid @RequestBody LogoutRequest logoutRequest) {
        try {
            // Remove refresh token from store
            refreshTokenStore.remove(logoutRequest.getRefreshToken());

            log.info("User logged out successfully");
            return APIResponse.success("Logged out successfully");

        } catch (Exception e) {
            log.error("Logout failed", e);
            return APIResponse.error("Logout failed", HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<APIResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotRequest) {
        try {
            Optional<User> userOpt = userService.getUserByEmail(forgotRequest.getEmail());

            if (userOpt.isEmpty()) {
                // Don't reveal if email exists for security
                return APIResponse.success("If email exists, password reset instructions have been sent");
            }

            String resetToken = UUID.randomUUID().toString();
            passwordResetStore.put(resetToken, new PasswordResetInfo(
                    forgotRequest.getEmail(),
                    LocalDateTime.now().plusHours(1) // 1 hour expiry
            ));

            // TODO: Send email with reset token
            log.info("Password reset requested for email: {} with token: {}", forgotRequest.getEmail(), resetToken);

            return APIResponse.success("If email exists, password reset instructions have been sent");

        } catch (Exception e) {
            log.error("Forgot password failed", e);
            return APIResponse.badRequest("Password reset request failed");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<APIResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest resetRequest) {
        try {
            PasswordResetInfo resetInfo = passwordResetStore.get(resetRequest.getResetToken());

            if (resetInfo == null || resetInfo.isExpired()) {
                passwordResetStore.remove(resetRequest.getResetToken());
                return APIResponse.unauthorised("Invalid or expired reset token");
            }

            // Update user password
            Optional<User> userOpt = userService.getUserByEmail(resetInfo.email());
            if (userOpt.isEmpty()) {
                return APIResponse.badRequest("User not found");
            }

            User user = userOpt.get();
            user.setPassword(resetRequest.getNewPassword());
            userService.createUserWithHashedPassword(user); // This will hash the password

            // Remove used reset token
            passwordResetStore.remove(resetRequest.getResetToken());

            log.info("Password reset successfully for user: {}", resetInfo.email());
            return APIResponse.success("Password reset successfully");

        } catch (Exception e) {
            log.error("Password reset failed", e);
            return APIResponse.badRequest("Password reset failed");
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<APIResponse<TokenValidationResponse>> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.substring(7); // Remove "Bearer " prefix

            if (tokenProvider.validateToken(token)) {
                String username = tokenProvider.getUsernameFromToken(token);
                List<String> permissions = tokenProvider.getPermissionsFromToken(token);

                TokenValidationResponse response = new TokenValidationResponse(true, username, permissions);
                return APIResponse.success(response);
            } else {
                return APIResponse.unauthorised("Invalid token");
            }
        } catch (Exception e) {
            return APIResponse.unauthorised("Token validation failed");
        }
    }

    // Helper methods
    private String generateRefreshToken(String username) {
        String refreshToken = UUID.randomUUID().toString();
        refreshTokenStore.put(refreshToken, new RefreshTokenInfo(
                username,
                LocalDateTime.now().plusDays(30) // 30 days expiry
        ));
        return refreshToken;
    }

    // Helper classes for token management
    private record RefreshTokenInfo(String username, LocalDateTime expiryDate) {
        public boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryDate);
        }
    }

    private record PasswordResetInfo(String email, LocalDateTime expiryDate) {

        public boolean isExpired() {
                return LocalDateTime.now().isAfter(expiryDate);
            }
        }
}
