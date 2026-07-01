package com.example.demo.controller;

import com.example.demo.model.Match;
import com.example.demo.model.Team;
import com.example.demo.model.Tournament;
import com.example.demo.model.User;
import com.example.demo.repository.MatchRepository;
import com.example.demo.repository.TeamRepository;
import com.example.demo.repository.TournamentRepository;
import com.example.demo.service.TournamentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private TournamentRepository tournamentRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private TournamentService tournamentService;

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) Long viewTournamentId, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        
        // Active tournament
        Optional<Tournament> activeCupOpt = tournamentRepository.findFirstByStatusOrderByIdDesc("BERJALAN");
        Tournament activeCup = activeCupOpt.orElse(null);
        model.addAttribute("activeCup", activeCup);

        // Find if this captain has already registered a team for the active tournament
        Optional<Team> myTeamOpt = Optional.empty();
        if (activeCup != null) {
            myTeamOpt = teamRepository.findByCaptainUsernameAndTournamentId(user.getUsername(), activeCup.getId());
        }
        Team myTeam = myTeamOpt.orElse(null);
        model.addAttribute("myTeam", myTeam);
 
        // Always initialize matches, maxRound, activeMatch to avoid NullPointerException in template
        List<Match> matches = new java.util.ArrayList<>();
        int maxRound = 1;
        Match activeMatch = null;
 
        if (activeCup != null) {
            matches = matchRepository.findByTournamentId(activeCup.getId());
            maxRound = matches.stream().mapToInt(Match::getRound).max().orElse(1);
 
            // Find current team's active match for Live Match Monitor
            if (myTeam != null) {
                for (Match m : matches) {
                    if ("SCHEDULED".equals(m.getStatus()) &&
                        (myTeam.getTeamId().equals(m.getTeam1Id()) || myTeam.getTeamId().equals(m.getTeam2Id()))) {
                        activeMatch = m;
                        break;
                    }
                }
            }
        }
 
        model.addAttribute("matches", matches);
        model.addAttribute("maxRound", maxRound);
        model.addAttribute("activeMatch", activeMatch);
 
        List<Tournament> history = tournamentRepository.findAll();
        model.addAttribute("history", history);
        
        // Load specific tournament details if clicked
        Tournament viewTournament = null;
        List<Match> viewMatches = new java.util.ArrayList<>();
        List<Team> viewTeams = new java.util.ArrayList<>();
        
        if (viewTournamentId != null) {
            Optional<Tournament> tOpt = tournamentRepository.findById(viewTournamentId);
            if (tOpt.isPresent()) {
                viewTournament = tOpt.get();
                viewMatches = matchRepository.findByTournamentId(viewTournamentId);
                
                viewTeams = teamRepository.findByTournamentId(viewTournamentId);
            }
        }
        model.addAttribute("viewTournament", viewTournament);
        model.addAttribute("viewMatches", viewMatches);
        model.addAttribute("viewTeams", viewTeams);
 
        return "user_dashboard";
    }

    @PostMapping("/team/register")
    public String registerTeam(@RequestParam String name,
                               @RequestParam String captainName,
                               @RequestParam String contact,
                               HttpSession session,
                               Model model) {
        User user = (User) session.getAttribute("user");
        Optional<Tournament> activeCupOpt = tournamentRepository.findFirstByStatusOrderByIdDesc("BERJALAN");
        if (activeCupOpt.isEmpty()) {
            return "redirect:/user/dashboard?error=no_active_tournament";
        }
        Tournament activeCup = activeCupOpt.get();
        Optional<Team> existingTeam = teamRepository.findByCaptainUsernameAndTournamentId(user.getUsername(), activeCup.getId());
        
        if (existingTeam.isPresent()) {
            return "redirect:/user/dashboard?error=already_registered";
        }

        tournamentService.registerTeam(name, captainName, contact, user.getUsername(), activeCup.getId());
        return "redirect:/user/dashboard?registered=true";
    }
}
