package biz.craftline.server.feature.usermanagement.api.controller;

import biz.craftline.server.feature.usermanagement.api.dto.LoginRequest;
import biz.craftline.server.feature.usermanagement.api.dto.LoginResponse;
import biz.craftline.server.feature.usermanagement.api.dto.RegisterRequest;
import biz.craftline.server.feature.usermanagement.api.dto.RegisterResponse;
import biz.craftline.server.feature.usermanagement.domain.model.AuthUser;
import biz.craftline.server.feature.usermanagement.domain.model.TokenInfo;
import biz.craftline.server.config.security.JwtTokenProvider;
import biz.craftline.server.feature.usermanagement.domain.model.User;
import biz.craftline.server.feature.usermanagement.domain.service.UserService;
import biz.craftline.server.feature.usermanagement.domain.service.RBACService;
import biz.craftline.server.feature.usermanagement.api.dto.*;
import biz.craftline.server.feature.usermanagement.infra.repository.RefreshTokenRepository;
import biz.craftline.server.util.APIResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import javax.security.auth.login.AccountException;
import javax.security.auth.login.AccountExpiredException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

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

    // Persistent refresh token repository (preferred for multi-instance deployments)
    private final RefreshTokenRepository refreshTokenRepository;
    // In-memory storage for password reset tokens (use Redis in production)
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
            newUser.setVerified(1);
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
            // Get full AuthUser with scope data (storeIds, businessIds) from Employee table
            AuthUser user = userService.getAuthUserByEmail(loginRequest.getUsername());
            if(user==null){
                throw new AccountNotFoundException("Account not found");

            }else if(user.getVerified()==null || user.getVerified()==0){
                   throw new AccountLockedException("Account not verified");
            }
            if(authentication.isAuthenticated()){

                // Derive effective permissions from DB (roles + user-allowed - user-denied)
                // This ensures the JWT contains the user's effective permission snapshot at login.
                List<String> permissions = rbacService.getUserPermissions(loginRequest.getUsername());

                TokenInfo tokenInfo = tokenProvider.generateTokenWithClaims(
                        loginRequest.getUsername(),
                        permissions,
                        user.getRoles(),
                        user.getStoreIds(),
                        user.getBusinessIds()
                );

                // Persist refresh token in DB so refresh endpoint and revocation work across instances.
                if (tokenInfo.getRefreshToken() != null) {
                    refreshTokenRepository.save(new biz.craftline.server.feature.usermanagement.infra.entity.RefreshTokenEntity(
                            tokenInfo.getRefreshToken(),
                            loginRequest.getUsername(),
                            LocalDateTime.now().plusDays(30)
                    ));
                }

                log.info("User {} authenticated successfully with {} permissions, roles: {}, storeIds: {}, businessIds: {}",
                        loginRequest.getUsername(), permissions.size(), user.getRoles(), user.getStoreIds(), user.getBusinessIds());

                LoginResponse response = new LoginResponse(user, tokenInfo);
                return APIResponse.success(response);

            }else{
                throw  new AccountException();
            }

        } catch (AuthenticationException e) {
            // Log full exception to capture stack traces like StackOverflowError wrapped in other exceptions
            log.warn("Authentication failed for user: {}", loginRequest.getUsername(), e);
            return APIResponse.error("Invalid username or password", HttpStatus.BAD_REQUEST);
        } catch (Throwable t) {
            // Catch any throwable (including StackOverflowError) to log full stack trace for debugging.
            log.error("Unexpected error during authentication for user: {}", loginRequest.getUsername(), t);
            return APIResponse.error("Authentication failed due to server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<APIResponse<TokenInfo>> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshRequest) {
        try {
            String refreshToken = refreshRequest.getRefreshToken();
            // Try to find a non-revoked token (normal flow)
            var tokenEntityOpt = refreshTokenRepository.findByTokenAndRevokedFalse(refreshToken);

            if (tokenEntityOpt.isEmpty()) {
                // Token either doesn't exist or is revoked. Check if it exists at all.
                var maybe = refreshTokenRepository.findByToken(refreshToken);
                if (maybe.isPresent()) {
                    // Token exists but is revoked -> reuse detected. Revoke all tokens for this user as a precaution.
                    String uname = maybe.get().getUsername();
                    var all = refreshTokenRepository.findAllByUsername(uname);
                    all.forEach(t -> t.setRevoked(true));
                    refreshTokenRepository.saveAll(all);
                    log.warn("Refresh token reuse detected for user {}. Revoked all tokens.", uname);
                    return APIResponse.unauthorised("Invalid or expired refresh token");
                }
                return APIResponse.unauthorised("Invalid or expired refresh token");
            }

            var tokenEntity = tokenEntityOpt.get();
            if (tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
                // expired -> mark as revoked
                tokenEntity.setRevoked(true);
                refreshTokenRepository.save(tokenEntity);
                return APIResponse.unauthorised("Invalid or expired refresh token");
            }

            // Generate new access token using current DB permissions
            List<String> permissions = rbacService.getUserPermissions(tokenEntity.getUsername());
            TokenInfo response = tokenProvider.generateTokenWithPermissions(tokenEntity.getUsername(), permissions);

            // Rotate refresh token: mark old token revoked and save replacedBy, persist new token
            tokenEntity.setRevoked(true);
            if (response.getRefreshToken() != null) {
                tokenEntity.setReplacedBy(response.getRefreshToken());
            }
            refreshTokenRepository.save(tokenEntity);

            if (response.getRefreshToken() != null) {
                refreshTokenRepository.save(new biz.craftline.server.feature.usermanagement.infra.entity.RefreshTokenEntity(
                        response.getRefreshToken(),
                        tokenEntity.getUsername(),
                        LocalDateTime.now().plusDays(30)
                ));
            }

            log.info("Token refreshed for user: {} (rotated refresh token)", tokenEntity.getUsername());
            return APIResponse.success(response);

        } catch (Exception e) {
            log.error("Token refresh failed", e);
            return APIResponse.error("Token refresh failed", HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<APIResponse<String>> logout(@Valid @RequestBody LogoutRequest logoutRequest) {
        try {
            // Mark given refresh token as revoked for audit
            var tokenOpt = refreshTokenRepository.findByToken(logoutRequest.getRefreshToken());
            tokenOpt.ifPresent(t -> {
                t.setRevoked(true);
                refreshTokenRepository.save(t);
            });

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

    /**
     * Revoke all refresh tokens for a given username. Admin-only operation.
     */
    @PostMapping("/revoke-all-refresh-tokens/{username}")
    public ResponseEntity<APIResponse<String>> revokeAllRefreshTokens(@PathVariable String username) {
        try {
            // Only allow SYSTEM_ADMIN to call this endpoint
            if (!rbacService.currentUserHasRole("SYSTEM_ADMIN")) {
                return APIResponse.unauthorised("Insufficient privileges");
            }

            // Mark all tokens revoked for audit and reuse-detection purposes
            var all = refreshTokenRepository.findAllByUsername(username);
            all.forEach(t -> t.setRevoked(true));
            refreshTokenRepository.saveAll(all);

            log.info("All refresh tokens revoked for user: {} by admin", username);
            return APIResponse.success("Revoked all refresh tokens for user: " + username);
        } catch (Exception e) {
            log.error("Failed to revoke refresh tokens for user: {}", username, e);
            return APIResponse.badRequest("Failed to revoke refresh tokens");
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
