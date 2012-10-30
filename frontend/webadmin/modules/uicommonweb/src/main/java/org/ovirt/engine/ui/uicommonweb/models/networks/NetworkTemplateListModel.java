package org.ovirt.engine.ui.uicommonweb.models.networks;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.NetworkView;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.queries.NetworkIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class NetworkTemplateListModel extends SearchableListModel
{
    public NetworkTemplateListModel()
    {
        setTitle(ConstantsManager.getInstance().getConstants().templatesTitle());
        setHashName("templates"); //$NON-NLS-1$
    }

    @Override
    public NetworkView getEntity()
    {
        return (NetworkView) super.getEntity();
    }

    public void setEntity(NetworkView value)
    {
        super.setEntity(value);
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
    }

    @Override
    protected void SyncSearch()
    {
        if (getEntity() == null)
        {
            return;
        }

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                NetworkTemplateListModel.this.setItems((ArrayList<VmTemplate>) ((VdcQueryReturnValue) ReturnValue).getReturnValue());
            }
        };

        NetworkIdParameters networkIdParams = new NetworkIdParameters(getEntity().getNetwork().getId());
        networkIdParams.setRefresh(getIsQueryFirstTime());

        // Frontend.RunQuery(VdcQueryType.GetTemplatesByNetworkId, networkIdParams, _asyncQuery);
    }


    @Override
    protected void AsyncSearch()
    {
        super.AsyncSearch();
        SyncSearch();
    }

    @Override
    protected String getListName() {
        return "NetworkTemplateListModel"; //$NON-NLS-1$
    }
}

