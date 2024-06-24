package io.shawlynot.producer.kraken;

import io.shawlynot.core.model.Tick;

import java.util.List;

public record KrakenTicks(
        String symbol,
        List<Tick> bids,
        List<Tick> asks
) {
}
