package biz.craftline.server.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "app.security.swagger-enabled", havingValue = "true", matchIfMissing = true)
@OpenAPIDefinition(
    info = @Info(
        title = "ClappN API",
        version = "1.0",
        description = "API Documentation for ClappN Application"
    )
)
public class OpenAPIConfig {
}
