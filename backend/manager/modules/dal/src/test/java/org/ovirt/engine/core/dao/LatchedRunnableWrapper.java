package org.ovirt.engine.core.dao;

import java.util.concurrent.CountDownLatch;

/**
 * Wrapper for a runnable that waits until countdown latch is satisfied (all other threads running code with a call to
 * CountdownLatch.await)
 */
public class LatchedRunnableWrapper implements Runnable {
    private Runnable runnable;
    private CountDownLatch latch;

    public LatchedRunnableWrapper(Runnable runnable, CountDownLatch latch) {
        this.runnable = runnable;
        this.latch = latch;
    }

    @Override
    public void run() {
        try {
            runnable.run();
        } finally {
            latch.countDown();
        }
    }
}
