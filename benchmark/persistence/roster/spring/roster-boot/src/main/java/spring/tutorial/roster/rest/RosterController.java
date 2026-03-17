package spring.tutorial.roster.rest;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import spring.tutorial.roster.request.Request;
import spring.tutorial.roster.util.LeagueDetails;
import spring.tutorial.roster.util.PlayerDetails;
import spring.tutorial.roster.util.TeamDetails;

@RestController
@RequestMapping("/roster")
public class RosterController {

    private final Request request;

    public RosterController(Request request) {
        this.request = request;
    }

    // ---- League ----

    @PostMapping("/league")
    public ResponseEntity<Void> createLeague(@RequestBody LeagueDetails details) {
        try {
            request.createLeague(details);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/league/{id}")
    public ResponseEntity<LeagueDetails> getLeague(@PathVariable String id) {
        LeagueDetails d = request.getLeague(id);
        return d != null ? ResponseEntity.ok(d) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/league/{id}")
    public ResponseEntity<Void> removeLeague(@PathVariable String id) {
        request.removeLeague(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/league/{id}/teams")
    public List<TeamDetails> getTeamsOfLeague(@PathVariable String id) {
        return request.getTeamsOfLeague(id);
    }

    // ---- Team ----

    @PostMapping("/team/league/{leagueId}")
    public ResponseEntity<Void> createTeamInLeague(@RequestBody TeamDetails details,
                                                    @PathVariable String leagueId) {
        request.createTeamInLeague(details, leagueId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/team/{id}")
    public ResponseEntity<TeamDetails> getTeam(@PathVariable String id) {
        TeamDetails d = request.getTeam(id);
        return d != null ? ResponseEntity.ok(d) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/team/{id}")
    public ResponseEntity<Void> removeTeam(@PathVariable String id) {
        request.removeTeam(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/team/{id}/players")
    public List<PlayerDetails> getPlayersOfTeam(@PathVariable String id) {
        return request.getPlayersOfTeam(id);
    }

    // ---- Player ----

    @PostMapping("/player")
    public ResponseEntity<Void> createPlayer(@RequestParam String id,
                                              @RequestParam String name,
                                              @RequestParam String position,
                                              @RequestParam double salary) {
        request.createPlayer(id, name, position, salary);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/player/{id}")
    public ResponseEntity<PlayerDetails> getPlayer(@PathVariable String id) {
        PlayerDetails d = request.getPlayer(id);
        return d != null ? ResponseEntity.ok(d) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/player/{id}")
    public ResponseEntity<Void> removePlayer(@PathVariable String id) {
        request.removePlayer(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/player/{pid}/team/{tid}")
    public ResponseEntity<Void> addPlayerToTeam(@PathVariable String pid,
                                                 @PathVariable String tid) {
        request.addPlayer(pid, tid);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/player/{pid}/team/{tid}")
    public ResponseEntity<Void> dropPlayerFromTeam(@PathVariable String pid,
                                                    @PathVariable String tid) {
        request.dropPlayer(pid, tid);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/player/{id}/leagues")
    public List<LeagueDetails> getLeaguesOfPlayer(@PathVariable String id) {
        return request.getLeaguesOfPlayer(id);
    }

    @GetMapping("/player/{id}/sports")
    public List<String> getSportsOfPlayer(@PathVariable String id) {
        return request.getSportsOfPlayer(id);
    }

    // ---- Queries ----

    @GetMapping("/players")
    public List<PlayerDetails> getAllPlayers() {
        return request.getAllPlayers();
    }

    @GetMapping("/players/position/{pos}")
    public List<PlayerDetails> getPlayersByPosition(@PathVariable String pos) {
        return request.getPlayersByPosition(pos);
    }

    @GetMapping("/players/salary/higher/{name}")
    public List<PlayerDetails> getPlayersByHigherSalary(@PathVariable String name) {
        return request.getPlayersByHigherSalary(name);
    }

    @GetMapping("/players/salary/range")
    public List<PlayerDetails> getPlayersBySalaryRange(@RequestParam double low,
                                                        @RequestParam double high) {
        return request.getPlayersBySalaryRange(low, high);
    }

    @GetMapping("/players/league/{id}")
    public List<PlayerDetails> getPlayersByLeagueId(@PathVariable String id) {
        return request.getPlayersByLeagueId(id);
    }

    @GetMapping("/players/sport/{sport}")
    public List<PlayerDetails> getPlayersBySport(@PathVariable String sport) {
        return request.getPlayersBySport(sport);
    }

    @GetMapping("/players/city/{city}")
    public List<PlayerDetails> getPlayersByCity(@PathVariable String city) {
        return request.getPlayersByCity(city);
    }

    @GetMapping("/players/not-on-team")
    public List<PlayerDetails> getPlayersNotOnTeam() {
        return request.getPlayersNotOnTeam();
    }

    @GetMapping("/players/position/{pos}/name/{name}")
    public List<PlayerDetails> getPlayersByPositionAndName(@PathVariable String pos,
                                                            @PathVariable String name) {
        return request.getPlayersByPositionAndName(pos, name);
    }
}
