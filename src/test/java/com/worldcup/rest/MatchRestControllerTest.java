package com.worldcup.rest;

import com.worldcup.config.PasswordEncoderConfig;
import com.worldcup.config.SecurityConfig;
import com.worldcup.domain.Match;
import com.worldcup.interceptor.AdminAuditInterceptor;
import com.worldcup.service.MatchService;
import com.worldcup.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MatchRestController.class)
@Import({SecurityConfig.class, PasswordEncoderConfig.class})
@WithMockUser
class MatchRestControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MatchService matchService;

    @MockitoBean
    UserService userService;

    @MockitoBean
    AdminAuditInterceptor adminAuditInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        when(adminAuditInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    void getMatchesByDate_shouldReturnJsonList() throws Exception {
        when(matchService.findByDate(any())).thenReturn(List.of());
        mockMvc.perform(get("/api/matches?date=2026-06-11"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getCapacityByCode_shouldReturnCapacity() throws Exception {
        when(matchService.getCapacityByStadiumCode("1001")).thenReturn(80000);
        mockMvc.perform(get("/api/matches/stadiums/1001/capacity"))
            .andExpect(status().isOk());
    }

    @Test
    void getMatchesByDate_whenMatchesExist_shouldReturnNonEmptyList() throws Exception {
        Match match = new Match();
        match.setId(1L);
        match.setCountryA("Brazil");
        match.setCountryB("France");
        match.setMatchDate(LocalDateTime.of(2026, 6, 11, 18, 0));

        when(matchService.findByDate(any())).thenReturn(List.of(match));

        mockMvc.perform(get("/api/matches?date=2026-06-11"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getCapacityByCode_unknownCode_shouldReturnDefault() throws Exception {
        when(matchService.getCapacityByStadiumCode("9999")).thenReturn(50000);
        mockMvc.perform(get("/api/matches/stadiums/9999/capacity"))
            .andExpect(status().isOk())
            .andExpect(content().string("50000"));
    }

    @Test
    void getMatchesByDate_whenAnonymous_shouldBeOk() throws Exception {
        when(matchService.findByDate(any())).thenReturn(List.of());
        mockMvc.perform(get("/api/matches?date=2026-07-01")
                .with(request -> { request.setRemoteUser(null); return request; }))
            .andExpect(status().isOk());
    }

    @Test
    void getCapacityByCode_knownStadium_shouldReturnCorrectValue() throws Exception {
        when(matchService.getCapacityByStadiumCode("1001")).thenReturn(82500);
        mockMvc.perform(get("/api/matches/stadiums/1001/capacity"))
            .andExpect(status().isOk())
            .andExpect(content().string("82500"));
    }
}
