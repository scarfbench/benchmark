package spring.tutorial.rsvp.web;

import spring.tutorial.rsvp.entity.Event;
import spring.tutorial.rsvp.entity.Response;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
@Scope("session") 
public class EventManager implements Serializable {

    private static final long serialVersionUID = -3240069895629955984L;
    private static final Logger logger = Logger.getLogger(EventManager.class.getName());

    protected Event currentEvent;
    private Response currentResponse;
    private RestClient client;
    private final String baseUri = "http://localhost:8080/webapi/status/";

    public EventManager() {
        // Default constructor
    }

    @PostConstruct
    private void init() {
        this.client = RestClient.builder()
                .baseUrl(baseUri)
                .build();
    }

    
    @PreDestroy
    private void clean() {
        // RestClient doesn't need explicit cleanup
    }

    public Event getCurrentEvent() {
        return currentEvent;
    }

    public void setCurrentEvent(Event currentEvent) {
        this.currentEvent = currentEvent;
    }

    public Response getCurrentResponse() {
        return currentResponse;
    }

    public void setCurrentResponse(Response currentResponse) {
        this.currentResponse = currentResponse;
    }

    public List<Response> retrieveEventResponses() {
        if (this.currentEvent == null) {
            logger.log(Level.WARNING, "current event is null");
            return null;
        }

        logger.log(Level.INFO, "getting responses for {0}", this.currentEvent.getName());
        try {
            Event event = client.get()
                    .uri("/{id}", this.currentEvent.getId().toString())
                    .accept(MediaType.APPLICATION_XML)
                    .retrieve()
                    .body(Event.class);
            if (event == null) {
                logger.log(Level.WARNING, "returned event is null");
                return null;
            } else {
                return event.getResponses();
            }
        } catch (Exception ex) {
            logger.log(Level.WARNING, "an error occurred when getting event responses.");
            return null;
        }
    }
 
    public String retrieveEventStatus(Event event) {
        this.setCurrentEvent(event);
        return "eventStatus";
    }

    public String viewResponse(Response response) {
        this.currentResponse = response;
        return "viewResponse";
    }
}
