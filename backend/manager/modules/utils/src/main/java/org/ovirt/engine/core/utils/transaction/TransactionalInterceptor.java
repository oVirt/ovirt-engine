package org.ovirt.engine.core.utils.transaction;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.transaction.Transaction;

import org.ovirt.engine.core.compat.TransactionScopeOption;
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

    @AroundInvoke
    public Object invoke(final InvocationContext context) throws Exception {
        Transaction current = TransactionSupport.current();
        if (current == null) {
            return runInTransaction(context, TransactionScopeOption.Required);
        } else {
            Transactional transactional = context.getMethod().getAnnotation(Transactional.class);
            if (transactional != null) {
                return runInTransaction(context, transactional.propogation());
            } else {
                return context.proceed();
            }
        }
    }

    private Object runInTransaction(final InvocationContext context, TransactionScopeOption scope) throws Exception {
        ExceptionAwareTransactionMethod method = new ExceptionAwareTransactionMethod(context);
        Object result = TransactionSupport.executeInScope(scope, method);
        if (method.getThrowable() != null) {
            throw method.getThrowable();
        }
        return result;
    }

    private static class ExceptionAwareTransactionMethod implements TransactionMethod<Object> {
        private InvocationContext context;
        private Exception throwable;

        public ExceptionAwareTransactionMethod(InvocationContext context) {
            this.context = context;
        }

        @Override
        public Object runInTransaction() {
            try {
                return context.proceed();
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
