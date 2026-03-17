package jakarta.tutorial.taskcreator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Task implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(Task.class);

    private final TaskRestPoster restPoster;
    private final String name;
    private final String type;
    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    private int counter = 1;

    public Task(TaskRestPoster restPoster) {
        this.restPoster = restPoster;
        this.name = "unknown";
        this.type = "IMMEDIATE";
    }

    public Task(TaskRestPoster restPoster, String name, String type) {
        this.restPoster = restPoster;
        this.name = name;
        this.type = type;
    }

    @Override
    public void run() {
        if ("PERIODIC".equals(type)) send("started run #" + counter);
        else                         send("started");
        try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        if ("PERIODIC".equals(type)) send("finished run #" + (counter++));
        else                         send("finished");
    }

    private void send(String details) {
        String time = dateFormat.format(Calendar.getInstance().getTime());
        String msg = time + " - " + type + " Task " + name + " " + details;
        try { restPoster.post(msg); } catch (Exception e) { log.error("Failed posting task update: {}", msg, e); }
    }

    public String getName() { return name; }
}