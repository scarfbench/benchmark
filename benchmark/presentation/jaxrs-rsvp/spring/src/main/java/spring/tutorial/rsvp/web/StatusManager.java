package spring.tutorial.rsvp.web;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Scope;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

import spring.tutorial.rsvp.entity.Event;
import spring.tutorial.rsvp.entity.Person;
import spring.tutorial.rsvp.util.ResponseEnum;
import jakarta.annotation.PreDestroy;

@Component("statusManager")
@Scope("session")
public class StatusManager implements Serializable {
  private static final long serialVersionUID = 1L;
  private static final Logger logger = Logger.getLogger(StatusManager.class.getName());

  private Event event;
    private List<Event> events;
    private RestClient client;
    private final String baseUri = "http://localhost:8080/webapi";

    public StatusManager() {
        client = RestClient.builder()
                .baseUrl(baseUri)
                .build();
    }

    @PreDestroy
    private void clean() {
        // RestClient doesn't need explicit cleanup
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public String getEventStatus(Event event) {
        this.setEvent(event);
        return "eventStatus";
    }

    public List<Event> getEvents() {
        List<Event> returnedEvents = null;
        try {
            Event[] eventsArray = client.get()
                    .uri("/status/all")
                    .accept(MediaType.APPLICATION_XML)
                    .retrieve()
                    .body(Event[].class);
            if (eventsArray == null) {
                logger.log(Level.SEVERE, "Returned events null.");
            } else {
                returnedEvents = java.util.Arrays.asList(eventsArray);
                logger.log(Level.INFO, "Events have been returned: {0}", returnedEvents.size());
            }
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error retrieving all events.");
            logger.log(Level.SEVERE, "base URI is {0}", baseUri);
            logger.log(Level.SEVERE, "path is {0}", "all");
            logger.log(Level.SEVERE, "Exception is {0}", ex.getMessage());
        }
        return returnedEvents;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public ResponseEnum[] getStatusValues() {
        return ResponseEnum.values();
    }

    public String changeStatus(ResponseEnum userResponse, Person person, Event event) {
        String navigation;    
        try {
            logger.log(Level.INFO,
                    "changing status to {0} for {1} {2} for event ID {3}.",
                    new Object[]{userResponse,
                            person.getFirstName(),
                            person.getLastName(),
                            event.getId().toString()});
            client.post()
                   .uri("/{eventId}/{inviteId}", event.getId().toString(), person.getId().toString())
                   .contentType(MediaType.APPLICATION_XML)
                   .body(userResponse.getLabel())
                   .retrieve()
                   .toBodilessEntity();
            navigation = "changedStatus";
        } catch (Exception ex) {
            logger.log(Level.WARNING, "couldn't change status for {0} {1}",
                    new Object[]{person.getFirstName(), person.getLastName()});
            logger.log(Level.WARNING, ex.getMessage());
            navigation = "error";
        }
        return navigation;
    }
}
