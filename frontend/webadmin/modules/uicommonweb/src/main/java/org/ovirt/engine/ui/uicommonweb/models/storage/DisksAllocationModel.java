package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.queries.GetAllRelevantQuotasForStorageParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.Linq.DiskModelByAliasComparer;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicompat.Constants;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class DisksAllocationModel extends EntityModel
{
    protected static Constants constants = ConstantsManager.getInstance().getConstants();

    private final IEventListener quota_storageEventListener = new IEventListener() {
        @Override
        public void eventRaised(Event ev, Object sender, EventArgs args) {
            updateDisksQuota(sender);
        }
    };

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
        sortDisks();
    }

    private void registerToEvents() {
        for (DiskModel disk : disks) {
            disk.getStorageDomain().getSelectedItemChangedEvent().removeListener(quota_storageEventListener);
            disk.getStorageDomain().getSelectedItemChangedEvent().addListener(quota_storageEventListener);
        }
    }

    public void sortDisks() {
        Linq.Sort(disks, new DiskModelByAliasComparer());
        OnPropertyChanged(new PropertyChangedEventArgs("Disks")); //$NON-NLS-1$
    }

    private HashMap<Guid, DiskImage> imageToDestinationDomainMap;

    public HashMap<Guid, DiskImage> getImageToDestinationDomainMap(Boolean isSingle)
    {
        updateImageToDestinationDomainMap(isSingle);
        return imageToDestinationDomainMap;
    }

    public HashMap<Guid, DiskImage> getImageToDestinationDomainMap()
    {
        return getImageToDestinationDomainMap(false);
    }

    public void setImageToDestinationDomainMap(HashMap<Guid, DiskImage> imageToDestinationDomainMap)
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

    private QuotaEnforcementTypeEnum quotaEnforcementType;

    public void setQuotaEnforcementType(QuotaEnforcementTypeEnum value) {
        this.quotaEnforcementType = value;
        OnPropertyChanged(new PropertyChangedEventArgs("QuotaEnforcmentType")); //$NON-NLS-1$
    }

    public QuotaEnforcementTypeEnum getQuotaEnforcementType() {
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
    private boolean isAliasChangable;
    private boolean isSourceStorageDomainAvailable;
    private boolean isSourceStorageDomainNameAvailable;
    private boolean isWarningAvailable;

    public DisksAllocationModel()
    {
        setIsSingleStorageDomain(new EntityModel());
        getIsSingleStorageDomain().setEntity(false);
        getIsSingleStorageDomain().getEntityChangedEvent().addListener(this);

        setStorageDomain(new ListModel());
        getStorageDomain().getItemsChangedEvent().addListener(this);
        getStorageDomain().getSelectedItemChangedEvent().addListener(this);

        setQuota(new ListModel());
        getQuota().getItemsChangedEvent().addListener(this);

        setSourceStorageDomain(new ListModel());
        getSourceStorageDomain().getItemsChangedEvent().addListener(this);
        getSourceStorageDomain().setIsAvailable(false);

        setSourceStorageDomainName(new EntityModel());
        getSourceStorageDomainName().setIsAvailable(false);

        setImageToDestinationDomainMap(new HashMap<Guid, DiskImage>());
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
        else if (ev.equals(ListModel.SelectedItemChangedEventDefinition) && sender == getStorageDomain()) {
            storage_domains storageDomain = (storage_domains) getStorageDomain().getSelectedItem();
            if (storageDomain != null) {
                updateQuota(storageDomain.getId(), null);
            }
        }
    }

    private void updateQuota(Guid storageDomainId, final ListModel isItem) {
        if (getQuotaEnforcementType() != QuotaEnforcementTypeEnum.DISABLED && storageDomainId != null) {
            Frontend.RunQuery(VdcQueryType.GetAllRelevantQuotasForStorage,
                    new GetAllRelevantQuotasForStorageParameters(storageDomainId),
                    new AsyncQuery(this,
                            new INewAsyncCallback() {

                                @Override
                                public void OnSuccess(Object innerModel, Object innerReturnValue) {
                                    ArrayList<Quota> list =
                                            (ArrayList<Quota>) ((VdcQueryReturnValue) innerReturnValue).getReturnValue();
                                    DisksAllocationModel diskAllocationModel = (DisksAllocationModel) innerModel;
                                    if (list != null) {
                                        if (isItem == null) {
                                            diskAllocationModel.getQuota().setItems(list);
                                            for (DiskModel diskModel : diskAllocationModel.getDisks()) {
                                                diskModel.getQuota().setItems(list);
                                                for (Quota quota : list) {
                                                    if (diskModel.getDisk() instanceof DiskImage
                                                            &&
                                                            quota.getId()
                                                                    .equals(((DiskImage) diskModel.getDisk()).getQuotaId())) {
                                                        diskModel.getQuota().setSelectedItem(quota);
                                                    }
                                                }
                                            }
                                        } else {
                                            Quota selectedQuota = null;
                                            if (isItem.getSelectedItem() != null) {
                                                selectedQuota = (Quota) isItem.getSelectedItem();
                                            }
                                            isItem.setItems(list);
                                            if (selectedQuota != null && list.size() > 1) {
                                                for (Quota quota : list) {
                                                    if (quota.getId().equals(selectedQuota.getId())) {
                                                        isItem.setSelectedItem(quota);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }));
        }
    }

    @Override
    protected void OnPropertyChanged(PropertyChangedEventArgs e)
    {
        super.OnPropertyChanged(e);

        if (e.PropertyName.equals("Disks")) //$NON-NLS-1$
        {
            UpdateStorageDomainsAvailability();
            UpdateQuotaAvailability();
            registerToEvents();
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
            diskModel.getSourceStorageDomain().setIsAvailable(isSourceStorageDomainAvailable);
            diskModel.getSourceStorageDomainName().setIsAvailable(isSourceStorageDomainNameAvailable);
            diskModel.getVolumeType().setIsAvailable(isVolumeFormatAvailable);
            diskModel.getVolumeType().setIsChangable(isVolumeFormatChangable);
            diskModel.getAlias().setIsChangable(isAliasChangable);
        }
    }

    private void updateImageToDestinationDomainMap(boolean isSingle) {

        if (disks == null) {
            return;
        }

        for (DiskModel diskModel : disks) {
            Guid diskId = ((DiskImage) diskModel.getDisk()).getId();
            Guid storageId = null;
            if (!isSingle) {
                storageId = ((storage_domains) diskModel.getStorageDomain().getSelectedItem()).getId();
            }
            else {
                storageId = ((storage_domains) getStorageDomain().getSelectedItem()).getId();
            }
            DiskImage diskImage = (DiskImage) diskModel.getDisk();
            ArrayList<Guid> storageIdList = new ArrayList<Guid>();
            storageIdList.add(storageId);
            diskImage.setstorage_ids(storageIdList);
            diskImage.setDiskAlias((String) diskModel.getAlias().getEntity());

            if (diskModel.getQuota().getSelectedItem() != null) {
                if (!isSingle) {
                    diskImage.setQuotaId(((Quota) diskModel.getQuota().getSelectedItem()).getId());
                }
                else {
                    diskImage.setQuotaId(((Quota) getQuota().getSelectedItem()).getId());
                }
            }
            imageToDestinationDomainMap.put(diskId, diskImage);
        }
    }

    private void UpdateQuotaAvailability() {
        getQuota().setIsAvailable(quotaEnforcementType != QuotaEnforcementTypeEnum.DISABLED);

        if (disks != null) {
            for (DiskModel diskModel : disks) {
                diskModel.getQuota().setIsAvailable(quotaEnforcementType != QuotaEnforcementTypeEnum.DISABLED);
            }
        }
    }

    private void updateDisksQuota(Object sender) {
        if (!(Boolean) isSingleStorageDomain.getEntity()) {

            storage_domains storageDomain = (storage_domains) ((ListModel) sender).getSelectedItem();
            if (storageDomain != null) {
                for (DiskModel innerDisk : disks) {
                    if (innerDisk.getStorageDomain().equals(sender)) {
                        updateQuota(storageDomain.getId(), innerDisk.getQuota());
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void ValidateEntity(IValidation[] validations)
    {
        super.ValidateEntity(validations);

        if (getDisks() == null) {
            return;
        }

        for (DiskModel diskModel : getDisks()) {
            if (!diskModel.getStorageDomain().getItems().iterator().hasNext()) {
                diskModel.getStorageDomain().getInvalidityReasons().add(
                        constants.storageDomainMustBeSpecifiedInvalidReason());
                diskModel.getStorageDomain().setIsValid(false);
                setIsValid(false);
            }
        }
    }

    public void setIsVolumeFormatAvailable(boolean isVolumeFormatAvailable) {
        this.isVolumeFormatAvailable = isVolumeFormatAvailable;
    }

    public boolean getIsVolumeFormatAvailable() {
        return isVolumeFormatAvailable;
    }

    public boolean getIsAliasChangable() {
        return isAliasChangable;
    }

    public void setIsAliasChangable(boolean isAliasChangable) {
        this.isAliasChangable = isAliasChangable;
    }

    public void setIsVolumeFormatChangable(boolean isVolumeFormatChangable) {
        this.isVolumeFormatChangable = isVolumeFormatChangable;
    }

    public void setIsSourceStorageDomainAvailable(boolean isSourceStorageDomainAvailable) {
        this.isSourceStorageDomainAvailable = isSourceStorageDomainAvailable;
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

    public boolean isWarningAvailable() {
        return isWarningAvailable;
    }

    public void setWarningAvailable(boolean isWarningAvailable) {
        this.isWarningAvailable = isWarningAvailable;
    }

}
