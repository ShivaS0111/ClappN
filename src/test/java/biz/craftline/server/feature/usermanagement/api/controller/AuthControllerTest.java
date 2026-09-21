package biz.craftline.server.feature.usermanagement.api.controller;

import biz.craftline.server.config.mail.MailService;
import biz.craftline.server.config.security.JwtTokenProvider;
import biz.craftline.server.config.security.UserScopeContext;
import biz.craftline.server.config.security.UserScopeContextHolder;
import biz.craftline.server.feature.usermanagement.api.dto.LoginRequest;
import biz.craftline.server.feature.usermanagement.api.dto.LoginResponse;
import biz.craftline.server.feature.usermanagement.api.dto.RegisterRequest;
import biz.craftline.server.feature.usermanagement.api.dto.RegisterResponse;
import biz.craftline.server.feature.usermanagement.domain.model.AuthUser;
import biz.craftline.server.feature.usermanagement.domain.model.TokenInfo;
import biz.craftline.server.feature.usermanagement.domain.model.User;
import biz.craftline.server.feature.usermanagement.domain.service.RBACService;
import biz.craftline.server.feature.usermanagement.domain.service.UserService;
import biz.craftline.server.feature.usermanagement.api.dto.ForgotPasswordRequest;
import biz.craftline.server.feature.usermanagement.api.dto.LogoutRequest;
import biz.craftline.server.feature.usermanagement.api.dto.RefreshTokenRequest;
import biz.craftline.server.feature.usermanagement.api.dto.ResetPasswordRequest;
import biz.craftline.server.feature.usermanagement.infra.entity.PasswordResetTokenEntity;
import biz.craftline.server.feature.usermanagement.infra.entity.RefreshTokenEntity;
import biz.craftline.server.feature.usermanagement.infra.repository.PasswordResetTokenRepository;
import biz.craftline.server.feature.usermanagement.infra.repository.RefreshTokenRepository;
import biz.craftline.server.util.APIResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider tokenProvider;
    @Mock private UserService userService;
    @Mock private RBACService rbacService;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private MailService mailService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authController, "resetPasswordUrl", "http://localhost/reset");
        UserScopeContextHolder.clear();
    }

    @AfterEach
    void tearDown() {
        UserScopeContextHolder.clear();
    }

    @Test
    void login_success() {
        LoginRequest req = new LoginRequest();
        req.setUsername("user@test.com");
        req.setPassword("pass");

        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);

        AuthUser user = new AuthUser();
        user.setId(1L);
        user.setEmail("user@test.com");
        user.setVerified(1);
        user.setRoles(List.of("MANAGER"));
        user.setStoreIds(List.of(1L));
        user.setBusinessIds(List.of(2L));
        when(userService.getAuthUserByEmail("user@test.com")).thenReturn(user);
        when(rbacService.getUserPermissions("user@test.com")).thenReturn(List.of("order.read"));

        TokenInfo tokenInfo = TokenInfo.builder()
                .token("jwt")
                .refreshToken("refresh")
                .tokenExpiry(123L)
                .build();
        when(tokenProvider.generateTokenWithClaims(eq("user@test.com"), anyList(), anyList(), anyList(), anyList()))
                .thenReturn(tokenInfo);

        ResponseEntity<APIResponse<LoginResponse>> response = authController.authenticateUser(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("jwt", response.getBody().getData().getTokenInfo().getToken());
        verify(refreshTokenRepository).save(any());
    }

    @Test
    void login_badCredentials() {
        LoginRequest req = new LoginRequest();
        req.setUsername("user@test.com");
        req.setPassword("bad");
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        ResponseEntity<APIResponse<LoginResponse>> response = authController.authenticateUser(req);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Invalid username or password", response.getBody().getMessage());
    }

    @Test
    void login_unverifiedAccount_forbidden() {
        LoginRequest req = new LoginRequest();
        req.setUsername("user@test.com");
        req.setPassword("pass");

        Authentication auth = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        AuthUser user = new AuthUser();
        user.setEmail("user@test.com");
        user.setVerified(0);
        when(userService.getAuthUserByEmail("user@test.com")).thenReturn(user);

        ResponseEntity<APIResponse<LoginResponse>> response = authController.authenticateUser(req);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void register_success() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Ada");
        req.setEmail("ada@test.com");
        req.setPassword("secret1");

        when(userService.getUserByEmail("ada@test.com")).thenReturn(Optional.empty());
        User saved = new User();
        saved.setId(10L);
        saved.setEmail("ada@test.com");
        saved.setFullName("Ada");
        when(userService.createUserWithHashedPassword(any(User.class))).thenReturn(saved);

        ResponseEntity<APIResponse<RegisterResponse>> response = authController.registerUser(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(10L, response.getBody().getData().userId());
    }

    @Test
    void register_duplicateEmail() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("ada@test.com");
        req.setFullName("Ada");
        req.setPassword("secret1");
        when(userService.getUserByEmail("ada@test.com")).thenReturn(Optional.of(new User()));

        ResponseEntity<APIResponse<RegisterResponse>> response = authController.registerUser(req);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void validateToken_missingHeader() {
        var response = authController.validateToken(null);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void validateToken_success() {
        when(tokenProvider.validateToken("abc")).thenReturn(true);
        when(tokenProvider.getUsernameFromToken("abc")).thenReturn("user@test.com");
        when(rbacService.getUserPermissions("user@test.com")).thenReturn(List.of("order.read"));

        var response = authController.validateToken("Bearer abc");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getData().isValid());
    }

    @Test
    void login_serverError() {
        LoginRequest req = new LoginRequest();
        req.setUsername("user@test.com");
        req.setPassword("pass");
        when(authenticationManager.authenticate(any())).thenThrow(new RuntimeException("db down"));

        ResponseEntity<APIResponse<LoginResponse>> response = authController.authenticateUser(req);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void refreshToken_success() {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("rt-1");
        RefreshTokenEntity entity = new RefreshTokenEntity("rt-1", "user@test.com", LocalDateTime.now().plusDays(1));
        when(refreshTokenRepository.findByTokenAndRevokedFalse("rt-1")).thenReturn(Optional.of(entity));

        AuthUser user = new AuthUser();
        user.setEmail("user@test.com");
        user.setRoles(List.of("MANAGER"));
        user.setStoreIds(List.of(1L));
        user.setBusinessIds(List.of(2L));
        when(userService.getAuthUserByEmail("user@test.com")).thenReturn(user);
        when(rbacService.getUserPermissions("user@test.com")).thenReturn(List.of("order.read"));
        when(tokenProvider.generateTokenWithClaims(anyString(), anyList(), anyList(), anyList(), anyList()))
                .thenReturn(TokenInfo.builder().token("new-jwt").refreshToken("new-rt").build());
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<APIResponse<TokenInfo>> response = authController.refreshToken(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("new-jwt", response.getBody().getData().getToken());
    }

    @Test
    void refreshToken_expired() {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("rt-old");
        RefreshTokenEntity entity = new RefreshTokenEntity("rt-old", "user@test.com", LocalDateTime.now().minusHours(1));
        when(refreshTokenRepository.findByTokenAndRevokedFalse("rt-old")).thenReturn(Optional.of(entity));
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<APIResponse<TokenInfo>> response = authController.refreshToken(req);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void refreshToken_reuseRevokesAll() {
        RefreshTokenRequest req = new RefreshTokenRequest();
        req.setRefreshToken("rt-reused");
        when(refreshTokenRepository.findByTokenAndRevokedFalse("rt-reused")).thenReturn(Optional.empty());
        RefreshTokenEntity revoked = new RefreshTokenEntity("rt-reused", "user@test.com", LocalDateTime.now().plusDays(1));
        revoked.setRevoked(true);
        when(refreshTokenRepository.findByToken("rt-reused")).thenReturn(Optional.of(revoked));
        when(refreshTokenRepository.findAllByUsername("user@test.com")).thenReturn(List.of(revoked));
        when(refreshTokenRepository.saveAll(any())).thenReturn(List.of());

        ResponseEntity<APIResponse<TokenInfo>> response = authController.refreshToken(req);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        verify(refreshTokenRepository).saveAll(any());
    }

    @Test
    void logout_revokesToken() {
        LogoutRequest req = new LogoutRequest();
        req.setRefreshToken("rt-1");
        RefreshTokenEntity entity = new RefreshTokenEntity("rt-1", "user@test.com", LocalDateTime.now().plusDays(1));
        when(refreshTokenRepository.findByToken("rt-1")).thenReturn(Optional.of(entity));
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ResponseEntity<APIResponse<String>> response = authController.logout(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(entity.isRevoked());
    }

    @Test
    void forgotPassword_sendsEmailWhenUserExists() throws Exception {
        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setEmail("user@test.com");
        when(userService.getUserByEmail("user@test.com")).thenReturn(Optional.of(new User()));
        doNothing().when(mailService).sendText(anyString(), anyString(), anyString());

        ResponseEntity<APIResponse<String>> response = authController.forgotPassword(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(passwordResetTokenRepository).invalidateAllForEmail("user@test.com");
        verify(mailService).sendText(eq("user@test.com"), anyString(), anyString());
    }

    @Test
    void forgotPassword_sameResponseWhenMissing() {
        ForgotPasswordRequest req = new ForgotPasswordRequest();
        req.setEmail("missing@test.com");
        when(userService.getUserByEmail("missing@test.com")).thenReturn(Optional.empty());

        ResponseEntity<APIResponse<String>> response = authController.forgotPassword(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(passwordResetTokenRepository, never()).save(any());
    }

    @Test
    void resetPassword_success() {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setResetToken("tok");
        req.setNewPassword("newpass1");
        PasswordResetTokenEntity token = new PasswordResetTokenEntity("tok", "user@test.com", LocalDateTime.now().plusHours(1));
        when(passwordResetTokenRepository.findByTokenAndUsedFalse("tok")).thenReturn(Optional.of(token));
        when(refreshTokenRepository.findAllByUsername("user@test.com")).thenReturn(List.of());
        doNothing().when(userService).updatePassword("user@test.com", "newpass1");

        ResponseEntity<APIResponse<String>> response = authController.resetPassword(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(token.isUsed());
    }

    @Test
    void resetPassword_invalidToken() {
        ResetPasswordRequest req = new ResetPasswordRequest();
        req.setResetToken("bad");
        req.setNewPassword("newpass1");
        when(passwordResetTokenRepository.findByTokenAndUsedFalse("bad")).thenReturn(Optional.empty());

        ResponseEntity<APIResponse<String>> response = authController.resetPassword(req);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void revokeAllRefreshTokens_requiresAdmin() {
        when(rbacService.currentUserHasRole("SYSTEM_ADMIN")).thenReturn(false);
        ResponseEntity<APIResponse<String>> response = authController.revokeAllRefreshTokens("user@test.com");
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void revokeAllRefreshTokens_success() {
        when(rbacService.currentUserHasRole("SYSTEM_ADMIN")).thenReturn(true);
        RefreshTokenEntity rt = new RefreshTokenEntity("rt", "user@test.com", LocalDateTime.now().plusDays(1));
        when(refreshTokenRepository.findAllByUsername("user@test.com")).thenReturn(List.of(rt));
        when(refreshTokenRepository.saveAll(any())).thenReturn(List.of());

        ResponseEntity<APIResponse<String>> response = authController.revokeAllRefreshTokens("user@test.com");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(rt.isRevoked());
    }

    @Test
    void validateToken_invalidToken() {
        when(tokenProvider.validateToken("bad")).thenReturn(false);
        var response = authController.validateToken("Bearer bad");
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void validateToken_usesScopePermissions() {
        UserScopeContextHolder.set(UserScopeContext.builder()
                .userId(1L).email("user@test.com").roles(List.of()).permissions(Set.of("order.read"))
                .unrestricted(false).build());
        when(tokenProvider.validateToken("abc")).thenReturn(true);
        when(tokenProvider.getUsernameFromToken("abc")).thenReturn("user@test.com");

        var response = authController.validateToken("Bearer abc");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().getData().getPermissions().contains("order.read"));
    }

    @Test
    void register_exceptionReturnsBadRequest() {
        RegisterRequest req = new RegisterRequest();
        req.setFullName("Ada");
        req.setEmail("ada@test.com");
        req.setPassword("secret1");
        when(userService.getUserByEmail("ada@test.com")).thenThrow(new RuntimeException("db"));

        ResponseEntity<APIResponse<RegisterResponse>> response = authController.registerUser(req);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void changePassword_usesScope() {
        UserScopeContextHolder.set(UserScopeContext.builder()
                .userId(1L).email("user@test.com").roles(List.of()).permissions(Set.of())
                .unrestricted(true).build());

        var req = new biz.craftline.server.feature.usermanagement.api.dto.ChangePasswordRequest();
        req.setCurrentPassword("old");
        req.setNewPassword("newpass1");
        doNothing().when(userService).changePassword("user@test.com", "old", "newpass1");
        when(refreshTokenRepository.findAllByUsername("user@test.com")).thenReturn(List.of());

        var response = authController.changePassword(req);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
