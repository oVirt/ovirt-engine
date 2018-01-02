package org.ovirt.engine.core.utils.transaction;

@FunctionalInterface
public interface TransactionSuccessListener extends TransactionCompletionListener{
    @Override
    default void onRollback() {};
}
