package com.worldcup.service;

import com.worldcup.domain.Match;
import com.worldcup.domain.Prediction;
import com.worldcup.repository.PredictionRepository;
import com.worldcup.repository.TeamRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@Slf4j
public class ScoringService {

    @Value("${scoring.exact}") private int X;
    @Value("${scoring.outcome}") private int Y;
    @Value("${scoring.bonus.exact}") private int B;
    @Value("${scoring.bonus.outcome}") private int C;

    private final PredictionRepository predictionRepository;
    private final TeamRepository teamRepository;

    public ScoringService(PredictionRepository predictionRepository,
                          TeamRepository teamRepository) {
        this.predictionRepository = predictionRepository;
        this.teamRepository = teamRepository;
    }

    public void calculateScoresForMatch(Match match) {
        if (match.getGoalsA() == null || match.getGoalsB() == null) return;

        List<Prediction> predictions = predictionRepository.findByMatch(match);

        for (Prediction p : predictions) {
            int points = 0;
            boolean exactCorrect = isExact(p, match);
            boolean outcomeCorrect = isOutcomeCorrect(p, match);

            if (exactCorrect) {
                points += X;
                if (isSoleExactInAnyTeam(p, predictions)) points += B;
            } else if (outcomeCorrect) {
                points += Y;
                if (isSoleOutcomeInAnyTeam(p, predictions, match)) points += C;
            }

            p.setPointsAwarded(points);
            predictionRepository.save(p);
            log.info("User {} scored {} points for match {}",
                p.getUser().getUsername(), points, match.getId());
        }
    }

    private boolean isSoleExactInAnyTeam(Prediction target, List<Prediction> allPredictions) {
        return target.getUser().getTeams().stream().anyMatch(team -> {
            long exactInTeam = allPredictions.stream()
                .filter(p -> team.getMembers().contains(p.getUser()))
                .filter(p -> isExact(p, target.getMatch()))
                .count();
            return exactInTeam == 1;
        });
    }

    private boolean isSoleOutcomeInAnyTeam(Prediction target, List<Prediction> allPredictions, Match match) {
        return target.getUser().getTeams().stream().anyMatch(team -> {
            long correctInTeam = allPredictions.stream()
                .filter(p -> team.getMembers().contains(p.getUser()))
                .filter(p -> isOutcomeCorrect(p, match))
                .count();
            return correctInTeam == 1;
        });
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
