package com.betvictor.repository.adapter.in.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, WordsProcessedMessage> wordsProcessedContainerFactory(
            ConsumerFactory<String, WordsProcessedMessage> consumerFactory,
            @Value("${app.kafka.consumer-concurrency}") int concurrency) {
        var factory = new ConcurrentKafkaListenerContainerFactory<String, WordsProcessedMessage>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(concurrency);
        return factory;
    }
}
