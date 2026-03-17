package spring.tutorial.roster.request;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;
import spring.tutorial.roster.repository.LeagueRepository;
import spring.tutorial.roster.repository.PlayerRepository;
import spring.tutorial.roster.repository.TeamRepository;
import spring.tutorial.roster.util.IncorrectSportException;
import spring.tutorial.roster.util.LeagueDetails;
import spring.tutorial.roster.util.PlayerDetails;
import spring.tutorial.roster.util.TeamDetails;
import jakartaee.tutorial.roster.entity.League;
import jakartaee.tutorial.roster.entity.Player;
import jakartaee.tutorial.roster.entity.SummerLeague;
import jakartaee.tutorial.roster.entity.Team;
import jakartaee.tutorial.roster.entity.WinterLeague;

@Service("requestBean")
@Transactional
public class RequestBean implements Request, Serializable {
  private final PlayerRepository playerRepository;
  private final TeamRepository teamRepository;
  private final LeagueRepository leagueRepository;

  public RequestBean(PlayerRepository playerRepository, TeamRepository teamRepository, LeagueRepository leagueRepository) {
    this.playerRepository = playerRepository;
    this.teamRepository = teamRepository;
    this.leagueRepository = leagueRepository;
  }

  @PostConstruct
  private void init() {
  }

  @Override
  public void createPlayer(String id, String name, String position, double salary) {
    Player p = new Player(id, name, position, salary);
    playerRepository.save(p);
  }

  @Override
  public void addPlayer(String playerId, String teamId) {
    Player p = playerRepository.findById(playerId).orElse(null);
    Team t = teamRepository.findById(teamId).orElse(null);
    if (p == null || t == null) return;
    t.addPlayer(p);
    teamRepository.save(t);
  }

  @Override
  public void removePlayer(String playerId) {
    playerRepository.findById(playerId).ifPresent(p -> {
      List<Team> teams = new ArrayList<>(Optional.ofNullable(p.getTeams()).orElse(List.of()));
      for (Team t : teams) {
        t.dropPlayer(p);
      }
      teamRepository.saveAll(teams);
      playerRepository.deleteById(playerId);
    });
  }

  @Override
  public void dropPlayer(String playerId, String teamId) {
    Player p = playerRepository.findById(playerId).orElse(null);
    Team t = teamRepository.findById(teamId).orElse(null);
    if (p == null || t == null) return;
    t.dropPlayer(p);
    teamRepository.save(t);
  }

  @Override
  public PlayerDetails getPlayer(String playerId) {
    return playerRepository.findById(playerId).map(this::toPlayerDetails).orElse(null);
  }

  @Override
  public List<PlayerDetails> getPlayersOfTeam(String teamId) {
    return playerRepository.findByTeams_Id(teamId).stream().map(this::toPlayerDetails).toList();
  }

  @Override
  public List<TeamDetails> getTeamsOfLeague(String leagueId) {
    return teamRepository.findByLeague_Id(leagueId).stream().map(this::toTeamDetails).toList();
  }

  @Override
  public List<PlayerDetails> getPlayersByPosition(String position) {
    return playerRepository.findByPosition(position).stream().map(this::toPlayerDetails).toList();
  }

  @Override
  public List<PlayerDetails> getPlayersByHigherSalary(String name) {
    Optional<Player> base = playerRepository.findFirstByName(name);
    if (base.isEmpty()) return List.of();
    return playerRepository.findBySalaryGreaterThan(base.get().getSalary()).stream().map(this::toPlayerDetails).toList();
  }

  @Override
  public List<PlayerDetails> getPlayersBySalaryRange(double low, double high) {
    return playerRepository.findBySalaryBetween(low, high).stream().map(this::toPlayerDetails).toList();
  }

  @Override
  public List<PlayerDetails> getPlayersByLeagueId(String leagueId) {
    return playerRepository.findDistinctByTeams_League_Id(leagueId).stream().map(this::toPlayerDetails).toList();
  }

  @Override
  public List<PlayerDetails> getPlayersBySport(String sport) {
    return playerRepository.findDistinctByTeams_League_Sport(sport).stream().map(this::toPlayerDetails).toList();
  }

