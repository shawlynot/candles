package io.shawlynot.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.shawlynot.consumer.CandleConsumer;
import org.springframework.stereotype.Component;
import org.springframework.util.function.ThrowingFunction;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


@Component
public class KrakenSubscriber {

    private final KrakenConfigProperties krakenConfigProperties;
    private final KrakenListener krakenListener;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final static long DEPTH = 10;

    public KrakenSubscriber(KrakenConfigProperties krakenConfigProperties, List<CandleConsumer> candleConsumers, Clock clock) {
        this.krakenConfigProperties = krakenConfigProperties;
        this.krakenListener = new KrakenListener(clock, candleConsumers, DEPTH);
    }

    public CompletableFuture<WebSocket> subscribe() {
        return HttpClient.newHttpClient().newWebSocketBuilder()
                .buildAsync(
                        URI.create(krakenConfigProperties.wsEndpoint()),
                        krakenListener
                )
                .thenCompose(ThrowingFunction.of(webSocket ->
                        webSocket.sendText(
                                objectMapper.writeValueAsString(getSubscriptionRequest("BTC/USD")),
                                true
                        ))
                );
    }

    private Map<String, Object> getSubscriptionRequest(String pair) {
        return Map.of(
                "method", "subscribe",
                "params", Map.of(
                        "channel", "book",
                        "symbol", List.of(pair),
                        "depth", DEPTH
                )
        );
    }
}
