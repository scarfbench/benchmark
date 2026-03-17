package spring.tutorial.rsvp.ejb;

import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import spring.tutorial.rsvp.util.ResponseEnum;
import org.springframework.http.MediaType;
import spring.tutorial.rsvp.entity.Response;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/webapi/{eventId}/{inviteId}")
public class ResponseController {

    @PersistenceContext
    private EntityManager em;

    private static final Logger logger = Logger.getLogger(ResponseController.class.getName());

    @GetMapping(produces = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public Response getResponse(@PathVariable Long eventId,
                                @PathVariable Long inviteId) {
        return (Response) em.createNamedQuery("rsvp.entity.Response.findResponseByEventAndPerson")
                .setParameter("eventId", eventId)
                .setParameter("personId", inviteId)
                .getSingleResult();
    }

    @PostMapping(consumes = {MediaType.APPLICATION_XML_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @Transactional
    public void putResponse(@RequestBody String userResponse,
                            @PathVariable Long eventId,
                            @PathVariable Long inviteId) {

        logger.log(Level.INFO,
                "Updating status to {0} for person ID {1} for event ID {2}",
                new Object[]{userResponse, inviteId, eventId});

        Response response = (Response) em.createNamedQuery("rsvp.entity.Response.findResponseByEventAndPerson")
                .setParameter("eventId", eventId)
                .setParameter("personId", inviteId)
                .getSingleResult();

        if (userResponse.equals(ResponseEnum.ATTENDING.getLabel())
                && response.getResponse() != ResponseEnum.ATTENDING) {
            response.setResponse(ResponseEnum.ATTENDING);
            em.merge(response);

        } else if (userResponse.equals(ResponseEnum.NOT_ATTENDING.getLabel())
                && response.getResponse() != ResponseEnum.NOT_ATTENDING) {
            response.setResponse(ResponseEnum.NOT_ATTENDING);
            em.merge(response);

        } else if (userResponse.equals(ResponseEnum.MAYBE_ATTENDING.getLabel())
                && response.getResponse() != ResponseEnum.MAYBE_ATTENDING) {
            response.setResponse(ResponseEnum.MAYBE_ATTENDING);
            em.merge(response);
        }
    }
}
