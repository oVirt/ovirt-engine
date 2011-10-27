package org.ovirt.engine.ui.uicommonweb.models.hosts;
import java.util.Collections;
import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.action.*;
import org.ovirt.engine.ui.frontend.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;
import org.ovirt.engine.core.common.*;

import org.ovirt.engine.ui.uicommonweb.models.clusters.*;
import org.ovirt.engine.ui.uicommonweb.models.common.*;
import org.ovirt.engine.ui.uicommonweb.models.configure.*;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.*;
import org.ovirt.engine.ui.uicommonweb.models.tags.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.*;
import org.ovirt.engine.ui.uicommonweb.dataprovider.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

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
		VDS host = (VDS)getModel().getSelectedItem();
		ConfigureLocalStorageModel model = (ConfigureLocalStorageModel)getModel().getWindow();

		//Activate host.
		VdcReturnValueBase returnValue = Frontend.RunAction(VdcActionType.ActivateVds, new VdsActionParameters(host.getvds_id()));

		if (returnValue == null || !returnValue.getSucceeded())
		{
			preparingEnlistment.ForceRollback();
			return;
		}

		//Wait for a host to be Up.
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
				//Wrap Thread.Sleep with try/catch to pass conversion to Java.
				try
				{
					Thread.sleep(WaitInterval);
				}
				catch (InterruptedException e)
				{
				}
			}
			else
			{
				break;
			}
		}

		//Add storage domain.
		storage_server_connections tempVar = new storage_server_connections();
		tempVar.setconnection((String)model.getStorage().getPath().getEntity());
		tempVar.setstorage_type(StorageType.LOCALFS);
		storage_server_connections connection = tempVar;

		storage_domain_static storageDomain = new storage_domain_static();
		storageDomain.setstorage_type(StorageType.LOCALFS);
		storageDomain.setstorage_domain_type(StorageDomainType.Data);
		storageDomain.setstorage_name((String)model.getFormattedStorageName().getEntity());

		returnValue = Frontend.RunAction(VdcActionType.AddStorageServerConnection, new StorageServerConnectionParametersBase(connection, host.getvds_id()));

		if (returnValue == null || !returnValue.getSucceeded())
		{
			//Don't rollback, just throw exception to indicate failure at this step.
			throw new TransactionAbortedException();
		}

		storageDomain.setstorage((String)returnValue.getActionReturnValue());

		StorageDomainManagementParameter tempVar2 = new StorageDomainManagementParameter(storageDomain);
		tempVar2.setVdsId(host.getvds_id());
		returnValue = Frontend.RunAction(VdcActionType.AddLocalStorageDomain, tempVar2);

		//Clean up connection.
		if (returnValue == null || !returnValue.getSucceeded())
		{
			Frontend.RunAction(VdcActionType.RemoveStorageServerConnection, new StorageServerConnectionParametersBase(connection, host.getvds_id()));

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