package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.businessentities.storage.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
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
import org.ovirt.engine.ui.uicommonweb.validation.I18NNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;

public class DisksAllocationModel extends EntityModel
{
    protected static final UIConstants constants = ConstantsManager.getInstance().getConstants();
    protected static final UIMessages messages = ConstantsManager.getInstance().getMessages();

    private final IEventListener<EventArgs> storageDomainEventListener = new IEventListener<EventArgs>() {
        @Override
        public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
            updateDisks(sender);
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

        if (disks == null) {
            return;
        }

        sortDisks();

        for (final DiskModel diskModel : disks) {
            diskModel.getStorageDomain().getSelectedItemChangedEvent().removeListener(storageDomainEventListener);
            diskModel.getStorageDomain().getSelectedItemChangedEvent().addListener(storageDomainEventListener);
            diskModel.getStorageDomain().getItemsChangedEvent().addListener(new IEventListener<EventArgs>() {
                @Override
                public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                    DiskImage disk = (DiskImage) diskModel.getDisk();
                    if (diskModel.getStorageDomain().getItems() != null && disk.getStorageIds() != null
                            && !disk.getStorageIds().isEmpty()) {
                        diskModel.getStorageDomain()
                                .setSelectedItem(Linq.firstOrDefault(diskModel.getStorageDomain().getItems(),
                                        new Linq.StoragePredicate(disk.getStorageIds().iterator().next())));
                    }
                }
            });
        }
    }

    public void sortDisks() {
        if (disks != null) {
            Collections.sort(disks, new DiskModelByAliasComparer());
            onPropertyChanged(new PropertyChangedEventArgs("Disks")); //$NON-NLS-1$
        }
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

        setDynamicWarning(new EntityModel<String>());
        getDynamicWarning().setIsAvailable(false);
    }

    private void updateQuota(Guid storageDomainId, final ListModel isItem) {
        if (getQuotaEnforcementType() != QuotaEnforcementTypeEnum.DISABLED && storageDomainId != null) {
            Frontend.getInstance().runQuery(VdcQueryType.GetAllRelevantQuotasForStorage,
                    new IdQueryParameters(storageDomainId),
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

    private void updateDiskProfile(Guid storageDomainId, final ListModel<DiskProfile> diskProfiles) {
        Frontend.getInstance().runQuery(VdcQueryType.GetDiskProfilesByStorageDomainId,
                new IdQueryParameters(storageDomainId),
                new AsyncQuery(new INewAsyncCallback() {

                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        List<DiskProfile> fetchedDiskProfiles =
                                (List<DiskProfile>) ((VdcQueryReturnValue) returnValue).getReturnValue();
                        DisksAllocationModel.this.setDiskProfilesList(diskProfiles, fetchedDiskProfiles);

                    }

                }));
    }

    private void setDiskProfilesList(final ListModel<DiskProfile> diskProfiles, List<DiskProfile> fetchedDiskProfiles) {
        if (fetchedDiskProfiles != null) {
            // normal flow, set items and selected item according to current selected.
            if (diskProfiles == null) {
                for (DiskModel diskModel : getDisks()) {
                    diskModel.getDiskProfile().setItems(fetchedDiskProfiles);
                    for (DiskProfile diskProfile : fetchedDiskProfiles) {
                        if (diskModel.getDisk().getDiskStorageType() == DiskStorageType.IMAGE
                                && diskProfile.getId().equals(((DiskImage) diskModel.getDisk()).getDiskProfileId())) {
                            diskModel.getDiskProfile().setSelectedItem(diskProfile);
                        }
                    }
                }
                // inner model disk profiles
            } else {
                DiskProfile selectedDiskProfile = null;
                if (diskProfiles.getSelectedItem() != null) {
                    selectedDiskProfile = diskProfiles.getSelectedItem();
                }
                diskProfiles.setItems(fetchedDiskProfiles);
                if (selectedDiskProfile != null && fetchedDiskProfiles.size() > 1) {
                    for (DiskProfile diskProfile : fetchedDiskProfiles) {
                        if (diskProfile.getId().equals(selectedDiskProfile.getId())) {
                            diskProfiles.setSelectedItem(diskProfile);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onPropertyChanged(PropertyChangedEventArgs e)
    {
        super.onPropertyChanged(e);
        if (e.propertyName.equals("Disks") || e.propertyName.equals("VolumeFormat")) //$NON-NLS-1$ //$NON-NLS-2$
        {
            updateStorageDomainsAvailability();
            updateQuotaAvailability();
        }
    }

    private void updateStorageDomainsAvailability()
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
            StorageDomain storageDomain = diskModel.getStorageDomain().getSelectedItem();
            DiskImage diskImage = (DiskImage) diskModel.getDisk();
            diskImage.setStorageIds(new ArrayList<Guid>(Arrays.asList(storageDomain.getId())));
            diskImage.setDiskAlias(diskModel.getAlias().getEntity());
            DiskProfile diskProfile = diskModel.getDiskProfile().getSelectedItem();
            diskImage.setDiskProfileId(diskProfile != null ? diskProfile.getId() : null);
            if (diskModel.getQuota().getSelectedItem() != null) {
                diskImage.setQuotaId(diskModel.getQuota().getSelectedItem().getId());
            }

            if (diskModel.getVolumeType().getIsAvailable()) {
                VolumeType volumeType = diskModel.getVolumeType().getSelectedItem();
                diskImage.setVolumeType(volumeType);
                diskImage.setvolumeFormat(AsyncDataProvider.getInstance().getDiskVolumeFormat(
                        volumeType, storageDomain.getStorageType()));
            }

            imageToDestinationDomainMap.put(diskImage.getId(), diskImage);
        }
    }

    private void updateQuotaAvailability() {
        if (disks != null) {
            for (DiskModel diskModel : disks) {
                diskModel.getQuota().setIsAvailable(quotaEnforcementType != QuotaEnforcementTypeEnum.DISABLED);
            }
        }
    }

    private void updateDisks(Object sender) {
        StorageDomain storageDomain = (StorageDomain) ((ListModel) sender).getSelectedItem();
        if (storageDomain != null) {
            for (DiskModel innerDisk : disks) {
                if (innerDisk.getStorageDomain().equals(sender)) {
                    updateQuota(storageDomain.getId(), innerDisk.getQuota());
                    updateDiskProfile(storageDomain.getId(), innerDisk.getDiskProfile());
                    break;
                }
            }
        }
    }

    @Override
    public void validateEntity(IValidation[] validations)
    {
        super.validateEntity(validations);

        if (getDisks() == null) {
            return;
        }

        boolean isModelValid = true;
        for (DiskModel diskModel : getDisks()) {
            ListModel diskStorageDomains = diskModel.getStorageDomain();
            if (!diskStorageDomains.getItems().iterator().hasNext() || diskStorageDomains.getSelectedItem() == null) {
                diskModel.getStorageDomain().getInvalidityReasons().add(
                        constants.storageDomainMustBeSpecifiedInvalidReason());
                diskModel.getStorageDomain().setIsValid(false);
                isModelValid = false;
            }
            diskModel.getAlias().validateEntity(new IValidation[] { new NotEmptyValidation(), new I18NNameValidation() });
            isModelValid = isModelValid && diskModel.getAlias().getIsValid();
        }
        setIsValid(isModelValid);
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
        if (this.isVolumeFormatChangable != isVolumeFormatChangable) {
            this.isVolumeFormatChangable = isVolumeFormatChangable;
            onPropertyChanged(new PropertyChangedEventArgs("VolumeFormat")); //$NON-NLS-1$
        }
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

    private EntityModel<String> dynamicWarning;

    public EntityModel<String> getDynamicWarning()
    {
        return dynamicWarning;
    }

    public void setDynamicWarning(EntityModel<String> value)
    {
        dynamicWarning = value;
    }

}
