package com.betvictor.processing;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "app.hipsum.base-url=http://localhost:18089",
                "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
        })
@EmbeddedKafka(partitions = 4, topics = "words.processed")
@WireMockTest(httpPort = 18089)
class ProcessingIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private EmbeddedKafkaBroker broker;

    private Consumer<String, String> consumer;

    @BeforeEach
    void setUpConsumer() {
        var consumerProps = KafkaTestUtils.consumerProps("it-group", "true", broker);
        consumer = new DefaultKafkaConsumerFactory<String, String>(
                consumerProps,
                new StringDeserializer(),
                new StringDeserializer())
                .createConsumer();
        broker.consumeFromAnEmbeddedTopic(consumer, "words.processed");
    }

    @AfterEach
    void tearDown() {
        consumer.close();
    }

    @Test
    void textEndpointReturnsJsonAndProducesOrderedKeyedKafkaMessage() {
        stubFor(get(urlPathEqualTo("/api/"))
                .willReturn(okJson("[\"beard beard vinyl kombucha\"]")));

        ResponseEntity<String> first = rest.getForEntity("/betvictor/text?p=3", String.class);
        ResponseEntity<String> second = rest.getForEntity("/betvictor/text?p=2", String.class);

        assertThat(first.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(first.getBody()).contains("\"freq_word\":\"beard\"");
        assertThat(second.getStatusCode().is2xxSuccessful()).isTrue();
        verify(5, getRequestedFor(urlPathEqualTo("/api/")));

        List<ConsumerRecord<String, String>> records = new ArrayList<>();
        ConsumerRecords<String, String> polled = KafkaTestUtils.getRecords(consumer, Duration.ofSeconds(10), 2);
        polled.forEach(records::add);

        assertThat(records).hasSize(2);
        assertThat(records).allSatisfy(r -> {
            assertThat(r.key()).isEqualTo("beard");
            assertThat(r.value()).contains("\"freq_word\":\"beard\"");
        });
        assertThat(records.get(0).partition()).isEqualTo(records.get(1).partition());
        assertThat(records.get(0).offset()).isLessThan(records.get(1).offset());
    }
}
