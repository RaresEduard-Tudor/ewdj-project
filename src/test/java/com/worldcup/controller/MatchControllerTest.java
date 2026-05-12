package com.worldcup.controller;

import com.worldcup.client.StadiumCapacityClient;
import com.worldcup.config.PasswordEncoderConfig;
import com.worldcup.config.SecurityConfig;
import com.worldcup.domain.Match;
import com.worldcup.domain.User;
import com.worldcup.exception.MatchNotFoundException;
import com.worldcup.interceptor.AdminAuditInterceptor;
import com.worldcup.repository.PredictionRepository;
import com.worldcup.service.MatchService;
import com.worldcup.service.PredictionService;
import com.worldcup.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MatchController.class)
@Import({SecurityConfig.class, PasswordEncoderConfig.class,
    com.worldcup.util.FlagUtil.class, com.worldcup.util.CountryRegistry.class})
class MatchControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean MatchService matchService;
    @MockitoBean PredictionService predictionService;
    @MockitoBean UserService userService;
    @MockitoBean PredictionRepository predictionRepository;
    @MockitoBean StadiumCapacityClient stadiumCapacityClient;
    @MockitoBean AdminAuditInterceptor adminAuditInterceptor;
    private Match sampleMatch;

    @BeforeEach
    void setUp() throws Exception {
        when(adminAuditInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        sampleMatch = new Match();
        sampleMatch.setId(1L);
        sampleMatch.setCountryA("Brazil");
        sampleMatch.setCountryB("France");
        sampleMatch.setMatchDate(LocalDateTime.of(2026, 6, 20, 18, 0));
        sampleMatch.setCity("New York");
        sampleMatch.setStadiumCode("1001");
    }

    @Test
    void matchList_whenAnonymous_shouldReturn200() throws Exception {
        when(matchService.findAll()).thenReturn(List.of(sampleMatch));
        mockMvc.perform(get("/matches"))
            .andExpect(status().isOk())
            .andExpect(view().name("match/list"))
            .andExpect(model().attributeExists("matches"));
    }

    @Test
    @WithMockUser
    void matchList_whenAuthenticated_shouldIncludeUserPredictions() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        when(matchService.findAll()).thenReturn(List.of(sampleMatch));
        when(userService.findByUsername("testuser")).thenReturn(user);
        when(predictionRepository.findByUser(user)).thenReturn(List.of());

        mockMvc.perform(get("/matches"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("userPredictions"));
    }

    @Test
    @WithMockUser
    void matchDetail_whenExists_shouldReturn200() throws Exception {
        when(matchService.findById(1L)).thenReturn(sampleMatch);
        when(stadiumCapacityClient.fetchCapacity("1001")).thenReturn(82500);

        mockMvc.perform(get("/matches/1"))
            .andExpect(status().isOk())
            .andExpect(view().name("match/detail"))
            .andExpect(model().attributeExists("match", "predictionDto"));
    }

    @Test
    @WithMockUser
    void matchDetail_whenNotFound_shouldReturn404() throws Exception {
        when(matchService.findById(99L)).thenThrow(new MatchNotFoundException("Not found"));
        mockMvc.perform(get("/matches/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void predict_whenAnonymous_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(post("/matches/1/predict")
                .param("goalsA", "2").param("goalsB", "1")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser
    void predict_whenValidInput_shouldRedirect() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        when(userService.findByUsername("testuser")).thenReturn(user);

        mockMvc.perform(post("/matches/1/predict")
                .param("goalsA", "2").param("goalsB", "1")
                .with(csrf()))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser
    void predict_whenNegativeGoals_shouldRedirectWithError() throws Exception {
        mockMvc.perform(post("/matches/1/predict")
                .param("goalsA", "-1").param("goalsB", "0")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(flash().attributeExists("predictionError"));
    }

    @Test
    @WithMockUser
    void predict_withCustomRedirectTo_shouldRedirectToCustomUrl() throws Exception {
        User user = new User();
        user.setUsername("testuser");
        when(userService.findByUsername("testuser")).thenReturn(user);

        mockMvc.perform(post("/matches/1/predict")
                .param("goalsA", "1").param("goalsB", "1")
                .param("redirectTo", "/predictions")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/predictions"));
    }
}
