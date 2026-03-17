package jakarta.tutorial.timersession.web;

import java.io.Serializable;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import jakarta.tutorial.timersession.ejb.TimerSessionService;

@Component
@SessionScope
public class TimerManager implements Serializable {

    private final TimerSessionService timerSessionService;

    private String lastProgrammaticTimeout = "never";
    private String lastAutomaticTimeout = "never";

    public TimerManager(TimerSessionService timerSessionService) {
        this.timerSessionService = timerSessionService;
    }

    public void setTimer() {
        long timeoutDuration = 8000;
        timerSessionService.setTimer(timeoutDuration);
    }

    public String getLastProgrammaticTimeout() {
        lastProgrammaticTimeout = timerSessionService.getLastProgrammaticTimeout();
        return lastProgrammaticTimeout;
    }

    public String getLastAutomaticTimeout() {
        lastAutomaticTimeout = timerSessionService.getLastAutomaticTimeout();
        return lastAutomaticTimeout;
    }

    public void setLastProgrammaticTimeout(String value) {
        this.lastProgrammaticTimeout = value;
    }

    public void setLastAutomaticTimeout(String value) {
        this.lastAutomaticTimeout = value;
    }
}
