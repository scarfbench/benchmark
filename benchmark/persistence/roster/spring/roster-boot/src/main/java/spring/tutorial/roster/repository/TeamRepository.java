package spring.tutorial.roster.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import jakartaee.tutorial.roster.entity.Team;

public interface TeamRepository extends JpaRepository<Team, String> {
  List<Team> findByLeague_Id(String leagueId);
}