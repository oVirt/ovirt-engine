package org.ovirt.engine.core.bll.context;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.function.Consumer;

public abstract class CompensationContextBase implements CompensationContext {

    private LinkedHashSet<CompensationListener> listeners = new LinkedHashSet<>();

    @Override
    public void addListener(CompensationListener compensationListener) {
        this.listeners.add(compensationListener);
    }

    @Override
    public final void afterCompensationCleanup() {
        processListenersInReverseOrder(CompensationListener::afterCompensation);
        doAfterCompensationCleanup();
    }

    @Override
    public final void cleanupCompensationDataAfterSuccessfulCommand() {
        processListenersInReverseOrder(CompensationListener::cleaningCompensationDataAfterSuccess);
        doCleanupCompensationDataAfterSuccessfulCommand();
    }

    private void processListenersInReverseOrder(Consumer<CompensationListener> afterCompensation) {
        new LinkedList<>(listeners).descendingIterator().forEachRemaining(afterCompensation);
    }

    protected void doAfterCompensationCleanup() {
    }

    protected void doCleanupCompensationDataAfterSuccessfulCommand() {

    }

    protected void doClearCollectedCompensationData() {
    }

}
