package com.worldcup.repository;

import com.worldcup.domain.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findAllByOrderByMatchDateAsc();

    @Query("SELECT m FROM Match m WHERE FUNCTION('DATE', m.matchDate) = :date ORDER BY m.matchDate")
    List<Match> findByDate(@Param("date") LocalDate date);

    boolean existsByStadiumAndMatchDate(String stadium, LocalDateTime matchDate);
}
