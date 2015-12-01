package org.ovirt.engine.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

public final class SyncronizeNumberOfAsyncOperations {
    private int _numberOfOperations;
    private ISingleAsyncOperationFactory _factory;

    public SyncronizeNumberOfAsyncOperations(int numberOfOperations, ArrayList<?> parameters,
            ISingleAsyncOperationFactory factory) {
        _numberOfOperations = numberOfOperations;
        _factory = factory;
        _factory.initialize(parameters);
    }

    public void execute() {

        List<AsyncOpThread> operations = new ArrayList<>();
        for (int i = 0; i < _numberOfOperations; i++) {
            operations.add(new AsyncOpThread(i));
        }

        if (_numberOfOperations > 0) {
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
            ISingleAsyncOperation operation = _factory.createSingleAsyncOperation();
            operation.execute(currentEventId);
            return null;
        }
    }

}
