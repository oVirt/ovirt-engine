package org.ovirt.engine.ui.uicommonweb.models.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.ui.uicommonweb.models.events.SubTabEventListModel;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class VolumeEventListModel extends SubTabEventListModel
{

    @Override
    public GlusterVolumeEntity getEntity()
    {
        return (GlusterVolumeEntity) ((super.getEntity() instanceof GlusterVolumeEntity) ? super.getEntity() : null);
    }

    public void setEntity(GlusterVolumeEntity value)
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
    public void search()
    {
        if (getEntity() != null)
        {
            setSearchString("Events: volume.name=" + getEntity().getName()); //$NON-NLS-1$
            super.search();
        }
    }

    @Override
    protected void entityPropertyChanged(Object sender, PropertyChangedEventArgs e)
    {
        super.entityPropertyChanged(sender, e);

        if (e.PropertyName.equals("gluster_volume_name")) //$NON-NLS-1$
        {
            getSearchCommand().Execute();
        }
    }
}
