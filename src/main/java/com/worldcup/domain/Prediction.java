package com.worldcup.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "predictions",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "match_id"}))
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private Match match;

    @Column(nullable = false)
    private Integer predictedGoalsA;

    @Column(nullable = false)
    private Integer predictedGoalsB;

    private Integer pointsAwarded;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Match getMatch() { return match; }
    public void setMatch(Match match) { this.match = match; }
    public Integer getPredictedGoalsA() { return predictedGoalsA; }
    public void setPredictedGoalsA(Integer predictedGoalsA) { this.predictedGoalsA = predictedGoalsA; }
    public Integer getPredictedGoalsB() { return predictedGoalsB; }
    public void setPredictedGoalsB(Integer predictedGoalsB) { this.predictedGoalsB = predictedGoalsB; }
    public Integer getPointsAwarded() { return pointsAwarded; }
    public void setPointsAwarded(Integer pointsAwarded) { this.pointsAwarded = pointsAwarded; }
}
