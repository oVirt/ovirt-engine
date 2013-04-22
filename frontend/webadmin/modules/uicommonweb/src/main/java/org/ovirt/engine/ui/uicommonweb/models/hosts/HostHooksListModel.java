package org.ovirt.engine.ui.uicommonweb.models.hosts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.GetVdsHooksByIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

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
        setTitle(ConstantsManager.getInstance().getConstants().hostHooksTitle());
        setHashName("host_hooks"); // $//$NON-NLS-1$
        setAvailableInModes(ApplicationMode.VirtOnly);
    }

    @Override
    protected void onEntityChanged()
    {
        super.onEntityChanged();

        getSearchCommand().Execute();
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.entityPropertyChanged(sender, e);

        if (e.PropertyName.equals("status")) //$NON-NLS-1$
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
            setItems(new ArrayList<HashMap<String, String>>());
            return;
        }

        super.SyncSearch();

        setIsEmpty(false);
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue)
            {
                HostHooksListModel hooklistModel = (HostHooksListModel) model;
                ArrayList<HashMap<String, String>> list =
                        new ArrayList<HashMap<String, String>>();
                HashMap<String, HashMap<String, HashMap<String, String>>> dictionary =
                        (HashMap<String, HashMap<String, HashMap<String, String>>>) ((VdcQueryReturnValue) ReturnValue).getReturnValue();
                HashMap<String, String> row;
                for (Map.Entry<String, HashMap<String, HashMap<String, String>>> keyValuePair : dictionary.entrySet())
                {
                    for (Map.Entry<String, HashMap<String, String>> keyValuePair1 : keyValuePair.getValue()
                            .entrySet())
                    {
                        for (Map.Entry<String, String> keyValuePair2 : keyValuePair1.getValue().entrySet())
                        {
                            row = new HashMap<String, String>();
                            row.put("EventName", keyValuePair.getKey()); //$NON-NLS-1$
                            row.put("ScriptName", keyValuePair1.getKey()); //$NON-NLS-1$
                            row.put("PropertyName", keyValuePair2.getKey()); //$NON-NLS-1$
                            row.put("PropertyValue", keyValuePair2.getValue()); //$NON-NLS-1$
                            list.add(row);
                        }
                    }
                }
                hooklistModel.setItems(list);
            }
        };
        GetVdsHooksByIdParameters tempVar = new GetVdsHooksByIdParameters();
        tempVar.setVdsId(getEntity().getId());
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
        return "HostHooksListModel"; //$NON-NLS-1$
    }
}
