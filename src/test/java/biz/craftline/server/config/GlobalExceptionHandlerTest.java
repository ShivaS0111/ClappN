package biz.craftline.server.config;

import biz.craftline.server.util.APIResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleGeneralException_BadCredentials() {
        // Given
        BadCredentialsException exception = new BadCredentialsException("Invalid credentials");

        // When
        ProblemDetail result = exceptionHandler.handleGeneralException(exception);

        // Then
        assertNotNull(result);
        assertEquals(401, result.getStatus());
        assertEquals("Invalid credentials", result.getDetail());
        assertEquals("The username or password is incorrect", result.getProperties().get("description"));
    }

    @Test
    void testHandleGeneralException_RuntimeException() {
        // Given
        RuntimeException exception = new RuntimeException("Unexpected error");

        // When
        ProblemDetail result = exceptionHandler.handleGeneralException(exception);

        // Then
        assertNotNull(result);
        assertEquals(500, result.getStatus());
        assertEquals("Internal server error", result.getDetail());
    }

    @Test
    void testHandleDataIntegrityViolationException() {
        // Given
        DataIntegrityViolationException exception = new DataIntegrityViolationException("Data duplication");

        // When
        ResponseEntity<APIResponse<String>> result = exceptionHandler.handleDataIntegrityViolationException(exception);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertFalse(result.getBody().isSuccess());
        assertEquals("Data duplication error", result.getBody().getMessage());
        assertNotNull(result.getBody().getTimestamp());
    }
}