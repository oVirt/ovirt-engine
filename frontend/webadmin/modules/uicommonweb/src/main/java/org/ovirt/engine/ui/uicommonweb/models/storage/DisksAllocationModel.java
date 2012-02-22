package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.List;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;

public class DisksAllocationModel extends EntityModel
{
    private EntityModel isSingleStorageDomain;

    public EntityModel getIsSingleStorageDomain()
    {
        return isSingleStorageDomain;
    }

    public void setIsSingleStorageDomain(EntityModel value)
    {
        isSingleStorageDomain = value;
    }

    private ListModel privateStorageDomain;

    public ListModel getStorageDomain()
    {
        return privateStorageDomain;
    }

    public void setStorageDomain(ListModel value)
    {
        privateStorageDomain = value;
    }

    private List<DiskModel> disks;

    public List<DiskModel> getDisks()
    {
        return disks;
    }

    public void setDisks(List<DiskModel> value)
    {
        if (disks != value)
        {
            disks = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Disks"));
        }
    }

    public DisksAllocationModel()
    {
        setIsSingleStorageDomain(new EntityModel());
        getIsSingleStorageDomain().setEntity(true);
        getIsSingleStorageDomain().getEntityChangedEvent().addListener(this);

        setStorageDomain(new ListModel());
        getStorageDomain().setIsChangable(true);
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.equals(EntityModel.EntityChangedEventDefinition) && sender == getIsSingleStorageDomain())
        {
            UpdateStorageDomainsAvailability();
        }
    }

    @Override
    protected void OnPropertyChanged(PropertyChangedEventArgs e)
    {
        super.OnPropertyChanged(e);

        if (e.PropertyName.equals("Disks"))
        {
            UpdateStorageDomainsAvailability();
        }
    }

    private void UpdateStorageDomainsAvailability()
    {
        boolean IsSingleStorageDomain = (Boolean) getIsSingleStorageDomain().getEntity();
        getStorageDomain().setIsChangable(IsSingleStorageDomain);

        for (DiskModel diskModel : disks) {
            diskModel.getStorageDomain().setIsChangable(!IsSingleStorageDomain);
        }
    }
}
