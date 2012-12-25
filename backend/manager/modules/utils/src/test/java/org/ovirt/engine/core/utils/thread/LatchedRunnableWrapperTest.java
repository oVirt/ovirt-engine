/**
 *
 */
package org.ovirt.engine.core.utils.thread;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Before;
import org.junit.Test;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class LatchedRunnableWrapperTest {
    private static final int THREADS_NUMBER = 20;

    private AtomicInteger counter;
    private LatchedRunnableExecutor latchedRunnableExecuter;
    private CountDownLatch latch;
    private ExecutorService threadPool;
    private RunnableCreator runnableCreator;

    private interface RunnableCreator {
        Runnable createRunnable();
    }

    private class DummyRunnable implements Runnable {
        @Override
        public void run() {
            counter.incrementAndGet();
        }
    }

    @Before
    public void setup() {
        counter = new AtomicInteger();
        threadPool = Executors.newFixedThreadPool(THREADS_NUMBER);
        runnableCreator = new RunnableCreator() {

            @Override
            public Runnable createRunnable() {
                return new DummyRunnable();
            }
        };
    }

    @Test
    public void regularExecution() {
        prepareMocks(THREADS_NUMBER);
        latchedRunnableExecuter.execute();
        assertEquals("the counter wasn't incremented the expected number of times", THREADS_NUMBER, counter.intValue());
        verifyCommonExecutionChecks();
    }

    @Test
    public void submitFullFailure() {
        boolean gotException = false;
        prepareMocks(0);
        try {
            latchedRunnableExecuter.execute();
        } catch (RejectedExecutionException e) {
            gotException = true;
        }
        assertTrue("expected RejectedExecutionException wasn't thrown", gotException);
        assertEquals("the counter was incremented more times then expected", 0, counter.intValue());
        assertEquals("latch counter wasn't in the expected value", THREADS_NUMBER, latch.getCount());
        verifyCommonFailureChecks();
    }

    @Test
    public void submitPartialFailure() {
        int expectedToRun = THREADS_NUMBER - 5;
        prepareMocks(expectedToRun);
        boolean gotException = false;
        try {
            latchedRunnableExecuter.execute();
        } catch (RejectedExecutionException e) {
            gotException = true;
        }
        assertTrue("expected RejectedExecutionException wasn't thrown", gotException);
        assertFalse("the counter wasn't incremented the expected number of times", expectedToRun < counter.intValue());
        assertTrue("latch counter value was lower than expected", latch.getCount() > 0);
        assertTrue("latch counter value was greater than expected", latch.getCount() < THREADS_NUMBER);
        verifyCommonFailureChecks();
    }

    /**
     * @param runnableCreator
     * @param isSubmitRetry
     * @param isExecuteOnFirstRun
     */
    private void prepareMocks(final int countToExecute) {
        List<Runnable> runnables = new LinkedList<Runnable>();
        for (int index = 0; index < THREADS_NUMBER; index++) {
            runnables.add(runnableCreator.createRunnable());
        }

        latchedRunnableExecuter = spy(new LatchedRunnableExecutor(runnables));
        latch = spy(latchedRunnableExecuter.createCountDownLatch());

        doReturn(latch).when(latchedRunnableExecuter).createCountDownLatch();

        final HashSet<Runnable> executedRunnables = new HashSet<Runnable>();

        doAnswer(new Answer<LatchedRunnableWrapper>() {
            @Override
            public LatchedRunnableWrapper answer(InvocationOnMock invocation) throws Throwable {
                final LatchedRunnableWrapper toReturn =
                        new LatchedRunnableWrapper((Runnable) invocation.getArguments()[0],
                                (CountDownLatch) invocation.getArguments()[1]);
                doAnswer(new Answer<Void>() {
                    @Override
                    public Void answer(InvocationOnMock invocation) throws Throwable {
                        if (executedRunnables.size() < countToExecute) {
                            threadPool.execute(toReturn);
                            executedRunnables.add(toReturn);
                        } else {
                            throw new RejectedExecutionException();
                        }
                        return null;
                    }
                }).when(latchedRunnableExecuter).executeRunnable(toReturn);
                return toReturn;
            }
        }).when(latchedRunnableExecuter).createLatchedRunnableWrapper(any(Runnable.class), any(CountDownLatch.class));
    }

    private void verifyCommonExecutionChecks() {
        verify(latch, times(THREADS_NUMBER)).countDown();
        assertEquals("latch counter value wasn't in the expected value", 0, latch.getCount());
        try {
            verify(latch, times(1)).await();
        } catch (InterruptedException e) {
            throw new MockitoException(e.toString());
        }
    }

    private void verifyCommonFailureChecks() {
        try {
            verify(latch, never()).await();
        } catch (InterruptedException e) {
            throw new MockitoException(e.toString());
        }
    }
}
