package com.betvictor.processing.adapter.in.web;

import com.betvictor.processing.application.port.in.AnalyzeTextUseCase;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class TextController {

    private final AnalyzeTextUseCase analyzeTextUseCase;

    public TextController(AnalyzeTextUseCase analyzeTextUseCase) {
        this.analyzeTextUseCase = analyzeTextUseCase;
    }

    @GetMapping("/betvictor/text")
    public AnalysisResponse text(@RequestParam("p") @Min(1) int paragraphs) {
        return AnalysisResponse.from(analyzeTextUseCase.analyze(paragraphs));
    }
}
