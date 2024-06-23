package io.shawlynot.book;


import io.shawlynot.consumer.CandleConsumer;
import io.shawlynot.model.BidsAndAsks;
import io.shawlynot.model.Candle;
import io.shawlynot.model.Tick;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Clock;
import java.util.*;

/**
 * Not thread safe
 */
public class PruningOrderBook {

    private final List<Tick> bids = new ArrayList<>();
    private final List<Tick> asks = new ArrayList<>();

    private final Clock clock;
    private static final int MINUTE_MILLIS = 1000 * 60;
    private final Comparator<Tick> BIDS_COMPARATOR = Comparator.comparing(Tick::price).reversed();
    private final Comparator<Tick> ASKS_COMPARATOR = Comparator.comparing(Tick::price);
    private final List<CandleConsumer> consumers;

    private final long depth;

    private CandleState candleState = null;

    public PruningOrderBook(Clock clock, List<CandleConsumer> consumers, long depth) {
        this.clock = clock;
        this.consumers = consumers;
        this.depth = depth;
    }

    public void snapshot(BidsAndAsks snapshot) {
        long now = clock.millis();
        if (snapshot.asks().isEmpty() || snapshot.bids().isEmpty()) {
            System.out.println("No bids or asks, ignoring");
            return;
        }

        bids.clear();
        asks.clear();
        bids.addAll(snapshot.bids());
        asks.addAll(snapshot.asks());

        bids.sort(BIDS_COMPARATOR);
        asks.sort(ASKS_COMPARATOR);

        BigDecimal mid = asks.get(0).price().add(bids.get(0).price()).divide(BigDecimal.valueOf(2), MathContext.UNLIMITED);

        if (candleState == null) {
            candleState = new CandleState(
                    mid,
                    mid,
                    mid,
                    now
            );
        }

        calculateCandle(mid, now);
    }

    public void update(BidsAndAsks update) {
        long now = clock.millis();
        updateSide(update.asks(), asks, ASKS_COMPARATOR);
        updateSide(update.bids(), bids, BIDS_COMPARATOR);

        BigDecimal mid = asks.get(0).price().add(bids.get(0).price()).divide(BigDecimal.valueOf(2), MathContext.UNLIMITED);
        calculateCandle(mid, now);
    }

    private void updateSide(List<Tick> updates, List<Tick> side, Comparator<Tick> sideComparator) {
        var removals = updates.stream()
                .filter(tick -> tick.quantity().compareTo(BigDecimal.ZERO) == 0)
                .map(Tick::price)
                .toList();
        if (!removals.isEmpty()) {
            side.removeIf(tick -> removals.contains(tick.price()));
        }
        var asksToAdd = updates.stream().filter(tick -> tick.quantity().compareTo(BigDecimal.ZERO) != 0).toList();
        side.addAll(asksToAdd);
        side.sort(sideComparator);

        // Kraken does not send a message to notify that a price level has gone "too deep". This will remove any extra orders
        while (side.size() > depth) {
            side.remove(side.size() - 1);
        }
    }

    private void calculateCandle(BigDecimal mid, long now) {
        if (mid.compareTo(candleState.high()) > 0) {
            candleState.setHigh(mid);
        }
        if (mid.compareTo(candleState.low()) < 0) {
            candleState.setLow(mid);
        }
        if (now > candleState.timestamp() + MINUTE_MILLIS) {
            //build candle
            var candle = new Candle(
                    candleState.open(),
                    candleState.high(),
                    candleState.low(),
                    mid,
                    candleState.timestamp()
            );


            //pass to consumer
            for (var consumer : consumers) {
                consumer.accept(candle);
            }

            //update state
            candleState = new CandleState(
                    mid,
                    mid,
                    mid,
                    now
            );
        }
    }


    private static final class CandleState {
        private final BigDecimal open;
        private BigDecimal high;
        private BigDecimal low;
        private final long timestamp;

        private CandleState(
                BigDecimal open,
                BigDecimal high,
                BigDecimal low,
                long timestamp
        ) {
            this.open = open;
            this.high = high;
            this.low = low;
            this.timestamp = timestamp;
        }

        public BigDecimal open() {
            return open;
        }

        public BigDecimal high() {
            return high;
        }

        public BigDecimal low() {
            return low;
        }

        public long timestamp() {
            return timestamp;
        }

        public void setHigh(BigDecimal high) {
            this.high = high;
        }

        public void setLow(BigDecimal low) {
            this.low = low;
        }
    }

}
