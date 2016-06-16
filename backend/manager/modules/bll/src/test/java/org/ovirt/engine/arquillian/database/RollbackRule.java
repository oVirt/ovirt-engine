package org.ovirt.engine.arquillian.database;

import javax.inject.Inject;

import org.jboss.arquillian.test.spi.event.suite.After;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

public class RollbackRule implements ArquillianRule {

    @Inject
    private PlatformTransactionManager transactionManager;

    private TransactionStatus transactionStatus;

    public void before(Before test) {
        transactionStatus = transactionManager.getTransaction(null);
        transactionStatus.setRollbackOnly();
    }

    public void after(After test) {
        transactionManager.rollback(transactionStatus);
    }

}
