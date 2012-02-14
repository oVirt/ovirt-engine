package org.ovirt.engine.ui.uicommon.models.hosts;
import java.util.Collections;
import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.action.*;
import org.ovirt.engine.ui.frontend.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;
import org.ovirt.engine.core.common.*;

import org.ovirt.engine.ui.uicommon.models.clusters.*;
import org.ovirt.engine.ui.uicommon.models.common.*;
import org.ovirt.engine.ui.uicommon.models.configure.*;
import org.ovirt.engine.ui.uicommon.models.datacenters.*;
import org.ovirt.engine.ui.uicommon.models.tags.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class AddClusterRM extends BaseRM
{
	public AddClusterRM(HostListModel model, DataBag data)
	{
		super(model, data);
		Transaction.Current.EnlistVolatile(this, EnlistmentOptions.None);
		ConfigureLocalStorageModel configureLocalStorageModel = (ConfigureLocalStorageModel)getModel().getWindow();
		if (configureLocalStorageModel.getDontCreateCluster())
		{
			getData().setClusterId(configureLocalStorageModel.getCluster().getClusterId().getValue());
		}
		new ChangeHostClusterRM(getModel(), getData());
	}

	@Override
	public void Prepare(PreparingEnlistment preparingEnlistment)
	{
		ConfigureLocalStorageModel model = (ConfigureLocalStorageModel)getModel().getWindow();
		if (!model.getDontCreateCluster())
		{
			ClusterModel m = model.getCluster();

			String name = (String)m.getName().getEntity();

			//Try to find existing cluster with the specified name.
			VDSGroup cluster = DataProvider.GetClusterByName(name);
			if (cluster != null)
			{
				getData().setClusterId(cluster.getId());
				preparingEnlistment.Prepared();
			}
			else
			{
				Version version = (Version)m.getVersion().getSelectedItem();

				cluster = new VDSGroup();
				cluster.setname(name);
				cluster.setdescription((String)m.getDescription().getEntity());
				cluster.setstorage_pool_id(getData().getDataCenterId());
				cluster.setcpu_name(((ServerCpu)m.getCPU().getSelectedItem()).getCpuName());
				cluster.setmax_vds_memory_over_commit(m.getMemoryOverCommit());
				cluster.setTransparentHugepages(version.compareTo(new Version("3.0")) >= 0);
				cluster.setcompatibility_version(version);
				cluster.setMigrateOnError(m.getMigrateOnErrorOption());

				VdcReturnValueBase returnValue = Frontend.RunAction(VdcActionType.AddVdsGroup, new VdsGroupOperationParameters(cluster));

				if (returnValue != null && returnValue.getSucceeded())
				{
					getData().setClusterId((Guid)returnValue.getActionReturnValue());
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
	}

	@Override
	public void InDoubt(Enlistment enlistment)
	{
	}
}