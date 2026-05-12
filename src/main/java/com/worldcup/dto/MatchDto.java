package com.worldcup.dto;

import com.worldcup.domain.Match;
import com.worldcup.validation.ValidChecksum;
import com.worldcup.validation.ValidCountries;
import com.worldcup.validation.ValidMatchDate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

@ValidChecksum
@ValidCountries
public class MatchDto {

    private Long id;

    @NotBlank(message = "{validation.country.blank}")
    private String countryA;

    @NotBlank(message = "{validation.country.blank}")
    private String countryB;

    @NotNull(message = "{validation.date.null}")
    @ValidMatchDate
    private LocalDateTime matchDate;

    private String city;
    private String stadium;

    @Pattern(regexp = "\\d{4}", message = "{validation.stadiumcode.pattern}")
    private String stadiumCode;

    private Integer checksum;

    private Integer goalsA;
    private Integer goalsB;

    public static MatchDto from(Match match) {
        MatchDto dto = new MatchDto();
        dto.id = match.getId();
        dto.countryA = match.getCountryA();
        dto.countryB = match.getCountryB();
        dto.matchDate = match.getMatchDate();
        dto.city = match.getCity();
        dto.stadium = match.getStadium();
        dto.stadiumCode = match.getStadiumCode();
        dto.checksum = match.getChecksum();
        dto.goalsA = match.getGoalsA();
        dto.goalsB = match.getGoalsB();
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
    public Integer getChecksum() { return checksum; }
    public void setChecksum(Integer checksum) { this.checksum = checksum; }
    public Integer getGoalsA() { return goalsA; }
    public void setGoalsA(Integer goalsA) { this.goalsA = goalsA; }
    public Integer getGoalsB() { return goalsB; }
    public void setGoalsB(Integer goalsB) { this.goalsB = goalsB; }
}
