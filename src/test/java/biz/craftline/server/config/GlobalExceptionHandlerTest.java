package biz.craftline.server.config;

import biz.craftline.server.util.APIResponse;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.security.auth.login.AccountException;
import javax.security.auth.login.AccountLockedException;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleBadCredentialsException() {
        ResponseEntity<APIResponse<String>> response =
                exceptionHandler.handleBadCredentialsException(new BadCredentialsException("Invalid credentials"));
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("The username or password is incorrect", response.getBody().getMessage());
    }

    @Test
    void testHandleGeneralException_RuntimeException() {
        ResponseEntity<APIResponse<String>> response =
                exceptionHandler.handleGeneralException(new Exception("Unexpected error"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Internal server error", response.getBody().getMessage());
    }

    @Test
    void testHandleDataIntegrityViolationException() {
        ResponseEntity<APIResponse<String>> result =
                exceptionHandler.handleDataIntegrityViolationException(new DataIntegrityViolationException("dup"));
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals("Data duplication error", result.getBody().getMessage());
    }

    @Test
    void handleEntityNotFound() {
        ResponseEntity<APIResponse<String>> response =
                exceptionHandler.handleNotFound(new EntityNotFoundException("Order missing"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Order missing", response.getBody().getMessage());
    }

    @Test
    void handleNoSuchElement() {
        assertEquals(HttpStatus.NOT_FOUND,
                exceptionHandler.handleNotFound(new NoSuchElementException("gone")).getStatusCode());
    }

    @Test
    void handleUsernameNotFound() {
        assertEquals(HttpStatus.NOT_FOUND,
                exceptionHandler.handleNotFound(new UsernameNotFoundException("user")).getStatusCode());
    }

    @Test
    void handleMissingServletRequestParameter() {
        ResponseEntity<APIResponse<String>> response =
                exceptionHandler.handleMissingParam(new MissingServletRequestParameterException("id", "Long"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("id"));
    }

    @Test
    void handleMultipartException() {
        ResponseEntity<APIResponse<String>> response =
                exceptionHandler.handleMultipart(new MultipartException("no file"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Multipart file upload required", response.getBody().getMessage());
    }

    @Test
    void handleMissingServletRequestPart() throws Exception {
        ResponseEntity<APIResponse<String>> response =
                exceptionHandler.handleMultipart(new MissingServletRequestPartException("file"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleRuntimeNotFound() {
        ResponseEntity<APIResponse<String>> response =
                exceptionHandler.handleRuntime(new RuntimeException("Invoice not found"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleRuntimeNotValid() {
        ResponseEntity<APIResponse<String>> response =
                exceptionHandler.handleRuntime(new RuntimeException("Brand not valid"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleNpe() {
        ResponseEntity<APIResponse<String>> response =
                exceptionHandler.handleRuntime(new NullPointerException("x"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid or incomplete request payload", response.getBody().getMessage());
    }

    @Test
    void handleIllegalArgument() {
        ResponseEntity<APIResponse<String>> response =
                exceptionHandler.handleIllegalArgumentException(new IllegalArgumentException("bad arg"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("bad arg", response.getBody().getMessage());
    }

    @Test
    void handleIllegalState() {
        assertEquals(HttpStatus.BAD_REQUEST,
                exceptionHandler.handleIllegalState(new IllegalStateException("bad state")).getStatusCode());
    }

    @Test
    void handleAccessDenied() {
        assertEquals(HttpStatus.FORBIDDEN,
                exceptionHandler.handleAccessDeniedException(new AccessDeniedException("no")).getStatusCode());
    }

    @Test
    void handleAccountExceptions() {
        assertEquals(HttpStatus.FORBIDDEN,
                exceptionHandler.handleAccountException(new AccountException("x")).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN,
                exceptionHandler.handleAccountLockedException(new AccountLockedException("x")).getStatusCode());
    }

    @Test
    void handleAccountStatus() {
        assertEquals(HttpStatus.FORBIDDEN,
                exceptionHandler.handleAccountStatusException(
                        new AccountStatusException("locked") {}).getStatusCode());
    }

    @Test
    void handleAuthentication() {
        assertEquals(HttpStatus.UNAUTHORIZED,
                exceptionHandler.handleAuthenticationException(
                        new InsufficientAuthenticationException("need auth")).getStatusCode());
    }

    @Test
    void handleConstraintViolation() {
        ConstraintViolationException ex = new ConstraintViolationException("constraint", new SQLException("x"), "uk");
        assertEquals(HttpStatus.BAD_REQUEST,
                exceptionHandler.handleConstraintViolationException(ex).getStatusCode());
    }

    @Test
    void handleInvalidDataAccess() {
        InvalidDataAccessApiUsageException ex = new InvalidDataAccessApiUsageException("bad", new IllegalArgumentException("root"));
        ResponseEntity<APIResponse<String>> response = exceptionHandler.handleInvalidDataAccess(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("root", response.getBody().getMessage());
    }

    @Test
    void handleNoHandlerFound() {
        assertEquals(HttpStatus.NOT_FOUND,
                exceptionHandler.handleNotFoundRoute(
                        new NoHandlerFoundException("GET", "/missing", null)).getStatusCode());
    }

    @Test
    void handleMethodNotAllowed() {
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED,
                exceptionHandler.handleMethodNotAllowed(
                        new HttpRequestMethodNotSupportedException("PUT")).getStatusCode());
    }

    @Test
    void handleTransactionSystemException() {
        TransactionSystemException ex = new TransactionSystemException("tx", new IllegalArgumentException("cause"));
        ResponseEntity<APIResponse<String>> response = exceptionHandler.handleTx(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("cause", response.getBody().getMessage());
    }

    @Test
    void handleValidationException() throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "req");
        bindingResult.addError(new FieldError("req", "email", "must be valid"));
        MethodParameter parameter = new MethodParameter(Object.class.getDeclaredMethod("toString"), -1);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<APIResponse<String>> response = exceptionHandler.handleValidationException(ex);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("email"));
    }

    @Test
    void handleNotFound_nullMessage() {
        ResponseEntity<APIResponse<String>> response =
                exceptionHandler.handleNotFound(new EntityNotFoundException());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not found", response.getBody().getMessage());
    }

    @Test
    void handleRuntimeNotConfigured() {
        ResponseEntity<APIResponse<String>> response =
                exceptionHandler.handleRuntime(new RuntimeException("Payment gateway not configured"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void handleUnhandledRuntime() {
        ResponseEntity<APIResponse<String>> response =
                exceptionHandler.handleRuntime(new RuntimeException("something exploded"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
