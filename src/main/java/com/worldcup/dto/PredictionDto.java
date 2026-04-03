package com.worldcup.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class PredictionDto {

    private Long matchId;

    @NotNull
    @Min(0)
    private Integer goalsA;

    @NotNull
    @Min(0)
    private Integer goalsB;

    public Long getMatchId() { return matchId; }
    public void setMatchId(Long matchId) { this.matchId = matchId; }
    public Integer getGoalsA() { return goalsA; }
    public void setGoalsA(Integer goalsA) { this.goalsA = goalsA; }
    public Integer getGoalsB() { return goalsB; }
    public void setGoalsB(Integer goalsB) { this.goalsB = goalsB; }
}
