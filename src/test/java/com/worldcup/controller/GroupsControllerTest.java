package com.worldcup.controller;

import com.worldcup.config.PasswordEncoderConfig;
import com.worldcup.config.SecurityConfig;
import com.worldcup.interceptor.AdminAuditInterceptor;
import com.worldcup.service.MatchService;
import com.worldcup.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GroupsController.class)
@Import({SecurityConfig.class, PasswordEncoderConfig.class, com.worldcup.util.CountryRegistry.class})
class GroupsControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean MatchService matchService;
    @MockitoBean UserService userService;
    @MockitoBean AdminAuditInterceptor adminAuditInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        when(adminAuditInterceptor.preHandle(any(), any(), any())).thenReturn(true);
        when(matchService.findAll()).thenReturn(List.of());
    }

    @Test
    void groupsPage_whenAnonymous_shouldBeOk() throws Exception {
        mockMvc.perform(get("/groups"))
            .andExpect(status().isOk())
            .andExpect(view().name("groups"))
            .andExpect(model().attributeExists("standings", "nextFixtures"));
    }

    @Test
    @WithMockUser
    void groupsPage_whenAuthenticated_shouldBeOk() throws Exception {
        mockMvc.perform(get("/groups"))
            .andExpect(status().isOk())
            .andExpect(view().name("groups"));
    }

    @Test
    void groupsPage_shouldContain12Groups() throws Exception {
        mockMvc.perform(get("/groups"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("standings"));
    }
}
