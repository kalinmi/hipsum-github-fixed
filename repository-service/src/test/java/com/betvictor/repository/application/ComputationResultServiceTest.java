package com.betvictor.repository.application;

import com.betvictor.repository.application.port.out.ComputationResultStore;
import com.betvictor.repository.domain.ComputationResult;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ComputationResultServiceTest {

    private final InMemoryStore store = new InMemoryStore();
    private final ComputationResultService service = new ComputationResultService(store);

    @Test
    void storeDelegatesToStore() {
        ComputationResult result = result("beard", 1);

        service.store(result);

        assertThat(store.saved).containsExactly(result);
    }

    @Test
    void historyReturnsAtMostTenNewestFirst() {
        for (int i = 1; i <= 12; i++) {
            service.store(result("word" + i, i));
        }

        List<ComputationResult> history = service.lastResults();

        assertThat(history).hasSize(10);
        assertThat(history.getFirst().freqWord()).isEqualTo("word12");
        assertThat(history.getLast().freqWord()).isEqualTo("word3");
    }

    private ComputationResult result(String word, int secondsOffset) {
        return new ComputationResult(word, 100.0, 0.5, 200L,
                Instant.parse("2026-07-20T10:00:00Z").plusSeconds(secondsOffset));
    }

    private static class InMemoryStore implements ComputationResultStore {
        final List<ComputationResult> saved = new ArrayList<>();

        @Override
        public void save(ComputationResult result) {
            saved.add(result);
        }

        @Override
        public List<ComputationResult> findLatest(int limit) {
            return saved.stream()
                    .sorted(Comparator.comparing(ComputationResult::receivedAt).reversed())
                    .limit(limit)
                    .toList();
        }
    }
}
