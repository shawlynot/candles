package io.shawlynot.producer.kraken;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.shawlynot.producer.book.PruningOrderBook;
import io.shawlynot.producer.consumer.CandleConsumer;
import io.shawlynot.core.model.BidsAndAsks;
import io.shawlynot.core.model.Candle;

import java.math.BigDecimal;
import java.math.MathContext;
import java.net.http.WebSocket;
import java.time.Clock;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class KrakenListener implements WebSocket.Listener {

    private final PruningOrderBook pruningOrderBook;
    private final Clock clock;
    private final ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private final String symbol;
    private BigDecimal mid;
    private CandleState candleState = null;

    private final List<CandleConsumer> candleConsumers;

    private final Lock lock = new ReentrantLock();


    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    public KrakenListener(long depth, Clock clock, String symbol, List<CandleConsumer> candleConsumers) {
        this.clock = clock;
        this.symbol = symbol;
        this.candleConsumers = candleConsumers;
        this.pruningOrderBook = new PruningOrderBook(depth);
    }

    /**
     * The spec of {@link WebSocket.Listener} states that this is invoked in a thread safe manner
     */
    @Override
    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        String asString = data.toString();

        maybeUpdateOrderBook(asString);

        webSocket.request(1);
        return null;
    }

    private void maybeUpdateOrderBook(String asString) {
        JsonNode tree;
        try {
            tree = objectMapper.readTree(asString);
            JsonNode maybeChannelIndicator = tree.get("channel");
            if (maybeChannelIndicator == null || !"book".equals(maybeChannelIndicator.asText())) {
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
                long tickCounts = bidAsks.bids().size() + bidAsks.asks().size();
                try {
                    lock.lock();
                    if (bookResponse.type().equals("update")) {
                        pruningOrderBook.update(new BidsAndAsks(bidAsks.bids(), bidAsks.asks()));
                    } else if (bookResponse.type().equals("snapshot")) {
                        pruningOrderBook.snapshot(new BidsAndAsks(bidAsks.bids(), bidAsks.asks()));
                    }
                    updateCandleState(tickCounts);
                } finally {
                    lock.unlock();
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
        scheduledExecutorService.scheduleAtFixedRate(this::publishCandle, 1, 1, TimeUnit.MINUTES);
        webSocket.request(1);
    }

    private void publishCandle() {
        try {
            lock.lock();
            var candle = generateCandle();
            for (var consumer : candleConsumers) {
                consumer.accept(candle);
            }
        } finally {
            lock.unlock();
        }
    }

    private void updateCandleState(long ticksCount) {
        mid = pruningOrderBook.getAsks().get(0).price().add(pruningOrderBook.getBids().get(0).price()).divide(BigDecimal.valueOf(2), MathContext.UNLIMITED);
        if (candleState == null) {
            candleState = new CandleState(
                    mid,
                    mid,
                    mid,
                    ticksCount,
                    clock.millis()
            );
        }
        candleState.incrementTicksCount(ticksCount);
        if (mid.compareTo(candleState.getHigh()) > 0) {
            candleState.setHigh(mid);
        }
        if (mid.compareTo(candleState.getLow()) < 0) {
            candleState.setLow(mid);
        }
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        error.printStackTrace();
        webSocket.sendClose(2000, "error");
    }


    public Candle generateCandle() {
        long now = clock.millis();
        if (candleState != null) {
            //build candle
            var candle = new Candle(
                    candleState.getOpen(),
                    candleState.getHigh(),
                    candleState.getLow(),
                    mid,
                    candleState.getTicks(),
                    candleState.timestamp()
            );

            //update state
            candleState = new CandleState(
                    mid,
                    mid,
                    mid,
                    0,
                    now);
            return candle;
        }
        return null;
    }

}
