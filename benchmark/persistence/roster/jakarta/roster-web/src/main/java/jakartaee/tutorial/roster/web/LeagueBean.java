package jakartaee.tutorial.roster.web;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import jakarta.tutorial.roster.request.RequestBean;
import jakarta.tutorial.roster.util.LeagueDetails;
import jakarta.tutorial.roster.util.TeamDetails;

@Named
@SessionScoped
public class LeagueBean implements Serializable {

    @EJB
    private RequestBean requestBean;

    private List<LeagueDetails> leagues;
    private List<TeamDetails> teams;

    private String newLeagueId;
    private String newLeagueName;
    private String newLeagueSport;

    @PostConstruct
    public void init() {
        loadData();
    }

    public void loadData() {
        loadLeagues();
        loadTeams();
    }

    private void loadLeagues() {
        try {
            leagues = requestBean.getAllLeagues();
            if (leagues == null) {
                leagues = new ArrayList<>();
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error loading leagues", e.getMessage()));
            leagues = new ArrayList<>();
        }
    }

    private void loadTeams() {
        try {
            teams = new ArrayList<>();
            // Get teams from all leagues
            for (LeagueDetails league : getLeagues()) {
                List<TeamDetails> leagueTeams = requestBean.getTeamsOfLeague(league.getId());
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

    public String createLeague() {
        try {
            if (newLeagueId == null || newLeagueId.isBlank()
                    || newLeagueName == null || newLeagueName.isBlank()
                    || newLeagueSport == null || newLeagueSport.isBlank()) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "All league fields are required."));
                return null;
            }

            LeagueDetails leagueDetails = new LeagueDetails(newLeagueId.trim(), newLeagueName.trim(),
                    newLeagueSport.trim());
            requestBean.createLeague(leagueDetails);

            newLeagueId = null;
            newLeagueName = null;
            newLeagueSport = null;

            clearCache();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "League created successfully."));
            return null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error creating league", e.getMessage()));
            return null;
        }
    }

    public void clearCache() {
        leagues = null;
        teams = null;
        loadData();
    }

    public List<String> getLeagueIds() {
        List<String> ids = new ArrayList<>();
        for (LeagueDetails league : getLeagues()) {
            ids.add(league.getId());
        }
        return ids;
    }

    // Getters

    public List<LeagueDetails> getLeagues() {
        if (leagues == null) {
            loadLeagues();
        }
        return leagues;
    }

    public List<TeamDetails> getTeams() {
        if (teams == null) {
            loadTeams();
        }
        return teams;
    }

    public String getNewLeagueId() {
        return newLeagueId;
    }

    public void setNewLeagueId(String newLeagueId) {
        this.newLeagueId = newLeagueId;
    }

    public String getNewLeagueName() {
        return newLeagueName;
    }

    public void setNewLeagueName(String newLeagueName) {
        this.newLeagueName = newLeagueName;
    }

    public String getNewLeagueSport() {
        return newLeagueSport;
    }

    public void setNewLeagueSport(String newLeagueSport) {
        this.newLeagueSport = newLeagueSport;
    }
}
