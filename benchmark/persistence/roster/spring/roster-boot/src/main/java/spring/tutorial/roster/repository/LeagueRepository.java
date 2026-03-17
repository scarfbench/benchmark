package spring.tutorial.roster.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import jakartaee.tutorial.roster.entity.League;

public interface LeagueRepository extends JpaRepository<League, String> {
}