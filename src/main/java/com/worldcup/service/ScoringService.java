package com.worldcup.service;

import com.worldcup.domain.Match;
import com.worldcup.domain.Prediction;
import com.worldcup.domain.Team;
import com.worldcup.repository.PredictionRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Slf4j
public class ScoringService {

    private int X;
    private int Y;
    private int B;
    private int C;

    private final PredictionRepository predictionRepository;
    private final MessageSource messageSource;

    public ScoringService(PredictionRepository predictionRepository, MessageSource messageSource) {
        this.predictionRepository = predictionRepository;
        this.messageSource = messageSource;
    }

    @PostConstruct
    void loadScoringConstants() {
        X = readInt("scoring.exact");
        Y = readInt("scoring.outcome");
        B = readInt("scoring.bonus.exact");
        C = readInt("scoring.bonus.outcome");
    }

    private int readInt(String key) {
        String raw = messageSource.getMessage(key, null, Locale.ROOT);
        return Integer.parseInt(raw.trim());
    }

    @Transactional
    public void calculateScoresForMatch(Match match) {
        if (match.getGoalsA() == null || match.getGoalsB() == null) return;

        List<Prediction> predictions = predictionRepository.findByMatch(match);

        Map<Long, Boolean> exactByPrediction = new HashMap<>();
        Map<Long, Boolean> outcomeByPrediction = new HashMap<>();
        Map<Long, Long> teamExactCount = new HashMap<>();
        Map<Long, Long> teamOutcomeCount = new HashMap<>();

        for (Prediction p : predictions) {
            boolean exact = isExact(p, match);
            boolean outcome = isOutcomeCorrect(p, match);
            exactByPrediction.put(p.getId(), exact);
            outcomeByPrediction.put(p.getId(), outcome);
            for (Team team : p.getUser().getTeams()) {
                if (exact) teamExactCount.merge(team.getId(), 1L, Long::sum);
                if (outcome) teamOutcomeCount.merge(team.getId(), 1L, Long::sum);
            }
        }

        for (Prediction p : predictions) {
            int points = 0;
            boolean exact = exactByPrediction.getOrDefault(p.getId(), false);
            boolean outcome = outcomeByPrediction.getOrDefault(p.getId(), false);
            if (exact) {
                points += X;
                if (isSoleInAnyTeam(p, teamExactCount)) points += B;
            } else if (outcome) {
                points += Y;
                if (isSoleInAnyTeam(p, teamOutcomeCount)) points += C;
            }
            p.setPointsAwarded(points);
            log.debug("User {} scored {} points for match {}",
                p.getUser().getUsername(), points, match.getId());
        }

        predictionRepository.saveAll(predictions);
    }

    private boolean isSoleInAnyTeam(Prediction target, Map<Long, Long> teamCounts) {
        return target.getUser().getTeams().stream()
            .anyMatch(t -> teamCounts.getOrDefault(t.getId(), 0L) == 1L);
    }

    private boolean isExact(Prediction p, Match m) {
        return p.getPredictedGoalsA().equals(m.getGoalsA()) &&
               p.getPredictedGoalsB().equals(m.getGoalsB());
    }

    private boolean isOutcomeCorrect(Prediction p, Match m) {
        int predWinner = Integer.compare(p.getPredictedGoalsA(), p.getPredictedGoalsB());
        int actualWinner = Integer.compare(m.getGoalsA(), m.getGoalsB());
        return predWinner == actualWinner;
    }
}
