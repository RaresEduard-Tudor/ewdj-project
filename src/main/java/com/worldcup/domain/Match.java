package com.worldcup.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "matches")
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String countryA;

    @Column(nullable = false)
    private String countryB;

    @Column(nullable = false)
    private LocalDateTime matchDate;

    private String city;
    private String stadium;

    @Column(length = 4)
    private String stadiumCode;

    private Integer checksum;

    private Integer goalsA;
    private Integer goalsB;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCountryA() { return countryA; }
    public void setCountryA(String countryA) { this.countryA = countryA; }
    public String getCountryB() { return countryB; }
    public void setCountryB(String countryB) { this.countryB = countryB; }
    public LocalDateTime getMatchDate() { return matchDate; }
    public void setMatchDate(LocalDateTime matchDate) { this.matchDate = matchDate; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getStadium() { return stadium; }
    public void setStadium(String stadium) { this.stadium = stadium; }
    public String getStadiumCode() { return stadiumCode; }
    public void setStadiumCode(String stadiumCode) { this.stadiumCode = stadiumCode; }
    public Integer getChecksum() { return checksum; }
    public void setChecksum(Integer checksum) { this.checksum = checksum; }
    public Integer getGoalsA() { return goalsA; }
    public void setGoalsA(Integer goalsA) { this.goalsA = goalsA; }
    public Integer getGoalsB() { return goalsB; }
    public void setGoalsB(Integer goalsB) { this.goalsB = goalsB; }
}
