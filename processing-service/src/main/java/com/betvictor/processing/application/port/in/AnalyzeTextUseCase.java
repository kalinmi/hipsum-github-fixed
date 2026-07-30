package com.betvictor.processing.application.port.in;

import com.betvictor.processing.domain.TextAnalysis;

public interface AnalyzeTextUseCase {
    TextAnalysis analyze(int paragraphCount);
}
