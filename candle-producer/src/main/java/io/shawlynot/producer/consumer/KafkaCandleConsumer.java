package io.shawlynot.producer.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.shawlynot.core.model.Candle;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Consumes candles and sends to kafka in a json format
 */
@Component
public class KafkaCandleConsumer implements CandleConsumer {

    private final KafkaTemplate<String, String> template;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final KafkaCandleProperties kafkaCandleProperties;

    public KafkaCandleConsumer(KafkaTemplate<String, String> template, KafkaCandleProperties kafkaCandleProperties) {
        this.template = template;
        this.kafkaCandleProperties = kafkaCandleProperties;
    }

    @Override
    public void accept(Candle candle) {
        try {
            template.send(kafkaCandleProperties.topic(), objectMapper.writeValueAsString(candle));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
