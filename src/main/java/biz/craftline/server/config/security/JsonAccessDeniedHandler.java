package biz.craftline.server.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Returns JSON 403 for Spring Security access-denied cases in the filter chain.
 */
@Component
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String message = accessDeniedException.getMessage() != null
                ? accessDeniedException.getMessage()
                : "Access denied";
        Map<String, Object> body = new HashMap<>();
        body.put("success", false);
        body.put("message", "Access denied: " + message);
        body.put("data", null);
        body.put("status", 403);
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
