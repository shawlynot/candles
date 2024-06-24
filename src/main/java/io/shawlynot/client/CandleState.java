package io.shawlynot.client;

import java.math.BigDecimal;

public class CandleState {

    private final BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;

    private long ticks;

    private final long timestamp;

    public CandleState(
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

    public BigDecimal getOpen() {
        return open;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public BigDecimal getLow() {
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

    public long getTicks() {
        return ticks;
    }
}
