package com.betvictor.repository.application;

import com.betvictor.repository.application.port.in.GetHistoryQuery;
import com.betvictor.repository.application.port.in.StoreComputationResultUseCase;
import com.betvictor.repository.application.port.out.ComputationResultStore;
import com.betvictor.repository.domain.ComputationResult;

import java.util.List;

public class ComputationResultService implements StoreComputationResultUseCase, GetHistoryQuery {

    static final int HISTORY_LIMIT = 10;

    private final ComputationResultStore store;

    public ComputationResultService(ComputationResultStore store) {
        this.store = store;
    }

    @Override
    public void store(ComputationResult result) {
        store.save(result);
    }

    @Override
    public List<ComputationResult> lastResults() {
        return store.findLatest(HISTORY_LIMIT);
    }
}
