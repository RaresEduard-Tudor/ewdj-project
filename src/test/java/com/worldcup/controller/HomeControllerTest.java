package com.worldcup.controller;

import com.worldcup.config.PasswordEncoderConfig;
import com.worldcup.config.SecurityConfig;
import com.worldcup.interceptor.AdminAuditInterceptor;
import com.worldcup.repository.PredictionRepository;
import com.worldcup.repository.TeamRepository;
import com.worldcup.service.MatchService;
import com.worldcup.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
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

    @Test
    void homePage_shouldReturn200() throws Exception {
        when(matchService.findAll()).thenReturn(List.of());
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(view().name("index"));
    }
}
