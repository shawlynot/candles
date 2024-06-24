package io.shawlynot.model;

import java.math.BigDecimal;

public record Candle(
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        Long ticks,
        Long timestamp
){}
