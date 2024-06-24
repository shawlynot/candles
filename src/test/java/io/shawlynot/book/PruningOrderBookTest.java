package io.shawlynot.book;


import io.shawlynot.model.BidsAndAsks;
import io.shawlynot.model.Tick;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PruningOrderBookTest {

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
    void testPrunesBidsTooDeep() {
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
                // the price=1 bid tick has been removed
        );
        assertThat(pruningOrderBook.getAsks()).containsExactly(
                Tick.fromLongs(6L, 10L)
        );
    }


    @Test
    void testPrunesAsksTooDeep() {
        pruningOrderBook.snapshot(new BidsAndAsks(
                List.of(Tick.fromLongs(1L, 1L)),
                List.of(Tick.fromLongs(6L, 10L), Tick.fromLongs(8L, 10L))
        ));
        pruningOrderBook.update(new BidsAndAsks(
                List.of(),
                List.of(Tick.fromLongs(5L, 1L))
        ));

        assertThat(pruningOrderBook.getBids()).containsExactly(
                Tick.fromLongs(1L, 1L)
        );
        assertThat(pruningOrderBook.getAsks()).containsExactly(
                Tick.fromLongs(5L, 1L),
                Tick.fromLongs(6L, 10L)
                // the price=8 ask tick has been removed
        );
    }


    @Test
    void testRemovesZeroQuantity(){
        pruningOrderBook.snapshot(new BidsAndAsks(
                List.of(Tick.fromLongs(1L, 1L), Tick.fromLongs(2L, 3L)),
                List.of(Tick.fromLongs(6L, 10L))
        ));
        pruningOrderBook.update(new BidsAndAsks(
                List.of(new Tick(BigDecimal.valueOf(20, 1), BigDecimal.valueOf(0, 8))), //remove price=2
                List.of()
        ));

        assertThat(pruningOrderBook.getBids()).containsExactly(
                Tick.fromLongs(1L, 1L)
                // the price=2 tick has been removed
        );
        assertThat(pruningOrderBook.getAsks()).containsExactly(
                Tick.fromLongs(6L, 10L)
        );
    }
}