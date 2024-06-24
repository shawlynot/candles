package io.shawlynot.producer.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.shawlynot.core.model.Candle;
import org.springframework.stereotype.Component;

/**
 * Consumes candles and outputs them to stdout in JSON format
 */
@Component
public class StdOutCandleConsumer implements CandleConsumer {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public void accept(Candle candle) {
        try {
            System.out.println(objectMapper.writeValueAsString(candle));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
