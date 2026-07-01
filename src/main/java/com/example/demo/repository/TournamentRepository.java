package com.example.demo.repository;

import com.example.demo.model.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    Optional<Tournament> findFirstByStatusOrderByIdDesc(String status);
    List<Tournament> findByStatus(String status);
}
