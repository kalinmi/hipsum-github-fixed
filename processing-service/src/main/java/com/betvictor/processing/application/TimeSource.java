package com.betvictor.processing.application;

@FunctionalInterface
public interface TimeSource {
    long nanoTime();
}
