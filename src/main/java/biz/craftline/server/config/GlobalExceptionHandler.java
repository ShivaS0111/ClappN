package biz.craftline.server.config;

import biz.craftline.server.util.APIResponse;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.security.auth.login.AccountException;
import javax.security.auth.login.AccountLockedException;
import java.util.stream.Collectors;

/**
 * Global exception handler that returns consistent JSON responses
 * matching the APIResponse format: { success, message, data, status }.
 *
 * Priority order (Spring picks the most specific match):
 *  1. AccessDeniedException       → 403
 *  2. BadCredentialsException     → 401 (extends AuthenticationException)
 *  3. AccountStatusException      → 403 (locked/disabled account)
 *  4. AuthenticationException     → 401 (catch-all auth failures)
 *  5. Validation / constraint     → 400
 *  6. Everything else             → 500
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ─── Security: 403 Forbidden ─────────────────────────────────────

    @ExceptionHandler(AccountException.class)
    public ResponseEntity<APIResponse<String>> handleAccountException(AccountException ex) {
        log.warn("Access exception: {}", ex.getMessage());
        return APIResponse.error("Access denied: " + ex.getMessage(), HttpStatus.FORBIDDEN);
    }
    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<APIResponse<String>> handleAccountLockedException(AccountLockedException ex) {
        log.warn("Access exception: {}", ex.getMessage());
        return APIResponse.error("Access denied: " + ex.getMessage(), HttpStatus.FORBIDDEN);
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<APIResponse<String>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return APIResponse.error("Access denied: " + ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AccountStatusException.class)
    public ResponseEntity<APIResponse<String>> handleAccountStatusException(AccountStatusException ex) {
        log.warn("Account status error: {}", ex.getMessage());
        return APIResponse.error("Account locked or disabled: " + ex.getMessage(), HttpStatus.FORBIDDEN);
    }

    // ─── Security: 401 Unauthorized ──────────────────────────────────

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<APIResponse<String>> handleBadCredentialsException(BadCredentialsException ex) {
        log.warn("Bad credentials: {}", ex.getMessage());
        return APIResponse.error("The username or password is incorrect", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<APIResponse<String>> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return APIResponse.error("Unauthorized: " + ex.getMessage(), HttpStatus.UNAUTHORIZED);
    }

    // ─── Validation: 400 Bad Request ─────────────────────────────────

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse<String>> handleValidationException(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation error: {}", errors);
        return APIResponse.error("Validation error: " + errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<APIResponse<String>> handleConstraintViolationException(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());
        return APIResponse.error("Constraint violation: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<APIResponse<String>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMessage());
        return APIResponse.error("Data duplication error", HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<APIResponse<String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return APIResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // ─── Catch-all: 500 Internal Server Error ────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse<String>> handleGeneralException(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return APIResponse.error("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
