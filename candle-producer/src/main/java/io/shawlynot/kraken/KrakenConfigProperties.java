package io.shawlynot.kraken;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kraken")
public record KrakenConfigProperties(
        String wsEndpoint,
        String symbol
) {
}
