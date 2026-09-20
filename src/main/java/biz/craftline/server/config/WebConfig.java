package biz.craftline.server.config;

import biz.craftline.server.config.security.RequirePermissionInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.security.swagger-enabled:true}")
    private boolean swaggerEnabled;

    private final RequirePermissionInterceptor requirePermissionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requirePermissionInterceptor)
                .addPathPatterns("/api/**", "/store/**");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        if (swaggerEnabled) {
            registry.addRedirectViewController("/", "/swagger-ui.html");
            registry.addRedirectViewController("/swagger-ui", "/swagger-ui.html");
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }
}
