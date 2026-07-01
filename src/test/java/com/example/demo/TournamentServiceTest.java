package com.example.demo;

import com.example.demo.model.Match;
import com.example.demo.model.Team;
import com.example.demo.model.Tournament;
import com.example.demo.repository.MatchRepository;
import com.example.demo.repository.TeamRepository;
import com.example.demo.repository.TournamentRepository;
import com.example.demo.service.TournamentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class TournamentServiceTest {

    @Autowired
    private TournamentService tournamentService;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @BeforeEach
    public void setUp() {
        matchRepository.deleteAll();
        teamRepository.deleteAll();
        tournamentRepository.deleteAll();
    }

    @Test
    public void testRegisterTeamIdSequence() {
        Team t1 = tournamentService.registerTeam("Aether Squad", "Alex", "0811", "alex123", 1L);
        assertEquals("T001", t1.getTeamId());

        Team t2 = tournamentService.registerTeam("Nova Legion", "Bob", "0812", "bob123", 1L);
        assertEquals("T002", t2.getTeamId());

        Team t3 = tournamentService.registerTeam("Cyber Knights", "Carl", "0813", "carl123", 1L);
        assertEquals("T003", t3.getTeamId());
    }

    @Test
    public void testGenerateBracketEven() {
        // Create active tournament
        Tournament tournament = new Tournament("Valorant Cup", "Valorant", LocalDate.now(), "BERJALAN");
        tournament = tournamentRepository.save(tournament);

        // Register 4 teams
        tournamentService.registerTeam("Team A", "Alex", "0811", "alex", tournament.getId());
        tournamentService.registerTeam("Team B", "Bob", "0812", "bob", tournament.getId());
        tournamentService.registerTeam("Team C", "Carl", "0813", "carl", tournament.getId());
        tournamentService.registerTeam("Team D", "Dave", "0814", "dave", tournament.getId());

        // Generate bracket
        tournamentService.generateBracket(tournament.getId());

        List<Match> matches = matchRepository.findByTournamentId(tournament.getId());
        assertEquals(2, matches.size(), "Even number of teams (4) should generate exactly 2 matches");
        
        for (Match m : matches) {
            assertEquals(1, m.getRound());
            assertEquals("SCHEDULED", m.getStatus());
            assertNull(m.getWinnerTeamId());
            assertNotNull(m.getTeam1Id());
            assertNotNull(m.getTeam2Id());
            assertNotEquals("BYE", m.getTeam2Id());
        }
    }

    @Test
    public void testGenerateBracketOddAndBYE() {
        // Create active tournament
        Tournament tournament = new Tournament("Valorant Cup", "Valorant", LocalDate.now(), "BERJALAN");
        tournament = tournamentRepository.save(tournament);

        // Register 5 teams
        tournamentService.registerTeam("Team A", "Alex", "0811", "alex", tournament.getId());
        tournamentService.registerTeam("Team B", "Bob", "0812", "bob", tournament.getId());
        tournamentService.registerTeam("Team C", "Carl", "0813", "carl", tournament.getId());
        tournamentService.registerTeam("Team D", "Dave", "0814", "dave", tournament.getId());
        tournamentService.registerTeam("Team E", "Eric", "0815", "eric", tournament.getId());

        // Generate bracket
        tournamentService.generateBracket(tournament.getId());

        List<Match> matches = matchRepository.findByTournamentId(tournament.getId());
        assertEquals(3, matches.size(), "Odd number of teams (5) should generate exactly 3 matches (2 normal, 1 BYE)");

        int normalCount = 0;
        int byeCount = 0;

        for (Match m : matches) {
            assertEquals(1, m.getRound());
            if ("BYE".equals(m.getStatus())) {
                byeCount++;
                assertEquals("BYE", m.getTeam2Id());
                assertEquals("[BYE]", m.getTeam2Name());
                assertEquals(1, m.getTeam1Score());
                assertEquals(0, m.getTeam2Score());
                assertEquals(m.getTeam1Id(), m.getWinnerTeamId(), "The team receiving the BYE should automatically win");
            } else {
                normalCount++;
                assertEquals("SCHEDULED", m.getStatus());
                assertNull(m.getWinnerTeamId());
            }
        }

        assertEquals(2, normalCount);
        assertEquals(1, byeCount);
    }

    @Test
    public void testSubmitScoreAndProgression() {
        // Create active tournament
        Tournament tournament = new Tournament("Valorant Cup", "Valorant", LocalDate.now(), "BERJALAN");
        tournament = tournamentRepository.save(tournament);

        // Register 2 teams (Final immediately)
        Team t1 = tournamentService.registerTeam("Team A", "Alex", "0811", "alex", tournament.getId());
        Team t2 = tournamentService.registerTeam("Team B", "Bob", "0812", "bob", tournament.getId());

        // Generate bracket
        tournamentService.generateBracket(tournament.getId());

        List<Match> matches = matchRepository.findByTournamentIdAndRound(tournament.getId(), 1);
        assertEquals(1, matches.size());
        Match finalMatch = matches.get(0);
        assertEquals("SCHEDULED", finalMatch.getStatus());

        // Submit score, Team A wins
        tournamentService.submitScore(finalMatch.getId(), 3, 1);

        // Check match is finished
        Match updatedMatch = matchRepository.findById(finalMatch.getId()).get();
        assertEquals("FINISHED", updatedMatch.getStatus());
        assertEquals(finalMatch.getTeam1Id(), updatedMatch.getWinnerTeamId());
        assertEquals(3, updatedMatch.getTeam1Score());
        assertEquals(1, updatedMatch.getTeam2Score());

        // Check tournament is completed and champion is set
        Tournament updatedTournament = tournamentRepository.findById(tournament.getId()).get();
        assertEquals("SELESAI", updatedTournament.getStatus());
        String expectedChampionName = finalMatch.getTeam1Id().equals(t1.getTeamId()) ? "Team A" : "Team B";
        assertEquals(expectedChampionName, updatedTournament.getChampionTeamName());
    }
}
