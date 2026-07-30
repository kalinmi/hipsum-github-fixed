package com.betvictor.repository.adapter.in.web;

import com.betvictor.repository.application.port.in.GetHistoryQuery;
import com.betvictor.repository.domain.ComputationResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HistoryController.class)
class HistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GetHistoryQuery getHistoryQuery;

    @Test
    void returnsHistoryAsSnakeCaseJsonArray() throws Exception {
        when(getHistoryQuery.lastResults()).thenReturn(List.of(
                new ComputationResult("beard", 120.5, 0.42, 350L, Instant.parse("2026-07-20T12:00:00Z")),
                new ComputationResult("vinyl", 90.0, 0.30, 200L, Instant.parse("2026-07-20T11:00:00Z"))));

        mockMvc.perform(get("/betvictor/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].freq_word").value("beard"))
                .andExpect(jsonPath("$[0].avg_paragraph_size").value(120.5))
                .andExpect(jsonPath("$[0].avg_paragraph_processing_time").value(0.42))
                .andExpect(jsonPath("$[0].total_processing_time").value(350))
                .andExpect(jsonPath("$[0].received_at").value("2026-07-20T12:00:00Z"))
                .andExpect(jsonPath("$[1].freq_word").value("vinyl"));
    }

    @Test
    void returnsEmptyArrayWhenNoHistory() throws Exception {
        when(getHistoryQuery.lastResults()).thenReturn(List.of());

        mockMvc.perform(get("/betvictor/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
