package org.ovirt.engine.core.utils;

import java.util.concurrent.CountDownLatch;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

public final class SyncronizeNumberOfAsyncOperations {
    private int _numberOfOperations;
    private ISingleAsyncOperationFactory _factory;

    public SyncronizeNumberOfAsyncOperations(int numberOfOperations, java.util.ArrayList parameters,
            ISingleAsyncOperationFactory factory) {
        _numberOfOperations = numberOfOperations;
        _factory = factory;
        _factory.Initialize(parameters);
    }

    private class AsyncOpThread implements Runnable {
        private CountDownLatch latch;
        private int currentEventId;

        public AsyncOpThread(CountDownLatch latch, int currentEventId) {
            this.latch = latch;
            this.currentEventId = currentEventId;
        }

        @Override
        public void run() {
            try {
                ISingleAsyncOperation operation = _factory.CreateSingleAsyncOperation();
                operation.execute(currentEventId);
            } finally {
                latch.countDown();
            }
        }
    }

    public void Execute() {
        CountDownLatch latch = new CountDownLatch(_numberOfOperations);

        for (int i = 0; i < _numberOfOperations; i++) {
            ThreadPoolUtil.execute(new AsyncOpThread(latch, i));
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
        }
    }
}
