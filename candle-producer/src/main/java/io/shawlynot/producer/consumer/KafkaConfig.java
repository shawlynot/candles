package io.shawlynot.producer.consumer;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
@EnableConfigurationProperties(KafkaCandleProperties.class)
public class KafkaConfig {

    @Bean
    public NewTopic topic(KafkaCandleProperties properties) {
        return TopicBuilder.name(properties.topic())
                .partitions(1)
                .replicas(1)
                .build();
    }
}
