package org.ovirt.engine.core.utils.lock;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.utils.lock.LockedObjectFactory.LockingInvocationHandler;

@RunWith(MockitoJUnitRunner.class)
public class LockedObjectFactoryLockingInvocationHandlerTest {

    @Mock
    private TestInterface testInterface;

    @Mock
    private ReentrantReadWriteLock lock;

    @Mock
    private ReadLock readLock;

    @Mock
    private WriteLock writeLock;

    private LockingInvocationHandler<TestInterface> handler;
    private static final Object[] NO_ARGUMENTS = new Object[0];
    private static final Object PROXY_INSTANCE = null;

    @Before
    public void setUp() throws Exception {
        handler = new LockingInvocationHandler<>(testInterface, lock);

        Mockito.when(lock.readLock()).thenReturn(readLock);
        Mockito.when(lock.writeLock()).thenReturn(writeLock);
    }

    @Test
    public void testNoLockAcquired() {
        invokeMethodOnHandler("unlockedMethod");
        verify(testInterface).unlockedMethod();
        verifyNoMoreInteractions(testInterface);
        verifyNoMoreInteractions(lock);
    }

    @Test
    public void testReadLockAcquired() {
        invokeMethodOnHandler("methodWithReadLock");
        verify(testInterface).methodWithReadLock();
        verifyNoMoreInteractions(testInterface);

        verify(lock).readLock();
        verify(readLock).lock();
        verify(readLock).unlock();

        verifyNoMoreInteractions(lock);
        verifyNoMoreInteractions(readLock);
        verifyNoMoreInteractions(writeLock);

    }

    @Test
    public void testWriteLockAcquired() {
        invokeMethodOnHandler("methodWithWriteLock");
        verify(testInterface).methodWithWriteLock();
        verifyNoMoreInteractions(testInterface);

        verify(lock).writeLock();
        verify(writeLock).lock();
        verify(writeLock).unlock();
        verifyNoMoreInteractions(lock);
        verifyNoMoreInteractions(readLock);
        verifyNoMoreInteractions(writeLock);
    }

    private void invokeMethodOnHandler(String methodName) {
        try {
            handler.invoke(PROXY_INSTANCE, TestInterface.class.getMethod(methodName), NO_ARGUMENTS);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    public interface TestInterface {
        void unlockedMethod();

        @AcquireReadLock
        void methodWithReadLock();

        @AcquireWriteLock
        void methodWithWriteLock();
    }
}
