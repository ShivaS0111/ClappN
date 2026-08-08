package biz.craftline.server.config.mail;

/**
 * Outbound email abstraction. Dev uses logging; SMTP when spring.mail.host is set.
 */
public interface MailService {

    void sendText(String to, String subject, String body);

    void sendHtml(String to, String subject, String htmlBody);

    default void sendWithAttachment(String to, String subject, String body,
                                    String filename, byte[] attachment, String contentType) {
        sendText(to, subject, body + "\n\n[Attachment omitted in plain send: " + filename + "]");
    }
}
