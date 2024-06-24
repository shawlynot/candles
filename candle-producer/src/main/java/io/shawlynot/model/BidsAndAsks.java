package io.shawlynot.model;

import java.util.List;

public record BidsAndAsks(
    List<Tick> bids,
    List<Tick> asks
) {
}
