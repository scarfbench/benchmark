package spring.tutorial.roster.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import jakartaee.tutorial.roster.entity.Player;

public interface PlayerRepository extends JpaRepository<Player, String> {
  List<Player> findByPosition(String position);
  List<Player> findBySalaryBetween(double low, double high);
  List<Player> findBySalaryGreaterThan(double salary);
  List<Player> findByTeams_Id(String teamId);
  List<Player> findByTeamsIsEmpty();
  List<Player> findByPositionAndName(String position, String name);
  List<Player> findDistinctByTeams_League_Id(String leagueId);
  List<Player> findDistinctByTeams_League_Sport(String sport);
  List<Player> findDistinctByTeams_City(String city);
  Optional<Player> findFirstByName(String name);
}