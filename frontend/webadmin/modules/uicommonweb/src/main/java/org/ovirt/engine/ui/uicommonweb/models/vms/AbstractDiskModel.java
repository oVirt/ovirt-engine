package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.BusinessEntitiesDefinitions;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.CinderVolumeType;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskInterface;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.LunDisk;
import org.ovirt.engine.core.common.businessentities.storage.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.storage.ScsiGenericIO;
import org.ovirt.engine.core.common.businessentities.storage.StorageType;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.HasEntity;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageModel;
import org.ovirt.engine.ui.uicommonweb.validation.I18NNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyQuotaValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.SpecialAsciiI18NOrNoneValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.UIConstants;

public abstract class AbstractDiskModel extends DiskModel {
    protected static final UIConstants constants = ConstantsManager.getInstance().getConstants();

    private EntityModel<Boolean> isWipeAfterDelete;
    private EntityModel<Boolean> isShareable;
    private EntityModel<Boolean> isPlugged;
    private EntityModel<Boolean> isReadOnly;
    private EntityModel<Boolean> isDirectLunDiskAvaialable;
    private EntityModel<Boolean> isUsingScsiReservation;
    private EntityModel<Boolean> isScsiPassthrough;
    private EntityModel<Boolean> isSgIoUnfiltered;
    private EntityModel<String> sizeExtend;
    private EntityModel<DiskStorageType> diskStorageType;
    private EntityModel<Boolean> isModelDisabled;

    private ListModel<StorageType> storageType;
    private ListModel<VDS> host;
    private ListModel<StoragePool> dataCenter;
    private ListModel<String> cinderVolumeType;

    private SanStorageModel sanStorageModel;
    private boolean previousIsQuotaAvailable;

    private SystemTreeItemModel systemTreeSelectedItem;
    private UICommand cancelCommand;

    private StorageModel storageModel;

    public EntityModel<Boolean> getIsWipeAfterDelete() {
        return isWipeAfterDelete;
    }

    public void setIsWipeAfterDelete(EntityModel<Boolean> isWipeAfterDelete) {
        this.isWipeAfterDelete = isWipeAfterDelete;
    }

    public EntityModel<Boolean> getIsShareable() {
        return isShareable;
    }

    public void setIsShareable(EntityModel<Boolean> isShareable) {
        this.isShareable = isShareable;
    }

    public EntityModel<Boolean> getIsPlugged() {
        return isPlugged;
    }

    public void setIsPlugged(EntityModel<Boolean> isPlugged) {
        this.isPlugged = isPlugged;
    }

    public EntityModel<Boolean> getIsReadOnly() {
        return isReadOnly;
    }

    public void setIsReadOnly(EntityModel<Boolean> isReadOnly) {
        this.isReadOnly = isReadOnly;
    }

    public EntityModel<Boolean> getIsDirectLunDiskAvaialable() {
        return isDirectLunDiskAvaialable;
    }

    public void setIsDirectLunDiskAvaialable(EntityModel<Boolean> isDirectLunDiskAvaialable) {
        this.isDirectLunDiskAvaialable = isDirectLunDiskAvaialable;
    }

    public EntityModel<Boolean> getIsUsingScsiReservation() {
        return isUsingScsiReservation;
    }

    public void setIsUsingScsiReservation(EntityModel<Boolean> isUsingScsiReservation) {
        this.isUsingScsiReservation = isUsingScsiReservation;
    }

    public EntityModel<Boolean> getIsScsiPassthrough() {
        return isScsiPassthrough;
    }

    public void setIsScsiPassthrough(EntityModel<Boolean> isScsiPassthrough) {
        this.isScsiPassthrough = isScsiPassthrough;
    }

    public EntityModel<Boolean> getIsSgIoUnfiltered() {
        return isSgIoUnfiltered;
    }

    public void setIsSgIoUnfiltered(EntityModel<Boolean> isSgIoUnfiltered) {
        this.isSgIoUnfiltered = isSgIoUnfiltered;
    }

    public ListModel<StorageType> getStorageType() {
        return storageType;
    }

    public void setStorageType(ListModel<StorageType> storageType) {
        this.storageType = storageType;
    }

    public ListModel<VDS> getHost() {
        return host;
    }

    public void setHost(ListModel<VDS> host) {
        this.host = host;
    }

    public ListModel<StoragePool> getDataCenter() {
        return dataCenter;
    }

    public void setDataCenter(ListModel<StoragePool> dataCenter) {
        this.dataCenter = dataCenter;
    }

    public ListModel<String> getCinderVolumeType() {
        return cinderVolumeType;
    }

    public void setCinderVolumeType(ListModel<String> cinderVolumeType) {
        this.cinderVolumeType = cinderVolumeType;
    }

    public SanStorageModel getSanStorageModel() {
        return sanStorageModel;
    }

    public void setSanStorageModel(SanStorageModel sanStorageModel) {
        this.sanStorageModel = sanStorageModel;
    }

