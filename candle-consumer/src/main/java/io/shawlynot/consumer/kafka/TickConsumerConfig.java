package io.shawlynot.consumer.kafka;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;

@Configuration
public class TickConsumerConfig {

    @KafkaListener(id = "myId", topics = "${kafka-candle.topic}")
    public void listen(String message) {
        System.out.println(message);
    }
}
