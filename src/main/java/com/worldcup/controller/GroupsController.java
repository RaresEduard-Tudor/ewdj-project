package com.worldcup.controller;

import com.worldcup.domain.Match;
import com.worldcup.dto.TeamStandingDto;
import com.worldcup.service.MatchService;
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

    public GroupsController(MatchService matchService) {
        this.matchService = matchService;
    }

    // [teamName, flagEmoji] — order determines initial table row before any results
    private static final Map<String, List<String[]>> GROUPS = new LinkedHashMap<>();

    static {
        GROUPS.put("A", List.of(
            new String[]{"Mexico",       "🇲🇽"},
            new String[]{"South Africa", "🇿🇦"},
            new String[]{"South Korea",  "🇰🇷"},
            new String[]{"Czechia",      "🇨🇿"}
        ));
        GROUPS.put("B", List.of(
            new String[]{"Canada",       "🇨🇦"},
            new String[]{"Bosnia",       "🇧🇦"},
            new String[]{"Qatar",        "🇶🇦"},
            new String[]{"Switzerland",  "🇨🇭"}
        ));
        GROUPS.put("C", List.of(
            new String[]{"Brazil",       "🇧🇷"},
            new String[]{"Morocco",      "🇲🇦"},
            new String[]{"Haiti",        "🇭🇹"},
            new String[]{"Scotland",     "🏴󠁧󠁢󠁳󠁣󠁴󠁿"}
        ));
        GROUPS.put("D", List.of(
            new String[]{"USA",          "🇺🇸"},
            new String[]{"Paraguay",     "🇵🇾"},
            new String[]{"Australia",    "🇦🇺"},
            new String[]{"Turkey",       "🇹🇷"}
        ));
        GROUPS.put("E", List.of(
            new String[]{"Germany",       "🇩🇪"},
            new String[]{"Curacao",       "🇨🇼"},
            new String[]{"Cote d'Ivoire", "🇨🇮"},
            new String[]{"Ecuador",       "🇪🇨"}
        ));
        GROUPS.put("F", List.of(
            new String[]{"Netherlands",  "🇳🇱"},
            new String[]{"Japan",        "🇯🇵"},
            new String[]{"Sweden",       "🇸🇪"},
            new String[]{"Tunisia",      "🇹🇳"}
        ));
        GROUPS.put("G", List.of(
            new String[]{"Belgium",      "🇧🇪"},
            new String[]{"Egypt",        "🇪🇬"},
            new String[]{"IR Iran",      "🇮🇷"},
            new String[]{"New Zealand",  "🇳🇿"}
        ));
        GROUPS.put("H", List.of(
            new String[]{"Spain",        "🇪🇸"},
            new String[]{"Cabo Verde",   "🇨🇻"},
            new String[]{"Saudi Arabia", "🇸🇦"},
            new String[]{"Uruguay",      "🇺🇾"}
        ));
        GROUPS.put("I", List.of(
            new String[]{"France",       "🇫🇷"},
            new String[]{"Senegal",      "🇸🇳"},
            new String[]{"Iraq",         "🇮🇶"},
            new String[]{"Norway",       "🇳🇴"}
        ));
        GROUPS.put("J", List.of(
            new String[]{"Argentina",    "🇦🇷"},
            new String[]{"Algeria",      "🇩🇿"},
            new String[]{"Austria",      "🇦🇹"},
            new String[]{"Jordan",       "🇯🇴"}
        ));
        GROUPS.put("K", List.of(
            new String[]{"Portugal",     "🇵🇹"},
            new String[]{"Congo",        "🇨🇩"},
            new String[]{"Uzbekistan",   "🇺🇿"},
            new String[]{"Colombia",     "🇨🇴"}
        ));
        GROUPS.put("L", List.of(
            new String[]{"England",      "🏴󠁧󠁢󠁥󠁮󠁧󠁿"},
            new String[]{"Croatia",      "🇭🇷"},
            new String[]{"Ghana",        "🇬🇭"},
            new String[]{"Panama",       "🇵🇦"}
        ));
    }

    @GetMapping("/groups")
    public String groups(Model model) {
        List<Match> allMatches = matchService.findAll();

        Map<String, List<TeamStandingDto>> standings = new LinkedHashMap<>();
        Map<String, Match> nextFixtures = new LinkedHashMap<>();

        for (Map.Entry<String, List<String[]>> entry : GROUPS.entrySet()) {
            String groupKey = entry.getKey();
            List<String[]> teams = entry.getValue();

            Set<String> teamNames = teams.stream()
                .map(t -> t[0])
                .collect(Collectors.toSet());

            // Initialise one standing row per team (preserves draw order)
            Map<String, TeamStandingDto> standingMap = new LinkedHashMap<>();
            for (String[] team : teams) {
                standingMap.put(team[0], new TeamStandingDto(team[0], team[1]));
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
