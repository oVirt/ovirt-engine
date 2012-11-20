package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;

@SuppressWarnings("unused")
public class DataCenterClusterListModel extends ClusterListModel
{

    @Override
    public storage_pool getEntity()
    {
        return (storage_pool) ((super.getEntity() instanceof storage_pool) ? super.getEntity() : null);
    }

    public void setEntity(storage_pool value)
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
            setSearchString("clusters: datacenter.name=" + getEntity().getname()); //$NON-NLS-1$
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
