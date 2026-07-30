package com.betvictor.repository.adapter.in.kafka;

import com.betvictor.repository.application.port.in.StoreComputationResultUseCase;
import com.betvictor.repository.domain.ComputationResult;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WordsProcessedListenerTest {

    private final StoreComputationResultUseCase useCase = mock(StoreComputationResultUseCase.class);
    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-07-20T12:00:00Z"), ZoneOffset.UTC);
    private final WordsProcessedListener listener = new WordsProcessedListener(useCase, fixedClock);

    @Test
    void mapsMessageToDomainAndStampsReceivedAt() {
        listener.onMessage(new WordsProcessedMessage("beard", 120.5, 0.42, 350L));

        ArgumentCaptor<ComputationResult> captor = ArgumentCaptor.forClass(ComputationResult.class);
        verify(useCase).store(captor.capture());
        ComputationResult stored = captor.getValue();
        assertThat(stored.freqWord()).isEqualTo("beard");
        assertThat(stored.avgParagraphSize()).isEqualTo(120.5);
        assertThat(stored.avgParagraphProcessingTime()).isEqualTo(0.42);
        assertThat(stored.totalProcessingTime()).isEqualTo(350L);
        assertThat(stored.receivedAt()).isEqualTo(Instant.parse("2026-07-20T12:00:00Z"));
    }
}
