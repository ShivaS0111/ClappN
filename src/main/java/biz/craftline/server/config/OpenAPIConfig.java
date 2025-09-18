package biz.craftline.server.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "ClappN API",
        version = "1.0",
        description = "API Documentation for ClappN Application"
    )
)
public class OpenAPIConfig {
}
