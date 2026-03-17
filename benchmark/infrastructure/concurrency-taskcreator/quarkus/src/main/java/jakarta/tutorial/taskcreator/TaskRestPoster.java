package jakarta.tutorial.taskcreator;

import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/** Simple REST poster to avoid bringing in heavy client. */
@ApplicationScoped
public class TaskRestPoster {
    private static final Logger log = Logger.getLogger(TaskRestPoster.class);

    public void post(String msg) throws IOException {
        URL url = new URL("http://localhost:8080/taskinfo");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        conn.setRequestProperty("Content-Type", "text/html; charset=utf-8");
        byte[] bytes = msg.getBytes(StandardCharsets.UTF_8);
        conn.setFixedLengthStreamingMode(bytes.length);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(bytes);
        }
        int code = conn.getResponseCode();
        if (code >= 300) {
            log.warnf("Non-success response posting task update: %d", code);
        }
        conn.disconnect();
    }
}
