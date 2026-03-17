package jakarta.tutorial.concurrency.jobs.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class JobServiceClient {

    private final RestClient restClient;

    public JobServiceClient(@Value("${jobs.service.url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public ResponseEntity<String> processJob(int jobID, String apiKey) {
        return restClient.post()
                .uri("/JobService/process?jobID={jobID}", jobID)
                .header("X-REST-API-Key", apiKey)
                .retrieve()
                .toEntity(String.class);
    }
}