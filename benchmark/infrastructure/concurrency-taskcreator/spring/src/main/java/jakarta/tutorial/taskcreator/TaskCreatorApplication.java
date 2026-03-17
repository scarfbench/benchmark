package jakarta.tutorial.taskcreator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TaskCreatorApplication {
    public static void main(String[] args) {
        SpringApplication.run(TaskCreatorApplication.class, args);
    }
}