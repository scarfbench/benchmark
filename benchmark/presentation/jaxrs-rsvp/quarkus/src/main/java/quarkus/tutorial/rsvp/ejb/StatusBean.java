package quarkus.tutorial.rsvp.ejb;

import java.util.List;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.hibernate.Hibernate;

import quarkus.tutorial.rsvp.entity.Event;

@Path("/status")
@ApplicationScoped
@Transactional
@Produces(MediaType.APPLICATION_JSON)
public class StatusBean {

    private static final Logger logger =
        Logger.getLogger("quarkus.tutorial.rsvp.ejb.StatusBean");

    @Inject
    EntityManager em;

    @GET
    @Path("{eventId}/")
    public Event getEvent(@PathParam("eventId") Long eventId) {
        Event event = em.find(Event.class, eventId);
        if (event == null) {
            throw new NotFoundException("Event " + eventId + " not found");
        }
        Hibernate.initialize(event.getResponses());
        return event;
    }

    @GET
    @Path("all")
    public List<Event> getAllCurrentEvents() {
        logger.info("Calling getAllCurrentEvents");
        @SuppressWarnings("unchecked")
        List<Event> events = (List<Event>) em
            .createNamedQuery("rsvp.entity.Event.getAllUpcomingEvents")
            .getResultList();

        if (events == null || events.isEmpty()) {
            logger.warning("No current events!");
            return events;
        }

        events.forEach(e -> Hibernate.initialize(e.getResponses()));
        return events;
    }

    public void setAllCurrentEvents(List<Event> events) {
    }
}