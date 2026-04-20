package com.worldcup.controller;

import com.worldcup.domain.Match;
import com.worldcup.domain.Prediction;
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
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class HomeController {

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
        List<Match> upcoming = matchService.findAll().stream()
            .filter(m -> m.getGoalsA() == null)
            .limit(6)
            .collect(Collectors.toList());
        model.addAttribute("upcomingMatches", upcoming);

        if (principal != null) {
            User user = userService.findByUsername(principal.getName());
            model.addAttribute("username", user.getUsername());
            List<Prediction> predictions = predictionRepository.findByUser(user);
            int totalPoints = predictions.stream()
                .filter(p -> p.getPointsAwarded() != null)
                .mapToInt(Prediction::getPointsAwarded).sum();
            model.addAttribute("userPoints", totalPoints);
            model.addAttribute("userPredictionCount", predictions.size());
            model.addAttribute("userTeamCount", teamRepository.countByMembersContaining(user));
        }
        return "index";
    }
}
