package io.shawlynot.book;


import io.shawlynot.model.BidsAndAsks;
import io.shawlynot.model.Candle;
import io.shawlynot.model.Tick;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * //TODO talk about KRaken depth
 */
public class PruningOrderBook {


    private final List<Tick> bids = new ArrayList<>();

    private final List<Tick> asks = new ArrayList<>();
    private BigDecimal mid;
    private final Clock clock;
    private static final int CANDLE_INTERVAL = 1000 * 60;
    private final Comparator<Tick> BIDS_COMPARATOR = Comparator.comparing(Tick::price).reversed();
    private final Comparator<Tick> ASKS_COMPARATOR = Comparator.comparing(Tick::price);
    private final long depth;

    private CandleState candleState = null;

    public PruningOrderBook(Clock clock, long depth) {
        this.clock = clock;
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
        mid = asks.get(0).price().add(bids.get(0).price()).divide(BigDecimal.valueOf(2), MathContext.UNLIMITED);

        if (candleState == null) {
            candleState = new CandleState(
                    mid,
                    mid,
                    mid,
                    0,
                    now);
        }
        updateCandleState(snapshot.asks().size() + snapshot.bids().size());
    }

    public void update(BidsAndAsks update) {
        long askTicksAdded = updateSide(update.asks(), asks, ASKS_COMPARATOR);
        long countTicksAdded = updateSide(update.bids(), bids, BIDS_COMPARATOR);
        updateCandleState(askTicksAdded + countTicksAdded);

    }

    public List<Tick> getBids() {
        return bids;

    }

    public List<Tick> getAsks() {
        return asks;
    }


    private long updateSide(List<Tick> updates, List<Tick> side, Comparator<Tick> sideComparator) {
        side.removeIf(tick -> updates.stream().anyMatch(update -> update.price().compareTo(tick.price()) == 0));
        var ticksToAdd = updates.stream().filter(tick -> tick.qty().compareTo(BigDecimal.ZERO) != 0).toList();
        side.addAll(ticksToAdd);
        side.sort(sideComparator);

        // Kraken does not send a message to notify that a price level has gone "too deep". This will remove any extra orders
        while (side.size() > depth) {
            side.remove(side.size() - 1);
        }
        return ticksToAdd.size();
    }

    private void updateCandleState(long ticksCount) {
        mid = asks.get(0).price().add(bids.get(0).price()).divide(BigDecimal.valueOf(2), MathContext.UNLIMITED);
        candleState.incrementTicksCount(ticksCount);
        if (mid.compareTo(candleState.high()) > 0) {
            candleState.setHigh(mid);
        }
        if (mid.compareTo(candleState.low()) < 0) {
            candleState.setLow(mid);
        }
    }

    public Candle getCandle() {
        long now = clock.millis();
        if (candleState != null) {
            //build candle
            var candle = new Candle(
                    candleState.open(),
                    candleState.high(),
                    candleState.low(),
                    mid,
                    candleState.ticks,
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


    private static final class CandleState {
        private final BigDecimal open;
        private BigDecimal high;
        private BigDecimal low;

        private long ticks;

        private final long timestamp;

        private CandleState(
                BigDecimal open,
                BigDecimal high,
                BigDecimal low,
                long ticks,
                long timestamp
        ) {
            this.open = open;
            this.high = high;
            this.low = low;
            this.ticks = ticks;
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

        public void incrementTicksCount(long ticks) {
            this.ticks += ticks;
        }
    }

}
