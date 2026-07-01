package com.example.demo.service;

import com.example.demo.model.Match;
import com.example.demo.model.Team;
import com.example.demo.model.Tournament;
import com.example.demo.repository.MatchRepository;
import com.example.demo.repository.TeamRepository;
import com.example.demo.repository.TournamentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class TournamentService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TournamentRepository tournamentRepository;

    @Transactional
    public Team registerTeam(String name, String captainName, String contact, String captainUsername, Long tournamentId) {
        // Find last registered team to generate sequential ID
        Team lastTeam = teamRepository.findFirstByOrderByIdDesc();
        String newTeamId = "T001";
        if (lastTeam != null && lastTeam.getTeamId() != null) {
            String lastIdStr = lastTeam.getTeamId();
            if (lastIdStr.startsWith("T")) {
                try {
                    int num = Integer.parseInt(lastIdStr.substring(1));
                    newTeamId = String.format("T%03d", num + 1);
                } catch (NumberFormatException e) {
                    // Fallback if formatting is unexpected
                }
            }
        }
        
        Team team = new Team(newTeamId, name, captainName, contact, captainUsername, tournamentId);
        return teamRepository.save(team);
    }

    @Transactional
    public void generateBracket(Long tournamentId) {
        // Delete old matches for this tournament to start fresh
        List<Match> oldMatches = matchRepository.findByTournamentId(tournamentId);
        matchRepository.deleteAll(oldMatches);

        List<Team> teams = teamRepository.findByTournamentId(tournamentId);
        if (teams.isEmpty()) {
            return;
        }

        // Shuffle teams using Collections.shuffle
        List<Team> shuffledTeams = new ArrayList<>(teams);
        Collections.shuffle(shuffledTeams);

        int matchIndex = 0;
        int i = 0;
        while (i < shuffledTeams.size()) {
            if (i + 1 < shuffledTeams.size()) {
                // Pair them up
                Team t1 = shuffledTeams.get(i);
                Team t2 = shuffledTeams.get(i + 1);
                Match match = new Match(
                        tournamentId,
                        t1.getTeamId(),
                        t2.getTeamId(),
                        t1.getName(),
                        t2.getName(),
                        1, // Round 1
                        matchIndex++,
                        "SCHEDULED"
                );
                matchRepository.save(match);
                i += 2;
            } else {
                // Odd team gets a BYE
                Team oddTeam = shuffledTeams.get(i);
                Match match = new Match(
                        tournamentId,
                        oddTeam.getTeamId(),
                        "BYE",
                        oddTeam.getName(),
                        "[BYE]",
                        1, // Round 1
                        matchIndex++,
                        "BYE"
                );
                match.setTeam1Score(1);
                match.setTeam2Score(0);
                match.setWinnerTeamId(oddTeam.getTeamId());
                matchRepository.save(match);
                i++;
            }
        }
    }

    @Transactional
    public void submitScore(Long matchId, Integer score1, Integer score2) {
        Optional<Match> matchOpt = matchRepository.findById(matchId);
        if (matchOpt.isEmpty()) {
            return;
        }
        Match match = matchOpt.get();
        if ("BYE".equals(match.getStatus())) {
            return; // Can't edit score for BYE matches
        }

        match.setTeam1Score(score1);
        match.setTeam2Score(score2);
        
        // Determine winner
        if (score1 > score2) {
            match.setWinnerTeamId(match.getTeam1Id());
        } else {
            match.setWinnerTeamId(match.getTeam2Id());
        }
        match.setStatus("FINISHED");
        matchRepository.save(match);

        // Check if all matches in the current round are completed
        checkAndProgressRound(match.getTournamentId(), match.getRound());
    }

    private void checkAndProgressRound(Long tournamentId, Integer currentRound) {
        List<Match> roundMatches = matchRepository.findByTournamentIdAndRound(tournamentId, currentRound);
        boolean allFinished = true;
        for (Match m : roundMatches) {
            if ("SCHEDULED".equals(m.getStatus())) {
                allFinished = false;
                break;
            }
        }

        if (allFinished && !roundMatches.isEmpty()) {
            // Get all winners from this round in order of match index
            List<String> roundWinners = new ArrayList<>();
            for (Match m : roundMatches) {
                if (m.getWinnerTeamId() != null) {
                    roundWinners.add(m.getWinnerTeamId());
                }
            }

            if (roundWinners.size() == 1) {
                // We have a final champion!
                Optional<Tournament> tournamentOpt = tournamentRepository.findById(tournamentId);
                if (tournamentOpt.isPresent()) {
                    Tournament tournament = tournamentOpt.get();
                    tournament.setStatus("SELESAI");
                    
                    // Lookup winner team name
                    String winnerId = roundWinners.get(0);
                    Optional<Team> winnerTeam = teamRepository.findByTeamId(winnerId);
                    if (winnerTeam.isPresent()) {
                        tournament.setChampionTeamName(winnerTeam.get().getName());
                    } else {
                        tournament.setChampionTeamName(winnerId);
                    }
                    tournamentRepository.save(tournament);
                }
            } else if (roundWinners.size() > 1) {
                // Generate next round matches
                int nextRound = currentRound + 1;
                int matchIndex = 0;
                int i = 0;
                while (i < roundWinners.size()) {
                    if (i + 1 < roundWinners.size()) {
                        String w1Id = roundWinners.get(i);
                        String w2Id = roundWinners.get(i + 1);
                        String w1Name = getTeamName(w1Id);
                        String w2Name = getTeamName(w2Id);

                        Match match = new Match(
                                tournamentId,
                                w1Id,
                                w2Id,
                                w1Name,
                                w2Name,
                                nextRound,
                                matchIndex++,
                                "SCHEDULED"
                        );
                        matchRepository.save(match);
                        i += 2;
                    } else {
                        // Odd winner gets a BYE in next round
                        String oddWinnerId = roundWinners.get(i);
                        String name = getTeamName(oddWinnerId);

                        Match match = new Match(
                                tournamentId,
                                oddWinnerId,
                                "BYE",
                                name,
                                "[BYE]",
                                nextRound,
                                matchIndex++,
                                "BYE"
                        );
                        match.setTeam1Score(1);
                        match.setTeam2Score(0);
                        match.setWinnerTeamId(oddWinnerId);
                        matchRepository.save(match);
                        i++;
                    }
                }
                
                // Recurse in case the next round only contains BYE matches (though mathematically not possible if winners > 1, 
                // but if all matches in the next round are BYEs, progress again)
                checkAndProgressRound(tournamentId, nextRound);
            }
        }
    }

    private String getTeamName(String teamId) {
        if ("BYE".equals(teamId)) {
            return "[BYE]";
        }
        Optional<Team> t = teamRepository.findByTeamId(teamId);
        return t.map(Team::getName).orElse(teamId);
    }
}
