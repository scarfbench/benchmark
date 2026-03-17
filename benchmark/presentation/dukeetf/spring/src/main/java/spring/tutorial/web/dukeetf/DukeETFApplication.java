package spring.tutorial.web.dukeetf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DukeETFApplication {
    public static void main(String[] args) {
        SpringApplication.run(DukeETFApplication.class, args);
    }
}
