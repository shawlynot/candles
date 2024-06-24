package io.shawlynot.producer.kraken;

public record KrakenUpdateOrSnapshot(
        KrakenTicks bidAsks,
        UpdateOrSnapshot type
) {

    public enum UpdateOrSnapshot {
        UPDATE,
        SNAPSHOT
    }
}
