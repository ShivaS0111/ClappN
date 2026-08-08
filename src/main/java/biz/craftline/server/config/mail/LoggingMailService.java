package biz.craftline.server.config.mail;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggingMailService implements MailService {

    @Override
    public void sendText(String to, String subject, String body) {
        log.info("[MAIL:log] to={} subject={}\n{}", to, subject, body);
    }

    @Override
    public void sendHtml(String to, String subject, String htmlBody) {
        log.info("[MAIL:log:html] to={} subject={}\n{}", to, subject, htmlBody);
    }

    @Override
    public void sendWithAttachment(String to, String subject, String body,
                                   String filename, byte[] attachment, String contentType) {
        log.info("[MAIL:log:attach] to={} subject={} attachment={} bytes={} type={}\n{}",
                to, subject, filename, attachment != null ? attachment.length : 0, contentType, body);
    }
}
