package com.worldcup.repository;

import com.worldcup.domain.Match;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {

    boolean existsByStadiumAndMatchDate(String stadium, LocalDateTime matchDate);

    boolean existsByStadiumAndMatchDateAndIdNot(String stadium, LocalDateTime matchDate, Long id);

    @Query("SELECT COUNT(m) > 0 FROM Match m WHERE " +
           "(m.countryA = :country OR m.countryB = :country) AND " +
           "FUNCTION('DATE', m.matchDate) = :date AND " +
           "(:excludeId IS NULL OR m.id <> :excludeId)")
    boolean existsCountryOnDate(@Param("country") String country,
                                @Param("date") LocalDate date,
                                @Param("excludeId") Long excludeId);

    List<Match> findAllByOrderByMatchDateAsc();

    List<Match> findTop6ByGoalsAIsNullOrderByMatchDateAsc();

    @Query("SELECT m FROM Match m WHERE FUNCTION('DATE', m.matchDate) = :date ORDER BY m.matchDate")
    List<Match> findByDate(@Param("date") LocalDate date);
}
