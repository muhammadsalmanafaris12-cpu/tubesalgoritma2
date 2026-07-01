package com.example.demo.repository;

import com.example.demo.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByTeamId(String teamId);
    Optional<Team> findByCaptainUsername(String captainUsername);
    Optional<Team> findByCaptainUsernameAndTournamentId(String captainUsername, Long tournamentId);
    java.util.List<Team> findByTournamentId(Long tournamentId);
    Team findFirstByOrderByIdDesc();
}
