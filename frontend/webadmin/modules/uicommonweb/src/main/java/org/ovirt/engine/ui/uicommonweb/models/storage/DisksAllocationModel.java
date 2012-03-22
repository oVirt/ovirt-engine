package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.QuotaEnforcmentTypeEnum;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.Linq.DiskModelByNameComparer;
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

    private ListModel privateSourceStorageDomain;

    public ListModel getSourceStorageDomain()
    {
        return privateSourceStorageDomain;
    }

    public void setSourceStorageDomain(ListModel value)
    {
        privateSourceStorageDomain = value;
    }

    private EntityModel sourceStorageDomainName;

    public EntityModel getSourceStorageDomainName()
    {
        return sourceStorageDomainName;
    }

    public void setSourceStorageDomainName(EntityModel value)
    {
        sourceStorageDomainName = value;
    }

    private List<DiskModel> disks;

    public List<DiskModel> getDisks()
    {
        return disks;
    }

    public void setDisks(List<DiskModel> value)
    {
        disks = value;
        Linq.Sort(disks, new DiskModelByNameComparer());
        OnPropertyChanged(new PropertyChangedEventArgs("Disks"));
    }

    private HashMap<Guid, Guid> imageToDestinationDomainMap;

    public HashMap<Guid, Guid> getImageToDestinationDomainMap()
    {
        updateImageToDestinationDomainMap();
        return imageToDestinationDomainMap;
    }

    public void setImageToDestinationDomainMap(HashMap<Guid, Guid> imageToDestinationDomainMap)
    {
        this.imageToDestinationDomainMap = imageToDestinationDomainMap;
    }

    private ArrayList<storage_domains> activeStorageDomains;

    public ArrayList<storage_domains> getActiveStorageDomains()
    {
        return activeStorageDomains;
    }

    public void setActiveStorageDomains(ArrayList<storage_domains> activeStorageDomains)
    {
        this.activeStorageDomains = activeStorageDomains;
    }

    private QuotaEnforcmentTypeEnum quotaEnforcementType;

    public void setQuotaEnforcementType(QuotaEnforcmentTypeEnum value) {
        this.quotaEnforcementType = value;
        OnPropertyChanged(new PropertyChangedEventArgs("QuotaEnforcmentType"));
    }

    public QuotaEnforcmentTypeEnum getQuotaEnforcementType() {
        return quotaEnforcementType;
    }

    private ListModel quota;

    public ListModel getQuota()
    {
        return quota;
    }

    public void setQuota(ListModel value)
    {
        quota = value;
    }

    private boolean isSingleDiskMove;
    private boolean isSingleDiskCopy;
    private boolean isVolumeFormatAvailable;
    private boolean isVolumeFormatChangable;
    private boolean isSourceStorageDomainAvailable;
    private boolean isSourceStorageDomainChangable;
    private boolean isSourceStorageDomainNameAvailable;


    public DisksAllocationModel()
    {
        setIsSingleStorageDomain(new EntityModel());
        getIsSingleStorageDomain().setEntity(true);
        getIsSingleStorageDomain().getEntityChangedEvent().addListener(this);

        setStorageDomain(new ListModel());
        getStorageDomain().getItemsChangedEvent().addListener(this);

        setQuota(new ListModel());
        getQuota().getItemsChangedEvent().addListener(this);

        setSourceStorageDomain(new ListModel());
        getSourceStorageDomain().getItemsChangedEvent().addListener(this);
        getSourceStorageDomain().setIsAvailable(false);

        setSourceStorageDomainName(new EntityModel());
        getSourceStorageDomainName().setIsAvailable(false);

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
        else if (ev.equals(ListModel.ItemsChangedEventDefinition) && sender == getStorageDomain() ||
                ev.equals(ListModel.ItemsChangedEventDefinition) && sender == getSourceStorageDomain())
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
            UpdateQuotaAvailability();
        }
    }

    private void UpdateSingleStorageDomainsAvailability()
    {
        boolean isStorageDomainsEmpty =
                getStorageDomain().getItems() != null && ((ArrayList) getStorageDomain().getItems()).isEmpty();
        getIsSingleStorageDomain().setIsChangable(!isStorageDomainsEmpty);
        if (isStorageDomainsEmpty) {
            getIsSingleStorageDomain().setEntity(false);
        }
    }

    private void UpdateStorageDomainsAvailability()
    {
        boolean isSingleStorageDomain = (Boolean) getIsSingleStorageDomain().getEntity();

        if (getStorageDomain().getItems() != null) {
            boolean isStorageDomainsEmpty = ((ArrayList) getStorageDomain().getItems()).isEmpty();
            getStorageDomain().setIsChangable(isSingleStorageDomain && !isStorageDomainsEmpty);
            getQuota().setIsChangable(isSingleStorageDomain && !isStorageDomainsEmpty);
        }

        if (disks == null) {
            return;
        }

        for (DiskModel diskModel : disks) {
            boolean isDestStoragesEmpty = diskModel.getStorageDomain().getItems() != null ?
                    ((ArrayList) diskModel.getStorageDomain().getItems()).isEmpty() : true;

            diskModel.getSourceStorageDomain().setIsAvailable(isSourceStorageDomainAvailable);
            diskModel.getSourceStorageDomain().setIsChangable(!isSingleStorageDomain && isSourceStorageDomainChangable);
            diskModel.getStorageDomain().setIsChangable(!isSingleStorageDomain && !isDestStoragesEmpty);
            diskModel.getQuota().setIsChangable(!isSingleStorageDomain && !isDestStoragesEmpty);
            diskModel.getVolumeType().setIsAvailable(isVolumeFormatAvailable);
            diskModel.getVolumeType().setIsChangable(isVolumeFormatChangable);

            if (diskModel.getSourceStorageDomain().getItems() != null
                    && diskModel.getSourceStorageDomain().getItems().iterator().hasNext()) {
                storage_domains sourceStorage =
                        ((storage_domains) diskModel.getSourceStorageDomain().getItems().iterator().next());
                String sourceStorageName = sourceStorage != null ? sourceStorage.getstorage_name() : "";
                diskModel.getSourceStorageDomainName().setIsAvailable(isSourceStorageDomainNameAvailable);
                diskModel.getSourceStorageDomainName().setEntity(sourceStorageName);
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

    private void UpdateQuotaAvailability() {
        getQuota().setIsAvailable(quotaEnforcementType != QuotaEnforcmentTypeEnum.DISABLED);

        if (disks != null) {
            for (DiskModel diskModel : disks) {
                diskModel.getQuota().setIsAvailable(quotaEnforcementType != QuotaEnforcmentTypeEnum.DISABLED);
            }
        }
    }

    public void setIsVolumeFormatAvailable(boolean isVolumeFormatAvailable) {
        this.isVolumeFormatAvailable = isVolumeFormatAvailable;
    }

    public boolean getIsVolumeFormatAvailable() {
        return isVolumeFormatAvailable;
    }

    public void setIsVolumeFormatChangable(boolean isVolumeFormatChangable) {
        this.isVolumeFormatChangable = isVolumeFormatChangable;
    }

    public void setIsSourceStorageDomainAvailable(boolean isSourceStorageDomainAvailable) {
        this.isSourceStorageDomainAvailable = isSourceStorageDomainAvailable;
    }

    public void setIsSourceStorageDomainChangable(boolean isSourceStorageDomainChangable) {
        this.isSourceStorageDomainChangable = isSourceStorageDomainChangable;
    }

    public void setIsSourceStorageDomainNameAvailable(boolean isSourceStorageDomainNameAvailable) {
        this.isSourceStorageDomainNameAvailable = isSourceStorageDomainNameAvailable;
    }

    public void setIsSingleDiskMove(boolean isSingleDiskMove) {
        this.isSingleDiskMove = isSingleDiskMove;
    }

    public void setIsSingleDiskCopy(boolean isSingleDiskCopy) {
        this.isSingleDiskCopy = isSingleDiskCopy;
    }

    public boolean getIsSingleDiskMove() {
        return isSingleDiskMove;
    }

    public boolean getIsSingleDiskCopy() {
        return isSingleDiskCopy;
    }
}
