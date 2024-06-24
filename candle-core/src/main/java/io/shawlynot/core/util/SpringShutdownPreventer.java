package io.shawlynot.core.util;

import org.springframework.beans.factory.DisposableBean;

import java.util.concurrent.CountDownLatch;

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
     * Means the application will still exit during @Spring boot test
     */
    @Override
    public void destroy() {
        stopShutdown.countDown();
    }
}
