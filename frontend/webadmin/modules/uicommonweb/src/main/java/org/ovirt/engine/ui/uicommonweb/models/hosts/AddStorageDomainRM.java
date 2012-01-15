package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.action.StorageServerConnectionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VdsActionParameters;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.storage_domain_static;
import org.ovirt.engine.core.common.businessentities.storage_server_connections;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicompat.Enlistment;
import org.ovirt.engine.ui.uicompat.EnlistmentOptions;
import org.ovirt.engine.ui.uicompat.PreparingEnlistment;
import org.ovirt.engine.ui.uicompat.Transaction;
import org.ovirt.engine.ui.uicompat.TransactionAbortedException;

@SuppressWarnings("unused")
public class AddStorageDomainRM extends BaseRM
{
    private static final int WaitInterval = 5000;
    private static final int WaitTries = 6;

    public AddStorageDomainRM(HostListModel model, DataBag data)
    {
        super(model, data);
        Transaction.Current.EnlistVolatile(this, EnlistmentOptions.None);
    }

    @Override
    public void Prepare(PreparingEnlistment preparingEnlistment) throws TransactionAbortedException
    {
        VDS host = (VDS) getModel().getSelectedItem();
        ConfigureLocalStorageModel model = (ConfigureLocalStorageModel) getModel().getWindow();

        // Activate host.
        VdcReturnValueBase returnValue =
                Frontend.RunAction(VdcActionType.ActivateVds, new VdsActionParameters(host.getvds_id()));

        if (returnValue == null || !returnValue.getSucceeded())
        {
            preparingEnlistment.ForceRollback();
            return;
        }

        // Wait for a host to be Up.
        for (int i = 0; i <= WaitTries; i++)
        {
            if (i == WaitTries)
            {
                preparingEnlistment.ForceRollback();
                return;
            }

            VDS tmpHost = DataProvider.GetHostById(host.getvds_id());
            if (tmpHost.getstatus() != VDSStatus.Up)
            {
                // Wrap Thread.Sleep with try/catch to pass conversion to Java.
                try
                {
                    Thread.sleep(WaitInterval);
                } catch (InterruptedException e)
                {
                }
            }
            else
            {
                break;
            }
        }

        // Add storage domain.
        storage_server_connections tempVar = new storage_server_connections();
        tempVar.setconnection((String) model.getStorage().getPath().getEntity());
        tempVar.setstorage_type(StorageType.LOCALFS);
        storage_server_connections connection = tempVar;

        storage_domain_static storageDomain = new storage_domain_static();
        storageDomain.setstorage_type(StorageType.LOCALFS);
        storageDomain.setstorage_domain_type(StorageDomainType.Data);
        storageDomain.setstorage_name((String) model.getFormattedStorageName().getEntity());

        returnValue =
                Frontend.RunAction(VdcActionType.AddStorageServerConnection,
                        new StorageServerConnectionParametersBase(connection, host.getvds_id()));

        if (returnValue == null || !returnValue.getSucceeded())
        {
            // Don't rollback, just throw exception to indicate failure at this step.
            throw new TransactionAbortedException();
        }

        storageDomain.setstorage((String) returnValue.getActionReturnValue());

        StorageDomainManagementParameter tempVar2 = new StorageDomainManagementParameter(storageDomain);
        tempVar2.setVdsId(host.getvds_id());
        returnValue = Frontend.RunAction(VdcActionType.AddLocalStorageDomain, tempVar2);

        // Clean up connection.
        if (returnValue == null || !returnValue.getSucceeded())
        {
            Frontend.RunAction(VdcActionType.RemoveStorageServerConnection,
                    new StorageServerConnectionParametersBase(connection, host.getvds_id()));

            throw new TransactionAbortedException();
        }

        preparingEnlistment.Prepared();
    }

    @Override
    public void Commit(Enlistment enlistment)
    {
        enlistment.Done();
    }

    @Override
    public void Rollback(Enlistment enlistment)
    {
    }

    @Override
    public void InDoubt(Enlistment enlistment)
    {
    }
}
