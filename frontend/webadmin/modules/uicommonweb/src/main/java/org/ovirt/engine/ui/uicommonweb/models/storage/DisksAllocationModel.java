package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;
import org.ovirt.engine.ui.uicommonweb.validation.I18NNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;
import org.ovirt.engine.ui.uicompat.UIMessages;

public class DisksAllocationModel extends EntityModel {
    protected static final UIConstants constants = ConstantsManager.getInstance().getConstants();
    protected static final UIMessages messages = ConstantsManager.getInstance().getMessages();

    private static final String VOLUME_TYPE = "VOLUME_TYPE";  //$NON-NLS-1$
    private static final String VOLUME_FORMAT = "VOLUME_FORMAT";  //$NON-NLS-1$
    private static final String THIN_PROVISIONING = "THIN_PROVISIONING";  //$NON-NLS-1$

    private final IEventListener<EventArgs> storageDomainEventListener = (ev, sender, args) -> updateDisks(sender);

    // A dummy domain with "Mixed" title
    private final StorageDomain mixedStorageDomain = new StorageDomain();

    private List<DiskModel> disks;
    private Model container;
    private EntityModel<Boolean> diskAllocationTargetEnabled;
    private ListModel<StorageDomain> targetStorageDomains;
    private Map<Guid, DiskImage> imageToDestinationDomainMap;
    private ArrayList<StorageDomain> activeStorageDomains;
    private QuotaEnforcementTypeEnum quotaEnforcementType;
    private EntityModel<String> dynamicWarning;

    private boolean isVolumeFormatAvailable;
    private boolean isVolumeFormatChangeable;
    private boolean isVolumeTypeAvailable;
    private boolean isVolumeTypeChangeable;
    private boolean isThinProvisioning;
    private boolean isAliasChangeable;
    private boolean isSourceStorageDomainAvailable;
    private boolean isSourceStorageDomainNameAvailable;
    private boolean isTargetAvailable = true;

    public Model getContainer() {
        return container;
    }

    public void setContainer(Model container) {
        this.container = container;
    }

    public EntityModel<Boolean> getDiskAllocationTargetEnabled() {
        return diskAllocationTargetEnabled;
    }

    public void setDiskAllocationTargetEnabled(EntityModel<Boolean> enabled) {
        this.diskAllocationTargetEnabled = enabled;
    }

    public ListModel<StorageDomain> getTargetStorageDomains() {
        return targetStorageDomains;
    }

    public void setTargetStorageDomains(ListModel<StorageDomain> targetStorageDomains) {
        this.targetStorageDomains = targetStorageDomains;
    }

    public Map<Guid, DiskImage> getImageToDestinationDomainMap() {
        updateImageToDestinationDomainMap();
        return imageToDestinationDomainMap;
    }

    public void setImageToDestinationDomainMap(Map<Guid, DiskImage> imageToDestinationDomainMap) {
        this.imageToDestinationDomainMap = imageToDestinationDomainMap;
    }

    public ArrayList<StorageDomain> getActiveStorageDomains() {
        return activeStorageDomains;
    }

    public void setActiveStorageDomains(ArrayList<StorageDomain> activeStorageDomains) {
        this.activeStorageDomains = activeStorageDomains;
    }

    public QuotaEnforcementTypeEnum getQuotaEnforcementType() {
        return quotaEnforcementType;
    }

    public void setQuotaEnforcementType(QuotaEnforcementTypeEnum value) {
        this.quotaEnforcementType = value;
        onPropertyChanged(new PropertyChangedEventArgs("QuotaEnforcmentType")); //$NON-NLS-1$
    }

    public EntityModel<String> getDynamicWarning() {
        return dynamicWarning;
    }

    public void setDynamicWarning(EntityModel<String> value) {
        dynamicWarning = value;
    }

    public boolean isTargetAvailable() {
        return isTargetAvailable;
    }

    public void setTargetAvailable(boolean targetAvailable) {
        isTargetAvailable = targetAvailable;
    }

    public boolean getIsVolumeTypeAvailable() {
        return isVolumeTypeAvailable;
    }

    public void setIsVolumeTypeAvailable(boolean isVolumeFormatAvailable) {
        this.isVolumeTypeAvailable = isVolumeFormatAvailable;
    }

    public boolean getIsVolumeFormatAvailable() {
        return isVolumeFormatAvailable;
    }

    public void setIsVolumeFormatAvailable(boolean isVolumeFormatAvailable) {
        this.isVolumeFormatAvailable = isVolumeFormatAvailable;
    }

    public boolean getIsAliasChangeable() {
        return isAliasChangeable;
    }

    public void setIsAliasChangeable(boolean isAliasChangeable) {
        this.isAliasChangeable = isAliasChangeable;
    }

