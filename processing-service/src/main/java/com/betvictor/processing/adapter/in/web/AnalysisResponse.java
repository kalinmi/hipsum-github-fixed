package com.betvictor.processing.adapter.in.web;

import com.betvictor.processing.domain.TextAnalysis;
import com.fasterxml.jackson.annotation.JsonProperty;

public record AnalysisResponse(
        @JsonProperty("freq_word") String freqWord,
        @JsonProperty("avg_paragraph_size") double avgParagraphSize,
        @JsonProperty("avg_paragraph_processing_time") double avgParagraphProcessingTime,
        @JsonProperty("total_processing_time") long totalProcessingTime) {

    public static AnalysisResponse from(TextAnalysis analysis) {
        return new AnalysisResponse(
                analysis.freqWord(),
                analysis.avgParagraphSize(),
                analysis.avgParagraphProcessingTimeMs(),
                analysis.totalProcessingTimeMs());
    }
}
