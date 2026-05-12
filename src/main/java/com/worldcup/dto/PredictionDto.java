package com.worldcup.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class PredictionDto {

    @NotNull
    @Min(0)
    private Integer goalsA = 0;

    @NotNull
    @Min(0)
    private Integer goalsB = 0;

    public Integer getGoalsA() { return goalsA; }
    public void setGoalsA(Integer goalsA) { this.goalsA = goalsA; }
    public Integer getGoalsB() { return goalsB; }
    public void setGoalsB(Integer goalsB) { this.goalsB = goalsB; }
}
