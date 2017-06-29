package org.ovirt.engine.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

public final class SynchronizeNumberOfAsyncOperations {
    private final int numberOfOperations;
    private final ISingleAsyncOperationFactory factory;

    public SynchronizeNumberOfAsyncOperations(int numberOfOperations, ArrayList<?> parameters,
                                              ISingleAsyncOperationFactory factory) {
        this.numberOfOperations = numberOfOperations;
        this.factory = factory;
        this.factory.initialize(parameters);
    }

    public void execute() {

        List<AsyncOpThread> operations = new ArrayList<>();
        for (int i = 0; i < numberOfOperations; i++) {
            operations.add(new AsyncOpThread(i));
        }

        if (numberOfOperations > 0) {
            ThreadPoolUtil.invokeAll(operations);
        }
    }

    private class AsyncOpThread implements Callable<Void> {

        private int currentEventId;

        public AsyncOpThread(int currentEventId) {
            this.currentEventId = currentEventId;
        }

        @Override
        public Void call() {
            ISingleAsyncOperation operation = factory.createSingleAsyncOperation();
            operation.execute(currentEventId);
            return null;
        }
    }

}
