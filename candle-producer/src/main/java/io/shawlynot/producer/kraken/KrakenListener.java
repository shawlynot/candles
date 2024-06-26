package io.shawlynot.producer.kraken;

import io.shawlynot.producer.book.PruningOrderBook;
import io.shawlynot.producer.consumer.CandleConsumer;
import io.shawlynot.core.model.BidsAndAsks;
import io.shawlynot.core.model.Candle;
import lombok.extern.slf4j.Slf4j;

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

/**
 * WebSocket listener that receives ticks. Every minute on a scheduled thread, a candle is produced and passed to the
 * registered consumers.
 */
@Slf4j
public class KrakenListener implements WebSocket.Listener {

    private final PruningOrderBook pruningOrderBook;
    private final Clock clock;
    private BigDecimal mid;
    private CandleState candleState = null;
    private final List<CandleConsumer> candleConsumers;
    private final Lock lock = new ReentrantLock();
    private final KrakenUpdateParser krakenUpdateParser;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    public KrakenListener(long depth, Clock clock, String symbol, List<CandleConsumer> candleConsumers) {
        this.clock = clock;
        this.candleConsumers = candleConsumers;
        this.pruningOrderBook = new PruningOrderBook(depth);
        krakenUpdateParser = new KrakenUpdateParser(symbol);
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

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket, int statusCode, String reason) {
        log.info("Connection to Kraken closed");
        return null;
    }

    @Override
    public void onOpen(WebSocket webSocket) {
        log.info("Connection to Kraken open");
        scheduledExecutorService.scheduleAtFixedRate(this::publishCandle, 1, 1, TimeUnit.MINUTES);
        webSocket.request(1);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        log.error("Error in websocket connection to Kraken. Closing connection", error);
        webSocket.sendClose(2000, "error");
    }

    private void maybeUpdateOrderBook(String update) {
        var updateOrSnapshot = krakenUpdateParser.parseKrakenUpdate(update);
        if (updateOrSnapshot == null) {
            return;
        }
        KrakenTicks bidAsks = updateOrSnapshot.bidAsks();
        long tickCounts = bidAsks.bids().size() + bidAsks.asks().size();
        try {
            lock.lock();
            var asOrderBookUpdate = new BidsAndAsks(bidAsks.bids(), bidAsks.asks());
            switch (updateOrSnapshot.type()) {
                case UPDATE -> pruningOrderBook.update(asOrderBookUpdate);
                case SNAPSHOT -> pruningOrderBook.snapshot(asOrderBookUpdate);
            }
            updateCandleState(tickCounts);
        } finally {
            lock.unlock();
        }

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


    private Candle generateCandle() {
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
