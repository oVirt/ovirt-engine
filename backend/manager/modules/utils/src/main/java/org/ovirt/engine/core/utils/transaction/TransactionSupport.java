package org.ovirt.engine.core.utils.transaction;

import javax.ejb.TransactionRolledbackLocalException;
import javax.enterprise.inject.spi.CDI;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionSupport {

    private static final Logger log = LoggerFactory.getLogger(TransactionSupport.class);

    /**
     * JBoss specific location of TransactionManager
     */
    private static TransactionManager findTransactionManager() {
        return CDI.current().select(TransactionManager.class).get();
    }

    /**
     * Returns current transaction
     */
    public static Transaction current() {
        try {
            TransactionManager tm = findTransactionManager();
            return tm.getTransaction();
        } catch (Exception e) {
            throw new RuntimeException("Unable to get handle to current transaction", e);
        }
    }

    /**
     * Attaches rollback handler to current transaction
     */
    public static void registerRollbackHandler(final TransactionCompletionListener transactionCompletionListener) {
        try {
            current().registerSynchronization(new RollbackHandlerSynchronization(transactionCompletionListener));
        } catch (Exception e) {
            throw new RuntimeException("Unable to register synchronization to current transaction", e);
        }
    }

    private static boolean needToRollback(int status) {
        return status == Status.STATUS_MARKED_ROLLBACK || status == Status.STATUS_ROLLEDBACK
                || status == Status.STATUS_ROLLING_BACK;
    }

    /**
     * Runs given code in a given transaction scope
     */
    public static <T> T executeInScope(TransactionScopeOption scope, TransactionMethod<T> code) {
        // check if we are already in rollback

        TransactionManager tm;
        try {
            tm = findTransactionManager();
            if (needToRollback(tm.getStatus())) {
                throw new TransactionRolledbackLocalException(
                        "Current transaction is marked for rollback, no further operations are possible or desired");
            }
        } catch (SystemException e) {
            throw new RuntimeException("Failed to check transaction status - this shouldn't ever happen");
        }

        switch (scope) {
        case RequiresNew:
            return executeInNewTransaction(code);
        case Suppress:
            return executeInSuppressed(tm, code);
        case Required:
            return executeInRequired(tm, code);
        default:
            throw new RuntimeException("Undefined Scope: " + scope);
        }
    }

    private static boolean statusOneOf(int status, int... options) {
        for (int option : options) {
            if (status == option) {
                return true;
            }
        }
        return false;
    }

    /**
     * Simply executes given code in current scope It won't actually open transaction since we are assuming we always
     * start with transaction from JBoss That assumption should always hold - if not we need to look at specific case
     * where it fails
     */
    private static <T> T executeInRequired(TransactionManager tm, TransactionMethod<T> code) {
        try {
            // verify we are not in a bad state
            int status = tm.getStatus();
            if (statusOneOf(status, Status.STATUS_COMMITTED, Status.STATUS_COMMITTING, Status.STATUS_MARKED_ROLLBACK,
                    Status.STATUS_PREPARED, Status.STATUS_PREPARING, Status.STATUS_ROLLEDBACK,
                    Status.STATUS_ROLLING_BACK, Status.STATUS_UNKNOWN)) {
                throw new RuntimeException(
                        "Transaction is required to proceed but current transaction status is wrong: " + status);
            }

            if (status == Status.STATUS_NO_TRANSACTION) {
                return executeInNewTransaction(code);
            } else {
                return code.runInTransaction();
            }
        } catch (RuntimeException rte) {
            throw rte;
        } catch (Exception e) {
            log.error("executeInRequired - Wrapping Exception: {} with RunTimeException", e.getClass().getName());
            throw new RuntimeException("Failed running code", e);
        }
    }

    /**
     * Forces "SUPPRESS" and executes the code in that scope
     */
    private static <T> T executeInSuppressed(TransactionManager tm, TransactionMethod<T> code) {
        T result = null;

        try {

            Transaction transaction = tm.getTransaction();

            if (transaction != null) {
                transaction = tm.suspend();
            }

            try {
                result = code.runInTransaction();
            } finally {
                if (transaction != null) {
                    tm.resume(transaction);
                }
            }

        } catch (RuntimeException rte) {
            throw rte;
        } catch (Exception e) {
            log.error("executeInSuppressed - Wrapping Exception: {} with RunTimeException", e.getClass().getName());
            throw new RuntimeException("Failed executing code", e);
        }
        return result;
    }

    public static <T> T executeInSuppressed(TransactionMethod<T> code) {
        return executeInSuppressed(findTransactionManager(), code);
    }

    /**
     * Forces "REQUIRES_NEW" and executes given code in that scope
     */
    public static <T> T executeInNewTransaction(TransactionMethod<T> code) {
        T result = null;
        Transaction transaction = null;
        TransactionManager tm = findTransactionManager();

        try {
            // suspend existing if exists
            transaction = tm.getTransaction();
            if (transaction != null) {
                transaction = tm.suspend();
            }

            // start new transaction
            tm.begin();
            Transaction newTransaction = tm.getTransaction();

            // run the code
            try {
                result = code.runInTransaction();
            } catch (RuntimeException rte) {
                tm.rollback();
                log.info("transaction rolled back");
                throw rte;
            } catch (Exception e) {
                // code failed need to rollback
                tm.rollback();
                log.info("transaction rolled back");
                log.error("executeInNewTransaction - Wrapping Exception: {} with RunTimeException",
                        e.getClass().getName());
                throw new RuntimeException("Failed executing code", e);
            }
            // commit or rollback according to state
            if (needToRollback(newTransaction.getStatus())) {
                tm.rollback();
            } else {
                tm.commit();
            }

        } catch (SystemException | NotSupportedException | HeuristicRollbackException | HeuristicMixedException |
                 RollbackException | IllegalStateException | SecurityException e) {
            throw new RuntimeException("Failed managing transaction", e);
        } finally {
            // check if we need to resume previous transaction
            if (transaction != null) {
                try {
                    tm.resume(transaction);
                } catch (Exception e) {
                    log.error("Unable to resume transaction", e);
                }
            }
        }
        // and we are done...
        return result;

    }

    /**
     * Marks the transaction for forced rollback. The actual rollback will happen when the code reaches the transaction
     * boundary
     */
    public static void setRollbackOnly() {
        try {
            TransactionManager tm = findTransactionManager();
            Transaction transaction = tm.getTransaction();
            if (transaction != null) {
                transaction.setRollbackOnly();
            }
        } catch (SystemException e) {
            throw new RuntimeException("Failed to mark transaction for rollback", e);
        }
    }

    private static class RollbackHandlerSynchronization implements Synchronization {
        private final TransactionCompletionListener transactionCompletionListener;

        public RollbackHandlerSynchronization(TransactionCompletionListener transactionCompletionListener) {
            this.transactionCompletionListener = transactionCompletionListener;
        }

        @Override
        public void beforeCompletion() {
        }

        @Override
        public void afterCompletion(int status) {
            if (needToRollback(status)) {
                transactionCompletionListener.onRollback();
            } else {
                transactionCompletionListener.onSuccess();
            }
        }
    }
}
