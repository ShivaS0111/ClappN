package biz.craftline.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.core.env.Environment;

@SpringBootApplication
@EnableAspectJAutoProxy
public class ServerApplication {

    private static final Logger log = LoggerFactory.getLogger(ServerApplication.class);

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ServerApplication.class);
        addDefaultProfileIfMissing(app);
        Environment env = app.run(args).getEnvironment();

        String appName = env.getProperty("spring.application.name", "server");
        String port = env.getProperty("local.server.port", env.getProperty("server.port", "8080"));
        String profiles = String.join(",", env.getActiveProfiles());

        log.info("Application '{}' started on port {} with profile(s) [{}]", appName, port, profiles);
    }

    private static void addDefaultProfileIfMissing(SpringApplication app) {
        String active = System.getProperty("spring.profiles.active");
        if ((active == null || active.isBlank()) && (System.getenv("SPRING_PROFILES_ACTIVE") == null || System.getenv("SPRING_PROFILES_ACTIVE").isBlank())) {
            app.setAdditionalProfiles("dev");
            log.info("No active Spring profile set, defaulting to 'dev'");
        }
    }

}
