package io.shawlynot.consumer;

import java.math.BigDecimal;

public record Ticker(
        BigDecimal price,
        BigDecimal quantity
) {
}
