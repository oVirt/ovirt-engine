package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.queries.GetAllRelevantQuotasForStorageParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.Linq.DiskModelByAliasComparer;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicompat.Constants;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class DisksAllocationModel extends EntityModel
{
    protected static Constants constants = ConstantsManager.getInstance().getConstants();

    private final IEventListener quota_storageEventListener = new IEventListener() {
        @Override
        public void eventRaised(Event ev, Object sender, EventArgs args) {
            updateDisksQuota(sender);
        }
    };

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
        onPropertyChanged(new PropertyChangedEventArgs("Disks")); //$NON-NLS-1$
    }

    private HashMap<Guid, DiskImage> imageToDestinationDomainMap;

    public HashMap<Guid, DiskImage> getImageToDestinationDomainMap()
    {
        updateImageToDestinationDomainMap();
        return imageToDestinationDomainMap;
    }

    public void setImageToDestinationDomainMap(HashMap<Guid, DiskImage> imageToDestinationDomainMap)
    {
        this.imageToDestinationDomainMap = imageToDestinationDomainMap;
    }

    private ArrayList<StorageDomain> activeStorageDomains;

    public ArrayList<StorageDomain> getActiveStorageDomains()
    {
        return activeStorageDomains;
    }

    public void setActiveStorageDomains(ArrayList<StorageDomain> activeStorageDomains)
    {
        this.activeStorageDomains = activeStorageDomains;
    }

    private QuotaEnforcementTypeEnum quotaEnforcementType;

    public void setQuotaEnforcementType(QuotaEnforcementTypeEnum value) {
        this.quotaEnforcementType = value;
        onPropertyChanged(new PropertyChangedEventArgs("QuotaEnforcmentType")); //$NON-NLS-1$
    }

    public QuotaEnforcementTypeEnum getQuotaEnforcementType() {
        return quotaEnforcementType;
    }

    private boolean isVolumeFormatAvailable;
    private boolean isVolumeFormatChangable;
    private boolean isAliasChangable;
    private boolean isSourceStorageDomainAvailable;
    private boolean isSourceStorageDomainNameAvailable;
    private boolean isWarningAvailable;

    public DisksAllocationModel()
    {
        setImageToDestinationDomainMap(new HashMap<Guid, DiskImage>());
    }

    private void updateQuota(Guid storageDomainId, final ListModel isItem) {
        if (getQuotaEnforcementType() != QuotaEnforcementTypeEnum.DISABLED && storageDomainId != null) {
            Frontend.RunQuery(VdcQueryType.GetAllRelevantQuotasForStorage,
                    new GetAllRelevantQuotasForStorageParameters(storageDomainId),
                    new AsyncQuery(this,
                            new INewAsyncCallback() {

                                @Override
                                public void onSuccess(Object innerModel, Object innerReturnValue) {
                                    ArrayList<Quota> list =
                                            (ArrayList<Quota>) ((VdcQueryReturnValue) innerReturnValue).getReturnValue();
                                    DisksAllocationModel diskAllocationModel = (DisksAllocationModel) innerModel;
                                    if (list != null) {
                                        if (isItem == null) {
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
    protected void onPropertyChanged(PropertyChangedEventArgs e)
    {
        super.onPropertyChanged(e);

        if (e.PropertyName.equals("Disks")) //$NON-NLS-1$
        {
            UpdateStorageDomainsAvailability();
            UpdateQuotaAvailability();
            registerToEvents();
        }
    }

    private void UpdateStorageDomainsAvailability()
    {
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

    private void updateImageToDestinationDomainMap() {

        if (disks == null) {
            return;
        }

        for (DiskModel diskModel : disks) {
            StorageDomain storageDomain = (StorageDomain) diskModel.getStorageDomain().getSelectedItem();
            DiskImage diskImage = (DiskImage) diskModel.getDisk();
            diskImage.setStorageIds(new ArrayList<Guid>(Arrays.asList(storageDomain.getId())));
            diskImage.setDiskAlias((String) diskModel.getAlias().getEntity());

            if (diskModel.getQuota().getSelectedItem() != null) {
                diskImage.setQuotaId(((Quota) diskModel.getQuota().getSelectedItem()).getId());
            }

            if (diskModel.getVolumeType().getIsAvailable()) {
                VolumeType volumeType = (VolumeType) diskModel.getVolumeType().getSelectedItem();
                diskImage.setVolumeType(volumeType);
                diskImage.setvolumeFormat(AsyncDataProvider.GetDiskVolumeFormat(
                        volumeType, storageDomain.getStorageType()));
            }

            imageToDestinationDomainMap.put(diskImage.getId(), diskImage);
        }
    }

    private void UpdateQuotaAvailability() {
        if (disks != null) {
            for (DiskModel diskModel : disks) {
                diskModel.getQuota().setIsAvailable(quotaEnforcementType != QuotaEnforcementTypeEnum.DISABLED);
            }
        }
    }

    private void updateDisksQuota(Object sender) {
        StorageDomain storageDomain = (StorageDomain) ((ListModel) sender).getSelectedItem();
        if (storageDomain != null) {
            for (DiskModel innerDisk : disks) {
                if (innerDisk.getStorageDomain().equals(sender)) {
                    updateQuota(storageDomain.getId(), innerDisk.getQuota());
                    break;
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

    public boolean isWarningAvailable() {
        return isWarningAvailable;
    }

    public void setWarningAvailable(boolean isWarningAvailable) {
        this.isWarningAvailable = isWarningAvailable;
    }

}
