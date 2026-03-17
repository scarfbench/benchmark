package jakarta.tutorial.taskcreator;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class TaskUpdateEvents {
    private final ApplicationEventPublisher publisher;
    public TaskUpdateEvents(ApplicationEventPublisher publisher) { this.publisher = publisher; }
    public void fire(String name) { publisher.publishEvent(name); }
}