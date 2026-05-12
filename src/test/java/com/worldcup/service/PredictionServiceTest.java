package com.worldcup.service;

import com.worldcup.domain.Match;
import com.worldcup.domain.Prediction;
import com.worldcup.domain.User;
import com.worldcup.dto.PredictionDto;
import com.worldcup.exception.MatchNotFoundException;
import com.worldcup.exception.PredictionDeadlineException;
import com.worldcup.repository.MatchRepository;
import com.worldcup.repository.PredictionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PredictionServiceTest {

    @Mock MatchRepository matchRepository;
    @Mock PredictionRepository predictionRepository;

    PredictionService predictionService;

    private User user;
    private Match futureMatch;
    private Match pastMatch;

    @BeforeEach
    void setUp() {
        predictionService = new PredictionService(matchRepository, predictionRepository, Clock.systemDefaultZone());

        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        futureMatch = new Match();
        futureMatch.setId(1L);
        futureMatch.setCountryA("Brazil");
        futureMatch.setCountryB("France");
        futureMatch.setMatchDate(LocalDateTime.now().plusDays(5));

        pastMatch = new Match();
        pastMatch.setId(2L);
        pastMatch.setCountryA("Germany");
        pastMatch.setCountryB("Spain");
        pastMatch.setMatchDate(LocalDateTime.now().minusDays(1));
    }

    @Test
    void savePrediction_whenNewPrediction_shouldCreateAndSave() {
        when(matchRepository.findById(1L)).thenReturn(Optional.of(futureMatch));
        when(predictionRepository.findByUserAndMatch(user, futureMatch)).thenReturn(Optional.empty());

        PredictionDto dto = new PredictionDto();
        dto.setGoalsA(2);
        dto.setGoalsB(1);

        predictionService.savePrediction(1L, dto, user);

        verify(predictionRepository).save(argThat(p ->
            p.getPredictedGoalsA() == 2 && p.getPredictedGoalsB() == 1
                && p.getUser().equals(user) && p.getMatch().equals(futureMatch)));
    }

    @Test
    void savePrediction_whenExistingPrediction_shouldUpdateAndSave() {
        Prediction existing = new Prediction();
        existing.setId(99L);
        existing.setPredictedGoalsA(0);
        existing.setPredictedGoalsB(0);

        when(matchRepository.findById(1L)).thenReturn(Optional.of(futureMatch));
        when(predictionRepository.findByUserAndMatch(user, futureMatch)).thenReturn(Optional.of(existing));

        PredictionDto dto = new PredictionDto();
        dto.setGoalsA(3);
        dto.setGoalsB(2);

        predictionService.savePrediction(1L, dto, user);

        verify(predictionRepository).save(argThat(p ->
            p.getId().equals(99L) && p.getPredictedGoalsA() == 3 && p.getPredictedGoalsB() == 2));
    }

    @Test
    void savePrediction_whenPastDeadline_shouldThrow() {
        when(matchRepository.findById(2L)).thenReturn(Optional.of(pastMatch));

        PredictionDto dto = new PredictionDto();
        dto.setGoalsA(1);
        dto.setGoalsB(0);

        assertThatThrownBy(() -> predictionService.savePrediction(2L, dto, user))
            .isInstanceOf(PredictionDeadlineException.class);
        verify(predictionRepository, never()).save(any());
    }

    @Test
    void savePrediction_whenMatchNotFound_shouldThrow() {
        when(matchRepository.findById(99L)).thenReturn(Optional.empty());

        PredictionDto dto = new PredictionDto();
        dto.setGoalsA(1);
        dto.setGoalsB(0);

        assertThatThrownBy(() -> predictionService.savePrediction(99L, dto, user))
            .isInstanceOf(MatchNotFoundException.class);
        verify(predictionRepository, never()).save(any());
    }

    @Test
    void findByUser_shouldDelegateToRepository() {
        Prediction p = new Prediction();
        p.setId(1L);
        when(predictionRepository.findByUser(user)).thenReturn(List.of(p));

        List<Prediction> result = predictionService.findByUser(user);

        assertThat(result).hasSize(1);
        verify(predictionRepository).findByUser(user);
    }

    @Test
    void savePrediction_justBeforeDeadline_shouldSucceed() {
        Match nearDeadlineMatch = new Match();
        nearDeadlineMatch.setId(3L);
        nearDeadlineMatch.setCountryA("Italy");
        nearDeadlineMatch.setCountryB("Argentina");
        nearDeadlineMatch.setMatchDate(LocalDateTime.now().plusMinutes(90));

        when(matchRepository.findById(3L)).thenReturn(Optional.of(nearDeadlineMatch));
        when(predictionRepository.findByUserAndMatch(user, nearDeadlineMatch)).thenReturn(Optional.empty());

        PredictionDto dto = new PredictionDto();
        dto.setGoalsA(1);
        dto.setGoalsB(1);

        predictionService.savePrediction(3L, dto, user);

        verify(predictionRepository).save(any(Prediction.class));
    }
}
