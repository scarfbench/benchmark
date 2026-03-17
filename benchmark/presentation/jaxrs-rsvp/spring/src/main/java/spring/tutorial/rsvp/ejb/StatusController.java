package spring.tutorial.rsvp.ejb;

import java.util.List;
import java.util.logging.Logger;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import spring.tutorial.rsvp.entity.Event;

@RestController
@RequestMapping("/webapi/status")
public class StatusController {

    private static final Logger logger =
            Logger.getLogger(StatusController.class.getName());

    @PersistenceContext
    private EntityManager em;

    private List<Event> allCurrentEvents;

    @GetMapping(value = "/{eventId}", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public Event getEvent(@PathVariable Long eventId) {
        return em.find(Event.class, eventId);
    }

    @GetMapping(value = "/all", produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public List<Event> getAllCurrentEvents() {
        logger.info("Calling getAllCurrentEvents");
        allCurrentEvents = em.createNamedQuery("rsvp.entity.Event.getAllUpcomingEvents", Event.class)
                             .getResultList();
        if (allCurrentEvents == null || allCurrentEvents.isEmpty()) {
            logger.warning("No current events!");
        }
        return allCurrentEvents;
    }

    public void setAllCurrentEvents(List<Event> events) {
        this.allCurrentEvents = events;
    }
}
