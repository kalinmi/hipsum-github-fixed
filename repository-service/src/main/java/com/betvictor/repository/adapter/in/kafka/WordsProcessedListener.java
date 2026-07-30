package com.betvictor.repository.adapter.in.kafka;

import com.betvictor.repository.application.port.in.StoreComputationResultUseCase;
import com.betvictor.repository.domain.ComputationResult;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.Clock;

@Component
public class WordsProcessedListener {

    private final StoreComputationResultUseCase storeUseCase;
    private final Clock clock;

    public WordsProcessedListener(StoreComputationResultUseCase storeUseCase, Clock clock) {
        this.storeUseCase = storeUseCase;
        this.clock = clock;
    }

    @KafkaListener(
            topics = "${app.kafka.topic}",
            groupId = "${app.kafka.group-id}",
            containerFactory = "wordsProcessedContainerFactory")
    public void onMessage(WordsProcessedMessage message) {
        storeUseCase.store(new ComputationResult(
                message.freqWord(),
                message.avgParagraphSize(),
                message.avgParagraphProcessingTime(),
                message.totalProcessingTime(),
                clock.instant()));
    }
}
