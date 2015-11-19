package org.ovirt.engine.core.utils.transaction;

/**
 * Listener used to react upon transaction completion.
 */
public interface TransactionCompletionListener {

    /**
     * Method is called after transaction is committed.
     */
    void onSuccess();

    /**
     * Method is called after transaction is rolledback.
     */
    void onRollback();
}
