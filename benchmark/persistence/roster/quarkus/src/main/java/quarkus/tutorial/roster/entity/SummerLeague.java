package quarkus.tutorial.roster.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import quarkus.tutorial.roster.util.IncorrectSportException;

@Entity
@DiscriminatorValue("SUMMER")
public class SummerLeague extends League {
    private static final long serialVersionUID = 1L;

    public SummerLeague() {
    }

    public SummerLeague(String id, String name, String sport) throws IncorrectSportException {
        super(id, name, sport);
        if (!sport.equalsIgnoreCase("soccer") && !sport.equalsIgnoreCase("swimming") &&
            !sport.equalsIgnoreCase("basketball") && !sport.equalsIgnoreCase("baseball")) {
            throw new IncorrectSportException("Sport is not a summer sport.");
        }
    }
}