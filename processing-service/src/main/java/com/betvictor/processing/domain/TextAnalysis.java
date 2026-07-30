package com.betvictor.processing.domain;

public record TextAnalysis(
        String freqWord,
        double avgParagraphSize,
        double avgParagraphProcessingTimeMs,
        long totalProcessingTimeMs) {
}
