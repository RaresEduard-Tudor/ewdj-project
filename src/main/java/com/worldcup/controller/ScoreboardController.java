package com.worldcup.controller;

import com.worldcup.domain.Team;
import com.worldcup.repository.PredictionRepository;
import com.worldcup.repository.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class ScoreboardController {

    private final TeamRepository teamRepository;
    private final PredictionRepository predictionRepository;

    public ScoreboardController(TeamRepository teamRepository, PredictionRepository predictionRepository) {
        this.teamRepository = teamRepository;
        this.predictionRepository = predictionRepository;
    }

    @GetMapping("/top10")
    @Transactional(readOnly = true)
    public String publicTop10(Model model) {
        List<Team> teams = teamRepository.findTop10ByTotalScore(PageRequest.of(0, 10));
        Map<Long, Integer> teamScores = teams.stream()
            .collect(Collectors.toMap(
                Team::getId,
                t -> t.getMembers().stream()
                    .mapToInt(m -> predictionRepository.getTotalScoreForUser(m))
                    .sum()));
        model.addAttribute("teams", teams);
        model.addAttribute("teamScores", teamScores);
        log.debug("Loaded top10 teams");
        return "scoreboard/public-top10";
    }
}
