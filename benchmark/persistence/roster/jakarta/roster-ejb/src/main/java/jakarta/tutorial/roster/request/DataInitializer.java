package jakarta.tutorial.roster.request;

import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakartaee.tutorial.roster.entity.League;
import jakartaee.tutorial.roster.entity.SummerLeague;
import jakartaee.tutorial.roster.entity.WinterLeague;

@Singleton
@Startup
public class DataInitializer {

    private static final Logger logger = Logger.getLogger("roster.request.DataInitializer");

    @PersistenceContext
    private EntityManager em;

    @PostConstruct
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void init() {
        seedCanonicalLeagues();
    }

    private void seedCanonicalLeagues() {
        try {
            if (em.find(League.class, "L1") == null) {
                em.persist(new SummerLeague("L1", "Mountain", "Soccer"));
            }
            if (em.find(League.class, "L2") == null) {
                em.persist(new SummerLeague("L2", "Valley", "Basketball"));
            }
            if (em.find(League.class, "L3") == null) {
                em.persist(new SummerLeague("L3", "Foothills", "Soccer"));
            }
            if (em.find(League.class, "L4") == null) {
                em.persist(new WinterLeague("L4", "Alpine", "Snowboarding"));
            }
            logger.info("Canonical leagues seeded successfully");
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Failed to seed canonical leagues", ex);
        }
    }
}
