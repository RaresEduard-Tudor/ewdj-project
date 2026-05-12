package com.worldcup.controller.admin;

import com.worldcup.config.PasswordEncoderConfig;
import com.worldcup.config.SecurityConfig;
import com.worldcup.domain.Match;
import com.worldcup.exception.DuplicateMatchException;
import com.worldcup.interceptor.AdminAuditInterceptor;
import com.worldcup.service.MatchService;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminMatchController.class)
@Import({SecurityConfig.class, PasswordEncoderConfig.class})
class AdminMatchControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MatchService matchService;

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

    @Test
    void adminMatchList_whenAnonymous_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/admin/matches"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminNewMatchForm_shouldHaveMatchDtoModel() throws Exception {
        mockMvc.perform(get("/admin/matches/new"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("matchDto"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminSaveMatch_withValidData_shouldRedirect() throws Exception {
        when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Success");

        mockMvc.perform(post("/admin/matches")
                .param("countryA", "Belgium")
                .param("countryB", "France")
                .param("matchDate", "2026-06-15T18:00")
                .param("stadiumCode", "1234")
                .param("checksum", "70")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/matches"));
        verify(matchService).save(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminSaveMatch_withSameCountries_shouldReturnForm() throws Exception {
        mockMvc.perform(post("/admin/matches")
                .param("countryA", "Brazil")
                .param("countryB", "Brazil")
                .param("matchDate", "2026-06-15T18:00")
                .param("stadiumCode", "1234")
                .param("checksum", "70")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/match-form"));
        verify(matchService, never()).save(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminSaveMatch_withDuplicateMatch_shouldReturnFormWithError() throws Exception {
        doThrow(new DuplicateMatchException("Duplicate")).when(matchService).save(any());

        mockMvc.perform(post("/admin/matches")
                .param("countryA", "Belgium")
                .param("countryB", "France")
                .param("matchDate", "2026-06-15T18:00")
                .param("stadiumCode", "1234")
                .param("checksum", "70")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/match-form"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminEditMatchForm_shouldShowFormWithData() throws Exception {
        Match match = new Match();
        match.setId(1L);
        match.setCountryA("Germany");
        match.setCountryB("Spain");
        match.setMatchDate(LocalDateTime.of(2026, 6, 20, 18, 0));
        match.setStadiumCode("1001");
        match.setChecksum(1001 % 97);

        when(matchService.findById(1L)).thenReturn(match);

        mockMvc.perform(get("/admin/matches/1/edit"))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/match-form"))
            .andExpect(model().attributeExists("matchDto"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminSaveResult_shouldDelegateToServiceAndRedirect() throws Exception {
        when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Result saved");

        mockMvc.perform(post("/admin/matches/1/result")
                .param("goalsA", "2")
                .param("goalsB", "1")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/matches"));

        verify(matchService).saveResult(1L, 2, 1);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminDeleteMatch_shouldDeleteAndRedirect() throws Exception {
        when(messageSource.getMessage(any(), any(), any(Locale.class))).thenReturn("Deleted");

        mockMvc.perform(post("/admin/matches/1/delete").with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/admin/matches"));

        verify(matchService).delete(1L);
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminSaveMatch_asUser_shouldBeForbidden() throws Exception {
        mockMvc.perform(post("/admin/matches")
                .param("countryA", "Belgium")
                .param("countryB", "France")
                .with(csrf()))
            .andExpect(status().isForbidden());
    }
}
