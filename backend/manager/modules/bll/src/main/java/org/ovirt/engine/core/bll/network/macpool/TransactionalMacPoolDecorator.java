package org.ovirt.engine.core.bll.network.macpool;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.common.businessentities.ReleaseMacsTransientCompensation;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.transaction.NoOpTransactionCompletionListener;
import org.ovirt.engine.core.utils.transaction.TransactionCompletionListener;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

public final class TransactionalMacPoolDecorator extends DelegatingMacPoolDecorator implements MacPoolDecorator {

    private final UsingCompensationState usingCompensationState;
    private final UsingTxDecoratorState usingTxDecoratorState;
    private final NontransactionalState nontransactionalState;

    public TransactionalMacPoolDecorator(CommandContext commandContext) {
        usingCompensationState = new UsingCompensationState(commandContext);
        usingTxDecoratorState = new UsingTxDecoratorState();
        nontransactionalState = new NontransactionalState();
    }

    private TransactionalStrategyState getStrategyForMacRelease() {
        if (usingCompensationState.shouldUseCompensation()) {
            return usingCompensationState;
        }

        boolean shouldUseTxDecorator = TransactionSupport.current() != null;
        if (shouldUseTxDecorator) {
            return usingTxDecoratorState;
        }

        return nontransactionalState;
    }

    private List<TransactionalStrategyState> getStrategyForMacAllocation() {
        List<TransactionalStrategyState> states = new ArrayList<>();
        if (usingCompensationState.shouldUseCompensation()) {
            states.add(usingCompensationState);
        }

        boolean shouldUseTxDecorator = TransactionSupport.current() != null;
        if (shouldUseTxDecorator) {
            states.add(usingTxDecoratorState);
        }

        if (states.isEmpty()) {
            states.add(nontransactionalState);
        }

        return states;
    }

    @Override
    public final void freeMac(String mac) {
        freeMacs(Collections.singletonList(mac));
    }

    @Override
    public final void freeMacs(List<String> macs) {
        List<String> macsToRelease = filterOutUnusedMacs(macs);
        if (! macsToRelease.isEmpty()) {
            getStrategyForMacRelease().releaseMacsOnCommit(macsToRelease);
        }
    }

    private List<String> filterOutUnusedMacs(List<String> macs) {
        return macs.stream().filter(super::isMacInUse).collect(toList());
    }

    @Override
    public final List<String> allocateMacAddresses(int numberOfAddresses) {
        List<String> allocatedMacAddresses = super.allocateMacAddresses(numberOfAddresses);
        getStrategyForMacAllocation().forEach(e->e.releaseMacsInCaseOfRollback(allocatedMacAddresses));
        return allocatedMacAddresses;
    }

    @Override
    public final void forceAddMac(String mac) {
        super.forceAddMac(mac);
        getStrategyForMacAllocation().forEach(e->e.releaseMacsInCaseOfRollback(Collections.singletonList(mac)));
    }

    @Override
    public final boolean addMac(String mac) {
        boolean added = super.addMac(mac);
        if (added) {
            getStrategyForMacAllocation().forEach(e->e.releaseMacsInCaseOfRollback(Collections.singletonList(mac)));
        }

        return added;
    }

    private interface TransactionalStrategyState {

        /**
         * Macs were allocated, and they remain allocated until end of transaction. If there's "rollback of some kind",
         * these macs will be released.
         * @param macs allocated macs.
         */
        void releaseMacsInCaseOfRollback(List<String> macs);

        /**
         * Macs were returned to the pool, but they remain allocated until end of transaction. Once
         * "transaction of some kind" is committed, these macs will be released.
         *
         * @param macs macs returned to the pool.
         */
        void releaseMacsOnCommit(List<String> macs);
    }


    private class UsingCompensationState implements TransactionalStrategyState {
        private final CommandContext commandContext;
        private final ReleaseMacsCompensationListener compensationListener;

        public UsingCompensationState(CommandContext commandContext) {
            this.commandContext = commandContext;
            compensationListener = new ReleaseMacsCompensationListener();
        }

        @Override
        public void releaseMacsInCaseOfRollback(List<String> macs) {
            CompensationContext compensationContext = this.commandContext.getCompensationContext();

            compensationContext.addListener(compensationListener);
            compensationContext.snapshotObject(new ReleaseMacsTransientCompensation(Guid.newGuid(), macPoolId, macs));
        }

        @Override
        public void releaseMacsOnCommit(List<String> macs) {
            CompensationContext compensationContext = this.commandContext.getCompensationContext();

            ReleaseMacsCompensationListener compensationListener = this.compensationListener;
            compensationListener.macsToReleaseOnCommit.addAll(macs);
            compensationContext.addListener(compensationListener);

        }

        public boolean shouldUseCompensation() {
            CompensationContext compensationContext = this.commandContext.getCompensationContext();
            return compensationContext != null && compensationContext.isCompensationEnabled();
        }

        private class ReleaseMacsCompensationListener implements CompensationContext.CompensationListener {
            private final List<String> macsToReleaseOnCommit = new ArrayList<>();

            @Override
            public void afterCompensation() {
                macsToReleaseOnCommit.clear();
            }

            @Override
            public void cleaningCompensationDataAfterSuccess() {
                macsToReleaseOnCommit.forEach(macPool::freeMac);
            }

        }
    }

    private class UsingTxDecoratorState implements TransactionalStrategyState {

        @Override
        public void releaseMacsInCaseOfRollback(List<String> macs) {
            registerRollbackHandler(new ReturnToPoolAfterRollback(macs));
        }

        @Override
        public void releaseMacsOnCommit(List<String> macs) {
            registerRollbackHandler(new ReturnToPoolOnCommit(macs));
        }

        private void registerRollbackHandler(TransactionCompletionListener rollbackHandler) {
            TransactionSupport.registerRollbackHandler(rollbackHandler);
        }

        private class ReturnToPoolOnCommit extends ReleaseMacsAfterEndOfTransaction {
            public ReturnToPoolOnCommit(List<String> macs) {
                super(macs);
            }

            @Override
            public void onSuccess() {
                releaseMacs();
            }
        }

        private class ReturnToPoolAfterRollback extends ReleaseMacsAfterEndOfTransaction {
            public ReturnToPoolAfterRollback(List<String> macs) {
                super(macs);
            }

            @Override
            public void onRollback() {
                releaseMacs();
            }

        }

        private abstract class ReleaseMacsAfterEndOfTransaction extends NoOpTransactionCompletionListener {
            private final List<String> macs;

            public ReleaseMacsAfterEndOfTransaction(List<String> macs) {
                this.macs = macs;
            }

            protected void releaseMacs() {
                macs.forEach(macPool::freeMac);
            }
        }
    }

    private class NontransactionalState implements TransactionalStrategyState {

        @Override
        public void releaseMacsInCaseOfRollback(List<String> macs) {
            //there won't be any.
        }

        @Override
        public void releaseMacsOnCommit(List<String> macs) {
            macs.forEach(macPool::freeMac);
        }
    }

}