  @Override
  public List<PlayerDetails> getPlayersByCity(String city) {
    return playerRepository.findDistinctByTeams_City(city).stream().map(this::toPlayerDetails).toList();
  }

  @Override
  public List<PlayerDetails> getAllPlayers() {
    return playerRepository.findAll().stream().sorted(Comparator.comparing(Player::getId)).map(this::toPlayerDetails).toList();
  }

  @Override
  public List<PlayerDetails> getPlayersNotOnTeam() {
    return playerRepository.findByTeamsIsEmpty().stream().map(this::toPlayerDetails).toList();
  }

  @Override
  public List<PlayerDetails> getPlayersByPositionAndName(String position, String name) {
    return playerRepository.findByPositionAndName(position, name).stream().map(this::toPlayerDetails).toList();
  }

  @Override
  public List<LeagueDetails> getLeaguesOfPlayer(String playerId) {
    return playerRepository.findById(playerId).map(p -> Optional.ofNullable(p.getTeams()).orElse(List.of()).stream()
        .map(Team::getLeague).filter(Objects::nonNull).collect(Collectors.toMap(League::getId, l -> l, (a, b) -> a)).values().stream()
        .map(this::toLeagueDetails).toList()).orElse(List.of());
  }

  @Override
  public List<String> getSportsOfPlayer(String playerId) {
    return playerRepository.findById(playerId).map(p -> Optional.ofNullable(p.getTeams()).orElse(List.of()).stream()
        .map(Team::getLeague).filter(Objects::nonNull).map(League::getSport).filter(Objects::nonNull)
        .collect(Collectors.toCollection(java.util.LinkedHashSet::new))).orElseGet(java.util.LinkedHashSet::new).stream().toList();
  }

  @Override
  public void createTeamInLeague(TeamDetails teamDetails, String leagueId) {
    League league = leagueRepository.findById(leagueId).orElse(null);
    if (league == null) return;
    Team t = new Team(teamDetails.getId(), teamDetails.getName(), teamDetails.getCity());
    t.setLeague(league);
    teamRepository.save(t);
  }

  @Override
  public void removeTeam(String teamId) {
    teamRepository.findById(teamId).ifPresent(t -> {
      List<Player> players = new ArrayList<>(Optional.ofNullable(t.getPlayers()).orElse(List.of()));
      for (Player p : players) {
        t.dropPlayer(p);
      }
      teamRepository.save(t);
      teamRepository.deleteById(teamId);
    });
  }

  @Override
  public TeamDetails getTeam(String teamId) {
    return teamRepository.findById(teamId).map(this::toTeamDetails).orElse(null);
  }

  @Override
  public void createLeague(LeagueDetails leagueDetails) {
    League l = instantiateLeague(leagueDetails);
    leagueRepository.save(l);
  }

  @Override
  public void removeLeague(String leagueId) {
    leagueRepository.deleteById(leagueId);
  }

  @Override
  public LeagueDetails getLeague(String leagueId) {
    return leagueRepository.findById(leagueId).map(this::toLeagueDetails).orElse(null);
  }

  private PlayerDetails toPlayerDetails(Player p) {
    return new PlayerDetails(p.getId(), p.getName(), p.getPosition(), p.getSalary());
  }

  private TeamDetails toTeamDetails(Team t) {
    return new TeamDetails(t.getId(), t.getName(), t.getCity());
  }

  private LeagueDetails toLeagueDetails(League l) {
    return new LeagueDetails(l.getId(), l.getName(), l.getSport());
  }

  private League instantiateLeague(LeagueDetails d) {
    try {
      String sport = d.getSport() == null ? "" : d.getSport().toLowerCase();
      if (sport.contains("ski") || sport.contains("hockey") || sport.contains("ice") || sport.contains("snow")) {
        return new WinterLeague(d.getId(), d.getName(), d.getSport());
      }
      return new SummerLeague(d.getId(), d.getName(), d.getSport());
    } catch (IncorrectSportException e) {
      throw new IllegalArgumentException(e);
    }
  }
}