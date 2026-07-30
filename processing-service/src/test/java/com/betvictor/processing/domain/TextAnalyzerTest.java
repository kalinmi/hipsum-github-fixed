package com.betvictor.processing.domain;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TextAnalyzerTest {

    private final TextAnalyzer analyzer = new TextAnalyzer();

    @Test
    void countsWordFrequenciesCaseInsensitively() {
        ParagraphStats stats = analyzer.analyze(new Paragraph("Craft beer craft BEER kombucha."));

        assertThat(stats.wordFrequencies())
                .containsEntry("craft", 2L)
                .containsEntry("beer", 2L)
                .containsEntry("kombucha", 1L);
    }

    @Test
    void paragraphSizeIsCharacterCount() {
        ParagraphStats stats = analyzer.analyze(new Paragraph("abcde"));

        assertThat(stats.sizeInChars()).isEqualTo(5);
    }

    @Test
    void ignoresPunctuationAndKeepsApostrophes() {
        ParagraphStats stats = analyzer.analyze(new Paragraph("90's vibes, +1 vibes!"));

        assertThat(stats.wordFrequencies())
                .containsEntry("90's", 1L)
                .containsEntry("vibes", 2L)
                .containsEntry("1", 1L);
    }

    @Test
    void mostFrequentWordAggregatesAcrossParagraphs() {
        List<ParagraphStats> stats = List.of(
                analyzer.analyze(new Paragraph("tofu tofu beard")),
                analyzer.analyze(new Paragraph("beard beard vinyl")));

        assertThat(analyzer.mostFrequentWord(stats)).isEqualTo("beard");
    }

    @Test
    void frequencyTieIsBrokenAlphabetically() {
        List<ParagraphStats> stats = List.of(analyzer.analyze(new Paragraph("zebra apple")));

        assertThat(analyzer.mostFrequentWord(stats)).isEqualTo("apple");
    }

    @Test
    void averageParagraphSizeIsMeanOfCharacterCounts() {
        List<ParagraphStats> stats = List.of(
                analyzer.analyze(new Paragraph("ab")),
                analyzer.analyze(new Paragraph("abcd")));

        assertThat(analyzer.averageParagraphSize(stats)).isEqualTo(3.0);
    }

    @Test
    void blankParagraphIsRejected() {
        assertThatThrownBy(() -> new Paragraph("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void mostFrequentWordOnEmptyStatsIsRejected() {
        assertThatThrownBy(() -> analyzer.mostFrequentWord(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
