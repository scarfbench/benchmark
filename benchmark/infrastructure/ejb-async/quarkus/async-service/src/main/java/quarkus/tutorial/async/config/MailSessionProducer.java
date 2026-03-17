package quarkus.tutorial.async.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import java.util.Properties;

@ApplicationScoped
public class MailSessionProducer {

    @Produces
    @ApplicationScoped
    jakarta.mail.Session mailSession() {
        Properties p = new Properties();
        p.put("mail.smtp.host", System.getProperty("quarkus.mailer.host", "localhost"));
        p.put("mail.smtp.port", System.getProperty("quarkus.mailer.port", "3025"));
        p.put("mail.smtp.auth", System.getProperty("quarkus.mailer.auth", "true"));
        p.put("mail.smtp.starttls.enable", System.getProperty("quarkus.mailer.start-tls", "false"));

        String user = System.getProperty("quarkus.mailer.username", "jack");
        String pass = System.getProperty("quarkus.mailer.password", "changeMe");

        return jakarta.mail.Session.getInstance(p, new jakarta.mail.Authenticator() {
            @Override
            protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                return new jakarta.mail.PasswordAuthentication(user, pass);
            }
        });
    }
}