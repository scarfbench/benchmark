package quarkus.tutorial.roster.entity;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "LEAGUE_TYPE")
public abstract class League implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    protected String id;
    protected String name;
    protected String sport;
    @OneToMany(mappedBy = "league")
    protected Collection<Team> teams = new ArrayList<>();

    public League() {
    }

    public League(String id, String name, String sport) {
        this.id = id;
        this.name = name;
        this.sport = sport;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSport() {
        return sport;
    }

    public void setSport(String sport) {
        this.sport = sport;
    }

    public Collection<Team> getTeams() {
        return teams;
    }

    public void setTeams(Collection<Team> teams) {
        this.teams = teams;
    }

    public void addTeam(Team team) {
        this.teams.add(team);
    }
}