package com.worldcup.dto;

public class TeamStandingDto {

    private String name;
    private String flag;
    private int played;
    private int won;
    private int drawn;
    private int lost;
    private int goalsFor;
    private int goalsAgainst;

    public TeamStandingDto(String name, String flag) {
        this.name = name;
        this.flag = flag;
    }

    public void recordResult(int scored, int conceded) {
        played++;
        goalsFor += scored;
        goalsAgainst += conceded;
        if (scored > conceded) won++;
        else if (scored == conceded) drawn++;
        else lost++;
    }

    public int getPoints()   { return won * 3 + drawn; }
    public int getGoalDiff() { return goalsFor - goalsAgainst; }

    public String getName()        { return name; }
    public String getFlag()        { return flag; }
    public int    getPlayed()      { return played; }
    public int    getWon()         { return won; }
    public int    getDrawn()       { return drawn; }
    public int    getLost()        { return lost; }
    public int    getGoalsFor()    { return goalsFor; }
    public int    getGoalsAgainst(){ return goalsAgainst; }
}