    public void setIsSourceStorageDomainAvailable(boolean isSourceStorageDomainAvailable) {
        this.isSourceStorageDomainAvailable = isSourceStorageDomainAvailable;
    }

    public void setIsSourceStorageDomainNameAvailable(boolean isSourceStorageDomainNameAvailable) {
        this.isSourceStorageDomainNameAvailable = isSourceStorageDomainNameAvailable;
    }

    public List<DiskModel> getDisks() {
        return disks;
    }

    public void setDisks(List<DiskModel> value) {
        disks = value;

        if (disks == null) {
            return;
        }

        sortDisks();

        setDefaultVolumeInformationSelection(disks);
        for (final DiskModel diskModel : disks) {
            diskModel.getStorageDomain().getSelectedItemChangedEvent().removeListener(storageDomainEventListener);
            diskModel.getStorageDomain().getSelectedItemChangedEvent().addListener(storageDomainEventListener);
            diskModel.getStorageDomain().getItemsChangedEvent().addListener((ev, sender, args) -> {
                DiskImage disk = (DiskImage) diskModel.getDisk();
                if (diskModel.getStorageDomain().getItems() != null && disk.getStorageIds() != null
                        && !disk.getStorageIds().isEmpty() && !diskModel.getStorageDomain().getItems().isEmpty()) {
                    diskModel.getStorageDomain().setSelectedItem(Linq.firstOrDefault(
                            diskModel.getStorageDomain().getItems(),
                            new Linq.IdsPredicate<>(disk.getStorageIds()),
                            diskModel.getStorageDomain().getItems().iterator().next()));
                }
            });
        }
    }

    protected void setDefaultVolumeInformationSelection(List<DiskModel> diskModels) {
        final Map<Guid, DiskModel> diskModelsMap = new HashMap<>();
        for (DiskModel diskModel : diskModels) {
            diskModelsMap.put(((DiskImage) diskModel.getDisk()).getImageId(), diskModel);
        }

        AsyncDataProvider.getInstance().getAncestorImagesByImagesIds(new AsyncQuery<>(imagesAncestors -> {
            for (Map.Entry<Guid, DiskImage> entry : imagesAncestors.entrySet()) {
                DiskModel diskModel = diskModelsMap.get(entry.getKey());
                diskModel.getVolumeType().setSelectedItem(entry.getValue().getVolumeType());
                diskModel.getVolumeFormat().setSelectedItem(entry.getValue().getVolumeFormat());
                updateStorageDomainsAvailability();
            }
        }), new ArrayList<>(diskModelsMap.keySet()));
    }

    public void sortDisks() {
        if (disks != null) {
            disks.sort(Comparator.comparing(d -> d.getDisk().getDiskAlias()));
            onPropertyChanged(new PropertyChangedEventArgs("Disks")); //$NON-NLS-1$
        }
    }

    public DisksAllocationModel() {
        setImageToDestinationDomainMap(new HashMap<>());

        setTargetStorageDomains(new ListModel<>());
        getTargetStorageDomains().setIsAvailable(false);

        setDynamicWarning(new EntityModel<>());
        getDynamicWarning().setIsAvailable(false);

        setDiskAllocationTargetEnabled(new EntityModel<>(false));
        getDiskAllocationTargetEnabled().setIsAvailable(false);

        mixedStorageDomain.setStorageName(constants.mixedTargetDomains());
    }

    private void updateQuota(Guid storageDomainId, final ListModel<Quota> isItem, final Guid diskQuotaId) {
        if (getQuotaEnforcementType() != QuotaEnforcementTypeEnum.DISABLED && storageDomainId != null) {
            AsyncDataProvider.getInstance().getAllRelevantQuotasForStorageSorted(new AsyncQuery<>(
                    list -> {
                        if (list == null) {
                            return;
                        }

                        Guid selectedQuota = isItem.getSelectedItem() != null ?
                                isItem.getSelectedItem().getId() : null;
                        selectedQuota = selectedQuota != null ? selectedQuota : diskQuotaId;

                        isItem.setItems(list);
                        if (selectedQuota != null && list.size() > 1) {
                            for (Quota quota : list) {
                                if (quota.getId().equals(selectedQuota)) {
                                    isItem.setSelectedItem(quota);
                                    break;
                                }
                            }
                        }
                    }), storageDomainId, null);
        }
    }

