package biz.craftline.server.feature.usermanagement.api.controller;

import biz.craftline.server.feature.usermanagement.api.dto.LoginRequest;
import biz.craftline.server.feature.usermanagement.api.dto.LoginResponse;
import biz.craftline.server.feature.usermanagement.api.dto.RegisterRequest;
import biz.craftline.server.feature.usermanagement.api.dto.RegisterResponse;
import biz.craftline.server.feature.usermanagement.domain.model.AuthUser;
import biz.craftline.server.feature.usermanagement.domain.model.User;
import biz.craftline.server.feature.usermanagement.domain.service.UserService;
import biz.craftline.server.util.APIResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService service;

    @PostMapping("/register")
    public ResponseEntity<APIResponse<RegisterResponse>> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
          /*   // Check if user already exists
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

            return APIResponse.success(response);*/
            throw new IllegalStateException("Registration is disabled in this demo");

        } catch (Exception e) {
            log.error("Registration failed for email: {}", registerRequest.getEmail(), e);
            return APIResponse.error("Registration failed: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<APIResponse<AuthUser>> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        /*try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // Use the authenticated principal's authorities to derive permissions (avoid calling RBACService here)
            List<String> permissions = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .distinct()
                    .collect(java.util.stream.Collectors.toList());

            String jwt = tokenProvider.generateTokenWithPermissions(loginRequest.getUsername(), permissions);
            String refreshToken = generateRefreshToken(loginRequest.getUsername());

            LoginResponse response = new LoginResponse(jwt, "Bearer", permissions, refreshToken);

            log.info("User {} authenticated successfully with {} permissions",
                    loginRequest.getUsername(), permissions.size());

            return APIResponse.success(response);

        } catch (AuthenticationException e) {
            // Log full exception to capture stack traces like StackOverflowError wrapped in other exceptions
            log.warn("Authentication failed for user: {}", loginRequest.getUsername(), e);
            return APIResponse.error("Invalid username or password", HttpStatus.BAD_REQUEST);
        } catch (Throwable t) {
            // Catch any throwable (including StackOverflowError) to log full stack trace for debugging.
            log.error("Unexpected error during authentication for user: {}", loginRequest.getUsername(), t);
            return APIResponse.error("Authentication failed due to server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
        */
        AuthUser user = service.getAuthUserByEmail(loginRequest.getUsername());
        return APIResponse.success(user);
    }
}

