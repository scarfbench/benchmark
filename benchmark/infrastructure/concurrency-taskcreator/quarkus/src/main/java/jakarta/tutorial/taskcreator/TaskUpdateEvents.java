package jakarta.tutorial.taskcreator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;

/** Helper to decouple direct injection of Event<String>. */
@ApplicationScoped
public class TaskUpdateEvents {
    @Inject
    Event<String> event;

    public void fire(String name) {
        event.fire(name);
    }
}
