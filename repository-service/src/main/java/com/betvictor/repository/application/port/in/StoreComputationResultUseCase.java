package com.betvictor.repository.application.port.in;

import com.betvictor.repository.domain.ComputationResult;

public interface StoreComputationResultUseCase {
    void store(ComputationResult result);
}
