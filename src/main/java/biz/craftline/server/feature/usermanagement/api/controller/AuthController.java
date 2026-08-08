package biz.craftline.server.feature.usermanagement.api.controller;

import biz.craftline.server.config.mail.MailService;
import biz.craftline.server.config.security.JwtTokenProvider;
import biz.craftline.server.config.security.RequirePermission;
import biz.craftline.server.config.security.UserScopeContext;
import biz.craftline.server.config.security.UserScopeContextHolder;
import biz.craftline.server.feature.usermanagement.api.dto.*;
import biz.craftline.server.feature.usermanagement.domain.model.AuthUser;
import biz.craftline.server.feature.usermanagement.domain.model.TokenInfo;
import biz.craftline.server.feature.usermanagement.domain.model.User;
import biz.craftline.server.feature.usermanagement.domain.service.RBACService;
import biz.craftline.server.feature.usermanagement.domain.service.UserService;
import biz.craftline.server.feature.usermanagement.infra.entity.PasswordResetTokenEntity;
import biz.craftline.server.feature.usermanagement.infra.repository.PasswordResetTokenRepository;
import biz.craftline.server.feature.usermanagement.infra.repository.RefreshTokenRepository;
import biz.craftline.server.util.APIResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.AccountException;
import javax.security.auth.login.AccountLockedException;
import javax.security.auth.login.AccountNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;
    private final RBACService rbacService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final MailService mailService;

    @Value("${app.frontend.reset-password-url:http://localhost:5173/reset-password}")
    private String resetPasswordUrl;

    @PostMapping("/register")
    public ResponseEntity<APIResponse<RegisterResponse>> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            Optional<User> existingUser = userService.getUserByEmail(registerRequest.getEmail());
            if (existingUser.isPresent()) {
                return APIResponse.error("User with this email already exists", HttpStatus.BAD_REQUEST);
            }

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

            AuthUser user = userService.getAuthUserByEmail(loginRequest.getUsername());
            if (user == null) {
                throw new AccountNotFoundException("Account not found");
            } else if (user.getVerified() == null || user.getVerified() == 0) {
                throw new AccountLockedException("Account not verified");
            }

            if (!authentication.isAuthenticated()) {
                throw new AccountException();
            }

            // Snapshot for FE convenience — authorization is still resolved from DB per request
            List<String> permissions = rbacService.getUserPermissions(loginRequest.getUsername());
            user.setPermissions(permissions);

            TokenInfo tokenInfo = tokenProvider.generateTokenWithClaims(
                    loginRequest.getUsername(),
                    permissions,
                    user.getRoles(),
                    user.getStoreIds(),
                    user.getBusinessIds()
            );

            if (tokenInfo.getRefreshToken() != null) {
                refreshTokenRepository.save(new biz.craftline.server.feature.usermanagement.infra.entity.RefreshTokenEntity(
                        tokenInfo.getRefreshToken(),
                        loginRequest.getUsername(),
                        LocalDateTime.now().plusDays(30)
                ));
            }

            log.info("User {} authenticated successfully with {} permissions, roles: {}, storeIds: {}, businessIds: {}",
                    loginRequest.getUsername(), permissions.size(), user.getRoles(), user.getStoreIds(), user.getBusinessIds());

            return APIResponse.success(new LoginResponse(user, tokenInfo));

        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user: {}", loginRequest.getUsername(), e);
            return APIResponse.error("Invalid username or password", HttpStatus.UNAUTHORIZED);
        } catch (AccountException e) {
            log.warn("Account issue for user {}: {}", loginRequest.getUsername(), e.getMessage());
            return APIResponse.error(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Throwable t) {
            log.error("Unexpected error during authentication for user: {}", loginRequest.getUsername(), t);
            return APIResponse.error("Authentication failed due to server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<APIResponse<TokenInfo>> refreshToken(@Valid @RequestBody RefreshTokenRequest refreshRequest) {
        try {
            String refreshToken = refreshRequest.getRefreshToken();
            var tokenEntityOpt = refreshTokenRepository.findByTokenAndRevokedFalse(refreshToken);

            if (tokenEntityOpt.isEmpty()) {
                var maybe = refreshTokenRepository.findByToken(refreshToken);
                if (maybe.isPresent()) {
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
                tokenEntity.setRevoked(true);
                refreshTokenRepository.save(tokenEntity);
                return APIResponse.unauthorised("Invalid or expired refresh token");
            }

            AuthUser user = userService.getAuthUserByEmail(tokenEntity.getUsername());
            List<String> permissions = rbacService.getUserPermissions(tokenEntity.getUsername());

            TokenInfo response = tokenProvider.generateTokenWithClaims(
                    tokenEntity.getUsername(),
                    permissions,
                    user.getRoles(),
                    user.getStoreIds(),
                    user.getBusinessIds()
            );

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
    @Transactional
    public ResponseEntity<APIResponse<String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotRequest) {
        try {
            Optional<User> userOpt = userService.getUserByEmail(forgotRequest.getEmail());

            // Always same response — do not reveal whether email exists
            if (userOpt.isPresent()) {
                passwordResetTokenRepository.invalidateAllForEmail(forgotRequest.getEmail());
                String resetToken = UUID.randomUUID().toString();
                passwordResetTokenRepository.save(new PasswordResetTokenEntity(
                        resetToken,
                        forgotRequest.getEmail(),
                        LocalDateTime.now().plusHours(1)
                ));
                try {
                    String link = resetPasswordUrl + (resetPasswordUrl.contains("?") ? "&" : "?")
                            + "token=" + resetToken;
                    mailService.sendText(
                            forgotRequest.getEmail(),
                            "Password reset",
                            "Use this link to reset your password (valid 1 hour):\n" + link
                                    + "\n\nOr use token: " + resetToken
                    );
                } catch (Exception mailEx) {
                    log.warn("Failed to send password reset email to {}: {}",
                            forgotRequest.getEmail(), mailEx.getMessage());
                }
            }

            return APIResponse.success("If email exists, password reset instructions have been sent");
        } catch (Exception e) {
            log.error("Forgot password failed", e);
            return APIResponse.badRequest("Password reset request failed");
        }
    }

    @PostMapping("/reset-password")
    @Transactional
    public ResponseEntity<APIResponse<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest resetRequest) {
        try {
            var tokenOpt = passwordResetTokenRepository.findByTokenAndUsedFalse(resetRequest.getResetToken());
            if (tokenOpt.isEmpty()) {
                return APIResponse.unauthorised("Invalid or expired reset token");
            }

            PasswordResetTokenEntity resetInfo = tokenOpt.get();
            if (resetInfo.isExpired()) {
                resetInfo.setUsed(true);
                passwordResetTokenRepository.save(resetInfo);
                return APIResponse.unauthorised("Invalid or expired reset token");
            }

            userService.updatePassword(resetInfo.getEmail(), resetRequest.getNewPassword());

            resetInfo.setUsed(true);
            passwordResetTokenRepository.save(resetInfo);
            passwordResetTokenRepository.invalidateAllForEmail(resetInfo.getEmail());

            // Force re-login: revoke all refresh tokens
            var all = refreshTokenRepository.findAllByUsername(resetInfo.getEmail());
            all.forEach(t -> t.setRevoked(true));
            refreshTokenRepository.saveAll(all);

            log.info("Password reset successfully for user: {}", resetInfo.getEmail());
            return APIResponse.success("Password reset successfully");

        } catch (Exception e) {
            log.error("Password reset failed", e);
            return APIResponse.badRequest("Password reset failed");
        }
    }

    @PostMapping("/change-password")
    @Transactional
    public ResponseEntity<APIResponse<String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        try {
            UserScopeContext scope = UserScopeContextHolder.require();
            userService.changePassword(scope.getEmail(), request.getCurrentPassword(), request.getNewPassword());

            var all = refreshTokenRepository.findAllByUsername(scope.getEmail());
            all.forEach(t -> t.setRevoked(true));
            refreshTokenRepository.saveAll(all);

            log.info("Password changed for user: {}", scope.getEmail());
            return APIResponse.success("Password changed successfully. Please login again.");
        } catch (AuthenticationException e) {
            return APIResponse.error(e.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            log.error("Change password failed", e);
            return APIResponse.badRequest("Password change failed");
        }
    }

    @PostMapping("/revoke-all-refresh-tokens/{username}")
    @RequirePermission("user.permissions")
    public ResponseEntity<APIResponse<String>> revokeAllRefreshTokens(@PathVariable String username) {
        try {
            if (!rbacService.currentUserHasRole("SYSTEM_ADMIN")) {
                return APIResponse.error("Insufficient privileges", HttpStatus.FORBIDDEN);
            }

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
    public ResponseEntity<APIResponse<TokenValidationResponse>> validateToken(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return APIResponse.unauthorised("Missing or invalid Authorization header");
            }
            String token = authHeader.substring(7);

            if (!tokenProvider.validateToken(token)) {
                return APIResponse.unauthorised("Invalid token");
            }

            String username = tokenProvider.getUsernameFromToken(token);
            // Prefer request-scoped DB permissions when available
            List<String> permissions;
            if (UserScopeContextHolder.isPresent()) {
                permissions = UserScopeContextHolder.require().getPermissions().stream().toList();
            } else {
                permissions = rbacService.getUserPermissions(username);
            }

            return APIResponse.success(new TokenValidationResponse(true, username, permissions));
        } catch (Exception e) {
            return APIResponse.unauthorised("Token validation failed");
        }
    }
}
