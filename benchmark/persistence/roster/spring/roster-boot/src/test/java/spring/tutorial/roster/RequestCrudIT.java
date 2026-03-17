package spring.tutorial.roster;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import spring.tutorial.roster.request.Request;
import spring.tutorial.roster.util.LeagueDetails;
import spring.tutorial.roster.util.TeamDetails;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class RequestCrudIT {

  @Autowired
  Request request;

  @Test
  void leagueTeamPlayerCrud() {
    request.createLeague(new LeagueDetails("Lx","LX","soccer"));
    request.createTeamInLeague(new TeamDetails("Tx","Tigers","Austin"), "Lx");
    request.createPlayer("Px","Alice","Forward",100.0);
    request.addPlayer("Px","Tx");

    assertEquals(1, request.getPlayersOfTeam("Tx").size());
    assertNotNull(request.getTeam("Tx"));
    assertNotNull(request.getLeague("Lx"));

    request.removeTeam("Tx");
    assertEquals(0, request.getTeamsOfLeague("Lx").size());
    assertEquals(0, request.getPlayersOfTeam("Tx").size());

    request.removeLeague("Lx");
    assertNull(request.getLeague("Lx"));

    request.removePlayer("Px");
    assertNull(request.getPlayer("Px"));
    assertEquals(0, request.getAllPlayers().size());
  }
}
