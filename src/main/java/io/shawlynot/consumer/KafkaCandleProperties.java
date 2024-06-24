package io.shawlynot.consumer;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kafka-candle")
public record KafkaCandleProperties(
        String topic
) {

}
