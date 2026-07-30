package com.betvictor.repository.adapter.in.web;

import com.betvictor.repository.domain.ComputationResult;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public record HistoryEntryResponse(
        @JsonProperty("freq_word") String freqWord,
        @JsonProperty("avg_paragraph_size") double avgParagraphSize,
        @JsonProperty("avg_paragraph_processing_time") double avgParagraphProcessingTime,
        @JsonProperty("total_processing_time") long totalProcessingTime,
        @JsonProperty("received_at") Instant receivedAt) {

    public static HistoryEntryResponse from(ComputationResult result) {
        return new HistoryEntryResponse(
                result.freqWord(),
                result.avgParagraphSize(),
                result.avgParagraphProcessingTime(),
                result.totalProcessingTime(),
                result.receivedAt());
    }
}
