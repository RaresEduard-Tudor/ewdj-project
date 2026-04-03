package com.worldcup.repository;

import com.worldcup.domain.Match;
import com.worldcup.domain.Prediction;
import com.worldcup.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {
    Optional<Prediction> findByUserAndMatch(User user, Match match);
    List<Prediction> findByMatch(Match match);
    List<Prediction> findByUser(User user);

    @Query("SELECT COALESCE(SUM(p.pointsAwarded), 0) FROM Prediction p WHERE p.user = :user")
    Integer getTotalScoreForUser(@Param("user") User user);
}
