package jakarta.tutorial.taskcreator.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final WebSocketHandler infoHandler;
    public WebSocketConfig(WebSocketHandler infoHandler) { this.infoHandler = infoHandler; }
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(infoHandler, "/wsinfo").setAllowedOrigins("*"); // adjust CORS as needed
    }
}