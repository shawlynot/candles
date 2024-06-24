package io.shawlynot.client;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

public class CandleState {

    @Getter
    private final BigDecimal open;
    @Setter
    @Getter
    private BigDecimal high;
    @Setter
    @Getter
    private BigDecimal low;

    @Getter
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

    public long timestamp() {
        return timestamp;
    }

    public void incrementTicksCount(long ticks) {
        this.ticks += ticks;
    }

}
