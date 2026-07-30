package com.betvictor.processing.application;

import com.betvictor.processing.application.port.out.AnalysisPublisher;
import com.betvictor.processing.application.port.out.ParagraphProvider;
import com.betvictor.processing.domain.Paragraph;
import com.betvictor.processing.domain.TextAnalysis;
import com.betvictor.processing.domain.TextAnalyzer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnalyzeTextServiceTest {

    private final AtomicInteger fetchCount = new AtomicInteger();
    private final List<TextAnalysis> published = new CopyOnWriteArrayList<>();
    private final AtomicLong fakeNanos = new AtomicLong();

    private final ParagraphProvider provider = () -> {
        fetchCount.incrementAndGet();
        return new Paragraph("beard beard vinyl");
    };
    private final AnalysisPublisher publisher = published::add;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
    private final TimeSource timeSource = () -> fakeNanos.getAndAdd(1_000_000L);

    private final AnalyzeTextService service =
            new AnalyzeTextService(provider, publisher, new TextAnalyzer(), timeSource, executor);

    @AfterEach
    void tearDown() {
        executor.shutdownNow();
    }

    @Test
    void fetchesExactlyPParagraphsOneAtATime() {
        service.analyze(3);

        assertThat(fetchCount.get()).isEqualTo(3);
    }

    @Test
    void computesAnalysisFromFetchedParagraphs() {
        TextAnalysis analysis = service.analyze(2);

        assertThat(analysis.freqWord()).isEqualTo("beard");
        assertThat(analysis.avgParagraphSize()).isEqualTo(17.0);
    }

    @Test
    void publishesExactlyOneMessagePerRequestWithSamePayload() {
        TextAnalysis analysis = service.analyze(2);

        assertThat(published).containsExactly(analysis);
    }

    @Test
    void measuresPerParagraphAndTotalTimes() {
        TextAnalysis analysis = service.analyze(2);

        assertThat(analysis.avgParagraphProcessingTimeMs()).isEqualTo(1.0);
        assertThat(analysis.totalProcessingTimeMs()).isGreaterThanOrEqualTo(1L);
    }

    @Test
    void rejectsNonPositiveParagraphCount() {
        assertThatThrownBy(() -> service.analyze(0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
