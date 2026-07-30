package com.betvictor.processing.adapter.in.web;

import com.betvictor.processing.adapter.out.hipsum.HipsumUnavailableException;
import com.betvictor.processing.application.port.in.AnalyzeTextUseCase;
import com.betvictor.processing.domain.TextAnalysis;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TextController.class)
class TextControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyzeTextUseCase analyzeTextUseCase;

    @Test
    void returnsAnalysisAsSnakeCaseJson() throws Exception {
        when(analyzeTextUseCase.analyze(3))
                .thenReturn(new TextAnalysis("beard", 120.5, 0.42, 350L));

        mockMvc.perform(get("/betvictor/text").param("p", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.freq_word").value("beard"))
                .andExpect(jsonPath("$.avg_paragraph_size").value(120.5))
                .andExpect(jsonPath("$.avg_paragraph_processing_time").value(0.42))
                .andExpect(jsonPath("$.total_processing_time").value(350));
    }

    @Test
    void rejectsZeroParagraphs() throws Exception {
        mockMvc.perform(get("/betvictor/text").param("p", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsMissingParameter() throws Exception {
        mockMvc.perform(get("/betvictor/text"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsNonNumericParameter() throws Exception {
        mockMvc.perform(get("/betvictor/text").param("p", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void mapsHipsumFailureToBadGateway() throws Exception {
        when(analyzeTextUseCase.analyze(2))
                .thenThrow(new HipsumUnavailableException("down"));

        mockMvc.perform(get("/betvictor/text").param("p", "2"))
                .andExpect(status().isBadGateway());
    }
}
