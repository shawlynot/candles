package io.shawlynot.kraken;

import io.shawlynot.consumer.StdOutCandleConsumer;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * For manual validation
 */

class KrakenSubscriberTest {

//    @Test
    void test() throws InterruptedException {
        KrakenSubscriber client = new KrakenSubscriber(
                new KrakenConfigProperties("wss://ws.kraken.com/v2", "BTC/USD"),
                List.of(new StdOutCandleConsumer()),
                Clock.systemUTC()
        );
        client.subscribe();
        new CountDownLatch(1).await();
    }
}