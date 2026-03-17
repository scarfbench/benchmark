package jakarta.tutorial.taskcreator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class TaskRestPoster {
    private static final Logger log = LoggerFactory.getLogger(TaskRestPoster.class);
    private final RestClient client;

    public TaskRestPoster(RestClient client) {
        this.client = client;
    }

    public void post(String msg) {
        try {
            var status = client.post()
                    .uri("/taskinfo")
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(msg)
                    .retrieve()
                    .toBodilessEntity()
                    .getStatusCode()
                    .value();
            if (status >= 300) {
                log.warn("Non-success response posting task update: {}", status);
            }
        } catch (RestClientResponseException e) {
            log.error("Failed posting task update ({}): {}", e.getRawStatusCode(), e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Failed posting task update", e);
        }
    }
}