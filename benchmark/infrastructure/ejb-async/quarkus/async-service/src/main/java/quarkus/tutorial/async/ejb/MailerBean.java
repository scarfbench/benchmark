package quarkus.tutorial.async.ejb;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

@Named
@ApplicationScoped
public class MailerBean {

    @Inject
    Session session;

    @Inject
    Logger log;

    public Future<String> sendMessage(String email) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String subject = "Test message from async example";
                String ts = LocalDateTime.now()
                        .format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG, FormatStyle.SHORT));
                String body = "This is a test message from the async example of the Jakarta EE Tutorial. "
                        + "It was sent on " + ts + ".";

                MimeMessage msg = new MimeMessage(session);
                msg.setFrom();
                msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email, false));
                msg.setSubject(subject);
                msg.setHeader("X-Mailer", "Jakarta Mail");
                msg.setText(body);
                msg.setSentDate(java.util.Date.from(java.time.Instant.now()));
                Transport.send(msg);

                log.infof("Mail sent to %s", email);
                return "Sent";
            } catch (Throwable t) {
                log.error("Error in sending message.", t);
                return "Encountered an error: " + t.getMessage();
            }
        });
    }
}