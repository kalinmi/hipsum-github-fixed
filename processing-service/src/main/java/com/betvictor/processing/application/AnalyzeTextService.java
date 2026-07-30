package com.betvictor.processing.application;

import com.betvictor.processing.application.port.in.AnalyzeTextUseCase;
import com.betvictor.processing.application.port.out.AnalysisPublisher;
import com.betvictor.processing.application.port.out.ParagraphProvider;
import com.betvictor.processing.domain.Paragraph;
import com.betvictor.processing.domain.ParagraphStats;
import com.betvictor.processing.domain.TextAnalysis;
import com.betvictor.processing.domain.TextAnalyzer;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

public class AnalyzeTextService implements AnalyzeTextUseCase {

    private final ParagraphProvider paragraphProvider;
    private final AnalysisPublisher analysisPublisher;
    private final TextAnalyzer textAnalyzer;
    private final TimeSource timeSource;
    private final ExecutorService executor;

    public AnalyzeTextService(ParagraphProvider paragraphProvider,
                              AnalysisPublisher analysisPublisher,
                              TextAnalyzer textAnalyzer,
                              TimeSource timeSource,
                              ExecutorService executor) {
        this.paragraphProvider = paragraphProvider;
        this.analysisPublisher = analysisPublisher;
        this.textAnalyzer = textAnalyzer;
        this.timeSource = timeSource;
        this.executor = executor;
    }

    @Override
    public TextAnalysis analyze(int paragraphCount) {
        if (paragraphCount <= 0) {
            throw new IllegalArgumentException("Paragraph count must be greater than 0");
        }
        long totalStart = timeSource.nanoTime();

        List<Paragraph> paragraphs = fetchParagraphs(paragraphCount);

        List<TimedStats> timedStats = paragraphs.stream().map(this::analyzeTimed).toList();
        List<ParagraphStats> stats = timedStats.stream().map(TimedStats::stats).toList();

        String freqWord = textAnalyzer.mostFrequentWord(stats);
        double avgSize = textAnalyzer.averageParagraphSize(stats);
        double avgProcessingMs = timedStats.stream()
                .mapToLong(TimedStats::elapsedNanos).average().orElseThrow() / 1_000_000.0;
        long totalMs = (timeSource.nanoTime() - totalStart) / 1_000_000;

        TextAnalysis analysis = new TextAnalysis(freqWord, avgSize, avgProcessingMs, totalMs);
        analysisPublisher.publish(analysis);
        return analysis;
    }

    private List<Paragraph> fetchParagraphs(int count) {
        List<Future<Paragraph>> futures = IntStream.range(0, count)
                .mapToObj(i -> executor.submit(paragraphProvider::fetchSingleParagraph))
                .toList();
        return futures.stream().map(this::join).toList();
    }

    private Paragraph join(Future<Paragraph> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while fetching paragraphs", e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new IllegalStateException("Paragraph fetch failed", e.getCause());
        }
    }

    private TimedStats analyzeTimed(Paragraph paragraph) {
        long start = timeSource.nanoTime();
        ParagraphStats stats = textAnalyzer.analyze(paragraph);
        return new TimedStats(stats, timeSource.nanoTime() - start);
    }

    private record TimedStats(ParagraphStats stats, long elapsedNanos) {
    }
}
