package io.shawlynot.client;

import io.shawlynot.consumer.CandleConsumer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.List;

@Configuration
@EnableConfigurationProperties(KrakenConfigProperties.class)
public class KrakenConfig {


}
