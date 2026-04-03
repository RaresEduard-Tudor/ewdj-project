package com.worldcup.rest;

import com.worldcup.service.MatchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MatchRestController.class)
@WithMockUser
class MatchRestControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    MatchService matchService;

    @Test
    void getMatchesByDate_shouldReturnJsonList() throws Exception {
        when(matchService.findByDate(any())).thenReturn(List.of());
        mockMvc.perform(get("/api/matches?date=2026-06-11"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void getCapacityByCode_shouldReturnCapacity() throws Exception {
        when(matchService.getCapacityByStadiumCode("1001")).thenReturn(80000);
        mockMvc.perform(get("/api/matches/stadiums/1001/capacity"))
            .andExpect(status().isOk());
    }
}
