package biz.craftline.server.config.mail;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.util.StringUtils;

@Configuration
public class MailConfig {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "app.mail.mode", havingValue = "log", matchIfMissing = true)
    public MailService loggingMailService() {
        return new LoggingMailService();
    }

    @Bean
    @Primary
    @ConditionalOnProperty(name = "app.mail.mode", havingValue = "smtp")
    public MailService smtpMailService(
            JavaMailSender mailSender,
            @Value("${app.mail.from:${spring.mail.username:noreply@clapp.local}}") String from) {
        if (!StringUtils.hasText(from)) {
            from = "noreply@clapp.local";
        }
        return new SmtpMailService(mailSender, from);
    }
}
