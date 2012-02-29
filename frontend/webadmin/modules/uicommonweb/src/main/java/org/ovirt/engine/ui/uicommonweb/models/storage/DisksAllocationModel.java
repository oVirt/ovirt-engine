package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.Guid;
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

    private HashMap<Guid, Guid> imageToDestinationDomainMap;

    public HashMap<Guid, Guid> getImageToDestinationDomainMap() {
        updateImageToDestinationDomainMap();
        return imageToDestinationDomainMap;
    }

    public void setImageToDestinationDomainMap(HashMap<Guid, Guid> imageToDestinationDomainMap) {
        this.imageToDestinationDomainMap = imageToDestinationDomainMap;
    }

    private boolean isVolumeFormatAvailable;
    private boolean isSourceStorageDomainAvailable;

    public DisksAllocationModel()
    {
        setIsSingleStorageDomain(new EntityModel());
        getIsSingleStorageDomain().setEntity(true);
        getIsSingleStorageDomain().getEntityChangedEvent().addListener(this);

        setStorageDomain(new ListModel());
        getStorageDomain().setIsChangable(true);
        getStorageDomain().getItemsChangedEvent().addListener(this);

        setImageToDestinationDomainMap(new HashMap<Guid, Guid>());
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.equals(EntityModel.EntityChangedEventDefinition) && sender == getIsSingleStorageDomain())
        {
            UpdateStorageDomainsAvailability();
        }
        else if (ev.equals(ListModel.ItemsChangedEventDefinition) && sender == getStorageDomain())
        {
            UpdateSingleStorageDomainsAvailability();
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

    private void UpdateSingleStorageDomainsAvailability()
    {
        boolean isStorageDomainsEmpty = ((ArrayList) getStorageDomain().getItems()).isEmpty();
        getIsSingleStorageDomain().setIsChangable(!isStorageDomainsEmpty);
        if (isStorageDomainsEmpty) {
            getIsSingleStorageDomain().setEntity(false);
        }
    }

    private void UpdateStorageDomainsAvailability()
    {
        boolean isStorageDomainsEmpty = ((ArrayList) getStorageDomain().getItems()).isEmpty();
        boolean isSingleStorageDomain = (Boolean) getIsSingleStorageDomain().getEntity();
        getStorageDomain().setIsChangable(isSingleStorageDomain && !isStorageDomainsEmpty);

        if (disks != null) {
            for (DiskModel diskModel : disks) {
                diskModel.getStorageDomain().setIsChangable(!isSingleStorageDomain);
                diskModel.getVolumeType().setIsAvailable(isVolumeFormatAvailable);
                diskModel.getSourceStorageDomain().setIsAvailable(isSourceStorageDomainAvailable);
            }
        }
    }

    private void updateImageToDestinationDomainMap() {
        for (DiskModel diskModel : disks) {
            Guid diskId = diskModel.getDiskImage().getId();
            Guid storageId = ((storage_domains) diskModel.getStorageDomain().getSelectedItem()).getId();
            imageToDestinationDomainMap.put(diskId, storageId);
        }
    }

    public void setIsVolumeFormatAvailable(boolean isVolumeFormatAvailable) {
        this.isVolumeFormatAvailable = isVolumeFormatAvailable;
    }

    public void setIsSourceStorageDomainAvailable(boolean isSourceStorageDomainAvailable) {
        this.isSourceStorageDomainAvailable = isSourceStorageDomainAvailable;
    }
}
