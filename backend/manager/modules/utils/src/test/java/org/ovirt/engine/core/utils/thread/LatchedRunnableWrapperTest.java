/**
 *
 */
package org.ovirt.engine.core.utils.thread;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class LatchedRunnableWrapperTest {

    private AtomicInteger counter;

    private interface RunnableCreator {
        Runnable createRunnable();
    }


    private class DummyRunnable implements Runnable {


        public DummyRunnable() {
        }

        @Override
        public void run() {
            counter.incrementAndGet();
        }
    }

    @Before
    public void setup() {
        counter = new AtomicInteger();
    }

    /**
     *
     */
    @Test
    public void latchedRunnableWrapperTest() {
        final int threadsNumber = 100;
        runThreads(threadsNumber, new RunnableCreator() {

            @Override
            public Runnable createRunnable() {
                return new DummyRunnable();
            }
        });
        assertEquals(threadsNumber, counter.intValue());
    }

    private void runThreads(final int threadsNumber, RunnableCreator runnableCreator) {
        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(threadsNumber);
        CountDownLatch latch = new CountDownLatch(threadsNumber);
        for (int index = 0; index < threadsNumber; index++) {
            fixedThreadPool.execute(new LatchedRunnableWrapper(runnableCreator.createRunnable(), latch));
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
