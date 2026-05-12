package com.worldcup.controller;

import com.worldcup.config.PasswordEncoderConfig;
import com.worldcup.config.SecurityConfig;
import com.worldcup.domain.Match;
import com.worldcup.domain.Prediction;
import com.worldcup.domain.User;
import com.worldcup.interceptor.AdminAuditInterceptor;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PredictionController.class)
@Import({SecurityConfig.class, PasswordEncoderConfig.class,
    com.worldcup.util.FlagUtil.class, com.worldcup.util.CountryRegistry.class})
class PredictionControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean PredictionService predictionService;
    @MockitoBean UserService userService;
    @MockitoBean AdminAuditInterceptor adminAuditInterceptor;

    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        when(adminAuditInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
    }

    @Test
    void predictionsPage_whenAnonymous_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/predictions"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void predictionsPage_whenAuthenticated_shouldReturn200() throws Exception {
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(predictionService.findByUser(testUser)).thenReturn(List.of());

        mockMvc.perform(get("/predictions"))
            .andExpect(status().isOk())
            .andExpect(view().name("prediction/list"))
            .andExpect(model().attributeExists("predictions", "editableIds"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void predictionsPage_withNoPredictions_shouldShowEmptyList() throws Exception {
        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(predictionService.findByUser(testUser)).thenReturn(List.of());

        mockMvc.perform(get("/predictions"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("predictions", List.of()));
    }

    @Test
    @WithMockUser(username = "testuser")
    void predictionsPage_withUpcomingMatch_shouldHaveEditableId() throws Exception {
        Match futureMatch = new Match();
        futureMatch.setId(1L);
        futureMatch.setCountryA("Brazil");
        futureMatch.setCountryB("France");
        futureMatch.setMatchDate(LocalDateTime.now().plusDays(10));

        Prediction prediction = new Prediction();
        prediction.setId(1L);
        prediction.setMatch(futureMatch);
        prediction.setUser(testUser);
        prediction.setPredictedGoalsA(2);
        prediction.setPredictedGoalsB(1);

        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(predictionService.findByUser(testUser)).thenReturn(List.of(prediction));

        mockMvc.perform(get("/predictions"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("editableIds"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void predictionsPage_withPastMatch_shouldNotBeEditable() throws Exception {
        Match pastMatch = new Match();
        pastMatch.setId(2L);
        pastMatch.setCountryA("Germany");
        pastMatch.setCountryB("Spain");
        pastMatch.setMatchDate(LocalDateTime.now().minusDays(1));

        Prediction prediction = new Prediction();
        prediction.setId(2L);
        prediction.setMatch(pastMatch);
        prediction.setUser(testUser);
        prediction.setPredictedGoalsA(0);
        prediction.setPredictedGoalsB(0);

        when(userService.findByUsername("testuser")).thenReturn(testUser);
        when(predictionService.findByUser(testUser)).thenReturn(List.of(prediction));

        mockMvc.perform(get("/predictions"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("predictions"));
    }
}
