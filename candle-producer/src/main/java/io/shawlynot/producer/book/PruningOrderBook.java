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
 * Order book that supports "pruning". Assume there is an active subscription to the Kraken exchange to tick level data
 * up to a depth of 10. Assume Kraken provides a new tick such that there are now 11 asks in the local order book.
 * Kraken does not send an explicit "remove" tick to remove the 11th ask, and expects clients to prune them
 * automatically.
 * <p>
 * This class is <b>not</b> thread safe.
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

    /**
     * Create a new order book that automatically prunes to the specified depth
     * @param depth the depth over which ticks will be pruned
     */
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

        side.removeIf(tick -> updatesContainSamePriceLevel(updates, tick));
        var ticksToAdd = updates.stream()
                .filter(tick -> tick.qty().compareTo(BigDecimal.ZERO) != 0)
                .toList();
        side.addAll(ticksToAdd);
        side.sort(sideComparator);

        while (side.size() > depth) {
            side.remove(side.size() - 1);
        }
    }

    private boolean updatesContainSamePriceLevel(List<Tick> updates, Tick toCompare) {
        return updates.stream().anyMatch(update -> update.price().compareTo(toCompare.price()) == 0);
    }
}
