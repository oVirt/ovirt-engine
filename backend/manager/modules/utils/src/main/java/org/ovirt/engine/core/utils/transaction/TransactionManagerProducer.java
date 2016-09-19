package org.ovirt.engine.core.utils.transaction;

import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

@Singleton
public class TransactionManagerProducer {

    @Produces
    @Singleton
    public TransactionManager getTransactionManager() throws NamingException {
        return (TransactionManager) new InitialContext().lookup("java:jboss/TransactionManager");
    }
}
