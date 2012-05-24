package org.ovirt.engine.core.bll;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;

public class ExecuteTransactionAnswer implements Answer<Object> {
    private int transactonMethodIndex;

    public ExecuteTransactionAnswer(int transactonMethodIndex) {
        this.transactonMethodIndex = transactonMethodIndex;
    }

    @Override
    public Object answer(InvocationOnMock invocationOnMock) {
        TransactionMethod<?> method = (TransactionMethod<?>) invocationOnMock.getArguments()[transactonMethodIndex];
        return method.runInTransaction();
    }
}
