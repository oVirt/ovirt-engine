package org.ovirt.engine.ui.uicommonweb.models.vms;
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

import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

@SuppressWarnings("unused")
public class VmAppListModel extends SearchableListModel
{

	public Iterable getItems()
	{
		return items;
	}
	public void setItems(Iterable value)
	{
		if (items != value)
		{
			ItemsChanging(value, items);
			items = value;
			ItemsChanged();
			getItemsChangedEvent().raise(this, EventArgs.Empty);
			OnPropertyChanged(new PropertyChangedEventArgs("Items"));
		}
	}



	public VmAppListModel()
	{
		setTitle("Applications");
	}

	@Override
	protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
	{
		super.EntityPropertyChanged(sender, e);
		if (e.PropertyName.equals("app_list"))
		{
			UpdateAppList();
		}
	}

	@Override
	protected void OnEntityChanged()
	{
		super.OnEntityChanged();

		//Deal with pool as Entity without failing.
		if (!(getEntity() instanceof vm_pools))
		{
			UpdateAppList();
		}
	}

	private void UpdateAppList()
	{
		VM vm = (VM)getEntity();

		//Items = (Entity != null && Entity.app_list != null)
		//    ? Entity.app_list.Split(',').OrderBy(a => a)
		//    : null;

		setItems(null);
		if (vm != null && vm.getapp_list() != null)
		{
			java.util.ArrayList<String> list = new java.util.ArrayList<String>();

			String[] array = vm.getapp_list().split("[,]", -1);
			for (String item : array)
			{
				list.add(item);
			}
			Collections.sort(list);

			setItems(list);
		}
	}

	@Override
	protected void SyncSearch()
	{
		UpdateAppList();
		setIsQueryFirstTime(false);
	}
}