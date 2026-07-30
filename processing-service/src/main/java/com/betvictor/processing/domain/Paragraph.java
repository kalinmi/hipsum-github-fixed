package com.betvictor.processing.domain;

public record Paragraph(String text) {
    public Paragraph {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Paragraph text must not be blank");
        }
    }
}
