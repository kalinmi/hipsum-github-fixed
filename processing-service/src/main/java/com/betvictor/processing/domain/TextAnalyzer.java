package com.betvictor.processing.domain;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TextAnalyzer {

    private static final Pattern NON_WORD = Pattern.compile("[^\\p{L}\\p{Nd}']+");

    public ParagraphStats analyze(Paragraph paragraph) {
        Map<String, Long> frequencies = NON_WORD.splitAsStream(paragraph.text().toLowerCase(Locale.ROOT))
                .map(word -> word.replaceAll("^'+|'+$", ""))
                .filter(word -> !word.isBlank())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        return new ParagraphStats(frequencies, paragraph.text().length());
    }

    public String mostFrequentWord(List<ParagraphStats> stats) {
        requireNonEmpty(stats);
        Map<String, Long> merged = new HashMap<>();
        stats.forEach(s -> s.wordFrequencies().forEach((word, count) -> merged.merge(word, count, Long::sum)));
        return merged.entrySet().stream()
                .max(Map.Entry.<String, Long>comparingByValue()
                        .thenComparing(Map.Entry.comparingByKey(Comparator.reverseOrder())))
                .orElseThrow(() -> new IllegalArgumentException("No words found in paragraphs"))
                .getKey();
    }

    public double averageParagraphSize(List<ParagraphStats> stats) {
        requireNonEmpty(stats);
        return stats.stream().mapToInt(ParagraphStats::sizeInChars).average().orElseThrow();
    }

    private void requireNonEmpty(List<ParagraphStats> stats) {
        if (stats.isEmpty()) {
            throw new IllegalArgumentException("At least one paragraph is required");
        }
    }
}
