package io.shawlynot.core.model;

import java.util.List;

public record BidsAndAsks(
    List<Tick> bids,
    List<Tick> asks
) {
}
