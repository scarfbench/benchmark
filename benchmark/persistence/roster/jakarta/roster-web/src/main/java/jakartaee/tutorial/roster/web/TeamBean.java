package jakartaee.tutorial.roster.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.tutorial.roster.request.RequestBean;
import jakarta.tutorial.roster.util.TeamDetails;

@Named
@SessionScoped
public class TeamBean implements Serializable {

    @EJB
    private RequestBean requestBean;

    @Inject
    private LeagueBean leagueBean;

    private List<TeamDetails> teams;
    private String newTeamId;
    private String newTeamName;
    private String newTeamCity;
    private String selectedLeagueId;

    @PostConstruct
    public void init() {
        loadTeams();
    }

    public void loadTeams() {
        try {
            teams = new ArrayList<>();
            // Get teams from all leagues
            for (String leagueId : leagueBean.getLeagueIds()) {
                List<TeamDetails> leagueTeams = requestBean.getTeamsOfLeague(leagueId);
                if (leagueTeams != null) {
                    teams.addAll(leagueTeams);
                }
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error loading teams", e.getMessage()));
            teams = new ArrayList<>();
        }
    }

    public String createTeam() {
        try {
            TeamDetails teamDetails = new TeamDetails(newTeamId, newTeamName, newTeamCity);
            requestBean.createTeamInLeague(teamDetails, selectedLeagueId);

            // Clear form
            newTeamId = null;
            newTeamName = null;
            newTeamCity = null;
            selectedLeagueId = null;

            // Reload teams
            loadTeams();
            leagueBean.clearCache(); // Clear the cache to reload teams for dropdowns

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Team created successfully"));

            return null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error creating team", e.getMessage()));
            return null;
        }
    }

    // Getters and setters

    public List<TeamDetails> getTeams() {
        if (teams == null) {
            loadTeams();
        }
        return teams;
    }

    public void setTeams(List<TeamDetails> teams) {
        this.teams = teams;
    }

    public String getNewTeamId() {
        return newTeamId;
    }

    public void setNewTeamId(String newTeamId) {
        this.newTeamId = newTeamId;
    }

    public String getNewTeamName() {
        return newTeamName;
    }

    public void setNewTeamName(String newTeamName) {
        this.newTeamName = newTeamName;
    }

    public String getNewTeamCity() {
        return newTeamCity;
    }

    public void setNewTeamCity(String newTeamCity) {
        this.newTeamCity = newTeamCity;
    }

    public String getSelectedLeagueId() {
        return selectedLeagueId;
    }

    public void setSelectedLeagueId(String selectedLeagueId) {
        this.selectedLeagueId = selectedLeagueId;
    }
}
