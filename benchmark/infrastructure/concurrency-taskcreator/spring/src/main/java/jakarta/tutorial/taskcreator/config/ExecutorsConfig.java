package jakarta.tutorial.taskcreator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
public class ExecutorsConfig {

    @Bean
    @Qualifier("taskExecutor")
    public ExecutorService taskExecutor() {
        return Executors.newFixedThreadPool(4);
    }

    @Bean
    @Qualifier("taskScheduler")
    public ScheduledExecutorService taskScheduler() {
        return Executors.newScheduledThreadPool(4);
    }
}