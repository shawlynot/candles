package io.shawlynot.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.shawlynot.book.PruningOrderBook;
import io.shawlynot.consumer.CandleConsumer;
import io.shawlynot.model.BidsAndAsks;

import java.net.http.WebSocket;
import java.time.Clock;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class KrakenListener implements WebSocket.Listener {

    private final PruningOrderBook pruningOrderBook;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String symbol;

    public KrakenListener(Clock clock, List<CandleConsumer> candleConsumers, long depth, String symbol) {
        this.symbol = symbol;
        this.pruningOrderBook = new PruningOrderBook(clock, candleConsumers, depth);
    }

    /**
     * The spec of {@link WebSocket.Listener} states that this is invoked in a thread safe manner
     */
    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        System.out.println(data);
        String asString = data.toString();

        maybeUpdateOrderBook(asString);

        webSocket.request(1);
        return null;
    }

    private void maybeUpdateOrderBook(String asString) {
        JsonNode tree;
        try {
            tree = objectMapper.readTree(asString);
            if (!"book".equals(tree.get("channel").asText())) {
                return;
            }
            var response = objectMapper.readValue(asString, KrakenResponse.class);
            if (response instanceof KrakenBookResponse bookResponse) {
                var bidAsksForSymbol = bookResponse.data().stream()
                        .filter(ticks -> ticks.symbol().equals(symbol))
                        .findAny();
                if (bidAsksForSymbol.isEmpty()) {
                    return;
                }
                var bidAsks = bidAsksForSymbol.get();
                if (bookResponse.type().equals("update")) {
                    pruningOrderBook.update(new BidsAndAsks(bidAsks.bids(), bidAsks.asks()));
                } else if (bookResponse.type().equals("snapshot")) {
                    pruningOrderBook.snapshot(new BidsAndAsks(bidAsks.bids(), bidAsks.asks()));
                }
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        System.out.println("Closed");
        return null;
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        System.out.println("Open");
        webSocket.request(1);
    }
}
