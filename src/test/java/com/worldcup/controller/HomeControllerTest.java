package com.worldcup.controller;

import com.worldcup.config.PasswordEncoderConfig;
import com.worldcup.config.SecurityConfig;
import com.worldcup.domain.User;
import com.worldcup.interceptor.AdminAuditInterceptor;
import com.worldcup.repository.PredictionRepository;
import com.worldcup.repository.TeamRepository;
import com.worldcup.service.MatchService;
import com.worldcup.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HomeController.class)
@Import({SecurityConfig.class, PasswordEncoderConfig.class})
class HomeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MatchService matchService;

    @MockitoBean
    UserService userService;

    @MockitoBean
    PredictionRepository predictionRepository;

    @MockitoBean
    TeamRepository teamRepository;

    @MockitoBean
    AdminAuditInterceptor adminAuditInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        when(adminAuditInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        when(matchService.findUpcoming(anyInt())).thenReturn(List.of());
    }

    @Test
    void homePage_shouldReturn200() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(view().name("index"));
    }

    @Test
    void homePage_whenAnonymous_shouldNotHaveUserHero() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(model().attributeDoesNotExist("username"));
    }

    @Test
    @WithMockUser(username = "alice")
    void homePage_whenAuthenticated_shouldHaveUsernameInModel() throws Exception {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.setUsername("alice");

        when(userService.findByUsername("alice")).thenReturn(user);
        when(predictionRepository.getTotalScoreForUser(user)).thenReturn(0);
        when(predictionRepository.countByUser(user)).thenReturn(0L);
        when(teamRepository.countByMembersContaining(user)).thenReturn(0L);

        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("username", "alice"));
    }

    @Test
    @WithMockUser(username = "alice")
    void homePage_whenAuthenticated_shouldHaveStatsInModel() throws Exception {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", 1L);
        user.setUsername("alice");

        when(userService.findByUsername("alice")).thenReturn(user);
        when(predictionRepository.getTotalScoreForUser(user)).thenReturn(10);
        when(predictionRepository.countByUser(user)).thenReturn(1L);
        when(teamRepository.countByMembersContaining(user)).thenReturn(2L);

        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(model().attribute("userPoints", 10))
            .andExpect(model().attribute("userPredictionCount", 1L))
            .andExpect(model().attribute("userTeamCount", 2L));
    }

    @Test
    void homePage_shouldHaveUpcomingMatchesModel() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("upcomingMatches"));
    }
}
