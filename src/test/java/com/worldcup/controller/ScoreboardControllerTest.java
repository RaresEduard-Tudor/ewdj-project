package com.worldcup.controller;

import com.worldcup.config.PasswordEncoderConfig;
import com.worldcup.config.SecurityConfig;
import com.worldcup.interceptor.AdminAuditInterceptor;
import com.worldcup.repository.PredictionRepository;
import com.worldcup.repository.TeamRepository;
import com.worldcup.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ScoreboardController.class)
@Import({SecurityConfig.class, PasswordEncoderConfig.class})
class ScoreboardControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean TeamRepository teamRepository;
    @MockitoBean PredictionRepository predictionRepository;
    @MockitoBean UserService userService;
    @MockitoBean AdminAuditInterceptor adminAuditInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        when(adminAuditInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    void publicTop10_whenAnonymous_shouldBeOk() throws Exception {
        when(teamRepository.findTop10ByTotalScore(any(Pageable.class))).thenReturn(List.of());
        mockMvc.perform(get("/top10"))
            .andExpect(status().isOk())
            .andExpect(view().name("scoreboard/public-top10"))
            .andExpect(model().attributeExists("teams", "teamScores"));
    }

    @Test
    @WithMockUser
    void publicTop10_whenAuthenticated_shouldBeOk() throws Exception {
        when(teamRepository.findTop10ByTotalScore(any(Pageable.class))).thenReturn(List.of());
        mockMvc.perform(get("/top10"))
            .andExpect(status().isOk())
            .andExpect(view().name("scoreboard/public-top10"));
    }

    @Test
    void publicTop10_withEmptyTeams_shouldShowEmptyState() throws Exception {
        when(teamRepository.findTop10ByTotalScore(any(Pageable.class))).thenReturn(List.of());
        mockMvc.perform(get("/top10"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("teams", List.of()));
    }
}
