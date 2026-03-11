package quarkus.tutorial.roster.request;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import quarkus.tutorial.roster.entity.League;
import quarkus.tutorial.roster.entity.League_;
import quarkus.tutorial.roster.entity.Player;
import quarkus.tutorial.roster.entity.Player_;
import quarkus.tutorial.roster.entity.SummerLeague;
import quarkus.tutorial.roster.entity.Team;
import quarkus.tutorial.roster.entity.Team_;
import quarkus.tutorial.roster.entity.WinterLeague;
import quarkus.tutorial.roster.util.IncorrectSportException;
import quarkus.tutorial.roster.util.LeagueDetails;
import quarkus.tutorial.roster.util.PlayerDetails;
import quarkus.tutorial.roster.util.TeamDetails;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import jakarta.transaction.Transactional;

@ApplicationScoped
@Path("/roster")
public class RequestBean implements Request, Serializable {
    private static final Logger logger = Logger.getLogger("quarkus.tutorial.roster.request.RequestBean");

    @Inject
    private EntityManager em;
    private CriteriaBuilder cb;

    @PostConstruct
    private void init() {
        cb = em.getCriteriaBuilder();
    }

    @Override
    @POST
    @Path("/player")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public void createPlayer(
            @FormParam("id") String id,
            @FormParam("name") String name,
            @FormParam("position") String position,
            @FormParam("salary") double salary) {
        logger.info("createPlayer");
        try {
            Player player = new Player(id, name, position, salary);
            em.persist(player);
            em.flush();
        } catch (Exception ex) {
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @POST
    @Path("/player/{playerId}/team/{teamId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public void addPlayer(@PathParam("playerId") String playerId, @PathParam("teamId") String teamId) {
        logger.info("addPlayer");
        try {
            Player player = em.find(Player.class, playerId);
            Team team = em.find(Team.class, teamId);
            if (player == null || team == null) {
                throw new WebApplicationException("Player or Team not found", Response.Status.NOT_FOUND);
            }
            team.addPlayer(player);
            player.addTeam(team);
            em.merge(player);
            em.merge(team);
            em.flush();
        } catch (Exception ex) {
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @DELETE
    @Path("/player/{playerId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public void removePlayer(@PathParam("playerId") String playerId) {
        logger.info("removePlayer");
        try {
            Player player = em.find(Player.class, playerId);
            if (player == null) {
                throw new WebApplicationException("Player not found", Response.Status.NOT_FOUND);
            }
            Collection<Team> teams = player.getTeams();
            for (Team team : teams) {
                team.dropPlayer(player);
            }
            em.remove(player);
            em.flush();
        } catch (Exception ex) {
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @DELETE
    @Path("/player/{playerId}/team/{teamId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public void dropPlayer(@PathParam("playerId") String playerId, @PathParam("teamId") String teamId) {
        logger.info("dropPlayer");
        try {
            Player player = em.find(Player.class, playerId);
            Team team = em.find(Team.class, teamId);
            if (player == null || team == null) {
                throw new WebApplicationException("Player or Team not found", Response.Status.NOT_FOUND);
            }
            team.dropPlayer(player);
            player.dropTeam(team);
            em.merge(player);
            em.merge(team);
            em.flush();
        } catch (Exception ex) {
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @GET
    @Path("/player/{playerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public PlayerDetails getPlayer(@PathParam("playerId") String playerId) {
        logger.info("getPlayerDetails");
        try {
            Player player = em.find(Player.class, playerId);
            if (player == null) {
                throw new WebApplicationException("Player not found", Response.Status.NOT_FOUND);
            }
            return new PlayerDetails(player.getId(), player.getName(), player.getPosition(), player.getSalary());
        } catch (Exception ex) {
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @GET
    @Path("/team/{teamId}/players")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PlayerDetails> getPlayersOfTeam(@PathParam("teamId") String teamId) {
        logger.info("getPlayersOfTeam");
        try {
            Team team = em.find(Team.class, teamId);
            if (team == null) {
                throw new WebApplicationException("Team not found", Response.Status.NOT_FOUND);
            }
            return copyPlayersToDetails((List<Player>) team.getPlayers());
        } catch (Exception ex) {
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @GET
    @Path("/league/{leagueId}/teams")
    @Produces(MediaType.APPLICATION_JSON)
    public List<TeamDetails> getTeamsOfLeague(@PathParam("leagueId") String leagueId) {
        logger.info("getTeamsOfLeague");
        List<TeamDetails> detailsList = new ArrayList<>();
        try {
            League league = em.find(League.class, leagueId);
            if (league == null) {
                throw new WebApplicationException("League not found", Response.Status.NOT_FOUND);
            }
            for (Team team : league.getTeams()) {
                detailsList.add(new TeamDetails(team.getId(), team.getName(), team.getCity()));
            }
            return detailsList;
        } catch (Exception ex) {
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @GET
    @Path("/players/position/{position}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PlayerDetails> getPlayersByPosition(@PathParam("position") String position) {
        logger.info("getPlayersByPosition");
        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            Root<Player> player = cq.from(Player.class);
            cq.where(cb.equal(player.get(Player_.position), position));
            cq.select(player);
            TypedQuery<Player> q = em.createQuery(cq);
            List<Player> players = q.getResultList();
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @GET
    @Path("/players/salary/higher/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PlayerDetails> getPlayersByHigherSalary(@PathParam("name") String name) {
        logger.info("getPlayersByHigherSalary");
        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            Root<Player> player1 = cq.from(Player.class);
            Root<Player> player2 = cq.from(Player.class);
            Predicate gtPredicate = cb.greaterThan(player1.get(Player_.salary), player2.get(Player_.salary));
            Predicate equalPredicate = cb.equal(player2.get(Player_.name), name);
            cq.where(gtPredicate, equalPredicate);
            cq.select(player1).distinct(true);
            TypedQuery<Player> q = em.createQuery(cq);
            List<Player> players = q.getResultList();
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @GET
    @Path("/players/salary/range")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PlayerDetails> getPlayersBySalaryRange(@QueryParam("low") double low, @QueryParam("high") double high) {
        logger.info("getPlayersBySalaryRange");
        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            Root<Player> player = cq.from(Player.class);
            cq.where(cb.between(player.get(Player_.salary), low, high));
            cq.select(player).distinct(true);
            TypedQuery<Player> q = em.createQuery(cq);
            List<Player> players = q.getResultList();
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @GET
    @Path("/players/league/{leagueId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public List<PlayerDetails> getPlayersByLeagueId(@PathParam("leagueId") String leagueId) {
        logger.info("getPlayersByLeagueId");
        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            Root<Player> player = cq.from(Player.class);
            Join<Player, Team> team = player.join(Player_.teams);
            Join<Team, League> league = team.join(Team_.league);
            cq.where(cb.equal(league.get(League_.id), leagueId));
            cq.select(player).distinct(true);
            TypedQuery<Player> q = em.createQuery(cq);
            List<Player> players = q.getResultList();
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @GET
    @Path("/players/sport/{sport}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PlayerDetails> getPlayersBySport(@PathParam("sport") String sport) {
        logger.info("getPlayersBySport");
        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            Root<Player> player = cq.from(Player.class);
            Join<Player, Team> team = player.join(Player_.teams);
            Join<Team, League> league = team.join(Team_.league);
            cq.where(cb.equal(league.get(League_.sport), sport));
            cq.select(player).distinct(true);
            TypedQuery<Player> q = em.createQuery(cq);
            List<Player> players = q.getResultList();
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @GET
    @Path("/players/city/{city}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PlayerDetails> getPlayersByCity(@PathParam("city") String city) {
        logger.info("getPlayersByCity");
        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            Root<Player> player = cq.from(Player.class);
            Join<Player, Team> team = player.join(Player_.teams);
            cq.where(cb.equal(team.get(Team_.city), city));
            cq.select(player).distinct(true);
            TypedQuery<Player> q = em.createQuery(cq);
            List<Player> players = q.getResultList();
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @GET
    @Path("/players")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PlayerDetails> getAllPlayers() {
        logger.info("getAllPlayers");
        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            Root<Player> player = cq.from(Player.class);
            cq.select(player);
            TypedQuery<Player> q = em.createQuery(cq);
            List<Player> players = q.getResultList();
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @GET
    @Path("/players/not-on-team")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PlayerDetails> getPlayersNotOnTeam() {
        logger.info("getPlayersNotOnTeam");
        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            Root<Player> player = cq.from(Player.class);
            cq.where(cb.isEmpty(player.get(Player_.teams)));
            cq.select(player).distinct(true);
            TypedQuery<Player> q = em.createQuery(cq);
            List<Player> players = q.getResultList();
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @GET
    @Path("/players/position/{position}/name/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PlayerDetails> getPlayersByPositionAndName(
            @PathParam("position") String position,
            @PathParam("name") String name) {
        logger.info("getPlayersByPositionAndName");
        try {
            CriteriaQuery<Player> cq = cb.createQuery(Player.class);
            Root<Player> player = cq.from(Player.class);
            cq.where(cb.equal(player.get(Player_.position), position),
                    cb.equal(player.get(Player_.name), name));
            cq.select(player).distinct(true);
            TypedQuery<Player> q = em.createQuery(cq);
            List<Player> players = q.getResultList();
            return copyPlayersToDetails(players);
        } catch (Exception ex) {
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @GET
    @Path("/player/{playerId}/leagues")
    @Produces(MediaType.APPLICATION_JSON)
    public List<LeagueDetails> getLeaguesOfPlayer(@PathParam("playerId") String playerId) {
        logger.info("getLeaguesOfPlayer");
        List<LeagueDetails> detailsList = new ArrayList<>();
        try {
            CriteriaQuery<League> cq = cb.createQuery(League.class);
            Root<League> leagueRoot = cq.from(League.class);
            Join<League, Team> team = leagueRoot.join(League_.teams);
            Join<Team, Player> player = team.join(Team_.players);
            cq.where(cb.equal(player.get(Player_.id), playerId));
            cq.select(leagueRoot).distinct(true);
            TypedQuery<League> q = em.createQuery(cq);
            List<League> leagues = q.getResultList();
            if (leagues == null || leagues.isEmpty()) {
                logger.warning("No leagues found for player with ID " + playerId);
                return null;
            }
            for (League currentLeague : leagues) {
                detailsList.add(new LeagueDetails(currentLeague.getId(), currentLeague.getName(), currentLeague.getSport()));
            }
            return detailsList;
        } catch (Exception ex) {
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @GET
    @Path("/player/{playerId}/sports")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getSportsOfPlayer(@PathParam("playerId") String playerId) {
        logger.info("getSportsOfPlayer");
        try {
            CriteriaQuery<String> cq = cb.createQuery(String.class);
            Root<Player> player = cq.from(Player.class);
            Join<Player, Team> team = player.join(Player_.teams);
            Join<Team, League> league = team.join(Team_.league);
            cq.where(cb.equal(player.get(Player_.id), playerId));
            cq.select(league.get(League_.sport)).distinct(true);
            TypedQuery<String> q = em.createQuery(cq);
            return q.getResultList();
        } catch (Exception ex) {
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @POST
    @Path("/team/league/{leagueId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public void createTeamInLeague(TeamDetails teamDetails, @PathParam("leagueId") String leagueId) {
        logger.info("createTeamInLeague");
        try {
            League league = em.find(League.class, leagueId);
            if (league == null) {
                throw new WebApplicationException("League not found", Response.Status.NOT_FOUND);
            }
            Team team = new Team(teamDetails.getId(), teamDetails.getName(), teamDetails.getCity());
            em.persist(team);
            team.setLeague(league);
            league.addTeam(team);
            em.merge(league);
            em.flush();
        } catch (Exception ex) {
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @DELETE
    @Path("/team/{teamId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public void removeTeam(@PathParam("teamId") String teamId) {
        logger.info("removeTeam");
        try {
            Team team = em.find(Team.class, teamId);
            if (team == null) {
                throw new WebApplicationException("Team not found", Response.Status.NOT_FOUND);
            }
            for (Player player : team.getPlayers()) {
                player.dropTeam(team);
                em.merge(player);
            }
            em.remove(team);
            em.flush();
        } catch (Exception ex) {
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @GET
    @Path("/team/{teamId}")
    @Produces(MediaType.APPLICATION_JSON)
    public TeamDetails getTeam(@PathParam("teamId") String teamId) {
        logger.info("getTeam");
        try {
            Team team = em.find(Team.class, teamId);
            if (team == null) {
                throw new WebApplicationException("Team not found", Response.Status.NOT_FOUND);
            }
            return new TeamDetails(team.getId(), team.getName(), team.getCity());
        } catch (Exception ex) {
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @POST
    @Path("/league")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public void createLeague(LeagueDetails leagueDetails) {
        logger.info("createLeague");
        try {
            String sport = leagueDetails.getSport().toLowerCase();
            if (sport.equals("soccer") || sport.equals("swimming") || sport.equals("basketball") || sport.equals("baseball")) {
                SummerLeague league = new SummerLeague(leagueDetails.getId(), leagueDetails.getName(), leagueDetails.getSport());
                em.persist(league);
                em.flush();
            } else if (sport.equals("hockey") || sport.equals("skiing") || sport.equals("snowboarding")) {
                WinterLeague league = new WinterLeague(leagueDetails.getId(), leagueDetails.getName(), leagueDetails.getSport());
                em.persist(league);
                em.flush();
            } else {
                throw new IncorrectSportException("The specified sport is not valid.");
            }
        } catch (Exception ex) {
            throw new WebApplicationException(ex, Response.Status.BAD_REQUEST);
        }
    }

    @Override
    @DELETE
    @Path("/league/{leagueId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    public void removeLeague(@PathParam("leagueId") String leagueId) {
        logger.info("removeLeague");
        try {
            League league = em.find(League.class, leagueId);
            if (league == null) {
                throw new WebApplicationException("League not found", Response.Status.NOT_FOUND);
            }
            em.remove(league);
            em.flush();
        } catch (Exception ex) {
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    @GET
    @Path("/league/{leagueId}")
    @Produces(MediaType.APPLICATION_JSON)
    public LeagueDetails getLeague(@PathParam("leagueId") String leagueId) {
        logger.info("getLeague");
        try {
            League league = em.find(League.class, leagueId);
            if (league == null) {
                throw new WebApplicationException("League not found", Response.Status.NOT_FOUND);
            }
            return new LeagueDetails(league.getId(), league.getName(), league.getSport());
        } catch (Exception ex) {
            throw new WebApplicationException(ex, Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

    private List<PlayerDetails> copyPlayersToDetails(List<Player> players) {
        List<PlayerDetails> detailsList = new ArrayList<>();
        for (Player player : players) {
            detailsList.add(new PlayerDetails(player.getId(), player.getName(), player.getPosition(), player.getSalary()));
        }
        return detailsList;
    }
}