    private void updateDiskProfile(Guid storageDomainId, final ListModel<DiskProfile> diskProfiles) {
        Frontend.getInstance().runQuery(QueryType.GetDiskProfilesByStorageDomainId,
                new IdQueryParameters(storageDomainId),
                new AsyncQuery<QueryReturnValue>(returnValue -> {
                    List<DiskProfile> fetchedDiskProfiles = returnValue.getReturnValue();
                    DisksAllocationModel.this.setDiskProfilesList(diskProfiles, fetchedDiskProfiles);

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
    protected void onPropertyChanged(PropertyChangedEventArgs e) {
        super.onPropertyChanged(e);
        if (e.propertyName.equals("Disks") || e.propertyName.equals(VOLUME_TYPE) //$NON-NLS-1$
                || e.propertyName.equals(VOLUME_FORMAT)
                || e.propertyName.equals(THIN_PROVISIONING)) {
            updateStorageDomainsAvailability();
            updateQuotaAvailability();
        }
    }

    private void updateStorageDomainsAvailability() {
        if (disks == null) {
            return;
        }

        for (DiskModel diskModel : disks) {
            diskModel.getSourceStorageDomain().setIsAvailable(isSourceStorageDomainAvailable);
            diskModel.getSourceStorageDomainName().setIsAvailable(isSourceStorageDomainNameAvailable);
            diskModel.getStorageDomain().setIsAvailable(isTargetAvailable);
            diskModel.getVolumeType().setIsAvailable(isVolumeTypeAvailable);
            diskModel.getVolumeType().setIsChangeable(isVolumeTypeChangeable);
            diskModel.getVolumeFormat().setIsAvailable(isVolumeFormatAvailable);
            diskModel.getVolumeFormat().setIsChangeable(isVolumeFormatChangeable);
            diskModel.getAlias().setIsChangeable(isAliasChangeable);

            if (isThinProvisioning) {
                diskModel.getVolumeFormat().setSelectedItem(VolumeFormat.COW);
            }
        }
    }

    private void updateImageToDestinationDomainMap() {

        if (disks == null) {
            return;
        }

        for (DiskModel diskModel : disks) {
            StorageDomain storageDomain = diskModel.getStorageDomain().getSelectedItem();
            DiskImage diskImage = (DiskImage) diskModel.getDisk();
            diskImage.setStorageIds(new ArrayList<>(Collections.singletonList(storageDomain.getId())));
            diskImage.setDiskAlias(diskModel.getAlias().getEntity());
            DiskProfile diskProfile = diskModel.getDiskProfile().getSelectedItem();
            diskImage.setDiskProfileId(diskProfile != null ? diskProfile.getId() : null);
            if (diskModel.getQuota().getSelectedItem() != null) {
                diskImage.setQuotaId(diskModel.getQuota().getSelectedItem().getId());
            }

            if (diskModel.getVolumeFormat().getIsAvailable()) {
                VolumeFormat volumeFormat = diskModel.getVolumeFormat().getSelectedItem();
                diskImage.setVolumeFormat(volumeFormat);
                diskImage.setVolumeType(AsyncDataProvider.getInstance()
                        .getVolumeType(volumeFormat,
                                storageDomain.getStorageType(),
                                diskModel.getVm(),
                                diskImage.getImage()));
            } else if (diskModel.getVolumeType().getIsAvailable()) {
                VolumeType volumeType = diskModel.getVolumeType().getSelectedItem();
                diskImage.setVolumeType(volumeType);
                diskImage.setVolumeFormat(AsyncDataProvider.getInstance().getDiskVolumeFormat(
                        volumeType, storageDomain.getStorageType()));
            }

            imageToDestinationDomainMap.put(diskImage.getId(), diskImage);
        }
    }

    public void updateTargetChangeable(boolean enabled) {
        if (disks != null) {
            for (DiskModel diskModel : disks) {
                diskModel.getStorageDomain().setIsChangeable(enabled);
                diskModel.getDiskProfile().setIsChangeable(enabled);
            }
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
        StorageDomain storageDomain = ((ListModel<StorageDomain>) sender).getSelectedItem();
        if (storageDomain != null) {
            for (DiskModel innerDisk : disks) {
                if (innerDisk.getStorageDomain().equals(sender)) {
                    Guid diskQuotaId = null;
                    if (innerDisk.getDisk() instanceof DiskImage) {
                        DiskImage img = (DiskImage) innerDisk.getDisk();
                        diskQuotaId = img.getQuotaId();
                    }

                    updateQuota(storageDomain.getId(), innerDisk.getQuota(), diskQuotaId);
                    updateDiskProfile(storageDomain.getId(), innerDisk.getDiskProfile());
                    break;
                }
            }
            updateSelectedTargetStorage();
        }
    }

    protected List<DiskModel> getDisksToOperate() {
        return disks.stream()
                .filter(d -> d.getStorageDomain().getIsChangable())
                .collect(Collectors.toList());
    }

    protected void updateTargetStorageDomains() {
        // Add the intersection of disks target domains to targetStorageDomains
        List<DiskModel> disksToOperate = getDisksToOperate();
        if (disksToOperate == null || disksToOperate.isEmpty()) {
            return;
        }

        Set<StorageDomain> commonSDs = new HashSet<>(disksToOperate.get(0).getStorageDomain().getItems());
        disksToOperate.stream()
                .map(diskModel -> diskModel.getStorageDomain().getItems())
                .forEach(commonSDs::retainAll);

        commonSDs.add(mixedStorageDomain);
        targetStorageDomains.setItems(commonSDs);
        targetStorageDomains.setSelectedItem(mixedStorageDomain);

        // Add event listener to update disk models
        Event<EventArgs> selectedItemChangedEvent = targetStorageDomains.getSelectedItemChangedEvent();
        selectedItemChangedEvent.addListener((ev, sender, args) -> {
            StorageDomain selectedSD = targetStorageDomains.getSelectedItem();
            if (targetStorageDomains.getItems() == null || selectedSD == mixedStorageDomain) {
                return;
            }

            // Set disks target domain according to the selected storage domain
            disksToOperate.forEach(diskModel -> diskModel.getStorageDomain().setSelectedItem(selectedSD));
        });
    }

    private void updateSelectedTargetStorage() {
        List<DiskModel> disksToOperate = getDisksToOperate();
        if (!targetStorageDomains.getIsAvailable() || targetStorageDomains.getItems() == null) {
            return;
        }

        boolean sameTargetDomainForAllDisks =
                disksToOperate.stream().map(d -> d.getStorageDomain().getSelectedItem())
                        .distinct()
                        .count() == 1;

        targetStorageDomains.setSelectedItem(sameTargetDomainForAllDisks ?
                disksToOperate.get(0).getStorageDomain().getSelectedItem() :
                mixedStorageDomain);
    }

    @Override
    public void validateEntity(IValidation[] validations) {
        super.validateEntity(validations);

        if (getDisks() == null) {
            return;
        }

        boolean isModelValid = true;
        for (DiskModel diskModel : getDisks()) {
            ListModel<StorageDomain> diskStorageDomains = diskModel.getStorageDomain();
            if (!diskStorageDomains.getItems().iterator().hasNext() || diskStorageDomains.getSelectedItem() == null) {
                diskModel.getStorageDomain().getInvalidityReasons().add(
                        constants.storageDomainMustBeSpecifiedInvalidReason());
                diskModel.getStorageDomain().setIsValid(false);
                isModelValid = false;
            }
            diskModel.getAlias().validateEntity(new IValidation[] {
                    new NotEmptyValidation(),
                    new I18NNameValidation(),
                    new LengthValidation(255)
            });
            isModelValid = isModelValid && diskModel.getAlias().getIsValid();
        }
        setIsValid(isModelValid);
    }

    public void setIsThinProvisioning(boolean isThinProvisioning) {
        if (this.isThinProvisioning != isThinProvisioning) {
            this.isThinProvisioning = isThinProvisioning;
            onPropertyChanged(new PropertyChangedEventArgs(THIN_PROVISIONING));
        }
    }

    public void setIsVolumeTypeChangeable(boolean isVolumeTypeChangeable) {
        if (this.isVolumeTypeChangeable != isVolumeTypeChangeable) {
            this.isVolumeTypeChangeable = isVolumeTypeChangeable;
            onPropertyChanged(new PropertyChangedEventArgs(VOLUME_TYPE));
        }
    }

    public void setIsVolumeFormatChangeable(boolean isVolumeFormatChangeable) {
        if (this.isVolumeFormatChangeable != isVolumeFormatChangeable) {
            this.isVolumeFormatChangeable = isVolumeFormatChangeable;
            onPropertyChanged(new PropertyChangedEventArgs(VOLUME_FORMAT));
        }
    }

    public boolean isSourceAvailable() {
        return isSourceStorageDomainAvailable || isSourceStorageDomainNameAvailable;
    }

    public void initializeAutoSelectTarget(boolean changeable, boolean value) {
        getDiskAllocationTargetEnabled().setIsAvailable(true);
        getDiskAllocationTargetEnabled().setIsChangeable(changeable);
        getDiskAllocationTargetEnabled().setEntity(value);
        updateTargetChangeable(changeable);
        getDiskAllocationTargetEnabled().getEntityChangedEvent()
                .addListener((ev, sender, args) -> updateTargetChangeable(!getDiskAllocationTargetEnabled().getEntity()));
    }

    @Override
    public void cleanup() {
        if (disks != null) {
            for (DiskModel diskModel : disks) {
                diskModel.cleanup();
            }
        }
        super.cleanup();
    }
}
