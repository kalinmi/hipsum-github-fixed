package com.betvictor.processing.domain;

import java.util.Map;

public record ParagraphStats(Map<String, Long> wordFrequencies, int sizeInChars) {
    public ParagraphStats {
        wordFrequencies = Map.copyOf(wordFrequencies);
    }
}
