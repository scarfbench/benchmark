package spring.tutorial.rsvp.config;

import java.util.Date;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import spring.tutorial.rsvp.entity.Event;
import spring.tutorial.rsvp.entity.Person;

/**
 * Seeds the in-memory database with two persons and one event (with invitees)
 * so that the smoke tests that look for event IDs and invitees can run rather
 * than self-skipping with "No valid events found".
 */
@Component
public class DataSeeder implements ApplicationRunner {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        TypedQuery<Long> countQuery = em.createQuery("SELECT COUNT(e) FROM Event e", Long.class);
        if (countQuery.getSingleResult() > 0) {
            return;
        }

        Person alice = new Person();
        alice.setFirstName("Alice");
        alice.setLastName("Smoke");
        em.persist(alice);

        Person bob = new Person();
        bob.setFirstName("Bob");
        bob.setLastName("Smoke");
        em.persist(bob);

        Event event = new Event();
        event.setName("Smoke Test Conference");
        event.setLocation("Online");
        event.setEventDate(new Date());
        event.setOwner(alice);
        event.getInvitees().add(bob);
        em.persist(event);
    }
}
