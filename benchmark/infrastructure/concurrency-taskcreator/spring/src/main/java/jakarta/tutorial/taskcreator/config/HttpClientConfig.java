package jakarta.tutorial.taskcreator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class HttpClientConfig {
    @Bean
    RestClient restClient() {
        return RestClient.builder()
                .baseUrl("http://localhost:9080") // matching my other jakarta and quarkus apps for thios benchmark
                .build();
    }
}