package com.worldcup.controller;

import com.worldcup.domain.User;
import com.worldcup.repository.PredictionRepository;
import com.worldcup.repository.TeamRepository;
import com.worldcup.service.MatchService;
import com.worldcup.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
@Slf4j
public class HomeController {

    private static final int UPCOMING_LIMIT = 6;

    private final MatchService matchService;
    private final UserService userService;
    private final PredictionRepository predictionRepository;
    private final TeamRepository teamRepository;

    public HomeController(MatchService matchService, UserService userService,
                          PredictionRepository predictionRepository, TeamRepository teamRepository) {
        this.matchService = matchService;
        this.userService = userService;
        this.predictionRepository = predictionRepository;
        this.teamRepository = teamRepository;
    }

    @GetMapping("/")
    public String home(Model model, Principal principal) {
        model.addAttribute("upcomingMatches", matchService.findUpcoming(UPCOMING_LIMIT));

        if (principal != null) {
            User user = userService.findByUsername(principal.getName());
            model.addAttribute("username", user.getUsername());
            model.addAttribute("userPoints", predictionRepository.getTotalScoreForUser(user));
            model.addAttribute("userPredictionCount", predictionRepository.countByUser(user));
            model.addAttribute("userTeamCount", teamRepository.countByMembersContaining(user));
        }
        return "index";
    }
}
