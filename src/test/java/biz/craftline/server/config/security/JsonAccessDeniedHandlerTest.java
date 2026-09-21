package biz.craftline.server.config.security;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JsonAccessDeniedHandlerTest {

    @Test
    void handle_writesJson403_withMessage() throws Exception {
        JsonAccessDeniedHandler handler = new JsonAccessDeniedHandler();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(WriteListener listener) {}
            @Override public void write(int b) { out.write(b); }
        });

        handler.handle(request, response, new AccessDeniedException("forbidden store"));

        verify(response).setStatus(403);
        String body = out.toString(StandardCharsets.UTF_8);
        assertTrue(body.contains("forbidden store"));
    }

    @Test
    void handle_writesJson403_defaultMessage() throws Exception {
        JsonAccessDeniedHandler handler = new JsonAccessDeniedHandler();
        HttpServletResponse response = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(WriteListener listener) {}
            @Override public void write(int b) { out.write(b); }
        });

        handler.handle(mock(HttpServletRequest.class), response, new AccessDeniedException(null));

        String body = out.toString(StandardCharsets.UTF_8);
        assertTrue(body.contains("Access denied"));
    }
}
