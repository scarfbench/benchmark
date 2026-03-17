package quarkus.tutorial.roster.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import quarkus.tutorial.roster.util.IncorrectSportException;

@Entity
@DiscriminatorValue("WINTER")
public class WinterLeague extends League {
    private static final long serialVersionUID = 1L;

    public WinterLeague() {
    }

    public WinterLeague(String id, String name, String sport) throws IncorrectSportException {
        super(id, name, sport);
        if (!sport.equalsIgnoreCase("hockey") && !sport.equalsIgnoreCase("skiing") &&
            !sport.equalsIgnoreCase("snowboarding")) {
            throw new IncorrectSportException("Sport is not a winter sport.");
        }
    }
}