package org.ovirt.engine.ui.uicommonweb.models.storage;
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

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

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
		if (getEntity() == null)
		{
			return;
		}

		super.SyncSearch();

		AsyncQuery _asyncQuery = new AsyncQuery();
		_asyncQuery.setModel(this);
		_asyncQuery.asyncCallback = new INewAsyncCallback() { public void OnSuccess(Object model, Object ReturnValue)
			{
				StorageTemplateListModel templateModel = (StorageTemplateListModel)model;
				templateModel.setItems((java.util.ArrayList<VmTemplate>)((VdcQueryReturnValue)ReturnValue).getReturnValue());
				templateModel.setIsEmpty(((java.util.List)templateModel.getItems()).size() == 0);
			}};

		StorageDomainQueryParametersBase tempVar = new StorageDomainQueryParametersBase(getEntity().getid());
		tempVar.setRefresh(getIsQueryFirstTime());
		Frontend.RunQuery(VdcQueryType.GetVmTemplatesFromStorageDomain, tempVar, _asyncQuery);
	}
	@Override
	protected void AsyncSearch()
	{
		super.AsyncSearch();
		SyncSearch();
	}
}