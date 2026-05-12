package com.worldcup.rest;

import com.worldcup.dto.MatchResponseDto;
import com.worldcup.service.MatchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/matches")
@Slf4j
public class MatchRestController {

    private final MatchService matchService;

    public MatchRestController(MatchService matchService) {
        this.matchService = matchService;
    }

    @GetMapping
    public List<MatchResponseDto> getMatchesByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        log.debug("REST: fetching matches for date {}", date);
        return matchService.findByDate(date).stream()
            .map(MatchResponseDto::from)
            .toList();
    }

    @GetMapping("/stadiums/{code}/capacity")
    public ResponseEntity<Integer> getCapacityByCode(@PathVariable String code) {
        int capacity = matchService.getCapacityByStadiumCode(code);
        return ResponseEntity.ok(capacity);
    }
}
