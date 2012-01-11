package org.ovirt.engine.ui.uicompat;

public interface IEnlistmentNotification {

    public void prepare(PreparingEnlistment preparingEnlistment);

    public void commit(Enlistment enlistment);

    public void rollback(Enlistment enlistment);
}
