package jakarta.tutorial.taskcreator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

/**
 * Replacement for the original EJB. Uses standard executors managed inside an
 * application scoped bean. Provides REST endpoint for task updates.
 */
@ApplicationScoped
@Path("/taskinfo")
public class TaskService {

    private static final Logger log = Logger.getLogger(TaskService.class);

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    private final ExecutorService executor = Executors.newFixedThreadPool(4);

    private final Map<String, ScheduledFuture<?>> periodicTasks = new ConcurrentHashMap<>();
    private volatile String infoField = "";

    @Inject
    TaskUpdateEvents events;

    public void submitTask(Task task, String type) {
        switch (type) {
            case "IMMEDIATE" -> executor.submit(task);
            case "DELAYED" -> scheduler.schedule(task, 3, TimeUnit.SECONDS);
            case "PERIODIC" -> {
                ScheduledFuture<?> fut = scheduler.scheduleAtFixedRate(task, 0, 8, TimeUnit.SECONDS);
                periodicTasks.put(task.getName(), fut);
            }
        }
    }

    public void cancelPeriodicTask(String name) {
        if (periodicTasks.containsKey(name)) {
            log.infof("[TaskService] Cancelling task %s", name);
            periodicTasks.get(name).cancel(true);
            periodicTasks.remove(name);
            events.fire("tasklist");
        }
    }

    @POST
    @Consumes({ MediaType.TEXT_HTML, MediaType.TEXT_PLAIN })
    public void addToInfoField(String msg) {
        infoField = msg + "\n" + infoField;
        log.infof("[TaskService] Added message %s", msg);
        events.fire("infobox");
    }

    public String getInfoField() {
        return infoField;
    }

    public void clearInfoField() {
        infoField = "";
    }

    public Set<String> getPeriodicTasks() {
        return periodicTasks.keySet();
    }

    void shutdown() {
        periodicTasks.values().forEach(f -> f.cancel(true));
        executor.shutdownNow();
        scheduler.shutdownNow();
    }
}
