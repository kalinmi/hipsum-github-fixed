package com.betvictor.repository.domain;

import java.time.Instant;

public record ComputationResult(
        String freqWord,
        double avgParagraphSize,
        double avgParagraphProcessingTime,
        long totalProcessingTime,
        Instant receivedAt) {
}
