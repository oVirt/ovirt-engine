package org.ovirt.engine.core.dao.jpa;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.persistence.EntityManager;
import javax.transaction.Transaction;

import org.ovirt.engine.core.compat.TransactionScopeOption;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.ovirt.engine.core.utils.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This interceptor is connected to the @Transactional annotation, and does the actual work of creating the transaction,
 * based on the required scope.
 */
@Transactional
@Interceptor
public class TransactionalInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(TransactionalInterceptor.class);

    @Inject
    private EntityManagerHolder entityManagerHolder;

    @AroundInvoke
    public Object invoke(final InvocationContext context) throws Exception {
        Transaction current = TransactionSupport.current();
        Transactional transactional = context.getMethod().getAnnotation(Transactional.class);

        if (current == null && transactional == null) {
            return runInTransaction(context, TransactionScopeOption.Required, false);
        } else {
            if (transactional != null) {
                return runInTransaction(context, transactional.propogation(), transactional.readOnly());
            } else {
                ensureEntityManager();
                Object result = context.proceed();
                return result;
            }
        }
    }

    private EntityManager ensureEntityManager() {
        EntityManager entityManager = entityManagerHolder.getEntityManager();
        entityManager.joinTransaction();
        return entityManager;
    }

    private Object runInTransaction(final InvocationContext context, TransactionScopeOption scope, boolean readOnly)
            throws Exception {
        ExceptionAwareTransactionMethod method =
                new ExceptionAwareTransactionMethod(context, readOnly);
        Object result = TransactionSupport.executeInScope(scope, method);
        if (method.getThrowable() != null) {
            throw method.getThrowable();
        }
        return result;
    }

    private class ExceptionAwareTransactionMethod implements TransactionMethod<Object> {
        private InvocationContext context;
        private Exception throwable;
        private boolean readOnly;

        public ExceptionAwareTransactionMethod(InvocationContext context,
                boolean readOnly) {
            this.context = context;
            this.readOnly = readOnly;
        }

        @Override
        public Object runInTransaction() {
            try {
                boolean createdEntityManager = false;
                if (entityManagerHolder.getEntityManagerDontCreate() == null) {
                    createdEntityManager = true;
                }
                EntityManager em = ensureEntityManager();
                Object result = context.proceed();
                if (!readOnly) {
                    em.flush();
                }
                if (createdEntityManager) {
                    em.close();
                    entityManagerHolder.nullEntityManager();
                }
                return result;
            } catch (Exception e) {
                logger.error("Failed to run operation in a new transaction", e);
                throwable = e;
                return null;
            }
        }

        public Exception getThrowable() {
            return throwable;
        }
    };
}
