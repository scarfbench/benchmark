package quarkus.tutorial.roster.request;

import quarkus.tutorial.roster.util.LeagueDetails;
import quarkus.tutorial.roster.util.TeamDetails;
import quarkus.tutorial.roster.util.PlayerDetails;

import java.util.List;

public interface Request {
    void createPlayer(String id, String name, String position, double salary);
    void addPlayer(String playerId, String teamId);
    void removePlayer(String playerId);
    void dropPlayer(String playerId, String teamId);
    PlayerDetails getPlayer(String playerId);
    List<PlayerDetails> getPlayersOfTeam(String teamId);
    List<TeamDetails> getTeamsOfLeague(String leagueId);
    List<PlayerDetails> getPlayersByPosition(String position);
    List<PlayerDetails> getPlayersByHigherSalary(String name);
    List<PlayerDetails> getPlayersBySalaryRange(double low, double high);
    List<PlayerDetails> getPlayersByLeagueId(String leagueId);
    List<PlayerDetails> getPlayersBySport(String sport);
    List<PlayerDetails> getPlayersByCity(String city);
    List<PlayerDetails> getAllPlayers();
    List<PlayerDetails> getPlayersNotOnTeam();
    List<PlayerDetails> getPlayersByPositionAndName(String position, String name);
    List<LeagueDetails> getLeaguesOfPlayer(String playerId);
    List<String> getSportsOfPlayer(String playerId);
    void createTeamInLeague(TeamDetails teamDetails, String leagueId);
    void removeTeam(String teamId);
    TeamDetails getTeam(String teamId);
    void createLeague(LeagueDetails leagueDetails);
    void removeLeague(String leagueId);
    LeagueDetails getLeague(String leagueId);
}