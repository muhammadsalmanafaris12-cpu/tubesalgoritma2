package com.example.demo.controller;

import com.example.demo.model.Match;
import com.example.demo.model.Team;
import com.example.demo.model.Tournament;
import com.example.demo.repository.MatchRepository;
import com.example.demo.repository.TeamRepository;
import com.example.demo.repository.TournamentRepository;
import com.example.demo.service.TournamentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TournamentService tournamentService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        try {
            // Step 1: Active tournament
            Tournament activeCup = null;
            try {
                Optional<Tournament> activeCupOpt = tournamentRepository.findFirstByStatusOrderByIdDesc("BERJALAN");
                activeCup = activeCupOpt.orElse(null);
            } catch (Exception e) {
                System.err.println("[BRACKTIX ERROR] Failed to fetch active tournament: " + e.getMessage());
                e.printStackTrace();
            }
            model.addAttribute("activeCup", activeCup);

            // Step 2: Matches (always initialize as empty list)
            List<Match> matches = new ArrayList<>();
            int maxRound = 1;
            try {
                if (activeCup != null) {
                    matches = matchRepository.findByTournamentId(activeCup.getId());
                    if (matches == null) matches = new ArrayList<>();
                    maxRound = matches.stream().mapToInt(Match::getRound).max().orElse(1);
                }
            } catch (Exception e) {
                System.err.println("[BRACKTIX ERROR] Failed to fetch matches: " + e.getMessage());
                e.printStackTrace();
                matches = new ArrayList<>();
            }
            model.addAttribute("matches", matches);
            model.addAttribute("maxRound", maxRound);

            // Step 3: Teams (filtered by active cup)
            List<Team> teams = new ArrayList<>();
            try {
                if (activeCup != null) {
                    teams = teamRepository.findByTournamentId(activeCup.getId());
                }
                if (teams == null) teams = new ArrayList<>();
            } catch (Exception e) {
                System.err.println("[BRACKTIX ERROR] Failed to fetch teams: " + e.getMessage());
                e.printStackTrace();
                teams = new ArrayList<>();
            }
            model.addAttribute("teams", teams);

            // Step 4: History
            List<Tournament> history = new ArrayList<>();
            try {
                history = tournamentRepository.findAll();
                if (history == null) history = new ArrayList<>();
            } catch (Exception e) {
                System.err.println("[BRACKTIX ERROR] Failed to fetch history: " + e.getMessage());
                e.printStackTrace();
                history = new ArrayList<>();
            }
            model.addAttribute("history", history);

            System.out.println("[BRACKTIX] Admin dashboard loaded. activeCup=" + activeCup +
                    " | teams=" + teams.size() + " | matches=" + matches.size());

            return "admin_dashboard";

        } catch (Exception e) {
            // If ALL else fails, send to diagnostic page
            System.err.println("[BRACKTIX CRITICAL] Admin dashboard crashed: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("errorClass", e.getClass().getName());
            return "admin_error";
        }
    }

    @PostMapping("/tournament/create")
    public String createTournament(@RequestParam String name,
                                   @RequestParam String game,
                                   @RequestParam String startDate) {
        try {
            Optional<Tournament> activeCupOpt = tournamentRepository.findFirstByStatusOrderByIdDesc("BERJALAN");
            if (activeCupOpt.isEmpty()) {
                Tournament tournament = new Tournament(name, game, LocalDate.parse(startDate), "BERJALAN");
                tournamentRepository.save(tournament);
            }
        } catch (Exception e) {
            System.err.println("[BRACKTIX ERROR] Failed to create tournament: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/team/update")
    public String updateTeam(@RequestParam Long id,
                             @RequestParam String name,
                             @RequestParam String captainName,
                             @RequestParam String contact) {
        try {
            Optional<Team> teamOpt = teamRepository.findById(id);
            if (teamOpt.isPresent()) {
                Team team = teamOpt.get();
                team.setName(name);
                team.setCaptainName(captainName);
                team.setContact(contact);
                teamRepository.save(team);
            }
        } catch (Exception e) {
            System.err.println("[BRACKTIX ERROR] Failed to update team: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/team/delete")
    public String deleteTeam(@RequestParam Long id) {
        try {
            teamRepository.deleteById(id);
        } catch (Exception e) {
            System.err.println("[BRACKTIX ERROR] Failed to delete team: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/match/schedule")
    public String scheduleMatch(@RequestParam Long matchId,
                                @RequestParam String matchDate,
                                @RequestParam String matchTime) {
        try {
            Optional<Match> matchOpt = matchRepository.findById(matchId);
            if (matchOpt.isPresent()) {
                Match match = matchOpt.get();
                match.setMatchDate(matchDate);
                match.setMatchTime(matchTime);
                
                String dayIndo = "";
                try {
                    java.time.LocalDate date = java.time.LocalDate.parse(matchDate);
                    switch(date.getDayOfWeek()) {
                        case MONDAY: dayIndo = "Senin"; break;
                        case TUESDAY: dayIndo = "Selasa"; break;
                        case WEDNESDAY: dayIndo = "Rabu"; break;
                        case THURSDAY: dayIndo = "Kamis"; break;
                        case FRIDAY: dayIndo = "Jumat"; break;
                        case SATURDAY: dayIndo = "Sabtu"; break;
                        case SUNDAY: dayIndo = "Minggu"; break;
                    }
                } catch (Exception e) {
                    dayIndo = "Hari";
                }
                
                String formattedDate = matchDate;
                try {
                    java.time.LocalDate date = java.time.LocalDate.parse(matchDate);
                    java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy");
                    formattedDate = date.format(formatter);
                } catch (Exception e) {}
                
                String jadwalTanding = dayIndo + ", " + formattedDate + " " + matchTime;
                match.setJadwalTanding(jadwalTanding);
                
                matchRepository.save(match);
            }
        } catch (Exception e) {
            System.err.println("[BRACKTIX ERROR] Failed to schedule match: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/bracket/generate")
    public String generateBracket(@RequestParam Long tournamentId) {
        try {
            tournamentService.generateBracket(tournamentId);
        } catch (Exception e) {
            System.err.println("[BRACKTIX ERROR] Failed to generate bracket: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/match/score")
    public String inputScore(@RequestParam Long matchId,
                             @RequestParam Integer team1Score,
                             @RequestParam Integer team2Score) {
        try {
            tournamentService.submitScore(matchId, team1Score, team2Score);
        } catch (Exception e) {
            System.err.println("[BRACKTIX ERROR] Failed to submit score: " + e.getMessage());
            e.printStackTrace();
        }
        return "redirect:/admin/dashboard";
    }
}
