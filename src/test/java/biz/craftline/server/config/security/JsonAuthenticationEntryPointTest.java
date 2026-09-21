package biz.craftline.server.config.security;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JsonAuthenticationEntryPointTest {

    @Test
    void commence_writesJson401() throws Exception {
        JsonAuthenticationEntryPoint entryPoint = new JsonAuthenticationEntryPoint();
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        when(response.getOutputStream()).thenReturn(new ServletOutputStream() {
            @Override public boolean isReady() { return true; }
            @Override public void setWriteListener(WriteListener listener) {}
            @Override public void write(int b) { out.write(b); }
        });

        entryPoint.commence(request, response, new BadCredentialsException("bad"));

        verify(response).setStatus(401);
        String body = out.toString(StandardCharsets.UTF_8);
        assertTrue(body.contains("\"success\":false"));
        assertTrue(body.contains("Unauthorized"));
    }
}
