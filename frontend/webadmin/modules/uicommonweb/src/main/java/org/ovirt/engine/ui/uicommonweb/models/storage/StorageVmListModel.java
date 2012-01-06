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
public class StorageVmListModel extends SearchableListModel
{

	public storage_domains getEntity()
	{
		return (storage_domains)super.getEntity();
	}
	public void setEntity(storage_domains value)
	{
		super.setEntity(value);
	}


	public StorageVmListModel()
	{
		setTitle("Virtual Machines");

		setIsTimerDisabled(true);
	}

	@Override
	protected void OnEntityChanged()
	{
		super.OnEntityChanged();

		getSearchCommand().Execute();
	}

	@Override
	public void setEntity(Object value)
	{
		if (value == null || !value.equals(getEntity())) {
			super.setEntity(value);
		}
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
				StorageVmListModel vmModel = (StorageVmListModel)model;
				vmModel.setItems((java.util.ArrayList<VM>)((VdcQueryReturnValue)ReturnValue).getReturnValue());
				vmModel.setIsEmpty(((java.util.List)vmModel.getItems()).size() == 0);
			}};

		StorageDomainQueryTopSizeVmsParameters tempVar = new StorageDomainQueryTopSizeVmsParameters(getEntity().getid(), 0);
		tempVar.setRefresh(getIsQueryFirstTime());
		Frontend.RunQuery(VdcQueryType.GetTopSizeVmsFromStorageDomain, tempVar, _asyncQuery);
	}

	@Override
	protected void AsyncSearch()
	{
		super.AsyncSearch();
		SyncSearch();
	}
    @Override
    protected String getListName() {
        return "StorageVmListModel";
    }
}