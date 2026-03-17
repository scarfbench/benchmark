package spring.tutorial.web.websocketbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;
import java.util.concurrent.Executors;
import java.util.concurrent.Executor;

@SpringBootApplication
@EnableWebSocket
public class WebsocketBotApplication {
  public static void main(String[] args) {
    SpringApplication.run(WebsocketBotApplication.class, args);
  }

  @Bean
  public ServerEndpointExporter serverEndpointExporter() {
    return new ServerEndpointExporter();
  }

  @Bean
  public Executor websocketBotExecutor() {
    return Executors.newCachedThreadPool();
  }


}
