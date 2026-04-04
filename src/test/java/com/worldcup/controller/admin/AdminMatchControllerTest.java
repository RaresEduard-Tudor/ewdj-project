package com.worldcup.controller.admin;

import com.worldcup.config.PasswordEncoderConfig;
import com.worldcup.config.SecurityConfig;
import com.worldcup.interceptor.AdminAuditInterceptor;
import com.worldcup.service.MatchService;
import com.worldcup.service.ScoringService;
import com.worldcup.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminMatchController.class)
@Import({SecurityConfig.class, PasswordEncoderConfig.class})
class AdminMatchControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MatchService matchService;

    @MockitoBean
    ScoringService scoringService;

    @MockitoBean
    MessageSource messageSource;

    @MockitoBean
    UserService userService;

    @MockitoBean
    AdminAuditInterceptor adminAuditInterceptor;

    @BeforeEach
    void allowInterceptor() throws Exception {
        when(adminAuditInterceptor.preHandle(
            any(HttpServletRequest.class),
            any(HttpServletResponse.class),
            any())).thenReturn(true);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminMatchList_asAdmin_shouldReturn200() throws Exception {
        when(matchService.findAll()).thenReturn(List.of());
        mockMvc.perform(get("/admin/matches"))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/matches"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminMatchList_asUser_shouldBeForbidden() throws Exception {
        mockMvc.perform(get("/admin/matches"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminNewMatchForm_asAdmin_shouldReturn200() throws Exception {
        mockMvc.perform(get("/admin/matches/new"))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/match-form"));
    }
}
