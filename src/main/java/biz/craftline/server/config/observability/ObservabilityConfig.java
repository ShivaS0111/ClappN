package biz.craftline.server.config.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Common metric tags and cardinality guards for production scraping.
 */
@Configuration
public class ObservabilityConfig {

    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags(
            @Value("${spring.application.name:server}") String appName,
            @Value("${spring.profiles.active:default}") String profile) {
        String env = profile.contains(",") ? profile.split(",")[0].trim() : profile.trim();
        return registry -> registry.config()
                .commonTags("application", appName, "env", env.isEmpty() ? "default" : env)
                .meterFilter(MeterFilter.deny(id -> {
                    String name = id.getName();
                    // Drop ultra-noisy JVM buffer pools if ever too chatty; keep core JVM + HTTP
                    return name.startsWith("jvm.buffer.");
                }));
    }
}
