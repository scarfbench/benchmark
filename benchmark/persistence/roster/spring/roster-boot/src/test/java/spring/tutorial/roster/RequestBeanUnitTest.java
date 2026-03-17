package spring.tutorial.roster;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.Mock;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import spring.tutorial.roster.request.RequestBean;
import spring.tutorial.roster.util.LeagueDetails;
import spring.tutorial.roster.repository.LeagueRepository;
import spring.tutorial.roster.repository.PlayerRepository;
import spring.tutorial.roster.repository.TeamRepository;
import jakartaee.tutorial.roster.entity.League;
import jakartaee.tutorial.roster.entity.Player;
import jakartaee.tutorial.roster.entity.SummerLeague;
import jakartaee.tutorial.roster.entity.Team;
import jakartaee.tutorial.roster.entity.WinterLeague;

@ExtendWith(MockitoExtension.class)
class RequestBeanUnitTest {

  @Mock PlayerRepository playerRepository;
  @Mock TeamRepository teamRepository;
  @Mock LeagueRepository leagueRepository;

  @Test
  void createLeagueChoosesSeasonType() {
    RequestBean bean = new RequestBean(playerRepository, teamRepository, leagueRepository);

    bean.createLeague(new LeagueDetails("L1","Name","soccer"));
    ArgumentCaptor<League> cap1 = ArgumentCaptor.forClass(League.class);
    verify(leagueRepository).save(cap1.capture());
    assertTrue(cap1.getValue() instanceof SummerLeague);

    reset(leagueRepository);
    bean.createLeague(new LeagueDetails("L2","Name","hockey"));
    ArgumentCaptor<League> cap2 = ArgumentCaptor.forClass(League.class);
    verify(leagueRepository).save(cap2.capture());
    assertTrue(cap2.getValue() instanceof WinterLeague);
  }

  @Test
  void addPlayerLinksAndSavesTeam() {
    RequestBean bean = new RequestBean(playerRepository, teamRepository, leagueRepository);
    Player p = new Player("P1","Alice","Forward",100);
    Team t = new Team("T1","Tigers","Austin");
    when(playerRepository.findById("P1")).thenReturn(Optional.of(p));
    when(teamRepository.findById("T1")).thenReturn(Optional.of(t));

    bean.addPlayer("P1","T1");

    verify(teamRepository).save(t);
    assertTrue(t.getPlayers().contains(p));
    assertTrue(p.getTeams().contains(t));
  }
}
