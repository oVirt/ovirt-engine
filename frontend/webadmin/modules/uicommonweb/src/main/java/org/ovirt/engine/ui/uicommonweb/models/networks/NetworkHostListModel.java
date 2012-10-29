package org.ovirt.engine.ui.uicommonweb.models.networks;

import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;

@SuppressWarnings("unused")
public class NetworkHostListModel extends HostListModel
{

    @Override
    public Network getEntity()
    {
        return (Network) ((super.getEntity() instanceof Network) ? super.getEntity() : null);
    }

    public void setEntity(Network value)
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
            setSearchString(StringFormat.format("hosts: network=%1$s", getEntity().getname())); //$NON-NLS-1$
            super.Search();
        }
    }

    @Override
    protected void SyncSearch()
    {
        SearchParameters tempVar = new SearchParameters(getSearchString(), SearchType.VDS);
        tempVar.setRefresh(getIsQueryFirstTime());
        super.SyncSearch(VdcQueryType.Search, tempVar);
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
