package io.shawlynot.producer.kraken;

import io.shawlynot.producer.consumer.CandleConsumer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Configuration
@EnableConfigurationProperties(KrakenConfigProperties.class)
public class KrakenConfig {
    @Bean
    public KrakenSubscriber krakenSubscriber(
            KrakenConfigProperties configProperties,
            List<CandleConsumer> candleConsumers,
            Clock clock
    ) throws ExecutionException, InterruptedException, TimeoutException {
        var krakenSubsciber = new KrakenSubscriber(
                configProperties,
                candleConsumers,
                clock
        );
        return krakenSubsciber.subscribe().get(1, TimeUnit.MINUTES);
    }
}
