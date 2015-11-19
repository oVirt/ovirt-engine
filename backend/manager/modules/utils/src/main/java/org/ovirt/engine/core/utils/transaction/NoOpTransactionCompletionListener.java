package org.ovirt.engine.core.utils.transaction;

public class NoOpTransactionCompletionListener implements TransactionCompletionListener {
    @Override
    public void onSuccess() {}

    @Override
    public void onRollback() {}
}
