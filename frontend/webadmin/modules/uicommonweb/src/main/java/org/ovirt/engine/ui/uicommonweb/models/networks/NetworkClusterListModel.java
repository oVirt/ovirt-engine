package org.ovirt.engine.ui.uicommonweb.models.networks;

import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;

@SuppressWarnings("unused")
public class NetworkClusterListModel extends ClusterListModel
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
            setSearchString(StringFormat.format("clusters: network.name=%1$s", getEntity().getname())); //$NON-NLS-1$
            super.Search();
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

