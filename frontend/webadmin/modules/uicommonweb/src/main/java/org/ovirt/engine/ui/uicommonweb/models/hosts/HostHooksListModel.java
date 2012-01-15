package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.GetVdsHooksByIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

@SuppressWarnings("unused")
public class HostHooksListModel extends SearchableListModel
{

    @Override
    public VDS getEntity()
    {
        return (VDS) super.getEntity();
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
        if (getEntity() == null)
        {
            return;
        }

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
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                HostHooksListModel hooklistModel = (HostHooksListModel) model;
                java.util.ArrayList<java.util.HashMap<String, String>> list =
                        new java.util.ArrayList<java.util.HashMap<String, String>>();
                java.util.HashMap<String, java.util.HashMap<String, java.util.HashMap<String, String>>> dictionary =
                        (java.util.HashMap<String, java.util.HashMap<String, java.util.HashMap<String, String>>>) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                java.util.HashMap<String, String> row;
                for (java.util.Map.Entry<String, java.util.HashMap<String, java.util.HashMap<String, String>>> keyValuePair : dictionary.entrySet())
                {
                    for (java.util.Map.Entry<String, java.util.HashMap<String, String>> keyValuePair1 : keyValuePair.getValue()
                            .entrySet())
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
            }
        };
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

    @Override
    protected String getListName() {
        return "HostHooksListModel";
    }
}
