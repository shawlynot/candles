package io.shawlynot.producer.kraken;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Passes tick update or snapshot from the Kraken book channel for a symbol
 */
public class KrakenUpdateParser {
    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final String symbol;

    public KrakenUpdateParser(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Parses a tick update or snapshot from the Kraken book channel.
     *
     * @param message The json body of a websocket tick update or snapshot
     * @return A set of bids and asks, or null
     */
    public KrakenUpdateOrSnapshot parseKrakenUpdate(String message) {
        JsonNode tree;
        try {
            tree = objectMapper.readTree(message);
            JsonNode maybeChannelIndicator = tree.get("channel");
            if (maybeChannelIndicator == null || !"book".equals(maybeChannelIndicator.asText())) {
                return null;
            }
            var response = objectMapper.readValue(message, KrakenResponse.class);
            if (response instanceof KrakenBookResponse bookResponse) {
                var bidAsksForSymbol = bookResponse.data().stream()
                        .filter(ticks -> ticks.symbol().equals(symbol))
                        .findAny();
                if (bidAsksForSymbol.isEmpty()) {
                    return null;
                }
                if (bookResponse.type().equals("update")) {
                    return new KrakenUpdateOrSnapshot(bidAsksForSymbol.get(), KrakenUpdateOrSnapshot.UpdateOrSnapshot.UPDATE);
                } else if (bookResponse.type().equals("snapshot")) {
                    return new KrakenUpdateOrSnapshot(bidAsksForSymbol.get(), KrakenUpdateOrSnapshot.UpdateOrSnapshot.SNAPSHOT);
                }
            }
            return null;
        } catch (
                JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
