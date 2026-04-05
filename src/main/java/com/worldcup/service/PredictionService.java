package com.worldcup.service;

import com.worldcup.domain.Match;
import com.worldcup.domain.Prediction;
import com.worldcup.domain.User;
import com.worldcup.dto.PredictionDto;
import com.worldcup.exception.PredictionDeadlineException;
import com.worldcup.exception.MatchNotFoundException;
import com.worldcup.repository.MatchRepository;
import com.worldcup.repository.PredictionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class PredictionService {

    private final MatchRepository matchRepository;
    private final PredictionRepository predictionRepository;

    public PredictionService(MatchRepository matchRepository,
                             PredictionRepository predictionRepository) {
        this.matchRepository = matchRepository;
        this.predictionRepository = predictionRepository;
    }

    @Transactional
    public void savePrediction(PredictionDto dto, User user) {
        Match match = matchRepository.findById(dto.getMatchId())
            .orElseThrow(() -> new MatchNotFoundException("Match not found: " + dto.getMatchId()));

        LocalDateTime deadline = match.getMatchDate().minusHours(1);
        if (LocalDateTime.now().isAfter(deadline)) {
            throw new PredictionDeadlineException(
                match.getCountryA() + " vs " + match.getCountryB(), deadline);
        }

        Prediction prediction = predictionRepository
            .findByUserAndMatch(user, match)
            .orElse(new Prediction());

        prediction.setUser(user);
        prediction.setMatch(match);
        prediction.setPredictedGoalsA(dto.getGoalsA());
        prediction.setPredictedGoalsB(dto.getGoalsB());
        predictionRepository.save(prediction);
        log.info("Prediction saved for user {} on match {}", user.getUsername(), match.getId());
    }

    public List<Prediction> findByUser(User user) {
        return predictionRepository.findByUser(user);
    }
}
