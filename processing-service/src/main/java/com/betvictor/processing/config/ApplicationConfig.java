package com.betvictor.processing.config;

import com.betvictor.processing.adapter.out.hipsum.HipsumParagraphProvider;
import com.betvictor.processing.adapter.out.hipsum.HipsumProperties;
import com.betvictor.processing.adapter.out.kafka.AnalysisMessage;
import com.betvictor.processing.adapter.out.kafka.KafkaAnalysisPublisher;
import com.betvictor.processing.application.AnalyzeTextService;
import com.betvictor.processing.application.TimeSource;
import com.betvictor.processing.application.port.in.AnalyzeTextUseCase;
import com.betvictor.processing.domain.TextAnalyzer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.http.client.ClientHttpRequestFactoryBuilder;
import org.springframework.boot.http.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.client.RestClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableConfigurationProperties(HipsumProperties.class)
public class ApplicationConfig {

    @Bean
    public RestClient hipsumRestClient(HipsumProperties properties) {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.defaults()
                .withConnectTimeout(properties.timeout())
                .withReadTimeout(properties.timeout());
        return RestClient.builder()
                .baseUrl(properties.baseUrl())
                .requestFactory(ClientHttpRequestFactoryBuilder.detect().build(settings))
                .build();
    }

    @Bean
    public HipsumParagraphProvider hipsumParagraphProvider(RestClient hipsumRestClient) {
        return new HipsumParagraphProvider(hipsumRestClient);
    }

    @Bean
    public KafkaAnalysisPublisher kafkaAnalysisPublisher(
            KafkaTemplate<String, AnalysisMessage> kafkaTemplate,
            @Value("${app.kafka.topic}") String topic) {
        return new KafkaAnalysisPublisher(kafkaTemplate, topic);
    }

    @Bean(destroyMethod = "close")
    public ExecutorService paragraphFetchExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    @Bean
    public AnalyzeTextUseCase analyzeTextUseCase(HipsumParagraphProvider provider,
                                                 KafkaAnalysisPublisher publisher,
                                                 ExecutorService paragraphFetchExecutor) {
        TimeSource timeSource = System::nanoTime;
        return new AnalyzeTextService(provider, publisher, new TextAnalyzer(), timeSource, paragraphFetchExecutor);
    }
}
