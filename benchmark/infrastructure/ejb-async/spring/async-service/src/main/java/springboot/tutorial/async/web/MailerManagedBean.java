package springboot.tutorial.async.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import springboot.tutorial.async.ejb.MailerService; // renamed below

@Component("mailerManagedBean")     // JSF EL: #{mailerManagedBean}
@SessionScope                      // Spring session scope (JoinFaces sees it)
public class MailerManagedBean implements Serializable {

    private static final Logger logger = LoggerFactory.getLogger(MailerManagedBean.class);

    @Autowired
    private MailerService mailerService;

    private String email;
    private String status;
    private Future<String> mailStatus;

    public String getStatus() {
        if (mailStatus != null && mailStatus.isDone()) {
            try {
                this.setStatus(mailStatus.get());
            } catch (ExecutionException | CancellationException | InterruptedException ex) {
                this.setStatus(ex.getCause() != null ? ex.getCause().toString() : ex.toString());
            }
        }
        return status;
    }

    public void setStatus(String status) { this.status = status; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String send() {
        try {
            mailStatus = mailerService.sendMessage(this.getEmail());
            this.setStatus("Processing... (refresh to check again)");
        } catch (Exception ex) {
            logger.error("Send failed", ex);
            this.setStatus("Encountered an error: " + ex.getMessage());
        }
        return "response?faces-redirect=true";
    }
}