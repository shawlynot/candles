package io.shawlynot.model;

import java.math.BigDecimal;

public record Tick(
        BigDecimal price,
        BigDecimal qty
) {
    public static Tick fromLongs(long price, long qty){
        return new Tick(BigDecimal.valueOf(price), BigDecimal.valueOf(qty));
    }
}
