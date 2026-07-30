package com.betvictor.repository.config;

import com.betvictor.repository.adapter.out.persistence.JpaComputationResultStore;
import com.betvictor.repository.application.ComputationResultService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ApplicationConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public ComputationResultService computationResultService(JpaComputationResultStore store) {
        return new ComputationResultService(store);
    }
}
