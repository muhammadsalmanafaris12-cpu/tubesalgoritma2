package com.example.demo.repository;

import com.example.demo.model.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findByTournamentId(Long tournamentId);
    List<Match> findByTournamentIdAndRound(Long tournamentId, Integer round);
    List<Match> findByTournamentIdAndStatusNot(Long tournamentId, String status);
}
