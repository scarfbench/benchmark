package jakarta.tutorial.timersession.ejb;

import java.time.Instant;
import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TimerSessionService {

    private static final Logger logger = Logger.getLogger(TimerSessionService.class.getName());

    private final TaskScheduler taskScheduler;

    private Date lastProgrammaticTimeout;
    private Date lastAutomaticTimeout;

    public TimerSessionService(TaskScheduler taskScheduler) {
        this.taskScheduler = taskScheduler;
    }

    public void setTimer(long intervalDurationMillis) {
        logger.log(Level.INFO,
                "Setting a programmatic timeout for {0} milliseconds from now.",
                intervalDurationMillis);

        Instant triggerTime = Instant.now().plusMillis(intervalDurationMillis);

        taskScheduler.schedule(this::programmaticTimeout, triggerTime);
    }

    private void programmaticTimeout() {
        this.lastProgrammaticTimeout = new Date();
        logger.info("Programmatic timeout occurred.");
    }

    @Scheduled(cron = "0 */1 * * * *")
    public void automaticTimeout() {
        this.lastAutomaticTimeout = new Date();
        logger.info("Automatic timeout occurred.");
    }

    public String getLastProgrammaticTimeout() {
        return lastProgrammaticTimeout != null
                ? lastProgrammaticTimeout.toString()
                : "never";
    }

    public String getLastAutomaticTimeout() {
        return lastAutomaticTimeout != null
                ? lastAutomaticTimeout.toString()
                : "never";
    }
}