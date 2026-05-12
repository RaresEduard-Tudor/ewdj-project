package com.worldcup.service;

import com.worldcup.domain.Match;
import com.worldcup.dto.MatchDto;
import com.worldcup.exception.DuplicateMatchException;
import com.worldcup.exception.MatchNotFoundException;
import com.worldcup.repository.MatchRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MatchService {

    private final MatchRepository matchRepository;
    private final ScoringService scoringService;

    private static final int DEFAULT_STADIUM_CAPACITY = 50000;
    private static final Map<String, Integer> STADIUM_CAPACITIES = new HashMap<>();
    static {
        STADIUM_CAPACITIES.put("1001", 82500);  // MetLife Stadium
        STADIUM_CAPACITIES.put("1111", 54500);  // BC Place
        STADIUM_CAPACITIES.put("1234", 80000);  // AT&T Stadium
        STADIUM_CAPACITIES.put("2222", 30000);  // BMO Field
        STADIUM_CAPACITIES.put("2345", 70240);  // SoFi Stadium
        STADIUM_CAPACITIES.put("3333", 87523);  // Estadio Azteca
        STADIUM_CAPACITIES.put("3456", 68500);  // Levi's Stadium
        STADIUM_CAPACITIES.put("4567", 65326);  // Hard Rock Stadium
        STADIUM_CAPACITIES.put("5678", 69176);  // Lincoln Financial Field
        STADIUM_CAPACITIES.put("6789", 76416);  // Arrowhead Stadium
        STADIUM_CAPACITIES.put("7890", 65878);  // Gillette Stadium
        STADIUM_CAPACITIES.put("8901", 76125);  // Empower Field at Mile High
        STADIUM_CAPACITIES.put("9012", 68740);  // Lumen Field
    }

    public MatchService(MatchRepository matchRepository, ScoringService scoringService) {
        this.matchRepository = matchRepository;
        this.scoringService = scoringService;
    }

    public List<Match> findAll() {
        return matchRepository.findAllByOrderByMatchDateAsc();
    }

    public List<Match> findByDate(LocalDate date) {
        return matchRepository.findByDate(date);
    }

    public List<Match> findUpcoming(int limit) {
        return matchRepository.findTop6ByGoalsAIsNullOrderByMatchDateAsc();
    }

    public Match findById(Long id) {
        return matchRepository.findById(id)
            .orElseThrow(() -> new MatchNotFoundException("Match not found: " + id));
    }

    @Transactional
    public void save(MatchDto dto) {
        if (dto.getStadium() != null && dto.getMatchDate() != null) {
            boolean duplicate = dto.getId() == null
                ? matchRepository.existsByStadiumAndMatchDate(dto.getStadium(), dto.getMatchDate())
                : matchRepository.existsByStadiumAndMatchDateAndIdNot(dto.getStadium(), dto.getMatchDate(), dto.getId());
            if (duplicate) {
                throw new DuplicateMatchException("A match at " + dto.getStadium() +
                    " on " + dto.getMatchDate() + " already exists.");
            }
        }
        Match match = (dto.getId() != null)
            ? matchRepository.findById(dto.getId())
                .orElseThrow(() -> new MatchNotFoundException("Match not found: " + dto.getId()))
            : new Match();
        match.setCountryA(dto.getCountryA());
        match.setCountryB(dto.getCountryB());
        match.setMatchDate(dto.getMatchDate());
        match.setCity(dto.getCity());
        match.setStadium(dto.getStadium());
        match.setStadiumCode(dto.getStadiumCode());
        match.setChecksum(dto.getChecksum());
        matchRepository.save(match);
        log.debug("Match saved: {} vs {}", dto.getCountryA(), dto.getCountryB());
    }

    @Transactional
    public Match saveResult(Long id, Integer goalsA, Integer goalsB) {
        Match match = findById(id);
        match.setGoalsA(goalsA);
        match.setGoalsB(goalsB);
        matchRepository.save(match);
        scoringService.calculateScoresForMatch(match);
        log.info("Result saved for match {}: {}-{}", id, goalsA, goalsB);
        return match;
    }

    @Transactional
    public void delete(Long id) {
        matchRepository.deleteById(id);
        log.debug("Match {} deleted", id);
    }

    public int getCapacityByStadiumCode(String code) {
        return STADIUM_CAPACITIES.getOrDefault(code, DEFAULT_STADIUM_CAPACITY);
    }
}
