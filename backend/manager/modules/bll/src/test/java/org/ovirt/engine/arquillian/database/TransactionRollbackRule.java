package org.ovirt.engine.arquillian.database;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

@Singleton
public class TransactionRollbackRule extends TestWatcher {

    private PlatformTransactionManager transactionManager;

    private TransactionStatus transactionStatus;

    @Override
    protected void finished(Description description) {
        transactionManager.rollback(transactionStatus);
        transactionStatus = null;
    }

    @Inject
    public void beforeSetup(PlatformTransactionManager transactionManager) {
        // In case an exception is thrown in a setup method, clean up before running the next test
        if (transactionStatus != null) {
            finished(null);
        }
        this.transactionManager = transactionManager;
        transactionStatus = transactionManager.getTransaction(null);
        transactionStatus.setRollbackOnly();
    }
}
