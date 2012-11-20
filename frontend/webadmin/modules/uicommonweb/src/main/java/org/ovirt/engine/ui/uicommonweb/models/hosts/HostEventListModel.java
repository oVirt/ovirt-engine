package org.ovirt.engine.ui.uicommonweb.models.hosts;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.models.events.SubTabEventListModel;

public class HostEventListModel extends SubTabEventListModel
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

    @Override
    protected void onEntityContentChanged()
    {
        super.onEntityContentChanged();

        if (getEntity() != null)
        {
            getSearchCommand().Execute();
        }
        else
        {
            setItems(null);
        }
    }

    @Override
    public void Search()
    {
        if (getEntity() != null)
        {
            setSearchString("events: host.name=" + getEntity().getvds_name()); //$NON-NLS-1$
            super.Search();
        }
    }

    @Override
    protected void EntityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.EntityPropertyChanged(sender, e);

        if (e.PropertyName.equals("vds_name")) //$NON-NLS-1$
        {
            getSearchCommand().Execute();
        }
    }
}
