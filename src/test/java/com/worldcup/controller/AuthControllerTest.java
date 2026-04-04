package com.worldcup.controller;

import com.worldcup.config.PasswordEncoderConfig;
import com.worldcup.config.SecurityConfig;
import com.worldcup.interceptor.AdminAuditInterceptor;
import com.worldcup.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, PasswordEncoderConfig.class})
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserService userService;

    @MockitoBean
    AdminAuditInterceptor adminAuditInterceptor;

    @Test
    void registerPage_shouldReturn200() throws Exception {
        mockMvc.perform(get("/register"))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/register"));
    }

    @Test
    void loginPage_shouldReturn200() throws Exception {
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk())
            .andExpect(view().name("auth/login"));
    }

    @Test
    void registerPost_withInvalidEmail_shouldReturnFormWithErrors() throws Exception {
        mockMvc.perform(post("/register")
                .param("username", "testuser")
                .param("email", "not-an-email")
                .param("password", "password123")
                .param("confirmPassword", "password123")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(model().attributeHasFieldErrors("registrationDto", "email"));
    }

    @Test
    void registerPost_withMismatchedPasswords_shouldReturnFormWithErrors() throws Exception {
        mockMvc.perform(post("/register")
                .param("username", "testuser")
                .param("email", "user@example.com")
                .param("password", "password123")
                .param("confirmPassword", "different")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(model().attributeHasErrors("registrationDto"));
    }

    @Test
    void registerPost_valid_shouldRedirect() throws Exception {
        mockMvc.perform(post("/register")
                .param("username", "testuser")
                .param("email", "user@example.com")
                .param("password", "password123")
                .param("confirmPassword", "password123")
                .with(csrf()))
            .andExpect(status().is3xxRedirection());
    }
}
