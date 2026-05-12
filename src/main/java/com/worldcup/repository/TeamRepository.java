package com.worldcup.repository;

import com.worldcup.domain.Team;
import com.worldcup.domain.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {
    Optional<Team> findByInviteCode(String inviteCode);
    Optional<Team> findByName(String name);
    boolean existsByInviteCode(String inviteCode);
    long countByMembersContaining(User user);

    @Query("SELECT t FROM Team t LEFT JOIN t.members m LEFT JOIN Prediction p ON p.user = m " +
           "GROUP BY t ORDER BY COALESCE(SUM(p.pointsAwarded), 0) DESC")
    List<Team> findTop10ByTotalScore(Pageable pageable);
}
