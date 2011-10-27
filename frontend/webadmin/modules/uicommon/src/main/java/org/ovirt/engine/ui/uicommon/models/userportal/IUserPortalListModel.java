package org.ovirt.engine.ui.uicommon.models.userportal;
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

import org.ovirt.engine.ui.uicommon.models.*;
import org.ovirt.engine.ui.uicommon.models.userportal.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.ui.uicommon.*;

@SuppressWarnings("unused")
public abstract class IUserPortalListModel extends ListWithDetailsModel
{

	private boolean canConnectAutomatically;
	public boolean getCanConnectAutomatically()
	{
		return canConnectAutomatically;
	}
	public void setCanConnectAutomatically(boolean value)
	{
		if (canConnectAutomatically != value)
		{
			canConnectAutomatically = value;
			OnPropertyChanged(new PropertyChangedEventArgs("CanConnectAutomatically"));
		}
	}


	public abstract void OnVmAndPoolLoad();

	protected java.util.HashMap<Guid, vm_pools> poolMap;

	public vm_pools ResolveVmPoolById(Guid id)
	{
		return poolMap.get(id);
	}

	// Return a list of VMs with status 'UP'
	public java.util.ArrayList<UserPortalItemModel> GetStatusUpVms(Iterable items)
	{
		return GetUpVms(items, true);
	}

	// Return a list of up VMs
	public java.util.ArrayList<UserPortalItemModel> GetUpVms(Iterable items)
	{
		return GetUpVms(items, false);
	}

	private java.util.ArrayList<UserPortalItemModel> GetUpVms(Iterable items, boolean onlyVmStatusUp)
	{
		java.util.ArrayList<UserPortalItemModel> upVms = new java.util.ArrayList<UserPortalItemModel>();
		if (items != null)
		{
			for (Object item : items)
			{
				UserPortalItemModel userPortalItemModel = (UserPortalItemModel)item;
				Object tempVar = userPortalItemModel.getEntity();
				VM vm = (VM)((tempVar instanceof VM) ? tempVar : null);
				if (vm == null)
				{
					continue;
				}
				if ((onlyVmStatusUp && vm.getstatus() == VMStatus.Up) || (!onlyVmStatusUp && userPortalItemModel.getDefaultConsole().IsVmUp()))
				{
					upVms.add(userPortalItemModel);
				}
			}
		}
		return upVms;
	}
}