package io.shawlynot.client;

import io.shawlynot.book.PruningOrderBook;
import io.shawlynot.consumer.CandleConsumer;

import java.net.http.WebSocket;
import java.time.Clock;
import java.util.List;
import java.util.concurrent.CompletionStage;

public class KrakenListener implements WebSocket.Listener {

    private final PruningOrderBook pruningOrderBook;

    public KrakenListener(Clock clock, List<CandleConsumer> candleConsumers, long depth) {
        this.pruningOrderBook = new PruningOrderBook(clock, candleConsumers, depth);
    }

    /**
     * The spec of {@link WebSocket.Listener} states that this is invoked in a thread safe manner
     */
    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        System.out.println(data);

        webSocket.request(1);
        return null;
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
