package com.worldcup.dto;

import com.worldcup.domain.Match;
import java.time.LocalDateTime;

public class MatchResponseDto {

    private Long id;
    private String countryA;
    private String countryB;
    private LocalDateTime matchDate;
    private String city;
    private String stadium;
    private String stadiumCode;

    public static MatchResponseDto from(Match match) {
        MatchResponseDto dto = new MatchResponseDto();
        dto.setId(match.getId());
        dto.setCountryA(match.getCountryA());
        dto.setCountryB(match.getCountryB());
        dto.setMatchDate(match.getMatchDate());
        dto.setCity(match.getCity());
        dto.setStadium(match.getStadium());
        dto.setStadiumCode(match.getStadiumCode());
        return dto;
    }

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
}