    public SystemTreeItemModel getSystemTreeSelectedItem() {
        return systemTreeSelectedItem;
    }

    public void setSystemTreeSelectedItem(SystemTreeItemModel systemTreeSelectedItem) {
        this.systemTreeSelectedItem = systemTreeSelectedItem;
    }

    public EntityModel<String> getSizeExtend() {
        return sizeExtend;
    }

    public void setSizeExtend(EntityModel<String> sizeExtend) {
        this.sizeExtend = sizeExtend;
    }

    public EntityModel<DiskStorageType> getDiskStorageType() {
        return diskStorageType;
    }

    public void setDiskStorageType(EntityModel<DiskStorageType> diskStorageType) {
        this.diskStorageType = diskStorageType;
    }

    private EntityModel<Boolean> isVirtioScsiEnabled;

    public EntityModel<Boolean> getIsVirtioScsiEnabled() {
        return isVirtioScsiEnabled;
    }

    public void setIsVirtioScsiEnabled(EntityModel<Boolean> virtioScsiEnabled) {
        this.isVirtioScsiEnabled = virtioScsiEnabled;
    }

    public EntityModel<Boolean> getIsModelDisabled() {
        return isModelDisabled;
    }

    public void setIsModelDisabled(EntityModel<Boolean> isModelDisabled) {
        this.isModelDisabled = isModelDisabled;
    }

    @Override
    public UICommand getCancelCommand() {
        return cancelCommand;
    }

    public void setCancelCommand(UICommand cancelCommand) {
        this.cancelCommand = cancelCommand;
    }

    public AbstractDiskModel() {
        setSizeExtend(new EntityModel<String>());
        getSizeExtend().setEntity("0");  //$NON-NLS-1$

        setIsWipeAfterDelete(new EntityModel<Boolean>());
        getIsWipeAfterDelete().setEntity(false);

        setIsShareable(new EntityModel<Boolean>());
        getIsShareable().setEntity(false);

        setIsPlugged(new EntityModel<Boolean>());
        getIsPlugged().setEntity(true);
        getIsPlugged().setIsAvailable(false);

        setIsReadOnly(new EntityModel<Boolean>());
        getIsReadOnly().setEntity(false);
        getIsReadOnly().getEntityChangedEvent().addListener(this);

        setIsUsingScsiReservation(new EntityModel<Boolean>());
        getIsUsingScsiReservation().setEntity(false);

        setIsScsiPassthrough(new EntityModel<Boolean>());
        getIsScsiPassthrough().setIsAvailable(false);
        getIsScsiPassthrough().setEntity(true);
        getIsScsiPassthrough().getEntityChangedEvent().addListener(this);

        setIsSgIoUnfiltered(new EntityModel<Boolean>());
        getIsSgIoUnfiltered().setIsAvailable(false);
        getIsSgIoUnfiltered().setEntity(false);
        getIsSgIoUnfiltered().getEntityChangedEvent().addListener(this);

        setDiskStorageType(new EntityModel<DiskStorageType>());
        getDiskStorageType().setEntity(DiskStorageType.IMAGE);
        getDiskStorageType().getEntityChangedEvent().addListener(this);

        setIsDirectLunDiskAvaialable(new EntityModel<Boolean>());
        getIsDirectLunDiskAvaialable().setEntity(true);

        setDataCenter(new ListModel<StoragePool>());
        getDataCenter().setIsAvailable(false);
        getDataCenter().getSelectedItemChangedEvent().addListener(this);

        getStorageDomain().getSelectedItemChangedEvent().addListener(this);

        setStorageType(new ListModel<StorageType>());
        getStorageType().setIsAvailable(false);
        getStorageType().setItems(AsyncDataProvider.getInstance().getStorageTypeList());
        getStorageType().getSelectedItemChangedEvent().addListener(this);

        setHost(new ListModel<VDS>());
        getHost().setIsAvailable(false);

        getVolumeType().getSelectedItemChangedEvent().addListener(this);
        getDiskInterface().getSelectedItemChangedEvent().addListener(this);

        setIsVirtioScsiEnabled(new EntityModel<Boolean>());

        setCinderVolumeType(new ListModel<String>());
        getCinderVolumeType().setIsAvailable(false);

        setIsModelDisabled(new EntityModel<Boolean>(false));
    }

    public abstract boolean getIsNew();

    public boolean getIsFloating() {
        return getVm() == null;
    }

    protected abstract boolean isDatacenterAvailable(StoragePool dataCenter);

    protected abstract DiskImage getDiskImage();

    protected abstract LunDisk getLunDisk();

    protected abstract CinderDisk getCinderDisk();

    public void setDefaultInterface() {
        Guid vmId = getVmId();
        if (Guid.isNullOrEmpty(vmId) || getDisk() == null) {
            getDiskInterface().setSelectedItem(DiskInterface.VirtIO);
        } else {
            getDiskInterface().setSelectedItem(getDisk().getDiskVmElementForVm(vmId).getDiskInterface());
        }
    }

