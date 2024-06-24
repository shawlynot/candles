package io.shawlynot.core.util;

import org.springframework.beans.factory.DisposableBean;

import java.util.concurrent.CountDownLatch;

/**
 * If registered, stops a non-web Spring boot application from exiting immediately, without breaking @SpringBootTest
 */
public class SpringShutdownPreventer implements DisposableBean {

    public static SpringShutdownPreventer get() {
        return new SpringShutdownPreventer();
    }

    private final CountDownLatch stopShutdown;

    private SpringShutdownPreventer() {
        stopShutdown = new CountDownLatch(1);
        new Thread(() -> {
            try {
                stopShutdown.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    /**
     * Stop the thread blocking shutdown. Called by Spring e.g. during JVM shutdown or when a @SpringBootTest method
     * completes.
     */
    @Override
    public void destroy() {
        stopShutdown.countDown();
    }
}
