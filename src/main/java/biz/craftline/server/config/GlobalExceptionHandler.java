package biz.craftline.server.config;

import biz.craftline.server.util.APIResponse;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import javax.security.auth.login.AccountException;
import javax.security.auth.login.AccountLockedException;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

/**
 * Global exception handler that returns consistent JSON responses
 * matching the APIResponse format: { success, message, data, status }.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

    @ExceptionHandler({
            EntityNotFoundException.class,
            NoSuchElementException.class,
            UsernameNotFoundException.class
    })
    public ResponseEntity<APIResponse<String>> handleNotFound(RuntimeException ex) {
        log.warn("Not found: {}", ex.getMessage());
        return APIResponse.error(ex.getMessage() != null ? ex.getMessage() : "Not found", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse<String>> handleValidationException(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation error: {}", errors);
        return APIResponse.error("Validation error: " + errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<APIResponse<String>> handleMissingParam(MissingServletRequestParameterException ex) {
        log.warn("Missing request parameter: {}", ex.getMessage());
        return APIResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({MultipartException.class, MissingServletRequestPartException.class})
    public ResponseEntity<APIResponse<String>> handleMultipart(Exception ex) {
        log.warn("Multipart request error: {}", ex.getMessage());
        return APIResponse.error("Multipart file upload required", HttpStatus.BAD_REQUEST);
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

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<APIResponse<String>> handleInvalidDataAccess(InvalidDataAccessApiUsageException ex) {
        log.warn("Invalid data access: {}", ex.getMessage());
        return APIResponse.error(ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage()
                : ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<APIResponse<String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return APIResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<APIResponse<String>> handleIllegalState(IllegalStateException ex) {
        log.warn("Illegal state: {}", ex.getMessage());
        return APIResponse.error(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<APIResponse<String>> handleNotFoundRoute(NoHandlerFoundException ex) {
        log.warn("No handler: {} {}", ex.getHttpMethod(), ex.getRequestURL());
        return APIResponse.error("Resource not found", HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<APIResponse<String>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
        log.warn("Method not allowed: {}", ex.getMessage());
        return APIResponse.error("HTTP method not allowed", HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<APIResponse<String>> handleTx(TransactionSystemException ex) {
        Throwable root = ex.getMostSpecificCause();
        String msg = root != null && root.getMessage() != null ? root.getMessage() : ex.getMessage();
        log.warn("Transaction failed: {}", msg);
        return APIResponse.error(msg, HttpStatus.BAD_REQUEST);
    }

    /**
     * Legacy services often throw bare RuntimeException for not-found / bad input.
     * Map obvious client errors to 4xx so callers do not see 500s.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<APIResponse<String>> handleRuntime(RuntimeException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Request failed";
        String lower = msg.toLowerCase();
        if (lower.contains("not found") || lower.contains("not valid") || lower.contains("not configured")) {
            log.warn("Client error (runtime): {}", msg);
            HttpStatus status = lower.contains("not found") || lower.contains("not configured")
                    ? HttpStatus.NOT_FOUND
                    : HttpStatus.BAD_REQUEST;
            return APIResponse.error(msg, status);
        }
        if (ex instanceof NullPointerException) {
            log.warn("Bad request (NPE from incomplete payload): {}", msg);
            return APIResponse.error("Invalid or incomplete request payload", HttpStatus.BAD_REQUEST);
        }
        log.error("Unhandled runtime exception: {}", msg, ex);
        return APIResponse.error("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse<String>> handleGeneralException(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return APIResponse.error("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
