package com.worldcup.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void adminPage_whenUnauthenticated_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/admin/matches"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminPage_whenUser_shouldBeForbidden() throws Exception {
        mockMvc.perform(get("/admin/matches"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminPage_whenAdmin_shouldBeOk() throws Exception {
        mockMvc.perform(get("/admin/matches"))
            .andExpect(status().isOk());
    }

    @Test
    void publicTop10_whenUnauthenticated_shouldBeOk() throws Exception {
        mockMvc.perform(get("/top10"))
            .andExpect(status().isOk());
    }
}
