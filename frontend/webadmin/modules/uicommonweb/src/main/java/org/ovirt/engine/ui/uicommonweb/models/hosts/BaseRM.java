package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.ui.uicompat.Enlistment;
import org.ovirt.engine.ui.uicompat.IEnlistmentNotification;
import org.ovirt.engine.ui.uicompat.PreparingEnlistment;
import org.ovirt.engine.ui.uicompat.TransactionAbortedException;

@SuppressWarnings("unused")
public abstract class BaseRM implements IEnlistmentNotification
{
    private HostListModel privateModel;

    protected HostListModel getModel()
    {
        return privateModel;
    }

    private void setModel(HostListModel value)
    {
        privateModel = value;
    }

    private DataBag privateData;

    protected DataBag getData()
    {
        return privateData;
    }

    private void setData(DataBag value)
    {
        privateData = value;
    }

    protected BaseRM(HostListModel model, DataBag data)
    {
        setModel(model);
        setData(data);
    }

    public abstract void Prepare(PreparingEnlistment preparingEnlistment) throws TransactionAbortedException;

    public abstract void Commit(Enlistment enlistment);

    public abstract void Rollback(Enlistment enlistment);

    public abstract void InDoubt(Enlistment enlistment);
}
