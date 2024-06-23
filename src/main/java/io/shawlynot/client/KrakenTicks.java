package io.shawlynot.client;

import io.shawlynot.model.Tick;

import java.util.List;

public record KrakenTicks(
        String symbol,
        List<Tick> bids,
        List<Tick> asks
) {
}
