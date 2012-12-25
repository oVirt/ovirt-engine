package org.ovirt.engine.core.utils.thread;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;

import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

public class LatchedRunnableExecutor {

    private Collection<Runnable> runnables;

    /**
     * @param runnables
     *            - list of runnables for execution
     */
    public LatchedRunnableExecutor(Collection<Runnable> runnables) {
        this.runnables = runnables;
    }

    protected void executeRunnable(LatchedRunnableWrapper runnable) {
        ThreadPoolUtil.execute(runnable);
    }

    protected LatchedRunnableWrapper createLatchedRunnableWrapper(Runnable runnable, CountDownLatch latch) {
        return new LatchedRunnableWrapper(runnable, latch);
    }

    protected CountDownLatch createCountDownLatch() {
        return new CountDownLatch(runnables.size());
    }

    /**
     * executes the list of Runnable provided to this executer during creations and waits till the execution of all
     * runnables is done.
     */
    public void execute() {
        CountDownLatch latch = createCountDownLatch();
        for (Runnable runnable : runnables) {
            LatchedRunnableWrapper latchWrapper = createLatchedRunnableWrapper(runnable, latch);
            executeRunnable(latchWrapper);
        }
        try {
            latch.await();
        } catch (InterruptedException e) {
        }
    }
}
