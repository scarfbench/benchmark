package jakartaee.tutorial.roster.web;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBException;
import jakarta.tutorial.roster.request.Request;
import jakarta.tutorial.roster.util.IncorrectSportException;
import jakarta.tutorial.roster.util.LeagueDetails;
import jakarta.tutorial.roster.util.PlayerDetails;
import jakarta.tutorial.roster.util.TeamDetails;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class RosterResource {

    private static final Logger logger = Logger.getLogger("roster.web.RosterResource");

    @EJB
    private Request requestBean;

    // -----------------------------------------------------------------------
    // League endpoints
    // -----------------------------------------------------------------------

    @POST
    @Path("/league")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createLeague(LeagueDetails league) {
        try {
            requestBean.createLeague(league);
            return Response.ok().build();
        } catch (EJBException e) {
            if (hasIncorrectSportCause(e)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("{\"error\":\"Invalid sport: " + league.getSport() + "\"}")
                        .build();
            }
            throw e;
        }
    }

    @GET
    @Path("/league/{id}")
    public Response getLeague(@PathParam("id") String id) {
        LeagueDetails league = requestBean.getLeague(id);
        if (league == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(league).build();
    }

    @DELETE
    @Path("/league/{id}")
    public Response removeLeague(@PathParam("id") String id) {
        requestBean.removeLeague(id);
        return Response.ok().build();
    }

    @GET
    @Path("/league/{id}/teams")
    public List<TeamDetails> getTeamsOfLeague(@PathParam("id") String id) {
        return requestBean.getTeamsOfLeague(id);
    }

    // -----------------------------------------------------------------------
    // Team endpoints
    // -----------------------------------------------------------------------

    @POST
    @Path("/team/league/{leagueId}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createTeamInLeague(TeamDetails team, @PathParam("leagueId") String leagueId) {
        requestBean.createTeamInLeague(team, leagueId);
        return Response.ok().build();
    }

    @GET
    @Path("/team/{id}")
    public Response getTeam(@PathParam("id") String id) {
        TeamDetails team = requestBean.getTeam(id);
        if (team == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(team).build();
    }

    @DELETE
    @Path("/team/{id}")
    public Response removeTeam(@PathParam("id") String id) {
        requestBean.removeTeam(id);
        return Response.ok().build();
    }

    @GET
    @Path("/team/{id}/players")
    public List<PlayerDetails> getPlayersOfTeam(@PathParam("id") String id) {
        return requestBean.getPlayersOfTeam(id);
    }

    // -----------------------------------------------------------------------
    // Player endpoints
    // -----------------------------------------------------------------------

    @POST
    @Path("/player")
    public Response createPlayer(
            @QueryParam("id") String id,
            @QueryParam("name") String name,
            @QueryParam("position") String position,
            @QueryParam("salary") double salary) {
        requestBean.createPlayer(id, name, position, salary);
        return Response.ok().build();
    }

    @GET
    @Path("/player/{id}")
    public Response getPlayer(@PathParam("id") String id) {
        try {
            PlayerDetails player = requestBean.getPlayer(id);
            if (player == null) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
            return Response.ok(player).build();
        } catch (EJBException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @DELETE
    @Path("/player/{id}")
    public Response removePlayer(@PathParam("id") String id) {
        try {
            requestBean.removePlayer(id);
            return Response.ok().build();
        } catch (EJBException e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @POST
    @Path("/player/{pid}/team/{tid}")
    public Response addPlayerToTeam(@PathParam("pid") String pid, @PathParam("tid") String tid) {
        requestBean.addPlayer(pid, tid);
        return Response.ok().build();
    }

    @DELETE
    @Path("/player/{pid}/team/{tid}")
    public Response dropPlayerFromTeam(@PathParam("pid") String pid, @PathParam("tid") String tid) {
        requestBean.dropPlayer(pid, tid);
        return Response.ok().build();
    }

    @GET
    @Path("/player/{id}/leagues")
    public List<LeagueDetails> getLeaguesOfPlayer(@PathParam("id") String id) {
        return requestBean.getLeaguesOfPlayer(id);
    }

    @GET
    @Path("/player/{id}/sports")
    public List<String> getSportsOfPlayer(@PathParam("id") String id) {
        return requestBean.getSportsOfPlayer(id);
    }

    // -----------------------------------------------------------------------
    // Query endpoints
    // -----------------------------------------------------------------------

    @GET
    @Path("/players")
    public List<PlayerDetails> getAllPlayers() {
        return requestBean.getAllPlayers();
    }

    @GET
    @Path("/players/position/{position}")
    public List<PlayerDetails> getPlayersByPosition(@PathParam("position") String position) {
        return requestBean.getPlayersByPosition(position);
    }

    @GET
    @Path("/players/salary/higher/{name}")
    public List<PlayerDetails> getPlayersByHigherSalary(@PathParam("name") String name) {
        return requestBean.getPlayersByHigherSalary(name);
    }

    @GET
    @Path("/players/salary/range")
    public List<PlayerDetails> getPlayersBySalaryRange(
            @QueryParam("low") double low,
            @QueryParam("high") double high) {
        return requestBean.getPlayersBySalaryRange(low, high);
    }

    @GET
    @Path("/players/league/{id}")
    public List<PlayerDetails> getPlayersByLeague(@PathParam("id") String id) {
        return requestBean.getPlayersByLeagueId(id);
    }

    @GET
    @Path("/players/sport/{sport}")
    public List<PlayerDetails> getPlayersBySport(@PathParam("sport") String sport) {
        return requestBean.getPlayersBySport(sport);
    }

    @GET
    @Path("/players/city/{city}")
    public List<PlayerDetails> getPlayersByCity(@PathParam("city") String city) {
        return requestBean.getPlayersByCity(city);
    }

    @GET
    @Path("/players/not-on-team")
    public List<PlayerDetails> getPlayersNotOnTeam() {
        return requestBean.getPlayersNotOnTeam();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private boolean hasIncorrectSportCause(EJBException e) {
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof IncorrectSportException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}
