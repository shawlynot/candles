package io.shawlynot.producer.kraken;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.shawlynot.producer.consumer.CandleConsumer;
import org.springframework.util.function.ThrowingFunction;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;


public class KrakenSubscriber {

    private final KrakenConfigProperties krakenConfigProperties;
    private final KrakenListener krakenListener;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final static long DEPTH = 10;

    public KrakenSubscriber(KrakenConfigProperties krakenConfigProperties, List<CandleConsumer> candleConsumers, Clock clock) {
        this.krakenConfigProperties = krakenConfigProperties;
        this.krakenListener = new KrakenListener(DEPTH, clock, krakenConfigProperties.symbol(), candleConsumers);
    }

    public CompletableFuture<KrakenSubscriber> subscribe() {
        return HttpClient.newHttpClient().newWebSocketBuilder()
                .buildAsync(
                        URI.create(krakenConfigProperties.wsEndpoint()),
                        krakenListener
                )
                .thenCompose(ThrowingFunction.of(webSocket ->
                        webSocket.sendText(
                                objectMapper.writeValueAsString(getSubscriptionRequest(krakenConfigProperties.symbol())),
                                true
                        ))
                ).thenApply((ws) -> this);
    }

    private Map<String, Object> getSubscriptionRequest(String symbol) {
        return Map.of(
                "method", "subscribe",
                "params", Map.of(
                        "channel", "book",
                        "symbol", List.of(symbol),
                        "depth", DEPTH
                )
        );
    }
}
