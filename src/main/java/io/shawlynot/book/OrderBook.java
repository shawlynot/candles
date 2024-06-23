package io.shawlynot.book;

import io.shawlynot.consumer.Ticker;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Not thread safe
 */
public class OrderBook {

    private final PriorityQueue<Ticker> bids = new PriorityQueue<>(Comparator.comparing(Ticker::price).reversed());
    private final PriorityQueue<Ticker> asks = new PriorityQueue<>(Comparator.comparing(Ticker::price));


    private BigDecimal mid;

    public void snapshot() {

    }

    public void update() {

    }
}
