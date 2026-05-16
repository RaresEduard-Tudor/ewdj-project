package com.worldcup.service;

import com.worldcup.domain.Match;
import com.worldcup.dto.MatchDto;
import com.worldcup.exception.DuplicateMatchException;
import com.worldcup.exception.MatchNotFoundException;
import com.worldcup.exception.ResultBeforeKickoffException;
import com.worldcup.repository.MatchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {

    @Mock MatchRepository matchRepository;
    @Mock ScoringService scoringService;
    @InjectMocks MatchService matchService;

    private Match sampleMatch;

    @BeforeEach
    void setUp() {
        sampleMatch = new Match();
        sampleMatch.setId(1L);
        sampleMatch.setCountryA("Brazil");
        sampleMatch.setCountryB("France");
        sampleMatch.setMatchDate(LocalDateTime.of(2026, 6, 20, 18, 0));
        sampleMatch.setStadium("MetLife Stadium");
        sampleMatch.setStadiumCode("1001");
    }

    @Test
    void findAll_shouldReturnAllMatches() {
        when(matchRepository.findAllByOrderByMatchDateAsc()).thenReturn(List.of(sampleMatch));
        List<Match> result = matchService.findAll();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCountryA()).isEqualTo("Brazil");
    }

    @Test
    void findById_whenExists_shouldReturnMatch() {
        when(matchRepository.findById(1L)).thenReturn(Optional.of(sampleMatch));
        Match result = matchService.findById(1L);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findById_whenNotFound_shouldThrowMatchNotFoundException() {
        when(matchRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> matchService.findById(99L))
            .isInstanceOf(MatchNotFoundException.class);
    }

    @Test
    void save_whenNoDuplicate_shouldPersist() {
        MatchDto dto = new MatchDto();
        dto.setCountryA("Germany");
        dto.setCountryB("Spain");
        dto.setMatchDate(LocalDateTime.of(2026, 6, 25, 15, 0));
        dto.setStadium("SoFi Stadium");
        dto.setStadiumCode("2345");

        matchService.save(dto);

        verify(matchRepository).save(any(Match.class));
    }

    @Test
    void save_whenDuplicateStadiumAndTime_shouldThrow() {
        when(matchRepository.existsByStadiumAndMatchDate(sampleMatch.getStadium(), sampleMatch.getMatchDate()))
            .thenReturn(true);

        MatchDto dto = new MatchDto();
        dto.setCountryA("Portugal");
        dto.setCountryB("Spain");
        dto.setMatchDate(sampleMatch.getMatchDate());
        dto.setStadium(sampleMatch.getStadium());

        assertThatThrownBy(() -> matchService.save(dto))
            .isInstanceOf(DuplicateMatchException.class);
        verify(matchRepository, never()).save(any());
    }

    @Test
    void save_whenCountryAlreadyPlaysSameDay_shouldThrow() {
        LocalDateTime when = LocalDateTime.of(2026, 6, 20, 19, 10);
        when(matchRepository.existsCountryOnDate(eq("Belgium"), eq(when.toLocalDate()), isNull()))
            .thenReturn(true);

        MatchDto dto = new MatchDto();
        dto.setCountryA("Belgium");
        dto.setCountryB("Egypt");
        dto.setMatchDate(when);
        dto.setStadium("BC Place");
        dto.setStadiumCode("1111");

        assertThatThrownBy(() -> matchService.save(dto))
            .isInstanceOf(DuplicateMatchException.class)
            .hasMessageContaining("Belgium");
        verify(matchRepository, never()).save(any());
    }

    @Test
    void save_whenEditingSameMatchOnSameDay_shouldNotThrow() {
        LocalDateTime when = LocalDateTime.of(2026, 6, 20, 19, 10);
        when(matchRepository.existsCountryOnDate(anyString(), eq(when.toLocalDate()), eq(1L)))
            .thenReturn(false);
        when(matchRepository.findById(1L)).thenReturn(Optional.of(sampleMatch));

        MatchDto dto = new MatchDto();
        dto.setId(1L);
        dto.setCountryA("Belgium");
        dto.setCountryB("Egypt");
        dto.setMatchDate(when);
        dto.setStadium("BC Place");
        dto.setStadiumCode("1111");

        matchService.save(dto);

        verify(matchRepository).save(any(Match.class));
    }

    @Test
    void save_whenEditingMissingMatch_shouldThrow() {
        when(matchRepository.findById(99L)).thenReturn(Optional.empty());

        MatchDto dto = new MatchDto();
        dto.setId(99L);
        dto.setCountryA("Brazil");
        dto.setCountryB("Italy");
        dto.setMatchDate(LocalDateTime.of(2026, 6, 25, 15, 0));

        assertThatThrownBy(() -> matchService.save(dto))
            .isInstanceOf(MatchNotFoundException.class);
        verify(matchRepository, never()).save(any());
    }

    @Test
    void saveResult_shouldUpdateGoalsAndTriggerScoring() {
        sampleMatch.setMatchDate(LocalDateTime.now().minusDays(1));
        when(matchRepository.findById(1L)).thenReturn(Optional.of(sampleMatch));

        matchService.saveResult(1L, 2, 1);

        verify(matchRepository).save(argThat(m -> m.getGoalsA() == 2 && m.getGoalsB() == 1));
        verify(scoringService).calculateScoresForMatch(sampleMatch);
    }

    @Test
    void saveResult_whenMatchInFuture_shouldThrow() {
        sampleMatch.setMatchDate(LocalDateTime.now().plusDays(1));
        when(matchRepository.findById(1L)).thenReturn(Optional.of(sampleMatch));

        assertThatThrownBy(() -> matchService.saveResult(1L, 2, 1))
            .isInstanceOf(ResultBeforeKickoffException.class);
        verify(matchRepository, never()).save(any());
        verify(scoringService, never()).calculateScoresForMatch(any());
    }

    @Test
    void saveResult_whenMatchNotFound_shouldThrow() {
        when(matchRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> matchService.saveResult(99L, 1, 0))
            .isInstanceOf(MatchNotFoundException.class);
    }

    @Test
    void delete_shouldCallRepository() {
        matchService.delete(1L);
        verify(matchRepository).deleteById(1L);
    }

    @Test
    void getCapacityByStadiumCode_knownCode_shouldReturnCapacity() {
        int capacity = matchService.getCapacityByStadiumCode("1001");
        assertThat(capacity).isEqualTo(82500);
    }

    @Test
    void getCapacityByStadiumCode_unknownCode_shouldReturnDefault() {
        int capacity = matchService.getCapacityByStadiumCode("9999");
        assertThat(capacity).isEqualTo(50000);
    }
}
