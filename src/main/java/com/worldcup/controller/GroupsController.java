package com.worldcup.controller;

import com.worldcup.domain.Match;
import com.worldcup.dto.TeamStandingDto;
import com.worldcup.service.MatchService;
import com.worldcup.util.CountryRegistry;
import com.worldcup.util.CountryRegistry.Country;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@Slf4j
public class GroupsController {

    private final MatchService matchService;
    private final CountryRegistry countryRegistry;

    public GroupsController(MatchService matchService, CountryRegistry countryRegistry) {
        this.matchService = matchService;
        this.countryRegistry = countryRegistry;
    }

    @GetMapping("/groups")
    public String groups(Model model) {
        List<Match> allMatches = matchService.findAll();

        Map<String, List<TeamStandingDto>> standings = new LinkedHashMap<>();
        Map<String, Match> nextFixtures = new LinkedHashMap<>();

        for (Map.Entry<String, List<Country>> entry : countryRegistry.getGroups().entrySet()) {
            String groupKey = entry.getKey();
            List<Country> teams = entry.getValue();

            Set<String> teamNames = teams.stream()
                .map(Country::name)
                .collect(Collectors.toSet());

            Map<String, TeamStandingDto> standingMap = new LinkedHashMap<>();
            for (Country team : teams) {
                standingMap.put(team.name(), new TeamStandingDto(team.name(), team.flag()));
            }

            Match nextFixture = null;
            for (Match match : allMatches) {
                if (!teamNames.contains(match.getCountryA())
                        || !teamNames.contains(match.getCountryB())) continue;

                if (match.getGoalsA() != null) {
                    standingMap.get(match.getCountryA())
                        .recordResult(match.getGoalsA(), match.getGoalsB());
                    standingMap.get(match.getCountryB())
                        .recordResult(match.getGoalsB(), match.getGoalsA());
                } else if (nextFixture == null) {
                    nextFixture = match;
                }
            }

            // FIFA WC 2026 tiebreaker: pts → GD → GF → alphabetical (head-to-head omitted)
            List<TeamStandingDto> sorted = standingMap.values().stream()
                .sorted(Comparator
                    .comparingInt(TeamStandingDto::getPoints).reversed()
                    .thenComparingInt(TeamStandingDto::getGoalDiff).reversed()
                    .thenComparingInt(TeamStandingDto::getGoalsFor).reversed()
                    .thenComparing(TeamStandingDto::getName))
                .collect(Collectors.toList());

            standings.put(groupKey, sorted);
            if (nextFixture != null) nextFixtures.put(groupKey, nextFixture);
        }

        model.addAttribute("standings", standings);
        model.addAttribute("nextFixtures", nextFixtures);
        log.debug("Computed standings for {} groups", standings.size());
        return "groups";
    }
}
