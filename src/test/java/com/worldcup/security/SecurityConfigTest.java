package com.worldcup.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
class SecurityConfigTest {

    @Autowired
    MockMvc mockMvc;

    // ── Public routes ──────────────────────────────────────────────

    @Test
    void homePage_whenUnauthenticated_shouldBeOk() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk());
    }

    @Test
    void matchesPage_whenUnauthenticated_shouldBeOk() throws Exception {
        mockMvc.perform(get("/matches"))
            .andExpect(status().isOk());
    }

    @Test
    void groupsPage_whenUnauthenticated_shouldBeOk() throws Exception {
        mockMvc.perform(get("/groups"))
            .andExpect(status().isOk());
    }

    @Test
    void publicTop10_whenUnauthenticated_shouldBeOk() throws Exception {
        mockMvc.perform(get("/top10"))
            .andExpect(status().isOk());
    }

    @Test
    void loginPage_whenUnauthenticated_shouldBeOk() throws Exception {
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk());
    }

    @Test
    void registerPage_whenUnauthenticated_shouldBeOk() throws Exception {
        mockMvc.perform(get("/register"))
            .andExpect(status().isOk());
    }

    @Test
    void apiMatches_whenUnauthenticated_shouldBeOk() throws Exception {
        mockMvc.perform(get("/api/matches?date=2026-06-11"))
            .andExpect(status().isOk());
    }

    // ── Protected routes: must redirect to login when unauthenticated ──

    @Test
    void teamPage_whenUnauthenticated_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/team"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void predictionsPage_whenUnauthenticated_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/predictions"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void adminPage_whenUnauthenticated_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/admin/matches"))
            .andExpect(status().is3xxRedirection());
    }

    // ── Admin: forbidden for USER role ──

    @Test
    @WithMockUser(roles = "USER")
    void adminPage_whenUser_shouldBeForbidden() throws Exception {
        mockMvc.perform(get("/admin/matches"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminNewMatch_whenUser_shouldBeForbidden() throws Exception {
        mockMvc.perform(get("/admin/matches/new"))
            .andExpect(status().isForbidden());
    }

    // ── Admin: allowed for ADMIN role ──

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminPage_whenAdmin_shouldBeOk() throws Exception {
        mockMvc.perform(get("/admin/matches"))
            .andExpect(status().isOk());
    }

    // ── CSRF protection ──

    @Test
    void postWithoutCsrf_shouldBeForbidden() throws Exception {
        mockMvc.perform(post("/register")
                .param("username", "user")
                .param("email", "user@example.com")
                .param("password", "password123")
                .param("confirmPassword", "password123"))
            .andExpect(status().isForbidden());
    }

    @Test
    void postWithCsrf_shouldNotBeForbiddenDueToCsrf() throws Exception {
        mockMvc.perform(post("/register")
                .param("username", "u")
                .param("email", "bad")
                .param("password", "short")
                .param("confirmPassword", "short")
                .with(csrf()))
            .andExpect(status().isOk()); // returns form with validation errors, not 403
    }

    // ── Logout ──

    @Test
    @WithMockUser
    void logout_shouldRedirectAfterLogout() throws Exception {
        mockMvc.perform(post("/logout").with(csrf()))
            .andExpect(status().is3xxRedirection());
    }

    // ── Authenticated user can access protected routes ──

    @Test
    @WithMockUser(roles = "USER")
    void predictionsPage_whenAuthenticated_shouldBeOk() throws Exception {
        mockMvc.perform(get("/predictions"))
            .andExpect(status().isOk());
    }
}
