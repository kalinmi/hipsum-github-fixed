package com.betvictor.repository.adapter.in.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;

public record WordsProcessedMessage(
        @JsonProperty("freq_word") String freqWord,
        @JsonProperty("avg_paragraph_size") double avgParagraphSize,
        @JsonProperty("avg_paragraph_processing_time") double avgParagraphProcessingTime,
        @JsonProperty("total_processing_time") long totalProcessingTime) {
}
