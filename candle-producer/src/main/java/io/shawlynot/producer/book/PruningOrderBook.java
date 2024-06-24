package io.shawlynot.producer.book;


import io.shawlynot.core.model.BidsAndAsks;
import io.shawlynot.core.model.Tick;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * //TODO talk about KRaken depth
 */
@Slf4j
public class PruningOrderBook {


    @Getter
    private final List<Tick> bids = new ArrayList<>();

    @Getter
    private final List<Tick> asks = new ArrayList<>();
    private final Comparator<Tick> BIDS_COMPARATOR = Comparator.comparing(Tick::price).reversed();
    private final Comparator<Tick> ASKS_COMPARATOR = Comparator.comparing(Tick::price);
    private final long depth;


    public PruningOrderBook(long depth) {
        this.depth = depth;
    }

    public void snapshot(BidsAndAsks snapshot) {
        if (snapshot.asks().isEmpty() || snapshot.bids().isEmpty()) {
            log.warn("Invalid snapshot: bids or asks is empty");
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

        if (asks.isEmpty() || bids.isEmpty()) {
            log.warn("Order book is in an invalid state: bids or asks is empty");
        }
    }


    private void updateSide(List<Tick> updates, List<Tick> side, Comparator<Tick> sideComparator) {
        if (updates.isEmpty()) {
            return;
        }

        side.removeIf(tick -> updates.stream().anyMatch(update -> update.price().compareTo(tick.price()) == 0));
        var ticksToAdd = updates.stream().filter(tick -> tick.qty().compareTo(BigDecimal.ZERO) != 0).toList();
        side.addAll(ticksToAdd);
        side.sort(sideComparator);

        // Kraken does not send a message to notify that a price level has gone "too deep" and requires clients "prune"
        // them. This will remove any extra orders
        while (side.size() > depth) {
            side.remove(side.size() - 1);
        }
    }


}
