package com.worldcup;

import com.worldcup.domain.Match;
import com.worldcup.domain.User;
import com.worldcup.repository.MatchRepository;
import com.worldcup.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final MatchRepository matchRepository;

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder,
                      MatchRepository matchRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.matchRepository = matchRepository;
    }

    @Override
    public void run(String... args) {
        seedAdmin();
        seedMatches();
    }

    private void seedAdmin() {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@worldcup.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ROLE_ADMIN");
            userRepository.save(admin);
            log.info("Seeded default admin user.");
        }
    }

    private void seedMatches() {
        if (matchRepository.count() > 0) return;

        // Stadium codes (4-digit) and checksums: code % 97
        // Azteca=3333→35, BBVA=4444→79, Akron=5555→26
        // BCPlace=1111→44, BMO=2222→88
        // MetLife=1001→31, ATT=1234→70, SoFi=2345→17, Levis=3456→61
        // HardRock=4567→8, Lincoln=5678→52, Arrowhead=6789→96
        // Gillette=7890→33, MileHigh=8901→74, Lumen=9012→88

        List<Match> matches = List.of(
            // Group A: Mexico, South Africa, South Korea, Czechia
            match("Mexico",          "South Korea",   "2026-06-11T18:00", "Mexico City",    "Estadio Azteca",           "3333", 35),
            match("South Africa",    "Czechia",       "2026-06-12T15:00", "Arlington",      "AT&T Stadium",             "1234", 70),

            // Group B: Canada, Bosnia, Qatar, Switzerland
            match("Canada",          "Qatar",         "2026-06-12T21:00", "Toronto",        "BMO Field",                "2222", 88),
            match("Switzerland",     "Bosnia",        "2026-06-13T15:00", "Inglewood",      "SoFi Stadium",             "2345", 17),

            // Group C: Brazil, Morocco, Haiti, Scotland
            match("Brazil",          "Scotland",      "2026-06-13T18:00", "East Rutherford","MetLife Stadium",           "1001", 31),
            match("Morocco",         "Haiti",         "2026-06-13T21:00", "Kansas City",    "Arrowhead Stadium",         "6789", 96),

            // Group D: USA, Paraguay, Australia, Turkey
            match("USA",             "Paraguay",      "2026-06-14T21:00", "Inglewood",      "SoFi Stadium",             "2345", 17),
            match("Australia",       "Turkey",        "2026-06-14T18:00", "Philadelphia",   "Lincoln Financial Field",   "5678", 52),

            // Group E: Germany, Curaçao, Côte d'Ivoire, Ecuador
            match("Germany",         "Ecuador",       "2026-06-15T18:00", "Miami Gardens",  "Hard Rock Stadium",         "4567",  8),
            match("Cote d'Ivoire",   "Curacao",       "2026-06-15T21:00", "Foxborough",     "Gillette Stadium",          "7890", 33),

            // Group F: Netherlands, Japan, Sweden, Tunisia
            match("Netherlands",     "Tunisia",       "2026-06-16T18:00", "Santa Clara",    "Levi's Stadium",            "3456", 61),
            match("Japan",           "Sweden",        "2026-06-16T21:00", "Denver",         "Empower Field Mile High",   "8901", 74),

            // Group G: Belgium, Egypt, IR Iran, New Zealand
            match("Belgium",         "New Zealand",   "2026-06-17T18:00", "Vancouver",      "BC Place",                  "1111", 44),
            match("Egypt",           "IR Iran",       "2026-06-17T21:00", "Seattle",        "Lumen Field",               "9012", 88),

            // Group H: Spain, Cabo Verde, Saudi Arabia, Uruguay
            match("Spain",           "Uruguay",       "2026-06-18T21:00", "East Rutherford","MetLife Stadium",           "1001", 31),
            match("Saudi Arabia",    "Cabo Verde",    "2026-06-18T18:00", "Mexico City",    "Estadio Azteca",            "3333", 35),

            // Group I: France, Senegal, Iraq, Norway
            match("France",          "Norway",        "2026-06-19T21:00", "Miami Gardens",  "Hard Rock Stadium",         "4567",  8),
            match("Senegal",         "Iraq",          "2026-06-19T18:00", "Arlington",      "AT&T Stadium",              "1234", 70),

            // Group J: Argentina, Algeria, Austria, Jordan
            match("Argentina",       "Jordan",        "2026-06-20T18:00", "Foxborough",     "Gillette Stadium",          "7890", 33),
            match("Algeria",         "Austria",       "2026-06-20T21:00", "Denver",         "Empower Field Mile High",   "8901", 74),

            // Group K: Portugal, Congo, Uzbekistan, Colombia
            match("Portugal",        "Colombia",      "2026-06-21T21:00", "Kansas City",    "Arrowhead Stadium",         "6789", 96),
            match("Uzbekistan",      "Congo",         "2026-06-21T18:00", "Philadelphia",   "Lincoln Financial Field",   "5678", 52),

            // Group L: England, Croatia, Ghana, Panama
            match("England",         "Panama",        "2026-06-22T21:00", "Seattle",        "Lumen Field",               "9012", 88),
            match("Croatia",         "Ghana",         "2026-06-22T18:00", "Vancouver",      "BC Place",                  "1111", 44)
        );

        matchRepository.saveAll(matches);
        log.info("Seeded {} WC 2026 matches.", matches.size());
    }

    private Match match(String a, String b, String dateTime, String city,
                        String stadium, String code, int checksum) {
        Match m = new Match();
        m.setCountryA(a);
        m.setCountryB(b);
        m.setMatchDate(LocalDateTime.parse(dateTime));
        m.setCity(city);
        m.setStadium(stadium);
        m.setStadiumCode(code);
        m.setChecksum(checksum);
        return m;
    }
}
