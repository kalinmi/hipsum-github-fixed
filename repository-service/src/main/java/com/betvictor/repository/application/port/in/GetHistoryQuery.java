package com.betvictor.repository.application.port.in;

import com.betvictor.repository.domain.ComputationResult;

import java.util.List;

public interface GetHistoryQuery {
    List<ComputationResult> lastResults();
}
