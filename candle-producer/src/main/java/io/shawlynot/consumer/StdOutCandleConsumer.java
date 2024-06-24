package io.shawlynot.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.shawlynot.model.Candle;
import org.springframework.stereotype.Component;

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
