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

import org.ovirt.engine.ui.uicommonweb.models.vms.*;
import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

@SuppressWarnings("unused")
public class HostHooksListModel extends SearchableListModel
{

	public VDS getEntity()
	{
		return (VDS)super.getEntity();
	}
	public void setEntity(VDS value)
	{
		super.setEntity(value);
	}


	public HostHooksListModel()
	{
		setTitle("Host Hooks");
	}

	@Override
	protected void OnEntityChanged()
	{
		super.OnEntityChanged();

		getSearchCommand().Execute();
	}

	@Override
	protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
	{
		super.EntityPropertyChanged(sender, e);

		if (e.PropertyName.equals("status"))
		{
			getSearchCommand().Execute();
		}
	}

	@Override
	public void Search()
	{
		if (getEntity() != null)
		{
			super.Search();
		}
	}

	@Override
	protected void SyncSearch()
	{
		if (!getEntity().getContainingHooks())
		{
			setIsEmpty(true);
			setItems(new java.util.ArrayList<java.util.HashMap<String, String>>());
			return;
		}

		super.SyncSearch();

		setIsEmpty(false);
		AsyncQuery _asyncQuery = new AsyncQuery();
		_asyncQuery.setModel(this);
		_asyncQuery.asyncCallback = new INewAsyncCallback() { public void OnSuccess(Object model, Object ReturnValue)
		{
			HostHooksListModel hooklistModel = (HostHooksListModel)model;
			java.util.ArrayList<java.util.HashMap<String, String>> list = new java.util.ArrayList<java.util.HashMap<String, String>>();
			java.util.HashMap<String, java.util.HashMap<String, java.util.HashMap<String, String>>> dictionary = (java.util.HashMap<String, java.util.HashMap<String, java.util.HashMap<String, String>>>)((VdcQueryReturnValue)ReturnValue).getReturnValue();
				java.util.HashMap<String, String> row;
				for (java.util.Map.Entry<String, java.util.HashMap<String, java.util.HashMap<String, String>>> keyValuePair : dictionary.entrySet())
				{
					for (java.util.Map.Entry<String, java.util.HashMap<String, String>> keyValuePair1 : keyValuePair.getValue().entrySet())
					{
						for (java.util.Map.Entry<String, String> keyValuePair2 : keyValuePair1.getValue().entrySet())
						{
							row = new java.util.HashMap<String, String>();
							row.put("EventName", keyValuePair.getKey());
							row.put("ScriptName", keyValuePair1.getKey());
							row.put("PropertyName", keyValuePair2.getKey());
							row.put("PropertyValue", keyValuePair2.getValue());
							list.add(row);
						}
					}
				}
				hooklistModel.setItems(list);
		}};
		GetVdsHooksByIdParameters tempVar = new GetVdsHooksByIdParameters();
		tempVar.setVdsId(getEntity().getvds_id());
		tempVar.setRefresh(getIsQueryFirstTime());
		Frontend.RunQuery(VdcQueryType.GetVdsHooksById2, tempVar, _asyncQuery);


	}

	@Override
	protected void AsyncSearch()
	{
		super.AsyncSearch();
		SyncSearch();
	}
}