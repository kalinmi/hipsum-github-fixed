package com.betvictor.repository;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "app.kafka.consumer-concurrency=3",
                "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
        })
@EmbeddedKafka(partitions = 4, topics = "words.processed")
@Testcontainers
class RepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private EmbeddedKafkaBroker broker;

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @Test
    void consumedMessageAppearsInHistory() {
        try (var producer = new KafkaProducer<String, String>(
                Map.of("bootstrap.servers", broker.getBrokersAsString()),
                new StringSerializer(), new StringSerializer())) {
            producer.send(new ProducerRecord<>("words.processed", "beard",
                    """
                    {"freq_word":"beard","avg_paragraph_size":120.5,\
                    "avg_paragraph_processing_time":0.42,"total_processing_time":350}"""));
            producer.flush();
        }

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            String body = rest.getForObject("/betvictor/history", String.class);
            assertThat(body).contains("\"freq_word\":\"beard\"");
            assertThat(body).contains("\"total_processing_time\":350");
        });
    }

    @Test
    void poisonMessageIsSkippedAndSubsequentValidMessageAppearsInHistory() {
        try (var producer = new KafkaProducer<String, String>(
                Map.of("bootstrap.servers", broker.getBrokersAsString()),
                new StringSerializer(), new StringSerializer())) {
            producer.send(new ProducerRecord<>("words.processed", "kombucha", "not-json{{"));
            producer.send(new ProducerRecord<>("words.processed", "kombucha",
                    """
                    {"freq_word":"kombucha","avg_paragraph_size":80.0,\
                    "avg_paragraph_processing_time":0.11,"total_processing_time":99}"""));
            producer.flush();
        }

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> {
            String body = rest.getForObject("/betvictor/history", String.class);
            assertThat(body).contains("\"freq_word\":\"kombucha\"");
            assertThat(body).contains("\"total_processing_time\":99");
        });
    }

    @Test
    void listenerRunsWithConfiguredConcurrency() {
        MessageListenerContainer container = registry.getListenerContainers().iterator().next();
        assertThat(((org.springframework.kafka.listener.ConcurrentMessageListenerContainer<?, ?>) container)
                .getConcurrency()).isEqualTo(3);
    }
}
