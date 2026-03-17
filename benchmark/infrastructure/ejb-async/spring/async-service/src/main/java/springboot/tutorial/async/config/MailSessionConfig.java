package springboot.tutorial.async.config;

import jakarta.mail.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class MailSessionConfig {

    @Bean
    public Session mailSession(
            @Value("${spring.mail.host:localhost}") String host,
            @Value("${spring.mail.port:3025}") int port,
            @Value("${spring.mail.username:}") String user,
            @Value("${spring.mail.password:}") String pass,
            @Value("${spring.mail.properties.mail.smtp.auth:false}") boolean auth,
            @Value("${spring.mail.properties.mail.smtp.starttls.enable:false}") boolean startTls,
            @Value("${app.mail.from:}") String from
    ) {
        Properties p = new Properties();
        p.put("mail.smtp.host", host);
        p.put("mail.smtp.port", Integer.toString(port));
        p.put("mail.smtp.auth", Boolean.toString(auth));
        p.put("mail.smtp.starttls.enable", Boolean.toString(startTls));
        if (!from.isEmpty()) {
            p.put("mail.from", from); // so msg.setFrom() has a value
        }

        if (auth) {
            return Session.getInstance(p, new jakarta.mail.Authenticator() {
                @Override
                protected jakarta.mail.PasswordAuthentication getPasswordAuthentication() {
                    return new jakarta.mail.PasswordAuthentication(user, pass);
                }
            });
        }
        return Session.getInstance(p);
    }
}