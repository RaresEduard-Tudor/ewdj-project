package com.worldcup.controller;

import com.worldcup.domain.Match;
import com.worldcup.service.MatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class HomeController {

    private final MatchService matchService;

    public HomeController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Match> upcoming = matchService.findAll().stream()
            .filter(m -> m.getGoalsA() == null)
            .limit(6)
            .collect(Collectors.toList());
        model.addAttribute("upcomingMatches", upcoming);
        return "index";
    }
}
