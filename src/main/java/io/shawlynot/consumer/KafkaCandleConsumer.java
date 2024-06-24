package io.shawlynot.consumer;

import io.shawlynot.model.Candle;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaCandleConsumer implements CandleConsumer {

    private final KafkaTemplate<String, String> template;
    public KafkaCandleConsumer(KafkaTemplate<String, String> template) {
        this.template = template;
    }

    @Override
    public void accept(Candle candle) {
//        template.send()
    }
}
