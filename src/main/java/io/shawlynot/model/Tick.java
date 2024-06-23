package io.shawlynot.model;

import java.math.BigDecimal;

public record Tick(
        BigDecimal price,
        BigDecimal quantity
) {
}
