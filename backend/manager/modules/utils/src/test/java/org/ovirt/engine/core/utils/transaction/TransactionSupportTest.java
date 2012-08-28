package org.ovirt.engine.core.utils.transaction;

/**
 * TODO:
 * Commented out test class in order to cancel dependency on PowerMock
 * This should be revisited.
 */


//import static org.junit.Assert.assertTrue;
//import static org.mockito.Matchers.any;
//import static org.mockito.Mockito.inOrder;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.verify;
//import static org.mockito.MockitoAnnotations.initMocks;
//import static org.powermock.api.mockito.PowerMockito.doThrow;
//import static org.powermock.api.mockito.PowerMockito.mockStatic;
//import static org.powermock.api.mockito.PowerMockito.when;
//
//import javax.ejb.TransactionRolledbackLocalException;
//import javax.transaction.HeuristicMixedException;
//import javax.transaction.HeuristicRollbackException;
//import javax.transaction.InvalidTransactionException;
//import javax.transaction.NotSupportedException;
//import javax.transaction.RollbackException;
//import javax.transaction.Status;
//import javax.transaction.Synchronization;
//import javax.transaction.SystemException;
//import javax.transaction.Transaction;
//import javax.transaction.TransactionManager;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.InOrder;
//import org.mockito.Mock;
//import org.ovirt.engine.core.compat.TransactionScopeOption;
//import org.ovirt.engine.core.utils.ejb.ContainerManagedResourceType;
//import org.ovirt.engine.core.utils.ejb.EjbUtils;
//import org.powermock.core.classloader.annotations.PowerMockIgnore;
//import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
//
//@PrepareForTest({ EjbUtils.class })
//@RunWith(PowerMockRunner.class)
//@PowerMockIgnore("org.apache.log4j.*")
//public class TransactionSupportTest {
//
//    @Mock
//    TransactionManager mgr = mock(TransactionManager.class);
//    @Mock
//    Transaction transaction;
//    TransactionMethod<Boolean> tm = new TransactionMethod<Boolean>() {
//        boolean succeeded = false;
//
//        @Override
//        public Boolean runInTransaction() {
//            succeeded = true;
//            return succeeded;
//        }
//    };
//
//    public TransactionSupportTest() {
//        initMocks(this);
//        mockStatic(EjbUtils.class);
//        when(EjbUtils.findResource(ContainerManagedResourceType.TRANSACTION_MANAGER)).thenReturn(mgr);
//    }
//
//    @Test(expected = RuntimeException.class)
//    public void resumeWithError() throws InvalidTransactionException, SystemException {
//        doThrow(new SystemException()).when(mgr).resume(any(Transaction.class));
//        TransactionSupport.resume(mock(Transaction.class));
//    }
//
//    @Test(expected = RuntimeException.class)
//    public void suspendWithError() throws SystemException {
//        doThrow(new SystemException()).when(mgr).suspend();
//        TransactionSupport.suspend();
//    }
//
//    @Test(expected = RuntimeException.class)
//    public void currentWithError() throws SystemException {
//        getTransactionThrowsSystemException();
//        TransactionSupport.current();
//    }
//
//    @Test
//    public void registerRollbackHandler() throws SystemException, RollbackException {
//        transactionManagerReturnsTransaction();
//        RollbackHandler handler = mock(RollbackHandler.class);
//        TransactionSupport.registerRollbackHandler(handler);
//        verify(transaction).registerSynchronization(any(Synchronization.class));
//    }
//
//    @Test(expected = RuntimeException.class)
//    public void statusCheckFailed() throws SystemException {
//        when(mgr.getStatus()).thenThrow(new SystemException());
//        TransactionSupport.executeInScope(TransactionScopeOption.Required, tm);
//    }
//
//    @Test(expected = TransactionRolledbackLocalException.class)
//    public void needToRollbackRollback() throws SystemException {
//        when(mgr.getStatus()).thenReturn(Status.STATUS_MARKED_ROLLBACK);
//        TransactionSupport.executeInScope(TransactionScopeOption.Required, tm);
//    }
//
//    @Test(expected = TransactionRolledbackLocalException.class)
//    public void needToRollbackRolledBack() throws SystemException {
//        when(mgr.getStatus()).thenReturn(Status.STATUS_ROLLEDBACK);
//        TransactionSupport.executeInScope(TransactionScopeOption.Required, tm);
//    }
//
//    @Test(expected = TransactionRolledbackLocalException.class)
//    public void needToRollbackInProgress() throws SystemException {
//        when(mgr.getStatus()).thenReturn(Status.STATUS_ROLLING_BACK);
//        TransactionSupport.executeInScope(TransactionScopeOption.Required, tm);
//    }
//
//    @Test
//    public void executeInScopeRequiresNew() throws SystemException {
//        transactionManagerReturnsTransaction();
//        assertTrue(TransactionSupport.executeInScope(TransactionScopeOption.RequiresNew, tm));
//    }
//
//    @Test
//    public void requiresNewSuspendsExisting() throws Exception {
//        Transaction existingTrans = mock(Transaction.class);
//        Transaction newTrans = mock(Transaction.class);
//        when(mgr.getTransaction()).thenReturn(existingTrans).thenReturn(newTrans);
//        when(mgr.suspend()).thenReturn(existingTrans);
//        TransactionSupport.executeInNewTransaction(tm);
//        InOrder inOrder = inOrder(mgr);
//        inOrder.verify(mgr).suspend();
//        inOrder.verify(mgr).begin();
//        inOrder.verify(mgr).commit();
//        inOrder.verify(mgr).resume(existingTrans);
//    }
//
//    @Test(expected = RuntimeException.class)
//    public void requiresNewHandlesCodeThrowing() throws SystemException {
//        transactionManagerReturnsTransaction();
//        try {
//            TransactionSupport.executeInNewTransaction(new TransactionMethod<Object>() {
//                @Override
//                public Object runInTransaction() {
//                    throw new RuntimeException();
//                }
//            });
//        } catch (RuntimeException e) {
//            verify(mgr).rollback();
//            throw e;
//        }
//    }
//
//    @Test(expected = RuntimeException.class)
//    public void requiresNewHandlesSystemException() throws SystemException {
//        getTransactionThrowsSystemException();
//        TransactionSupport.executeInNewTransaction(tm);
//    }
//
//    @Test(expected = RuntimeException.class)
//    public void requiresNewHandlesSecurityException() throws SystemException {
//        transactionManagerReturnsTransaction();
//        when(transaction.getStatus()).thenReturn(Status.STATUS_MARKED_ROLLBACK);
//        doThrow(new SecurityException()).when(mgr).rollback();
//        TransactionSupport.executeInNewTransaction(tm);
//    }
//
//    @Test(expected = RuntimeException.class)
//    public void requiresNewHandlesIllegalStateException() throws Exception {
//        exceptionDuringCommitTest(new IllegalStateException());
//    }
//
//    @Test(expected = RuntimeException.class)
//    public void requiresNewHandlesRollbackException() throws Exception {
//        exceptionDuringCommitTest(new RollbackException("foo"));
//    }
//
//    @Test(expected = RuntimeException.class)
//    public void requiresNewHandlesHeuristicMixedException() throws Exception {
//        exceptionDuringCommitTest(new HeuristicMixedException());
//    }
//
//    @Test(expected = RuntimeException.class)
//    public void requiresNewHandlesHeuristicRollbackException() throws Exception {
//        exceptionDuringCommitTest(new HeuristicRollbackException());
//    }
//
//    @Test(expected = RuntimeException.class)
//    public void requiresNewHandlesNotSupportedException() throws Exception {
//        transactionManagerReturnsTransaction();
//        doThrow(new NotSupportedException()).when(mgr).begin();
//        TransactionSupport.executeInNewTransaction(tm);
//    }
//
//    @Test(expected = RuntimeException.class)
//    public void requiresHandlesRuntimeException() throws Exception {
//        transactionManagerReturnsTransaction();
//        TransactionSupport.executeInScope(TransactionScopeOption.Required, new TransactionMethod<Object>() {
//            @Override
//            public Object runInTransaction() {
//                throw new RuntimeException("foo");
//            }
//        });
//    }
//
//    @Test(expected = RuntimeException.class)
//    public void requiredHasCompletedTransaction() throws Exception {
//        transactionManagerReturnsTransaction();
//        when(mgr.getStatus()).thenReturn(Status.STATUS_COMMITTED);
//        TransactionSupport.executeInScope(TransactionScopeOption.Required, tm);
//    }
//
//    @Test(expected = RuntimeException.class)
//    public void requiredTransactionRollbackNeeded() throws Exception {
//        transactionManagerReturnsTransaction();
//        when(mgr.getStatus()).thenReturn(Status.STATUS_MARKED_ROLLBACK);
//        TransactionSupport.executeInScope(TransactionScopeOption.Required, tm);
//    }
//
//    @Test
//    public void executeInScopeSuppress() {
//        assertTrue(TransactionSupport.executeInScope(TransactionScopeOption.Suppress, tm));
//    }
//
//    @Test
//    public void executeInScopeRequired() {
//        assertTrue(TransactionSupport.executeInScope(TransactionScopeOption.Required, tm));
//    }
//
//    @Test
//    public void setRollback() throws SystemException {
//        transactionManagerReturnsTransaction();
//        TransactionSupport.setRollbackOnly();
//        verify(transaction).setRollbackOnly();
//    }
//
//    @Test(expected = RuntimeException.class)
//    public void setRollbackFailure() throws SystemException {
//        getTransactionThrowsSystemException();
//        TransactionSupport.setRollbackOnly();
//    }
//
//    @Test
//    public void executeSuppressed() throws SystemException, InvalidTransactionException {
//        transactionManagerReturnsTransaction();
//        suspendReturnsTransaction();
//        assertTrue(TransactionSupport.executeInScope(TransactionScopeOption.Suppress, tm));
//        InOrder inOrder = inOrder(mgr);
//        inOrder.verify(mgr).suspend();
//        inOrder.verify(mgr).resume(transaction);
//    }
//
//    @Test(expected = RuntimeException.class)
//    public void executeSuppressedThrowsRuntime() throws SystemException {
//        when(mgr.getTransaction()).thenThrow(new RuntimeException());
//        TransactionSupport.executeInScope(TransactionScopeOption.Suppress, tm);
//    }
//
//    @Test(expected = RuntimeException.class)
//    public void executeSuppressedHandlesSystemException() throws SystemException {
//        getTransactionThrowsSystemException();
//        TransactionSupport.executeInScope(TransactionScopeOption.Suppress, tm);
//    }
//
//    @Test(expected = RuntimeException.class)
//    public void executeSuppressedHandlesInvalidTransactionException() throws SystemException,
//            InvalidTransactionException {
//        transactionManagerReturnsTransaction();
//        suspendReturnsTransaction();
//        doThrow(new InvalidTransactionException()).when(mgr).resume(transaction);
//        TransactionSupport.executeInScope(TransactionScopeOption.Suppress, tm);
//    }
//
//    private void exceptionDuringCommitTest(Exception e) throws Exception {
//        transactionManagerReturnsTransaction();
//        doThrow(e).when(mgr).commit();
//        TransactionSupport.executeInNewTransaction(tm);
//    }
//
//    private void transactionManagerReturnsTransaction() throws SystemException {
//        when(mgr.getTransaction()).thenReturn(transaction);
//    }
//
//    private void suspendReturnsTransaction() throws SystemException {
//        when(mgr.suspend()).thenReturn(transaction);
//    }
//
//    private void getTransactionThrowsSystemException() throws SystemException {
//        when(mgr.getTransaction()).thenThrow(new SystemException());
//    }
// }
