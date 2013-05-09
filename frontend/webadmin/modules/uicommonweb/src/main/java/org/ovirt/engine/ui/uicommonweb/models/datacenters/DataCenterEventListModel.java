package org.ovirt.engine.ui.uicommonweb.models.datacenters;

import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.ui.uicommonweb.models.events.SubTabEventListModel;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class DataCenterEventListModel extends SubTabEventListModel
{

    @Override
    public StoragePool getEntity()
    {
        return (StoragePool) ((super.getEntity() instanceof StoragePool) ? super.getEntity() : null);
    }

    public void setEntity(StoragePool value)
    {
        super.setEntity(value);
    }

    @Override
    protected void onEntityContentChanged()
    {
        super.onEntityContentChanged();

        if (getEntity() != null)
        {
            getSearchCommand().execute();
        }
        else
        {
            setItems(null);
        }
    }

    @Override
    public void search()
    {
        if (getEntity() != null)
        {
            setSearchString("Events: event_datacenter=" + getEntity().getname()); //$NON-NLS-1$
            super.search();
        }
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.entityPropertyChanged(sender, e);

        if (e.PropertyName.equals("name")) //$NON-NLS-1$
        {
            getSearchCommand().execute();
        }
    }
}
