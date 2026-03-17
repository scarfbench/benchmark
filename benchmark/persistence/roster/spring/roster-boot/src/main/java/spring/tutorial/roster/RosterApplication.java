package spring.tutorial.roster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("jakartaee.tutorial.roster.entity")
@EnableJpaRepositories("spring.tutorial.roster.repository")
public class RosterApplication {
  public static void main(String[] args) {
    SpringApplication.run(RosterApplication.class, args);
  }
}