    protected abstract void updateVolumeType(StorageType storageType);

    public boolean isEditEnabled() {
        return (getIsFloating() || getIsNew() || getVm().isDown() || !getDisk().getPlugged()) && getIsChangable();
    }

    @Override
    public void initialize() {
        commonInitialize();
    }

    public void initialize(List<DiskModel> currentDisks) {
        commonInitialize();
        updateBootableFrom(currentDisks != null ? currentDisks : new ArrayList<DiskModel>());
    }

    private void commonInitialize() {
        super.initialize();

        // Create and set commands
        UICommand onSaveCommand = new UICommand("OnSave", this); //$NON-NLS-1$
        onSaveCommand.setTitle(constants.ok());
        onSaveCommand.setIsDefault(true);
        getCommands().add(onSaveCommand);
        getCommands().add(getCancelCommand());

        // Update data
        if (getVm() != null) {
            updateBootableDiskAvailable();
            getIsUsingScsiReservation().setIsAvailable(true);
        }
        updateDatacenters();
    }

    protected void updateStorageDomains(final StoragePool datacenter) {
        AsyncDataProvider.getInstance().getPermittedStorageDomainsByStoragePoolId(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                AbstractDiskModel diskModel = (AbstractDiskModel) target;
                ArrayList<StorageDomain> storageDomains = (ArrayList<StorageDomain>) returnValue;
                ArrayList<StorageDomain> filteredStorageDomains = new ArrayList<>();
                switch (getDiskStorageType().getEntity()) {
                    case IMAGE:
                        filteredStorageDomains.addAll(Linq.filterStorageDomainsByStorageDomainType(
                                storageDomains, StorageDomainType.Master));
                        filteredStorageDomains.addAll(Linq.filterStorageDomainsByStorageDomainType(
                                storageDomains, StorageDomainType.Data));
                        break;
                    case CINDER:
                        filteredStorageDomains.addAll(Linq.filterStorageDomainsByStorageType(
                                storageDomains, StorageType.CINDER));
                        break;
                }

                filteredStorageDomains = (ArrayList<StorageDomain>) Linq.filterStorageDomainsByStorageStatus(
                        filteredStorageDomains, StorageDomainStatus.Active);
                Collections.sort(filteredStorageDomains, new NameableComparator());
                StorageDomain storage = Linq.firstOrNull(filteredStorageDomains);
                diskModel.getStorageDomain().setItems(filteredStorageDomains, storage);
                if (storage == null) {
                    switch (getDiskStorageType().getEntity()) {
                        case IMAGE:
                            diskModel.setMessage(constants.noActiveStorageDomainsInDC());
                            diskModel.getIsModelDisabled().setEntity(true);
                            break;
                        case CINDER:
                            diskModel.setMessage(constants.noCinderStorageDomainsInDC());
                            diskModel.getIsModelDisabled().setEntity(true);
                            break;
                    }
                }
            }
        }), datacenter.getId(), ActionGroup.CREATE_DISK);
    }

    private void updateHosts(StoragePool datacenter) {
        AsyncDataProvider.getInstance().getHostListByDataCenter(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                AbstractDiskModel diskModel = (AbstractDiskModel) target;
                Iterable<VDS> hosts = (Iterable<VDS>) returnValue;
                ArrayList<VDS> filteredHosts = new ArrayList<>();

                for (VDS host : hosts) {
                    if (isHostAvailable(host)) {
                        filteredHosts.add(host);
                    }
                }

                diskModel.getHost().setItems(filteredHosts);
            }
        }), datacenter.getId());
    }

    private void updateDatacenters() {
        boolean isInVm = getVm() != null;
        getDataCenter().setIsAvailable(!isInVm);
        setMessage(""); //$NON-NLS-1$
        getIsModelDisabled().setEntity(false);

        if (isInVm) {
            AsyncDataProvider.getInstance().getDataCenterById(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void onSuccess(Object target, Object returnValue) {
                    AbstractDiskModel diskModel = (AbstractDiskModel) target;
                    StoragePool dataCenter = (StoragePool) returnValue;
                    ArrayList<StoragePool> dataCenters = new ArrayList<>();

                    if (isDatacenterAvailable(dataCenter)) {
                        dataCenters.add(dataCenter);
                    }

                    diskModel.getDataCenter().setItems(dataCenters, Linq.firstOrNull(dataCenters));

                    if (dataCenters.isEmpty()) {
                        diskModel.setMessage(constants.relevantDCnotActive());
                        diskModel.getIsModelDisabled().setEntity(true);
                    }
                }
            }), getVm().getStoragePoolId());
            updateBootableDiskAvailable();
        }
        else {
            AsyncDataProvider.getInstance().getDataCenterList(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void onSuccess(Object target, Object returnValue) {
                    AbstractDiskModel diskModel = (AbstractDiskModel) target;
                    ArrayList<StoragePool> dataCenters = (ArrayList<StoragePool>) returnValue;
                    ArrayList<StoragePool> filteredDataCenters = new ArrayList<>();

                    for (StoragePool dataCenter : dataCenters) {
                        if (isDatacenterAvailable(dataCenter)) {
                            filteredDataCenters.add(dataCenter);
                        }
                    }

                    diskModel.getDataCenter().setItems(filteredDataCenters);

                    if (filteredDataCenters.isEmpty()) {
                        diskModel.setMessage(constants.noActiveDataCenters());
                        diskModel.getIsModelDisabled().setEntity(true);
                    }
                }
            }));
        }
    }

    protected void updateBootableDiskAvailable() {
        AsyncDataProvider.getInstance().getVmDiskList(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                ArrayList<Disk> disks = (ArrayList<Disk>) returnValue;
                updateCanSetBoot(disks);
            }
        }), getVm().getId());
    }

    public void updateCanSetBoot(List<Disk> vmDisks) {
        getIsBootable().setIsChangeable(true);
        if (getDisk() == null || !getDisk().isDiskSnapshot()) {
            for (Disk disk : vmDisks) {
                if (disk.getDiskVmElementForVm(getVmId()).isBoot() && !disk.equals(getDisk())) {
                    getIsBootable().setEntity(false);
                    if (!disk.isDiskSnapshot()) {
                        getIsBootable().setChangeProhibitionReason(constants.onlyOneBootableDisk());
                        getIsBootable().setIsChangeable(false);
                        break;
                    }
                }
            }
        }
    }

    public void updateBootableFrom(List<DiskModel> vmDisks) {
        getIsBootable().setIsChangeable(true);
        if (getDisk() == null || !getDisk().isDiskSnapshot()) {
            for (DiskModel disk : vmDisks) {
                if (disk.getDiskVmElement().isBoot() && !disk.getDisk().equals(getDisk())) {
                    getIsBootable().setEntity(false);
                    if (!disk.getDisk().isDiskSnapshot()) {
                        getIsBootable().setChangeProhibitionReason(constants.onlyOneBootableDisk());
                        getIsBootable().setIsChangeable(false);
                        break;
                    }
                }
            }
        }
    }

    private void updateShareableDiskEnabled() {
        getIsShareable().setChangeProhibitionReason(constants.shareableDiskNotSupported());
        getIsShareable().setIsChangeable(isEditEnabled());
    }

    private void updateDirectLunDiskEnabled() {
        if (getDiskStorageType().getEntity() != DiskStorageType.LUN) {
            return;
        }

        getIsDirectLunDiskAvaialable().setEntity(true);
        setMessage(""); //$NON-NLS-1$
        getIsModelDisabled().setEntity(false);
    }

    private void updateShareable(VolumeType volumeType, StorageType storageType) {
        if (storageType.isBlockDomain() && volumeType == VolumeType.Sparse) {
            getIsShareable().setChangeProhibitionReason(constants.shareableDiskNotSupportedByConfiguration());
            getIsShareable().setIsChangeable(false);
            getIsShareable().setEntity(false);
        }
        else {
            getIsShareable().setIsChangeable(isEditEnabled());
        }
    }

    public void updateInterface(final Version clusterVersion) {
        if (getVm() != null) {
            AsyncDataProvider.getInstance().isVirtioScsiEnabledForVm(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object returnValue1) {
                    getIsVirtioScsiEnabled().setEntity(Boolean.TRUE.equals(returnValue1));

                    updateInterfaceList(clusterVersion);

                }
            }), getVm().getId());
        } else {
            setInterfaces(AsyncDataProvider.getInstance().getDiskInterfaceList());
        }
    }

    public void updateInterfaceList(final Version clusterVersion) {
        AsyncQuery asyncQuery = new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue2) {
                ArrayList<DiskInterface> diskInterfaces = (ArrayList<DiskInterface>) returnValue2;

                if (Boolean.FALSE.equals(getIsVirtioScsiEnabled().getEntity())) {
                    diskInterfaces.remove(DiskInterface.VirtIO_SCSI);
                }

                setInterfaces(diskInterfaces);
            }
        });

        AsyncDataProvider.getInstance().getDiskInterfaceList(getVm().getOs(), clusterVersion, asyncQuery);
    }

    private void setInterfaces(ArrayList<DiskInterface> diskInterfaces) {
        getDiskInterface().setItems(diskInterfaces);
        setDefaultInterface();
    }

    protected void updateCinderVolumeTypes() {
        StorageDomain storageDomain = getStorageDomain().getSelectedItem();
        if (storageDomain == null || storageDomain.getStorageType() != StorageType.CINDER) {
            return;
        }

        AsyncDataProvider.getInstance().getCinderVolumeTypesList(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                AbstractDiskModel diskModel = (AbstractDiskModel) target;
                List<CinderVolumeType> cinderVolumeTypes = (ArrayList<CinderVolumeType>) returnValue;
                List<String> volumeTypesNames = new ArrayList<>();
                for (CinderVolumeType cinderVolumeType : cinderVolumeTypes) {
                    volumeTypesNames.add(cinderVolumeType.getName());
                }
                diskModel.getCinderVolumeType().setItems(volumeTypesNames);
            }
        }), storageDomain.getId());
    }

    private void updateDiskProfiles(StoragePool selectedItem) {
        StorageDomain storageDomain = getStorageDomain().getSelectedItem();
        if (storageDomain == null) {
            return;
        }

        Frontend.getInstance().runQuery(VdcQueryType.GetDiskProfilesByStorageDomainId,
                new IdQueryParameters(storageDomain.getId()),
                new AsyncQuery(this,
                        new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object innerModel, Object value) {
                                AbstractDiskModel.this.setDiskProfilesList((List<DiskProfile>) ((VdcQueryReturnValue) value).getReturnValue());
                            }

                        }));
    }

    private void setDiskProfilesList(List<DiskProfile> diskProfiles) {
        // set disk profiles
        if (diskProfiles != null && !diskProfiles.isEmpty()) {
            getDiskProfile().setItems(diskProfiles);
        }
        // handle disk profile selected item
        Guid defaultProfileId =
                (getDisk() != null && getDisk().getDiskStorageType() == DiskStorageType.IMAGE)? ((DiskImage) getDisk()).getDiskProfileId() : null;
        if (defaultProfileId != null) {
            for (DiskProfile profile : diskProfiles) {
                if (profile.getId().equals(defaultProfileId)) {
                    getDiskProfile().setSelectedItem(profile);
                    return;
                }
            }
            // set dummy disk profile (if not fetched because of permissions, and it's attached to disk.
            DiskProfile diskProfile = new DiskProfile();
            diskProfile.setId(defaultProfileId);
            if (getDisk() != null) {
                diskProfile.setName(getDiskImage().getDiskProfileName());
            }
            diskProfiles.add(diskProfile);
            getDiskProfile().setItems(diskProfiles);
            getDiskProfile().setSelectedItem(diskProfile);
        }
    }

    private void updateQuota(StoragePool datacenter) {
        if (datacenter.getQuotaEnforcementType().equals(QuotaEnforcementTypeEnum.DISABLED) ||
                !getDiskStorageType().getEntity().isInternal()) {
            getQuota().setIsAvailable(false);
            return;
        }

        getQuota().setIsAvailable(true);
        StorageDomain storageDomain = getStorageDomain().getSelectedItem();
        if (storageDomain == null) {
            return;
        }

        final Guid diskQuota = getDisk() != null ? ((DiskImage) getDisk()).getQuotaId() : null;
        final Guid vmQuota = getVm() != null ? getVm().getQuotaId() : null;
        final Guid defaultQuota = diskQuota != null ? diskQuota : vmQuota;

        AsyncDataProvider.getInstance().getAllRelevantQuotasForStorageSorted(new AsyncQuery(
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        List<Quota> quotaList = (List<Quota>) returnValue;
                        if (quotaList != null && !quotaList.isEmpty()) {
                            getQuota().setItems(quotaList);

                            // If list contains default quota, return
                            if (quotaList.get(0).getId().equals(defaultQuota)) {
                                return;
                            }
                        }

                        if (diskQuota != null) {
                            Quota quota = new Quota();
                            quota.setId(diskQuota);
                            quota.setQuotaName(getDiskImage().getQuotaName());

                            if (quotaList == null) {
                                quotaList = new ArrayList<>();
                            }

                            quotaList.add(quota);
                            getQuota().setItems(quotaList);
                            getQuota().setSelectedItem(quota);
                        }
                    }
                }), storageDomain.getId(), defaultQuota);
    }

    private boolean isHostAvailable(VDS host) {
        boolean isStatusUp = host.getStatus() == VDSStatus.Up;

        return isStatusUp;
    }

    protected void diskStorageType_EntityChanged() {
        boolean isInVm = getVm() != null;
        boolean isDiskImage = getDiskStorageType().getEntity() == DiskStorageType.IMAGE;
        boolean isLunDisk = getDiskStorageType().getEntity() == DiskStorageType.LUN;
        boolean isCinderDisk = getDiskStorageType().getEntity() == DiskStorageType.CINDER;

        getSize().setIsAvailable(isDiskImage || isCinderDisk);
        getSizeExtend().setIsAvailable((isDiskImage || isCinderDisk) && !getIsNew());
        getStorageDomain().setIsAvailable(isDiskImage || isCinderDisk);
        getVolumeType().setIsAvailable(isDiskImage);
        getIsWipeAfterDelete().setIsAvailable(isDiskImage);
        getHost().setIsAvailable(isLunDisk);
        getStorageType().setIsAvailable(isLunDisk);
        getDataCenter().setIsAvailable(!isInVm);
        getDiskProfile().setIsAvailable(isDiskImage);
        getCinderVolumeType().setIsAvailable(isCinderDisk);

        if (!isDiskImage) {
            previousIsQuotaAvailable = getQuota().getIsAvailable();
        }

        getQuota().setIsAvailable(isDiskImage ? previousIsQuotaAvailable : false);

        setIsChangeable(true);
        updateDatacenters();
    }

    protected void volumeType_SelectedItemChanged() {
        if (getVolumeType().getSelectedItem() == null || getDataCenter().getSelectedItem() == null
                || getStorageDomain().getSelectedItem() == null) {
            return;
        }

        VolumeType volumeType = getVolumeType().getSelectedItem();
        StorageType storageType = getStorageDomain().getSelectedItem().getStorageType();

        updateShareable(volumeType, storageType);
    }

    private void DiskInterface_SelectedItemChanged() {
        boolean isLunDisk = getDiskStorageType().getEntity() == DiskStorageType.LUN;
        DiskInterface diskInterface = getDiskInterface().getSelectedItem();
        getIsSgIoUnfiltered().setIsAvailable(isLunDisk && DiskInterface.VirtIO_SCSI.equals(diskInterface));
        getIsScsiPassthrough().setIsAvailable(isLunDisk && DiskInterface.VirtIO_SCSI.equals(diskInterface));
        getIsUsingScsiReservation().setIsAvailable(isLunDisk && DiskInterface.VirtIO_SCSI.equals(diskInterface));

        updateScsiPassthroughChangeability();
        updateScsiReservationChangeability();
        updateReadOnlyChangeability();
        updatePlugChangeability();
    }

    protected void updateScsiPassthroughChangeability() {
        getIsScsiPassthrough().setIsChangeable(!getIsReadOnly().getEntity() && isEditEnabled());
        getIsScsiPassthrough().setChangeProhibitionReason(constants.cannotEnableScsiPassthroughForLunReadOnlyDisk());
        updateSgIoUnfilteredChangeability();
    }

    protected void updateSgIoUnfilteredChangeability() {
        if (!getIsScsiPassthrough().getEntity()) {
            getIsSgIoUnfiltered().setChangeProhibitionReason(constants.cannotEnableSgioWhenScsiPassthroughDisabled());
            getIsSgIoUnfiltered().setIsChangeable(false);
            getIsSgIoUnfiltered().setEntity(false);
            return;
        }
        if (isEditEnabled()) {
            getIsSgIoUnfiltered().setChangeProhibitionReason(null);
        }
        getIsSgIoUnfiltered().setIsChangeable(isEditEnabled());
    }

    protected void updateScsiReservationChangeability() {
        boolean isSgioUnfiltered = getIsSgIoUnfiltered().getEntity();
        if (getVm() != null) {
            if (isSgioUnfiltered) {
                getIsUsingScsiReservation().setIsChangeable(true);
            } else {
                getIsUsingScsiReservation().setIsChangeable(false);
                getIsUsingScsiReservation().setEntity(false);
            }
        } else {
            getIsUsingScsiReservation().setIsAvailable(false);
            getIsUsingScsiReservation().setEntity(false);
            getIsUsingScsiReservation().setIsChangeable(false);
        }
    }

    protected void updateReadOnlyChangeability() {
        if (getVm() == null) { // read-only is a characteristic of a VM device, not a disk
            getIsReadOnly().setIsAvailable(false);
            getIsReadOnly().setEntity(false);
            getIsReadOnly().setIsChangeable(false);
            return;
        }

        DiskInterface diskInterface = getDiskInterface().getSelectedItem();

        if (diskInterface == DiskInterface.IDE) {
            getIsReadOnly().setChangeProhibitionReason(constants.cannotEnableIdeInterfaceForReadOnlyDisk());
            getIsReadOnly().setIsChangeable(false);
            getIsReadOnly().setEntity(false);
            return;
        }

        boolean isDirectLUN = getDiskStorageType().getEntity() == DiskStorageType.LUN;
        boolean isScsiPassthrough = getIsScsiPassthrough().getEntity();
        if (diskInterface == DiskInterface.VirtIO_SCSI && isDirectLUN && isScsiPassthrough) {
            getIsReadOnly().setChangeProhibitionReason(constants.cannotEnableReadonlyWhenScsiPassthroughEnabled());
            getIsReadOnly().setIsChangeable(false);
            getIsReadOnly().setEntity(false);
            return;
        }

        if (isVmAttachedToPool() && !getIsNew()) {
            getIsReadOnly().setIsChangeable(false);
        } else {
            getIsReadOnly().setIsChangeable(isEditEnabled());
        }
        getIsReadOnly().setEntity(getIsNew() ? Boolean.FALSE : getDisk().getReadOnly());
    }

    protected void updateWipeAfterDeleteChangeability() {
        getIsWipeAfterDelete().setIsChangeable(!isVmAttachedToPool());
    }

    private void updatePlugChangeability() {
        if (getVm() == null) { // No point in updating plug to VM if there's no VM
            return;
        }

        DiskInterface diskInterface = getDiskInterface().getSelectedItem();
        boolean isVmRunning = getVm() != null && getVm().getStatus() != VMStatus.Down;

        if (DiskInterface.IDE.equals(diskInterface) && isVmRunning) {
            getIsPlugged().setChangeProhibitionReason(constants.cannotHotPlugDiskWithIdeInterface());
            getIsPlugged().setIsChangeable(false);
            getIsPlugged().setEntity(false);
        }
        else {
            if (!canDiskBePlugged(getVm())) {
                getIsPlugged().setChangeProhibitionReason(constants.cannotPlugDiskIncorrectVmStatus());
                getIsPlugged().setIsChangeable(false);
                getIsPlugged().setEntity(false);
            }
            else {
                getIsPlugged().setIsChangeable(isEditEnabled());
                getIsPlugged().setEntity(true);
            }
        }
    }

    private void updateDiskSize(DiskImage diskImage) {
        long sizeToAddInGigabytes = Long.parseLong(getSizeExtend().getEntity());
        if (sizeToAddInGigabytes > 0) {
            diskImage.setSizeInGigabytes(diskImage.getSizeInGigabytes() + sizeToAddInGigabytes);
        }
    }

    private boolean canDiskBePlugged(VM vm) {
        return vm.getStatus() == VMStatus.Up || vm.getStatus() == VMStatus.Down || vm.getStatus() == VMStatus.Paused;
    }

    private boolean isVmAttachedToPool() {
        return getVm() != null && getVm().getVmPoolId() != null;
    }

    protected void datacenter_SelectedItemChanged() {
        StoragePool datacenter = getDataCenter().getSelectedItem();
        boolean isInVm = getVm() != null;

        if (datacenter == null) {
            return;
        }

        setMessage(null);
        getIsModelDisabled().setEntity(false);
        updateShareableDiskEnabled();
        updateDirectLunDiskEnabled();
        updateInterface(isInVm ? getVm().getCompatibilityVersion() : null);

        switch (getDiskStorageType().getEntity()) {
            case IMAGE:
            case CINDER:
                updateStorageDomains(datacenter);
                break;
            default:
                updateHosts(datacenter);
                break;
        }
    }

    private void storageDomain_SelectedItemChanged() {
        StorageDomain selectedStorage = getStorageDomain().getSelectedItem();
        if (selectedStorage != null) {
            updateVolumeType(selectedStorage.getStorageType());
            if (getIsNew()) {
                getIsWipeAfterDelete().setEntity(selectedStorage.getWipeAfterDelete());
            }
        }
        updateQuota(getDataCenter().getSelectedItem());
        updateDiskProfiles(getDataCenter().getSelectedItem());
        updateCinderVolumeTypes();
    }

    public boolean validate() {
        getDescription().validateEntity(new IValidation[] {
                new SpecialAsciiI18NOrNoneValidation(),
                new LengthValidation(BusinessEntitiesDefinitions.DISK_DESCRIPTION_MAX_SIZE)});

        getAlias().validateEntity(getDiskAliasValidations());

        StoragePool dataCenter = getDataCenter().getSelectedItem();
        if (dataCenter != null && dataCenter.getQuotaEnforcementType() == QuotaEnforcementTypeEnum.HARD_ENFORCEMENT) {
            getQuota().validateSelectedItem(new IValidation[] { new NotEmptyQuotaValidation() });
        }

        if (getCinderVolumeType().getIsAvailable()) {
            getCinderVolumeType().validateSelectedItem(new IValidation[]{new NotEmptyValidation()});
        }

        return getAlias().getIsValid() && getDescription().getIsValid() && getQuota().getIsValid()
                && getDiskInterface().getIsValid() && getCinderVolumeType().getIsValid();
    }

    private IValidation[] getDiskAliasValidations() {
        Collection<IValidation> diskAliasValidations = new ArrayList<>(Arrays.asList(
                new I18NNameValidation(), new LengthValidation(BusinessEntitiesDefinitions.GENERAL_NAME_SIZE)));
        if (getVm() == null) {
            diskAliasValidations.add(new NotEmptyValidation());
        }
        return diskAliasValidations.toArray(new IValidation[diskAliasValidations.size()]);
    }

    protected void forceCreationWarning(ArrayList<String> usedLunsMessages) {
        ConfirmationModel confirmationModel = new ConfirmationModel();
        setConfirmWindow(confirmationModel);

        confirmationModel.setTitle(constants.forceStorageDomainCreation());
        confirmationModel.setMessage(constants.lunsAlreadyPartOfSD());
        confirmationModel.setHelpTag(HelpTag.force_lun_disk_creation);
        confirmationModel.setHashName("force_lun_disk_creation"); //$NON-NLS-1$
        confirmationModel.setItems(usedLunsMessages);

        UICommand forceSaveCommand = new UICommand("OnForceSave", this); //$NON-NLS-1$
        forceSaveCommand.setTitle(constants.ok());
        forceSaveCommand.setIsDefault(true);
        confirmationModel.getCommands().add(forceSaveCommand);

        UICommand cancelconfirmCommand = new UICommand("CancelConfirm", this); //$NON-NLS-1$
        cancelconfirmCommand.setTitle(constants.cancel());
        cancelconfirmCommand.setIsCancel(true);
        confirmationModel.getCommands().add(cancelconfirmCommand);
    }

    private void onForceSave() {
        ConfirmationModel confirmationModel = (ConfirmationModel) getConfirmWindow();
        if (confirmationModel != null && !confirmationModel.validate()) {
            return;
        }
        cancelConfirm();

        getSanStorageModel().setForce(true);
        onSave();
    }

    private void cancelConfirm() {
        setConfirmWindow(null);
    }

    protected void cancel() {
        getCancelCommand().execute();
    }

    protected Guid getVmId() {
        return getVm() != null ? getVm().getId() : Guid.Empty;
    }

    public void onSave() {
        flush();
        store(null);
    }

    public void flush() {
        switch (getDiskStorageType().getEntity()) {
            case LUN:
                LunDisk lunDisk = getLunDisk();
                DiskInterface diskInterface = getDiskInterface().getSelectedItem();
                if (DiskInterface.VirtIO_SCSI.equals(diskInterface)) {
                    lunDisk.setSgio(!getIsScsiPassthrough().getEntity() ? null :
                            getIsSgIoUnfiltered().getEntity() ?
                                    ScsiGenericIO.UNFILTERED : ScsiGenericIO.FILTERED);
                    lunDisk.setUsingScsiReservation(getIsUsingScsiReservation().getEntity());
                } else {
                    getIsScsiPassthrough().setEntity(false);
                    lunDisk.setSgio(null);
                    lunDisk.setUsingScsiReservation(false);
                }
                setDisk(lunDisk);
                break;
            case CINDER:
                CinderDisk cinderDisk = getCinderDisk();
                updateQuota(cinderDisk);
                updateDiskSize(cinderDisk);
                setDisk(cinderDisk);
                break;
            case IMAGE:
                DiskImage diskImage = getDiskImage();
                // For a long time it was possible to delete all disk profiles
                if (getDiskProfile().getSelectedItem() != null) {
                    diskImage.setDiskProfileId(getDiskProfile().getSelectedItem().getId());
                }
                updateQuota(diskImage);
                updateDiskSize(diskImage);
                setDisk(diskImage);
                break;
        }

        getDisk().setDiskAlias(getAlias().getEntity());
        getDisk().setDiskDescription(getDescription().getEntity());
        getDisk().setWipeAfterDelete(getIsWipeAfterDelete().getEntity());
        getDisk().setShareable(getIsShareable().getEntity());
        getDisk().setPlugged(getIsPlugged().getEntity());
        getDisk().setPropagateErrors(PropagateErrors.Off);
        getDisk().setReadOnly(getIsReadOnly().getIsAvailable() ? getIsReadOnly().getEntity() : null);

        if (getVm() != null) {
            getDiskVmElement().setBoot(getIsBootable().getEntity());
            getDiskVmElement().setDiskInterface(getDiskInterface().getSelectedItem());
        }
    }

    private void updateQuota(DiskImage diskImage) {
        if (getQuota().getIsAvailable() && getQuota().getSelectedItem() != null) {
            diskImage.setQuotaId(getQuota().getSelectedItem().getId());
        }
    }

    public abstract void store(IFrontendActionAsyncCallback callback);

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

        if ("OnSave".equals(command.getName())) { //$NON-NLS-1$
            onSave();
        }
        if ("OnForceSave".equals(command.getName())) { //$NON-NLS-1$
            onForceSave();
        }
        else if ("CancelConfirm".equals(command.getName())) { //$NON-NLS-1$
            cancelConfirm();
        }
    }

    @Override
    public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(HasEntity.entityChangedEventDefinition)) {
            if (sender == getIsReadOnly()) {
                updateScsiPassthroughChangeability();
            } else if (sender == getIsScsiPassthrough()) {
                updateScsiPassthroughChangeability();
                updateSgIoUnfilteredChangeability();
                updateReadOnlyChangeability();
                updateScsiReservationChangeability();
            } else if (sender == getIsSgIoUnfiltered()) {
                updateScsiReservationChangeability();
            } else if (sender == getDiskStorageType()) {
                diskStorageType_EntityChanged();
            }
        }
        else if (ev.matchesDefinition(ListModel.selectedItemChangedEventDefinition)) {
            if (sender == getVolumeType()) {
                volumeType_SelectedItemChanged();
            } else if (sender == getDiskInterface()) {
                DiskInterface_SelectedItemChanged();
            } else if (sender == getDataCenter()) {
                datacenter_SelectedItemChanged();
            } else if (sender == getStorageDomain()) {
                storageDomain_SelectedItemChanged();
            }
        }
    }

    public StorageModel getStorageModel() {
        return storageModel;
    }

    public void setStorageModel(StorageModel storageModel) {
        this.storageModel = storageModel;
    }
}
