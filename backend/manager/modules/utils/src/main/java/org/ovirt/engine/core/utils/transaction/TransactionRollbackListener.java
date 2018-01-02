package org.ovirt.engine.core.utils.transaction;

@FunctionalInterface
public interface TransactionRollbackListener extends TransactionCompletionListener {
    @Override
    default void onSuccess() {};
}
