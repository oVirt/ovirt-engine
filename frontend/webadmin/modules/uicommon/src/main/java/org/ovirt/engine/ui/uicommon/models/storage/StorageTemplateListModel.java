package org.ovirt.engine.ui.uicommon.models.storage;
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

import org.ovirt.engine.core.common.interfaces.*;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class StorageTemplateListModel extends SearchableListModel
{

	public storage_domains getEntity()
	{
		return (storage_domains)super.getEntity();
	}
	public void setEntity(storage_domains value)
	{
		super.setEntity(value);
	}


	public StorageTemplateListModel()
	{
		setTitle("Templates");
	}

	@Override
	protected void OnEntityChanged()
	{
		super.OnEntityChanged();

		getSearchCommand().Execute();
	}

	@Override
	public void Search()
	{
		if (getEntity() != null)
		{
			super.Search();
		}
		else
		{
			setItems(null);
		}
	}

	@Override
	protected void SyncSearch()
	{
		super.SyncSearch();

		VdcQueryReturnValue returnValue = Frontend.RunQuery(VdcQueryType.GetVmTemplatesFromStorageDomain, new StorageDomainQueryParametersBase(getEntity().getid()));

		if (returnValue != null && returnValue.getSucceeded())
		{
			setItems((java.util.ArrayList<VmTemplate>)returnValue.getReturnValue());
		}
		else
		{
			setItems(new java.util.ArrayList<VmTemplate>());
		}

		setIsEmpty(((java.util.List)getItems()).size() == 0);
	}

	@Override
	protected void AsyncSearch()
	{
		super.AsyncSearch();
		SyncSearch();
	}
}