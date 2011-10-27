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
public class ChangeHostClusterRM extends BaseRM
{
	public ChangeHostClusterRM(HostListModel model, DataBag data)
	{
		super(model, data);
		Transaction.Current.EnlistVolatile(this, EnlistmentOptions.None);
		ConfigureLocalStorageModel configureLocalStorageModel = (ConfigureLocalStorageModel)getModel().getWindow();
		//Remember an old cluster, to enable rollback.
		VDS host = (VDS)getModel().getSelectedItem();
		getData().setOldClusterId(host.getvds_group_id());
		new AddStorageDomainRM(getModel(), getData());
	}

	@Override
	public void Prepare(PreparingEnlistment preparingEnlistment)
	{
		ConfigureLocalStorageModel model = (ConfigureLocalStorageModel)getModel().getWindow();
		if (!model.getDontChangeHostCluster())
		{
			VDS host = (VDS)getModel().getSelectedItem();
			VdcReturnValueBase returnValue = Frontend.RunAction(VdcActionType.ChangeVDSCluster, new ChangeVDSClusterParameters(getData().getClusterId(), host.getvds_id()));

			if (returnValue != null && returnValue.getSucceeded())
			{
				preparingEnlistment.Prepared();
			}
			else
			{
				preparingEnlistment.ForceRollback();
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