package com.worldcup.controller;

import com.worldcup.config.PasswordEncoderConfig;
import com.worldcup.config.SecurityConfig;
import com.worldcup.domain.*;
import com.worldcup.interceptor.AdminAuditInterceptor;
import com.worldcup.service.TeamService;
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

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TeamController.class)
@Import({SecurityConfig.class, PasswordEncoderConfig.class,
    com.worldcup.util.FlagUtil.class, com.worldcup.util.CountryRegistry.class})
class TeamDetailRenderTest {

    @Autowired MockMvc mockMvc;

    @MockitoBean TeamService teamService;
    @MockitoBean UserService userService;
    @MockitoBean AdminAuditInterceptor adminAuditInterceptor;

    @BeforeEach
    void allow() throws Exception {
        when(adminAuditInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Test
    @WithMockUser(username = "owner")
    void detail_rendersWithBreakdown() throws Exception {
        User owner = new User();
        ReflectionTestUtils.setField(owner, "id", 1L);
        owner.setUsername("owner");
        owner.setEmail("o@x.com");
        owner.setRole(Role.USER);
        owner.setTeams(new HashSet<>());

        Team team = new Team();
        team.setId(1L);
        team.setName("Dream");
        team.setInviteCode("ABCD1234");
        team.setOwner(owner);
        team.setMembers(new HashSet<>(List.of(owner)));

        Match match = new Match();
        match.setId(10L);
        match.setCountryA("Brazil");
        match.setCountryB("France");
        match.setMatchDate(LocalDateTime.of(2026, 6, 20, 18, 0));

        Prediction p = new Prediction();
        p.setId(100L);
        p.setUser(owner);
        p.setMatch(match);
        p.setPredictedGoalsA(2);
        p.setPredictedGoalsB(1);
        p.setPointsAwarded(10);

        when(userService.findByUsername("owner")).thenReturn(owner);
        when(teamService.findById(1L)).thenReturn(team);
        when(teamService.computeMemberScores(team)).thenReturn(Map.of(1L, 10));
        when(teamService.computeMemberPredictions(team)).thenReturn(Map.of(1L, List.of(p)));

        mockMvc.perform(get("/team/1"))
            .andExpect(status().isOk());
    }
}
