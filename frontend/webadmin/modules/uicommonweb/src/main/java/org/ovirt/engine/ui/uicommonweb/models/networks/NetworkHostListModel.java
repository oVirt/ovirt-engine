package org.ovirt.engine.ui.uicommonweb.models.networks;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.NetworkView;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.queries.NetworkIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;

@SuppressWarnings("unused")
public class NetworkHostListModel extends HostListModel
{
    private NetworkHostFilter viewFilterType;

    public NetworkHostFilter getViewFilterType() {
        return viewFilterType;
    }

    public void setViewFilterType(NetworkHostFilter viewFilterType) {
        this.viewFilterType = viewFilterType;
        Search();
    }

    @Override
    public NetworkView getEntity()
    {
        return (NetworkView) ((super.getEntity() instanceof NetworkView) ? super.getEntity() : null);
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
        _asyncQuery.setModel(getViewFilterType());
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                if (model.equals(getViewFilterType())){
                    NetworkHostListModel.this.setItems((ArrayList<VDS>) ((VdcQueryReturnValue) ReturnValue).getReturnValue());
                }
            }
        };

        NetworkIdParameters networkIdParams = new NetworkIdParameters(getEntity().getNetwork().getId());
        networkIdParams.setRefresh(getIsQueryFirstTime());

        if (NetworkHostFilter.unattached.equals(getViewFilterType())){
            Frontend.RunQuery(VdcQueryType.GetVdsWithoutNetwork, networkIdParams, _asyncQuery);
        }else{
         //   Frontend.RunQuery(VdcQueryType.GetVdsByNetworkId, networkIdParams, _asyncQuery);
        }
    }

    @Override
    protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.EntityPropertyChanged(sender, e);

        if (e.PropertyName.equals("name")) //$NON-NLS-1$
        {
            getSearchCommand().Execute();
        }
    }
}
