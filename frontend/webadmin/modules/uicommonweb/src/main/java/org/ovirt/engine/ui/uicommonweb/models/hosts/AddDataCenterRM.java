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
public class AddDataCenterRM extends BaseRM
{
	public AddDataCenterRM(HostListModel model)
	{
		super(model, new DataBag());
		Transaction.Current.EnlistVolatile(this, EnlistmentOptions.None);
		ConfigureLocalStorageModel configureLocalStorageModel = (ConfigureLocalStorageModel)getModel().getWindow();
		if (configureLocalStorageModel.getDontCreateDataCenter())
		{
			getData().setDataCenterId(configureLocalStorageModel.getDataCenter().getDataCenterId().getValue());
		}
		new AddClusterRM(getModel(), getData());
	}

	@Override
	public void Prepare(PreparingEnlistment preparingEnlistment)
	{
		ConfigureLocalStorageModel model = (ConfigureLocalStorageModel)getModel().getWindow();
		if (!model.getDontCreateDataCenter())
		{
			DataCenterModel m = model.getDataCenter();
			String name = (String)m.getName().getEntity();
			//Try to find existing data center with the specified name.
			storage_pool dataCenter = DataProvider.GetDataCenterByName(name);
			if (dataCenter != null)
			{
				getData().setDataCenterId(dataCenter.getId());
				preparingEnlistment.Prepared();
			}

			else
			{
				dataCenter = new storage_pool();
				dataCenter.setname(name);
				dataCenter.setdescription((String)m.getDescription().getEntity());
				dataCenter.setstorage_pool_type((StorageType)m.getStorageTypeList().getSelectedItem());
				dataCenter.setcompatibility_version((Version)m.getVersion().getSelectedItem());

				VdcReturnValueBase returnValue = Frontend.RunAction(VdcActionType.AddEmptyStoragePool, new StoragePoolManagementParameter(dataCenter));

				if (returnValue != null && returnValue.getSucceeded())
				{
					getData().setDataCenterId((Guid)returnValue.getActionReturnValue());
					preparingEnlistment.Prepared();
				}
				else
				{
					preparingEnlistment.ForceRollback();
				}
			}
		}
		else
		{
			preparingEnlistment.Prepared();
		}
	}


	@Override
	public void Commit(Enlistment enlistment)
	{
		enlistment.Done();
	}

	@Override
	public void Rollback(Enlistment enlistment)
	{
		if (getModel() == null || getModel().getSelectedItem() == null)
		{
			return;
		}
		VDS host = (VDS)getModel().getSelectedItem();
		//perform rollback only when the host is in maintenance
		if (host.getstatus() != VDSStatus.Maintenance)
		{
			return;
		}

		storage_pool dataCenter = DataProvider.GetDataCenterById(getData().getDataCenterId());

		//perform rollback only when the Data Center is un uninitialized
		if (dataCenter.getstatus() != StoragePoolStatus.Uninitialized)
		{
			return;
		}

		if (getData().getOldClusterId() != null && !host.getvds_group_id().getValue().equals(getData().getOldClusterId().getValue()))
		{
			//Switch host back to previous cluster.
			VdcReturnValueBase returnValue = Frontend.RunAction(VdcActionType.ChangeVDSCluster, new ChangeVDSClusterParameters(getData().getOldClusterId(), host.getvds_id()));

			if(returnValue != null && returnValue.getSucceeded())
			{
				//Remove cluster.
				if (getData().getClusterId() != null)
				{
					Frontend.RunAction(VdcActionType.RemoveVdsGroup, new VdsGroupParametersBase(getData().getClusterId()));
				}

				//Remove data center.
				if (getData().getDataCenterId() != null)
				{
					Frontend.RunAction(VdcActionType.RemoveStoragePool, new StoragePoolParametersBase(getData().getDataCenterId()));
				}
			}
		}
	}

	@Override
	public void InDoubt(Enlistment enlistment)
	{
	}
}