package org.ovirt.engine.ui.uicompat;

public abstract class IEnlistmentNotification {

    private final String correlationId;

    public IEnlistmentNotification(String correlationId){
        this.correlationId = correlationId;
    }

    public abstract void prepare(PreparingEnlistment preparingEnlistment);

    public abstract void commit(Enlistment enlistment);

    public abstract void rollback(Enlistment enlistment);

    public String getCorrelationId() {
        return correlationId;
    }
}
