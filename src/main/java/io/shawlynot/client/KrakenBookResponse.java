package io.shawlynot.client;

import java.util.List;

public record KrakenBookResponse(
        String type,
        List<KrakenTicks> data
) implements KrakenResponse {
}
