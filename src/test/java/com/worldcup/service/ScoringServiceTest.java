package com.worldcup.service;

import com.worldcup.domain.Match;
import com.worldcup.domain.Prediction;
import com.worldcup.domain.Role;
import com.worldcup.domain.Team;
import com.worldcup.domain.User;
import com.worldcup.repository.MatchRepository;
import com.worldcup.repository.PredictionRepository;
import com.worldcup.repository.TeamRepository;
import com.worldcup.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.properties")
@Transactional
class ScoringServiceTest {

    @Autowired ScoringService scoringService;
    @Autowired MatchRepository matchRepository;
    @Autowired PredictionRepository predictionRepository;
    @Autowired UserRepository userRepository;
    @Autowired TeamRepository teamRepository;
    @Autowired EntityManager entityManager;

    private Long matchId;
    private Long userAId;
    private Long userBId;

    @BeforeEach
    void setUp() {
        Match match = new Match();
        match.setCountryA("Belgium");
        match.setCountryB("France");
        match.setMatchDate(LocalDateTime.of(2026, 6, 15, 18, 0));
        match.setCity("Test City");
        match.setStadium("Test Stadium");
        match.setStadiumCode("1234");
        match.setChecksum(70);
        match = matchRepository.save(match);
        matchId = match.getId();

        User userA = createUser("scoringUserA");
        User userB = createUser("scoringUserB");
        userAId = userA.getId();
        userBId = userB.getId();

        Team team = new Team();
        team.setName("TestTeam-" + UUID.randomUUID().toString().substring(0, 6));
        team.setInviteCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        team.setOwner(userA);
        team.getMembers().add(userA);
        team.getMembers().add(userB);
        teamRepository.save(team);

        // Flush to DB and clear persistence context so scoring service
        // loads fresh entities with correct inverse-side collections
        entityManager.flush();
        entityManager.clear();
    }

    private User createUser(String prefix) {
        User user = new User();
        user.setUsername(prefix + "-" + UUID.randomUUID().toString().substring(0, 6));
        user.setEmail(user.getUsername() + "@test.com");
        user.setPassword("encoded");
        user.setRole(Role.USER);
        return userRepository.save(user);
    }

    private void predict(Long userId, int goalsA, int goalsB) {
        User user = userRepository.findById(userId).orElseThrow();
        Match match = matchRepository.findById(matchId).orElseThrow();
        Prediction p = new Prediction();
        p.setUser(user);
        p.setMatch(match);
        p.setPredictedGoalsA(goalsA);
        p.setPredictedGoalsB(goalsB);
        predictionRepository.save(p);
        entityManager.flush();
        entityManager.clear();
    }

    private void setResult(int goalsA, int goalsB) {
        Match match = matchRepository.findById(matchId).orElseThrow();
        match.setGoalsA(goalsA);
        match.setGoalsB(goalsB);
        matchRepository.save(match);
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void exactScore_shouldAwardXPlusBonus() {
        predict(userAId, 2, 1);
        setResult(2, 1);

        Match match = matchRepository.findById(matchId).orElseThrow();
        scoringService.calculateScoresForMatch(match);
        entityManager.flush();
        entityManager.clear();

        User userA = userRepository.findById(userAId).orElseThrow();
        Match m = matchRepository.findById(matchId).orElseThrow();
        Prediction updated = predictionRepository.findByUserAndMatch(userA, m).orElseThrow();
        // Exact (10) + sole exact bonus in team (5) = 15
        assertEquals(15, updated.getPointsAwarded());
    }

    @Test
    void correctOutcomeOnly_shouldAwardYPlusBonus() {
        predict(userAId, 3, 0); // correct winner (A wins) but wrong score
        setResult(2, 1);

        Match match = matchRepository.findById(matchId).orElseThrow();
        scoringService.calculateScoresForMatch(match);
        entityManager.flush();
        entityManager.clear();

        User userA = userRepository.findById(userAId).orElseThrow();
        Match m = matchRepository.findById(matchId).orElseThrow();
        Prediction updated = predictionRepository.findByUserAndMatch(userA, m).orElseThrow();
        // Outcome (5) + sole outcome bonus (3) = 8
        assertEquals(8, updated.getPointsAwarded());
    }

    @Test
    void wrongPrediction_shouldAwardZeroPoints() {
        predict(userAId, 0, 3); // predicted B wins, but A wins
        setResult(2, 1);

        Match match = matchRepository.findById(matchId).orElseThrow();
        scoringService.calculateScoresForMatch(match);
        entityManager.flush();
        entityManager.clear();

        User userA = userRepository.findById(userAId).orElseThrow();
        Match m = matchRepository.findById(matchId).orElseThrow();
        Prediction updated = predictionRepository.findByUserAndMatch(userA, m).orElseThrow();
        assertEquals(0, updated.getPointsAwarded());
    }

    @Test
    void exactScore_notSoleInTeam_shouldNotGetBonus() {
        predict(userAId, 2, 1); // exact
        predict(userBId, 2, 1); // also exact — neither is sole
        setResult(2, 1);

        Match match = matchRepository.findById(matchId).orElseThrow();
        scoringService.calculateScoresForMatch(match);
        entityManager.flush();
        entityManager.clear();

        User userA = userRepository.findById(userAId).orElseThrow();
        User userB = userRepository.findById(userBId).orElseThrow();
        Match m = matchRepository.findById(matchId).orElseThrow();
        Prediction pA = predictionRepository.findByUserAndMatch(userA, m).orElseThrow();
        Prediction pB = predictionRepository.findByUserAndMatch(userB, m).orElseThrow();
        // Both get exact (10) but no sole bonus
        assertEquals(10, pA.getPointsAwarded());
        assertEquals(10, pB.getPointsAwarded());
    }

    @Test
    void draw_correctPrediction_shouldAwardPoints() {
        predict(userAId, 1, 1); // exact draw
        setResult(1, 1);

        Match match = matchRepository.findById(matchId).orElseThrow();
        scoringService.calculateScoresForMatch(match);
        entityManager.flush();
        entityManager.clear();

        User userA = userRepository.findById(userAId).orElseThrow();
        Match m = matchRepository.findById(matchId).orElseThrow();
        Prediction updated = predictionRepository.findByUserAndMatch(userA, m).orElseThrow();
        // Exact (10) + sole exact bonus (5) = 15
        assertEquals(15, updated.getPointsAwarded());
    }

    @Test
    void noResult_shouldNotScore() {
        predict(userAId, 2, 1);
        // match has no result set (goalsA/goalsB are null)

        Match match = matchRepository.findById(matchId).orElseThrow();
        scoringService.calculateScoresForMatch(match);

        User userA = userRepository.findById(userAId).orElseThrow();
        Match m = matchRepository.findById(matchId).orElseThrow();
        Prediction updated = predictionRepository.findByUserAndMatch(userA, m).orElseThrow();
        assertNull(updated.getPointsAwarded());
    }
}
