package jakarta.tutorial.taskcreator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import org.springframework.beans.factory.annotation.Qualifier;

@RestController
@RequestMapping("/taskinfo") // matches your original path
public class TaskService {
    private static final Logger log = LoggerFactory.getLogger(TaskService.class);

    private final ScheduledExecutorService scheduler;
    private final ExecutorService executor;
    private final Map<String, ScheduledFuture<?>> periodicTasks = new ConcurrentHashMap<>();
    private volatile String infoField = "";

    private final TaskUpdateEvents events;

    private final TaskRestPoster poster;

    public TaskService(@Qualifier("taskScheduler") ScheduledExecutorService scheduler,
                       @Qualifier("taskExecutor") ExecutorService executor,
                       TaskUpdateEvents events,
                       TaskRestPoster poster) {
        this.scheduler = scheduler;
        this.executor = executor;
        this.events = events;
        this.poster = poster;
    }

    public Task newTask(String name, String type) {
        return new Task(poster, name, type);
    }

    public void submitTask(Task task, String type) {
        switch (type) {
            case "IMMEDIATE" -> executor.submit(task);
            case "DELAYED"   -> scheduler.schedule(task, 3, TimeUnit.SECONDS);
            case "PERIODIC"  -> {
                ScheduledFuture<?> fut = scheduler.scheduleAtFixedRate(task, 0, 8, TimeUnit.SECONDS);
                periodicTasks.put(task.getName(), fut);
            }
        }
    }

    public void cancelPeriodicTask(String name) {
        if (periodicTasks.containsKey(name)) {
            log.info("[TaskService] Cancelling task {}", name);
            periodicTasks.get(name).cancel(true);
            periodicTasks.remove(name);
            events.fire("tasklist");
        }
    }

    @PostMapping(consumes = {MediaType.TEXT_HTML_VALUE, MediaType.TEXT_PLAIN_VALUE})
    public void addToInfoField(@RequestBody String msg) {
        infoField = msg + "\n" + infoField;
        log.info("[TaskService] Added message {}", msg);
        events.fire("infobox");
    }

    public String getInfoField() { return infoField; }
    public void clearInfoField() { infoField = ""; }
    public Set<String> getPeriodicTasks() { return periodicTasks.keySet(); }

    /** Optional: call on shutdown via @PreDestroy if you like */
    void shutdown() {
        periodicTasks.values().forEach(f -> f.cancel(true));
        executor.shutdownNow();
        scheduler.shutdownNow();
    }
}