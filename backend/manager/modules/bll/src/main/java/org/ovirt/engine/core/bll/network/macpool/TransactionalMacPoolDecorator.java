package org.ovirt.engine.core.bll.network.macpool;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.context.CompensationContext;
import org.ovirt.engine.core.common.businessentities.ReleaseMacsTransientCompensation;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.transaction.TransactionCompletionListener;
import org.ovirt.engine.core.utils.transaction.TransactionRollbackListener;
import org.ovirt.engine.core.utils.transaction.TransactionSuccessListener;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TransactionalMacPoolDecorator extends DelegatingMacPoolDecorator implements MacPoolDecorator {

    private static final Logger log = LoggerFactory.getLogger(TransactionalMacPoolDecorator.class);

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

        log.debug("Using {} as allocation strategies", states);
        return states;
    }

    @Override
    public final void freeMac(String mac) {
        freeMacs(Collections.singletonList(mac));
    }

    @Override
    public final void freeMacs(List<String> macs) {
        List<String> macsToRelease = filterOutUnusedMacs(macs);
        if (macsToRelease.isEmpty()) {
            log.warn("Trying to release MACs using empty collection as parameter.");
        } else {
            //we need to recalculate this on every call, since command context might change in between calls
            TransactionalStrategyState strategyForMacRelease = getStrategyForMacRelease();
            log.debug("Using {} as release strategy", strategyForMacRelease);
            strategyForMacRelease.releaseMacsOnCommit(macsToRelease);
        }
    }

    private List<String> filterOutUnusedMacs(List<String> macs) {
        return macs.stream().filter(super::isMacInUse).collect(toList());
    }

    @Override
    public String allocateNewMac() {
        String allocatedMacAddress = super.allocateNewMac();
        getStrategyForMacAllocation().forEach(e->e.releaseMacsInCaseOfRollback(Collections.singletonList(allocatedMacAddress)));
        return allocatedMacAddress;
    }

    @Override
    public final List<String> allocateMacAddresses(int numberOfAddresses) {
        List<String> allocatedMacAddresses = super.allocateMacAddresses(numberOfAddresses);
        getStrategyForMacAllocation().forEach(e->e.releaseMacsInCaseOfRollback(allocatedMacAddresses));
        return allocatedMacAddresses;
    }

    @Override
    public final boolean addMac(String mac) {
        boolean added = super.addMac(mac);
        if (added) {
            getStrategyForMacAllocation().forEach(e->e.releaseMacsInCaseOfRollback(Collections.singletonList(mac)));
        }

        return added;
    }

    @Override
    public List<String> addMacs(List<String> macs) {
        List<String> notAddedMacs = super.addMacs(macs);

        boolean atLeastOneAddedMac = notAddedMacs.size() < macs.size();
        if (atLeastOneAddedMac) {
            List<String> addedMacs = macs.stream().filter(e->!notAddedMacs.contains(e)).collect(toList());
            getStrategyForMacAllocation().forEach(e->e.releaseMacsInCaseOfRollback(addedMacs));
        }

        return notAddedMacs;
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
        private final Logger log = LoggerFactory.getLogger(UsingCompensationState.class);

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
            compensationContext.snapshotObject(new ReleaseMacsTransientCompensation(Guid.newGuid(),
                    macPool.getId(),
                    macs));
        }

        @Override
        public void releaseMacsOnCommit(List<String> macs) {
            CompensationContext compensationContext = this.commandContext.getCompensationContext();

            ReleaseMacsCompensationListener compensationListener = this.compensationListener;
            log.debug("Registering macs: {} to be released in case of successful execution", macs);
            compensationListener.macsToReleaseOnCommit.addAll(macs);

            log.debug("Registering compensation listener {}" + compensationListener);
            compensationContext.addListener(compensationListener);
        }

        public boolean shouldUseCompensation() {

            CompensationContext compensationContext = this.commandContext.getCompensationContext();
            boolean result = compensationContext != null && compensationContext.isCompensationEnabled();
            log.debug("Should use compensation?: {}", result);

            return result;
        }

        @Override
        public String toString() {
            return ToStringBuilder.forInstance(this).build();
        }

        private class ReleaseMacsCompensationListener implements CompensationContext.CompensationListener {
            private final List<String> macsToReleaseOnCommit = new ArrayList<>();

            @Override
            public void afterCompensation() {
                log.debug("Compensation occurred, clearing macs to be released after commit: {}",
                        macsToReleaseOnCommit);

                macsToReleaseOnCommit.clear();
            }

            @Override
            public void cleaningCompensationDataAfterSuccess() {
                log.debug("Command successfully executed, releasing macs: {}" + macsToReleaseOnCommit);
                macPool.freeMacs(macsToReleaseOnCommit);
            }

        }
    }

    private class UsingTxDecoratorState implements TransactionalStrategyState {
        private final Logger log = LoggerFactory.getLogger(UsingTxDecoratorState.class);

        @Override
        public void releaseMacsInCaseOfRollback(List<String> macs) {
            registerRollbackHandler((TransactionRollbackListener)() -> {
                log.debug("Rollback occurred, releasing macs {}.", macs);
                macPool.freeMacs(macs);
            });
        }

        @Override
        public void releaseMacsOnCommit(List<String> macs) {
            registerRollbackHandler((TransactionSuccessListener)() -> {
                log.debug("Command succeeded, releasing macs {}.", macs);
                macPool.freeMacs(macs);
            });
        }

        private void registerRollbackHandler(TransactionCompletionListener rollbackHandler) {
            log.debug("Registering rollback handler {}", rollbackHandler);
            TransactionSupport.registerRollbackHandler(rollbackHandler);
        }

        @Override
        public String toString() {
            return ToStringBuilder.forInstance(this).build();
        }
    }

    private class NontransactionalState implements TransactionalStrategyState {
        private final Logger log = LoggerFactory.getLogger(NontransactionalState.class);

        @Override
        public void releaseMacsInCaseOfRollback(List<String> macs) {
            //there won't be any.
        }

        @Override
        public void releaseMacsOnCommit(List<String> macs) {
            log.debug("Non-tx, non-compensation state, immediately releasing macs {}.", macs);
            macPool.freeMacs(macs);
        }

        @Override
        public String toString() {
            return ToStringBuilder.forInstance(this).build();
        }
    }

}
