package io.shawlynot.book;


import io.shawlynot.consumer.CandleConsumer;
import io.shawlynot.model.BidsAndAsks;
import io.shawlynot.model.Candle;
import io.shawlynot.model.Tick;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PruningOrderBookTest {

    Clock clock = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);
    PruningOrderBook pruningOrderBook;

    @BeforeEach
    void setUp() {
        pruningOrderBook = new PruningOrderBook(2);
    }

    @Test
    void testUpdatesReplaceLevelsInSnapShot() {
        pruningOrderBook.snapshot(new BidsAndAsks(
                List.of(Tick.fromLongs(1L, 1L), Tick.fromLongs(2L, 3L)),
                List.of(Tick.fromLongs(6L, 10L))
        ));
        pruningOrderBook.update(new BidsAndAsks(
                List.of(Tick.fromLongs(2L, 1L)),
                List.of(Tick.fromLongs(6L, 2L))
        ));


        assertThat(pruningOrderBook.getBids()).containsExactly(
                Tick.fromLongs(2L, 1L),
                Tick.fromLongs(1L, 1L)
        );
        assertThat(pruningOrderBook.getAsks()).containsExactly(
                Tick.fromLongs(6L, 2L)
        );
    }

    @Test
    void testPrunesTicksTooDeep() {
        pruningOrderBook.snapshot(new BidsAndAsks(
                List.of(Tick.fromLongs(1L, 1L), Tick.fromLongs(2L, 3L)),
                List.of(Tick.fromLongs(6L, 10L))
        ));
        pruningOrderBook.update(new BidsAndAsks(
                List.of(Tick.fromLongs(4L, 1L)),
                List.of()
        ));

        assertThat(pruningOrderBook.getBids()).containsExactly(
                Tick.fromLongs(4L, 1L),
                Tick.fromLongs(2L, 3L)
        );
        assertThat(pruningOrderBook.getAsks()).containsExactly(
                Tick.fromLongs(6L, 10L)
        );
    }


    static class StubConsumer implements CandleConsumer {

        List<Candle> candles = new ArrayList<>();

        @Override
        public void accept(Candle candle) {
            candles.add(candle);
        }
    }

}