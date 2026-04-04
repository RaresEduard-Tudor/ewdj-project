package com.worldcup.service;

import com.worldcup.domain.Match;
import com.worldcup.dto.MatchDto;
import com.worldcup.exception.DuplicateMatchException;
import com.worldcup.exception.TeamNotFoundException;
import com.worldcup.repository.MatchRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class MatchService {

    private final MatchRepository matchRepository;

    private static final Map<String, Integer> STADIUM_CAPACITIES = new HashMap<>();
    static {
        STADIUM_CAPACITIES.put("1001", 80000);
        STADIUM_CAPACITIES.put("1002", 70000);
        STADIUM_CAPACITIES.put("1003", 65000);
        STADIUM_CAPACITIES.put("1004", 60000);
    }

    public MatchService(MatchRepository matchRepository) {
        this.matchRepository = matchRepository;
    }

    public List<Match> findAll() {
        return matchRepository.findAllByOrderByMatchDateAsc();
    }

    public List<Match> findByDate(LocalDate date) {
        return matchRepository.findByDate(date);
    }

    public Match findById(Long id) {
        return matchRepository.findById(id)
            .orElseThrow(() -> new TeamNotFoundException("Match not found: " + id));
    }

    public void save(MatchDto dto) {
        if (dto.getStadium() != null && dto.getMatchDate() != null) {
            boolean duplicate = matchRepository.findAll().stream()
                .filter(m -> !m.getId().equals(dto.getId()))
                .anyMatch(m -> m.getStadium() != null &&
                    m.getStadium().equals(dto.getStadium()) &&
                    m.getMatchDate() != null &&
                    m.getMatchDate().equals(dto.getMatchDate()));
            if (duplicate) {
                throw new DuplicateMatchException("A match at " + dto.getStadium() +
                    " on " + dto.getMatchDate() + " already exists.");
            }
        }
        Match match = (dto.getId() != null) ?
            matchRepository.findById(dto.getId()).orElse(new Match()) : new Match();
        match.setCountryA(dto.getCountryA());
        match.setCountryB(dto.getCountryB());
        match.setMatchDate(dto.getMatchDate());
        match.setCity(dto.getCity());
        match.setStadium(dto.getStadium());
        match.setStadiumCode(dto.getStadiumCode());
        match.setChecksum(dto.getChecksum());
        matchRepository.save(match);
        log.info("Match saved: {} vs {}", dto.getCountryA(), dto.getCountryB());
    }

    public void saveResult(Long id, Integer goalsA, Integer goalsB) {
        Match match = findById(id);
        match.setGoalsA(goalsA);
        match.setGoalsB(goalsB);
        matchRepository.save(match);
        log.info("Result saved for match {}: {}-{}", id, goalsA, goalsB);
    }

    public void delete(Long id) {
        matchRepository.deleteById(id);
        log.info("Match {} deleted", id);
    }

    public int getCapacityByStadiumCode(String code) {
        return STADIUM_CAPACITIES.getOrDefault(code, 50000);
    }
}
