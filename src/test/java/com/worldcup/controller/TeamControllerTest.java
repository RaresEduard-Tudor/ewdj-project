package com.worldcup.controller;

import com.worldcup.config.PasswordEncoderConfig;
import com.worldcup.config.SecurityConfig;
import com.worldcup.domain.Team;
import com.worldcup.domain.User;
import com.worldcup.exception.DuplicateTeamNameException;
import com.worldcup.exception.TeamJoinException;
import com.worldcup.exception.TeamNotFoundException;
import com.worldcup.interceptor.AdminAuditInterceptor;
import com.worldcup.repository.PredictionRepository;
import com.worldcup.service.TeamService;
import com.worldcup.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TeamController.class)
@Import({SecurityConfig.class, PasswordEncoderConfig.class})
class TeamControllerTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean TeamService teamService;
    @MockitoBean UserService userService;
    @MockitoBean PredictionRepository predictionRepository;
    @MockitoBean AdminAuditInterceptor adminAuditInterceptor;

    private User owner;
    private Team team;

    @BeforeEach
    void setUp() throws Exception {
        when(adminAuditInterceptor.preHandle(any(), any(), any())).thenReturn(true);

        owner = new User();
        owner.setId(1L);
        owner.setUsername("owner");
        owner.setRole("ROLE_USER");
        owner.setTeams(new HashSet<>());

        team = new Team();
        team.setId(1L);
        team.setName("Dream Team");
        team.setInviteCode("ABCD1234");
        team.setOwner(owner);
        team.setInviteEnabled(true);
        team.setMembers(new HashSet<>());
        team.getMembers().add(owner);
    }

    @Test
    void teamList_whenAnonymous_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/team"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(username = "owner")
    void teamList_whenAuthenticated_shouldReturn200() throws Exception {
        when(userService.findByUsername("owner")).thenReturn(owner);
        mockMvc.perform(get("/team"))
            .andExpect(status().isOk())
            .andExpect(view().name("team/list"));
    }

    @Test
    @WithMockUser(username = "owner")
    void createTeam_whenSuccess_shouldRedirectToTeam() throws Exception {
        when(teamService.createTeam("Dream Team", "owner")).thenReturn(team);

        mockMvc.perform(post("/team/create")
                .param("name", "Dream Team")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/team/1"));
    }

    @Test
    @WithMockUser(username = "owner")
    void createTeam_whenDuplicateName_shouldRedirectWithError() throws Exception {
        when(teamService.createTeam(any(), any()))
            .thenThrow(new DuplicateTeamNameException("Name taken"));

        mockMvc.perform(post("/team/create")
                .param("name", "Taken Name")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/team"))
            .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @WithMockUser(username = "owner")
    void joinTeam_whenSuccess_shouldRedirectToTeam() throws Exception {
        when(teamService.joinTeam("ABCD1234", "owner")).thenReturn(team);

        mockMvc.perform(post("/team/join")
                .param("inviteCode", "ABCD1234")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/team/1"));
    }

    @Test
    @WithMockUser(username = "owner")
    void joinTeam_whenInvalidCode_shouldRedirectWithError() throws Exception {
        when(teamService.joinTeam(any(), any()))
            .thenThrow(new TeamNotFoundException("No team found"));

        mockMvc.perform(post("/team/join")
                .param("inviteCode", "BADCODE1")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/team"))
            .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @WithMockUser(username = "owner")
    void joinTeam_whenAlreadyMember_shouldRedirectWithError() throws Exception {
        when(teamService.joinTeam(any(), any()))
            .thenThrow(new TeamJoinException("team.error.already.member"));

        mockMvc.perform(post("/team/join")
                .param("inviteCode", "ABCD1234")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/team"))
            .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @WithMockUser(username = "owner")
    void regenerateCode_asOwner_shouldRedirect() throws Exception {
        when(userService.findByUsername("owner")).thenReturn(owner);
        doNothing().when(teamService).regenerateInviteCode(eq(1L), eq(owner));

        mockMvc.perform(post("/team/1/regenerate").with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/team/1"));
    }

    @Test
    @WithMockUser(username = "owner")
    void updateSettings_asOwner_shouldRedirect() throws Exception {
        when(userService.findByUsername("owner")).thenReturn(owner);
        doNothing().when(teamService).updateSettings(anyLong(), anyBoolean(), anyInt(), any());

        mockMvc.perform(post("/team/1/settings")
                .param("inviteEnabled", "true")
                .param("maxMembers", "10")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/team/1"))
            .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @WithMockUser(username = "owner")
    void removeMember_asOwner_shouldRedirect() throws Exception {
        when(userService.findByUsername("owner")).thenReturn(owner);
        doNothing().when(teamService).removeMember(eq(1L), eq(2L), eq(owner));

        mockMvc.perform(post("/team/1/remove/2").with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/team/1"));
    }

    @Test
    @WithMockUser(username = "notmember")
    void teamDetail_asNonMember_shouldBeForbidden() throws Exception {
        User nonMember = new User();
        nonMember.setId(99L);
        nonMember.setUsername("notmember");

        when(teamService.findById(1L)).thenReturn(team);
        when(userService.findByUsername("notmember")).thenReturn(nonMember);

        mockMvc.perform(get("/team/1"))
            .andExpect(status().isForbidden());
    }

    @Test
    void teamDetail_whenAnonymous_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/team/1"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrlPattern("**/login"));
    }
}
