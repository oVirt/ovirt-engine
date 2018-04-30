package org.ovirt.engine.core.bll.network.macpool;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.AdditionalAnswers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.utils.InjectedMock;
import org.ovirt.engine.core.utils.InjectorExtension;
import org.ovirt.engine.core.utils.lock.LockedObjectFactory;

@ExtendWith({MockitoExtension.class, InjectorExtension.class})
public class TransactionalMacPoolDecoratorRollbackTest {
    @Mock
    private LockedObjectFactory lockedObjectFactory;

    @Mock
    @InjectedMock
    public TransactionManager transactionManager;

    @Mock
    private MacPool sourceMacPool;

    @Mock
    private MacPool targetMacPool;

    private Transaction transaction = new TransactionStub();
    private final CommandContext commandContext = new CommandContext(new EngineContext());

    private static final List<String> SOURCE_POOL_MACS = Arrays.asList("90:2b:34:d6:72:49", "90:2b:34:d6:72:4a");

    @Test
    public void testUnsuccessfulMigrationRevertsToOriginalState() throws Exception {
        when(transactionManager.getTransaction()).thenReturn(transaction);
        mockLockObjectFactoryToDisableLocking();
        mockThatDuringAddingToTargetPoolOnlyFirstMacWillBeAdded();

        DecoratedMacPoolFactory decoratedMacPoolFactory = new DecoratedMacPoolFactory(this.lockedObjectFactory);
        MacPool decoratedSourceMacPool = createDecoratedPool(decoratedMacPoolFactory, sourceMacPool);
        MacPool decoratedTargetMacPool = createDecoratedPool(decoratedMacPoolFactory, targetMacPool);

        decoratedSourceMacPool.freeMacs(SOURCE_POOL_MACS);
        decoratedTargetMacPool.addMacs(SOURCE_POOL_MACS);

        verify(sourceMacPool).getId();
        verify(targetMacPool).getId();

        //related to releasing macs.
        verify(sourceMacPool, times(2)).isMacInUse(any());
        //actual freing won't be invoked, macs are being held until TX end.
        verify(sourceMacPool, never()).freeMacs(SOURCE_POOL_MACS);

        verify(targetMacPool).addMacs(SOURCE_POOL_MACS);
        verifyNoMoreInteractions(sourceMacPool, targetMacPool);

        //simulates TX rollback
        transaction.rollback();

        //no macs were actually released(release was waiting for commit), thus no macs will be added back.
        verify(sourceMacPool, never()).addMac(any());
        verify(sourceMacPool, never()).addMacs(any());

        //because we mocked, that second mac won't be added due to duplicity, only first one will be released.
        verify(targetMacPool).freeMacs(Collections.singletonList(SOURCE_POOL_MACS.get(0)));

        verifyNoMoreInteractions(sourceMacPool, targetMacPool);
    }

    /**
     * … the other one won't be added, due to duplicity
     */
    private void mockThatDuringAddingToTargetPoolOnlyFirstMacWillBeAdded() {
        when(targetMacPool.addMacs(any())).thenAnswer(invocation -> {
            List<String> macs = invocation.getArgument(0);
            return Collections.singletonList(macs.get(1));
        });
    }

    private void mockLockObjectFactoryToDisableLocking() {
        when(lockedObjectFactory
                .createLockingInstance(any(), eq(MacPool.class), any()))
                .thenAnswer(AdditionalAnswers.returnsArgAt(0));
    }

    private MacPool createDecoratedPool(DecoratedMacPoolFactory decoratedMacPoolFactory, MacPool pool) {
        List<MacPoolDecorator> decorators = singletonList(new TransactionalMacPoolDecorator(commandContext));
        return decoratedMacPoolFactory.createDecoratedPool(pool, decorators);
    }

    private static class TransactionStub implements Transaction {

        private Synchronization sync;

        @Override
        public void commit() throws SecurityException, IllegalStateException {

        }

        @Override
        public boolean delistResource(XAResource xaRes, int flag) throws IllegalStateException {
            throw new UnsupportedOperationException("Not implemented yet");
        }

        @Override
        public boolean enlistResource(XAResource xaRes) throws IllegalStateException {
            throw new UnsupportedOperationException("Not implemented yet");
        }

        @Override
        public int getStatus() {
            return Status.STATUS_ROLLEDBACK;
        }

        @Override
        public void registerSynchronization(Synchronization sync) throws IllegalStateException {
            this.sync = sync;
        }

        @Override
        public void rollback() throws IllegalStateException {
            this.sync.afterCompletion(getStatus());
        }

        @Override
        public void setRollbackOnly() throws IllegalStateException {
            throw new UnsupportedOperationException("Not implemented yet");
        }
    }
}
