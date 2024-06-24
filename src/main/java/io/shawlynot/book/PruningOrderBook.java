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
    private final Comparator<Tick> BIDS_COMPARATOR = Comparator.comparing(Tick::price).reversed();
    private final Comparator<Tick> ASKS_COMPARATOR = Comparator.comparing(Tick::price);
    private final long depth;


    public PruningOrderBook(long depth) {
        this.depth = depth;
    }

    public void snapshot(BidsAndAsks snapshot) {
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

    }

    public void update(BidsAndAsks update) {
        updateSide(update.asks(), asks, ASKS_COMPARATOR);
        updateSide(update.bids(), bids, BIDS_COMPARATOR);
    }

    public List<Tick> getBids() {
        return bids;

    }

    public List<Tick> getAsks() {
        return asks;
    }


    private void updateSide(List<Tick> updates, List<Tick> side, Comparator<Tick> sideComparator) {
        side.removeIf(tick -> updates.stream().anyMatch(update -> update.price().compareTo(tick.price()) == 0));
        var ticksToAdd = updates.stream().filter(tick -> tick.qty().compareTo(BigDecimal.ZERO) != 0).toList();
        side.addAll(ticksToAdd);
        side.sort(sideComparator);

        // Kraken does not send a message to notify that a price level has gone "too deep". This will remove any extra orders
        while (side.size() > depth) {
            side.remove(side.size() - 1);
        }
    }


}
