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
import jakarta.tutorial.roster.util.PlayerDetails;

@Named
@SessionScoped
public class PlayerBean implements Serializable {

    @EJB
    private RequestBean requestBean;

    private List<PlayerDetails> players;
    private String newPlayerId;
    private String newPlayerName;
    private String newPlayerPosition;
    private double newPlayerSalary;
    private String selectedTeamId;

    @PostConstruct
    public void init() {
        loadPlayers();
    }

    public void loadPlayers() {
        try {
            players = requestBean.getAllPlayers();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error loading players", e.getMessage()));
            players = new ArrayList<>();
        }
    }

    public String createPlayer() {
        try {
            // Generate a simple ID if not provided
            String playerId = (newPlayerId != null && !newPlayerId.isEmpty())
                    ? newPlayerId
                    : "P" + System.currentTimeMillis();

            // Create the player
            requestBean.createPlayer(playerId, newPlayerName, newPlayerPosition, newPlayerSalary);

            // Add player to team if one is selected
            if (selectedTeamId != null && !selectedTeamId.isEmpty()) {
                requestBean.addPlayer(playerId, selectedTeamId);
            }

            // Clear form
            newPlayerId = null;
            newPlayerName = null;
            newPlayerPosition = null;
            newPlayerSalary = 0.0;
            selectedTeamId = null;

            // Reload players
            loadPlayers();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Player created successfully"));

            return null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error creating player", e.getMessage()));
            return null;
        }
    }

    public String deletePlayer(String playerId) {
        try {
            requestBean.removePlayer(playerId);
            loadPlayers();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Player deleted successfully"));

            return null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error deleting player", e.getMessage()));
            return null;
        }
    }

    // Getters and setters

    public List<PlayerDetails> getPlayers() {
        if (players == null) {
            loadPlayers();
        }
        return players;
    }

    public void setPlayers(List<PlayerDetails> players) {
        this.players = players;
    }

    public String getNewPlayerId() {
        return newPlayerId;
    }

    public void setNewPlayerId(String newPlayerId) {
        this.newPlayerId = newPlayerId;
    }

    public String getNewPlayerName() {
        return newPlayerName;
    }

    public void setNewPlayerName(String newPlayerName) {
        this.newPlayerName = newPlayerName;
    }

    public String getNewPlayerPosition() {
        return newPlayerPosition;
    }

    public void setNewPlayerPosition(String newPlayerPosition) {
        this.newPlayerPosition = newPlayerPosition;
    }

    public double getNewPlayerSalary() {
        return newPlayerSalary;
    }

    public void setNewPlayerSalary(double newPlayerSalary) {
        this.newPlayerSalary = newPlayerSalary;
    }

    public String getSelectedTeamId() {
        return selectedTeamId;
    }

    public void setSelectedTeamId(String selectedTeamId) {
        this.selectedTeamId = selectedTeamId;
    }
}
