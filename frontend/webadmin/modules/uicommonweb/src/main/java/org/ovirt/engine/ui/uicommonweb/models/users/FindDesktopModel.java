package org.ovirt.engine.ui.uicommonweb.models.users;
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

import org.ovirt.engine.core.common.queries.*;

import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

@SuppressWarnings("unused")
public class FindDesktopModel extends SearchableListModel
{

	private Iterable privateExcludeItems;
	public Iterable getExcludeItems()
	{
		return privateExcludeItems;
	}
	public void setExcludeItems(Iterable value)
	{
		privateExcludeItems = value;
	}


	public FindDesktopModel()
	{
		setIsTimerDisabled(true);
	}

	@Override
	protected void SyncSearch()
	{
		//			List<VM> exclude = ExcludeItems != null ? Linq.Cast<VM>(ExcludeItems) : new List<VM>();

		java.util.HashSet<Guid> exludeGuids = new java.util.HashSet<Guid>();
		if (getExcludeItems() != null)
		{
			for (Object item : getExcludeItems())
			{
				VM vm = (VM)item;
				exludeGuids.add(vm.getvm_guid());
			}
		}


		VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.Search, new SearchParameters(StringFormat.format("Vms: pool=null type=desktop %1$s", getSearchString()), SearchType.VM));
		if (returnValue != null && returnValue.getSucceeded())
		{
			//                List<EntityModel> items = ((List<IVdcQueryable>)returnValue.ReturnValue)
			//					.Cast<VM>()
			//					.Where(a => !exclude.Any(b => b.vm_guid == a.vm_guid))
			//					.Select(a => new EntityModel() { Entity = a })
			//					.ToList();
			java.util.ArrayList<EntityModel> items = new java.util.ArrayList<EntityModel>();
			for (IVdcQueryable item : (java.util.ArrayList<IVdcQueryable>)returnValue.getReturnValue())
			{
				VM vm = (VM)item;
				if (!exludeGuids.contains(vm.getvm_guid()))
				{
					EntityModel tempVar = new EntityModel();
					tempVar.setEntity(vm);
					items.add(tempVar);
				}
			}

			setItems(items);
		}
		else
		{
			setItems(null);
		}
	}

	@Override
	protected void AsyncSearch()
	{
		super.AsyncSearch();
		SyncSearch();
	}
    @Override
    protected String getListName() {
        return "FindDesktopModel";
    }
}