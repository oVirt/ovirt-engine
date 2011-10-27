package org.ovirt.engine.ui.uicommon.models.vms;
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

import org.ovirt.engine.ui.uicommon.models.events.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class VmEventListModel extends EventListModel
{
	@Override
	protected void OnEntityChanged()
	{
		super.OnEntityChanged();

		//Deal with pool as Entity without failing.
		if (getEntity() != null && !(getEntity() instanceof vm_pools))
		{
			getSearchCommand().Execute();
		}
		else
		{
			setItems(null);
		}
	}

	@Override
	public void Search()
	{
		VM vm = (VM)getEntity();

		if (getEntity() != null)
		{
			setSearchString("events: vm.name=" + vm.getvm_name());
			super.Search();
		}
	}

	@Override
	protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
	{
		super.EntityPropertyChanged(sender, e);

		if (e.PropertyName.equals("vm_name"))
		{
			getSearchCommand().Execute();
		}
	}
}