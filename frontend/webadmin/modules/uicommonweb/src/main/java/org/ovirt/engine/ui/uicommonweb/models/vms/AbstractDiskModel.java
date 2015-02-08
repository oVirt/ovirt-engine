package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.ScsiGenericIO;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
import org.ovirt.engine.core.common.businessentities.profiles.DiskProfile;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
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
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModel;
import org.ovirt.engine.ui.uicommonweb.validation.I18NNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyQuotaValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.SpecialAsciiI18NOrNoneValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.UIConstants;

public abstract class AbstractDiskModel extends DiskModel
{
    protected static final UIConstants CONSTANTS = ConstantsManager.getInstance().getConstants();

    private EntityModel<Boolean> isWipeAfterDelete;
    private EntityModel<Boolean> isBootable;
    private EntityModel<Boolean> isShareable;
    private EntityModel<Boolean> isPlugged;
    private EntityModel<Boolean> isReadOnly;
    private EntityModel<Boolean> isDirectLunDiskAvaialable;
    private EntityModel<Boolean> isScsiPassthrough;
    private EntityModel<Boolean> isSgIoUnfiltered;
    private EntityModel<String> sizeExtend;
    private EntityModel<DiskStorageType> diskStorageType;

    private ListModel<StorageType> storageType;
    private ListModel<VDS> host;
    private ListModel<StoragePool> dataCenter;

    private SanStorageModel sanStorageModel;
    private VolumeFormat volumeFormat;
    private boolean previousIsQuotaAvailable;

    private SystemTreeItemModel systemTreeSelectedItem;
    private UICommand cancelCommand;

    public EntityModel<Boolean> getIsWipeAfterDelete() {
        return isWipeAfterDelete;
    }

    public void setIsWipeAfterDelete(EntityModel<Boolean> isWipeAfterDelete) {
        this.isWipeAfterDelete = isWipeAfterDelete;
    }

    public EntityModel<Boolean> getIsBootable() {
        return isBootable;
    }

    public void setIsBootable(EntityModel<Boolean> isBootable) {
        this.isBootable = isBootable;
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
    public SanStorageModel getSanStorageModel() {
        return sanStorageModel;
    }

    public void setSanStorageModel(SanStorageModel sanStorageModel) {
        this.sanStorageModel = sanStorageModel;
    }

    public VolumeFormat getVolumeFormat() {
        return volumeFormat;
    }

    public void setVolumeFormat(VolumeFormat volumeFormat) {
        this.volumeFormat = volumeFormat;
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

        setIsBootable(new EntityModel<Boolean>());
        getIsBootable().setEntity(false);

        setIsShareable(new EntityModel<Boolean>());
        getIsShareable().setEntity(false);

        setIsPlugged(new EntityModel<Boolean>());
        getIsPlugged().setEntity(true);
        getIsPlugged().setIsAvailable(false);

        setIsReadOnly(new EntityModel<Boolean>());
        getIsReadOnly().setEntity(false);
        getIsReadOnly().getEntityChangedEvent().addListener(this);

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
    }

    public abstract boolean getIsNew();

    public boolean getIsFloating() {
        return getVm() == null;
    }

    protected abstract boolean isDatacenterAvailable(StoragePool dataCenter);

    protected abstract DiskImage getDiskImage();

    protected abstract LunDisk getLunDisk();

    protected abstract void setDefaultInterface();

    protected abstract void updateVolumeType(StorageType storageType);

    protected boolean isEditEnabled() {
        return getIsFloating() || getIsNew() || getVm().isDown() || !getDisk().getPlugged();
    }

    @Override
    public void initialize() {
        super.initialize();

        // Create and set commands
        UICommand onSaveCommand = new UICommand("OnSave", this); //$NON-NLS-1$
        onSaveCommand.setTitle(CONSTANTS.ok());
        onSaveCommand.setIsDefault(true);
        getCommands().add(onSaveCommand);
        getCommands().add(getCancelCommand());

        // Update data
        if (getVm() != null) {
            updateBootableDiskAvailable();
        }
        updateDatacenters();
    }

    protected void updateStorageDomains(final StoragePool datacenter) {
        AsyncDataProvider.getInstance().getPermittedStorageDomainsByStoragePoolId(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                DiskModel diskModel = (DiskModel) target;
                ArrayList<StorageDomain> storageDomains = (ArrayList<StorageDomain>) returnValue;

                ArrayList<StorageDomain> filteredStorageDomains = new ArrayList<StorageDomain>();
                for (StorageDomain a : storageDomains)
                {
                    if (!a.getStorageDomainType().isIsoOrImportExportDomain() && a.getStatus() == StorageDomainStatus.Active)
                    {
                        filteredStorageDomains.add(a);
                    }
                }

                Collections.sort(filteredStorageDomains, new NameableComparator());
                StorageDomain storage = Linq.firstOrDefault(filteredStorageDomains);

                diskModel.getStorageDomain().setItems(filteredStorageDomains);
                diskModel.getStorageDomain().setSelectedItem(storage);

                diskModel.setMessage(storage == null ? CONSTANTS.noActiveStorageDomainsInDC() : "");
            }
        }), datacenter.getId(), ActionGroup.CREATE_DISK);
    }

    private void updateHosts(StoragePool datacenter) {
        AsyncDataProvider.getInstance().getHostListByDataCenter(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                AbstractDiskModel diskModel = (AbstractDiskModel) target;
                Iterable<VDS> hosts = (Iterable<VDS>) returnValue;
                ArrayList<VDS> filteredHosts = new ArrayList<VDS>();

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

        if (isInVm) {
            AsyncDataProvider.getInstance().getDataCenterById((new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void onSuccess(Object target, Object returnValue) {
                    AbstractDiskModel diskModel = (AbstractDiskModel) target;
                    StoragePool dataCenter = (StoragePool) returnValue;
                    ArrayList<StoragePool> dataCenters = new ArrayList<StoragePool>();

                    if (isDatacenterAvailable(dataCenter)) {
                        dataCenters.add(dataCenter);
                    }

                    diskModel.getDataCenter().setItems(dataCenters);

                    if (dataCenters.isEmpty()) {
                        diskModel.setMessage(CONSTANTS.relevantDCnotActive());
                    }
                }
            })), getVm().getStoragePoolId());
        }
        else {
            AsyncDataProvider.getInstance().getDataCenterList(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void onSuccess(Object target, Object returnValue) {
                    AbstractDiskModel diskModel = (AbstractDiskModel) target;
                    ArrayList<StoragePool> dataCenters = (ArrayList<StoragePool>) returnValue;
                    ArrayList<StoragePool> filteredDataCenters = new ArrayList<StoragePool>();

                    for (StoragePool dataCenter : dataCenters) {
                        if (isDatacenterAvailable(dataCenter)) {
                            filteredDataCenters.add(dataCenter);
                        }
                    }

                    diskModel.getDataCenter().setItems(filteredDataCenters);

                    if (filteredDataCenters.isEmpty()) {
                        diskModel.setMessage(CONSTANTS.noActiveDataCenters());
                    }
                }
            }));
        }
    }

    private void updateBootableDiskAvailable() {
        AsyncDataProvider.getInstance().getVmDiskList(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                AbstractDiskModel diskModel = (AbstractDiskModel) target;
                ArrayList<Disk> disks = (ArrayList<Disk>) returnValue;

                diskModel.getIsBootable().setEntity(true);
                if (getDisk() == null || !getDisk().isDiskSnapshot()) {
                    for (Disk disk : disks) {
                        if (disk.isBoot() && !disk.equals(getDisk())) {
                            diskModel.getIsBootable().setEntity(false);
                            if (!disk.isDiskSnapshot()) {
                                diskModel.getIsBootable().setChangeProhibitionReason(CONSTANTS.onlyOneBootableDisk());
                                diskModel.getIsBootable().setIsChangable(false);
                                break;
                            }
                        }
                 }
                }

                if (!getIsNew()) {
                    getIsBootable().setEntity(getDisk().isBoot());
                }
            }
        }), getVm().getId());
    }

    private void updateShareableDiskEnabled(StoragePool datacenter) {
        boolean isShareableDiskEnabled = (Boolean) AsyncDataProvider.getInstance().getConfigValuePreConverted(
                ConfigurationValues.ShareableDiskEnabled, datacenter.getCompatibilityVersion().getValue());

        getIsShareable().setChangeProhibitionReason(CONSTANTS.shareableDiskNotSupported());
        getIsShareable().setIsChangable(isShareableDiskEnabled && isEditEnabled());
    }

    private void updateDirectLunDiskEnabled(StoragePool datacenter) {
        if (getDiskStorageType().getEntity() != DiskStorageType.LUN) {
            return;
        }

        boolean isDirectLUNDiskkEnabled = (Boolean) AsyncDataProvider.getInstance().getConfigValuePreConverted(
                ConfigurationValues.DirectLUNDiskEnabled, datacenter.getCompatibilityVersion().getValue());

        getIsDirectLunDiskAvaialable().setEntity(isDirectLUNDiskkEnabled);
        setMessage(!isDirectLUNDiskkEnabled ? CONSTANTS.directLUNDiskNotSupported() : ""); //$NON-NLS-1$
    }

    private void updateShareable(VolumeType volumeType, StorageType storageType) {
        if (storageType.isBlockDomain() && volumeType == VolumeType.Sparse) {
            getIsShareable().setChangeProhibitionReason(CONSTANTS.shareableDiskNotSupportedByConfiguration());
            getIsShareable().setIsChangable(false);
            getIsShareable().setEntity(false);
        }
        else {
            getIsShareable().setIsChangable(isEditEnabled());
        }
    }

    private void updateVolumeFormat(VolumeType volumeType, StorageType storageType) {
        setVolumeFormat(AsyncDataProvider.getInstance().getDiskVolumeFormat(volumeType, storageType));
    }

    public void updateInterface(final Version clusterVersion) {
        if (getVm() != null) {
            AsyncDataProvider.getInstance().isVirtioScsiEnabledForVm(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object returnValue1) {
                    getIsVirtioScsiEnabled().setEntity(Boolean.TRUE.equals(returnValue1));

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
            }), getVm().getId());
        } else {
            setInterfaces(AsyncDataProvider.getInstance().getDiskInterfaceList());
        }
    }

    private void setInterfaces(ArrayList<DiskInterface> diskInterfaces) {
        getDiskInterface().setItems(diskInterfaces);
        setDefaultInterface();
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
                getDisk() != null ? ((DiskImage) getDisk()).getDiskProfileId() : null;
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
                getDiskStorageType().getEntity() != DiskStorageType.IMAGE) {
            getQuota().setIsAvailable(false);
            return;
        }

        getQuota().setIsAvailable(true);
        StorageDomain storageDomain = getStorageDomain().getSelectedItem();
        if (storageDomain == null) {
            return;
        }

        IdQueryParameters parameters = new IdQueryParameters(storageDomain.getId());
        Frontend.getInstance().runQuery(VdcQueryType.GetAllRelevantQuotasForStorage, parameters, new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object innerModel, Object innerReturnValue) {
                        ArrayList<Quota> quotaList = ((VdcQueryReturnValue) innerReturnValue).getReturnValue();
                        if (quotaList != null && !quotaList.isEmpty()) {
                            getQuota().setItems(quotaList);
                        }

                        Guid defaultQuota = getDisk() != null ? ((DiskImage) getDisk()).getQuotaId() : null;
                        if (defaultQuota != null) {
                            for (Quota quota : quotaList) {
                                if (quota.getId().equals(defaultQuota)) {
                                    getQuota().setSelectedItem(quota);
                                    return;
                                }
                            }
                            Quota quota = new Quota();
                            quota.setId(defaultQuota);
                            if (getDisk() != null) {
                                quota.setQuotaName(getDiskImage().getQuotaName());
                            }
                            quotaList.add(quota);
                            getQuota().setItems(quotaList);
                            getQuota().setSelectedItem(quota);
                        }
                    }
                }));
    }

    private boolean isHostAvailable(VDS host) {
        boolean isStatusUp = host.getStatus() == VDSStatus.Up;

        return isStatusUp;
    }

    protected void diskStorageType_EntityChanged() {
        boolean isInVm = getVm() != null;
        boolean isDiskImage = getDiskStorageType().getEntity() == DiskStorageType.IMAGE;
        boolean isLunDisk = getDiskStorageType().getEntity() == DiskStorageType.LUN;

        getSize().setIsAvailable(isDiskImage);
        getSizeExtend().setIsAvailable(isDiskImage && !getIsNew());
        getStorageDomain().setIsAvailable(isDiskImage);
        getVolumeType().setIsAvailable(isDiskImage);
        getIsWipeAfterDelete().setIsAvailable(isDiskImage);
        getHost().setIsAvailable(isLunDisk);
        getStorageType().setIsAvailable(isLunDisk);
        getDataCenter().setIsAvailable(!isInVm);
        getDiskProfile().setIsAvailable(isDiskImage);

        if (!isDiskImage) {
            previousIsQuotaAvailable = getQuota().getIsAvailable();
        }

        getQuota().setIsAvailable(isDiskImage ? previousIsQuotaAvailable : false);

        updateDatacenters();
    }

    protected void volumeType_SelectedItemChanged() {
        if (getVolumeType().getSelectedItem() == null || getDataCenter().getSelectedItem() == null
                || getStorageDomain().getSelectedItem() == null) {
            return;
        }

        VolumeType volumeType = getVolumeType().getSelectedItem();
        StorageType storageType = getStorageDomain().getSelectedItem().getStorageType();

        updateVolumeFormat(volumeType, storageType);
        updateShareable(volumeType, storageType);
    }

    private void DiskInterface_SelectedItemChanged() {
        boolean isLunDisk = getDiskStorageType().getEntity() == DiskStorageType.LUN;
        DiskInterface diskInterface = getDiskInterface().getSelectedItem();
        getIsSgIoUnfiltered().setIsAvailable(isLunDisk && DiskInterface.VirtIO_SCSI.equals(diskInterface));
        getIsScsiPassthrough().setIsAvailable(isLunDisk && DiskInterface.VirtIO_SCSI.equals(diskInterface));

        updateScsiPassthroguhChangeability();
        updateReadOnlyChangeability();
        updatePlugChangeability();
    }

    protected void updateScsiPassthroguhChangeability() {
        getIsScsiPassthrough().setIsChangable(!getIsReadOnly().getEntity() && isEditEnabled());
        getIsScsiPassthrough().setChangeProhibitionReason(CONSTANTS.cannotEnableScsiPassthroughForLunReadOnlyDisk());

        updateSgIoUnfilteredChangeability();
    }

    protected void updateSgIoUnfilteredChangeability() {
        if (!getIsScsiPassthrough().getEntity()) {
            getIsSgIoUnfiltered().setChangeProhibitionReason(CONSTANTS.cannotEnableSgioWhenScsiPassthroughDisabled());
            getIsSgIoUnfiltered().setIsChangable(false);
            getIsSgIoUnfiltered().setEntity(false);
            return;
        }
        getIsSgIoUnfiltered().setIsChangable(isEditEnabled());
    }

    protected void updateReadOnlyChangeability() {
        DiskInterface diskInterface = getDiskInterface().getSelectedItem();

        if (diskInterface == DiskInterface.IDE) {
            getIsReadOnly().setChangeProhibitionReason(CONSTANTS.cannotEnableIdeInterfaceForReadOnlyDisk());
            getIsReadOnly().setIsChangable(false);
            getIsReadOnly().setEntity(false);
            return;
        }

        boolean isDirectLUN = getDiskStorageType().getEntity() == DiskStorageType.LUN;
        boolean isScsiPassthrough = getIsScsiPassthrough().getEntity();
        if (diskInterface == DiskInterface.VirtIO_SCSI && isDirectLUN && isScsiPassthrough) {
            getIsReadOnly().setChangeProhibitionReason(CONSTANTS.cannotEnableReadonlyWhenScsiPassthroughEnabled());
            getIsReadOnly().setIsChangable(false);
            getIsReadOnly().setEntity(false);
            return;
        }

        if (isVmAttachedToPool() && !getIsNew()) {
            getIsReadOnly().setIsChangable(false);
        } else {
            getIsReadOnly().setIsChangable(isEditEnabled());
        }
        getIsReadOnly().setEntity(getIsNew() ? Boolean.FALSE : getDisk().getReadOnly());
    }

    protected void updateWipeAfterDeleteChangeability() {
        if (isVmAttachedToPool()) {
            getIsWipeAfterDelete().setIsChangable(false);
        } else {
            getIsWipeAfterDelete().setIsChangable(isEditEnabled());
        }
    }

    private void updatePlugChangeability() {
        if (getVm() == null) { // No point in updating plug to VM if there's no VM
            return;
        }

        DiskInterface diskInterface = getDiskInterface().getSelectedItem();
        boolean isVmRunning = getVm() != null && getVm().getStatus() != VMStatus.Down;

        if (DiskInterface.IDE.equals(diskInterface) && isVmRunning) {
            getIsPlugged().setChangeProhibitionReason(CONSTANTS.cannotHotPlugDiskWithIdeInterface());
            getIsPlugged().setIsChangable(false);
            getIsPlugged().setEntity(false);
        }
        else {
            if (!canDiskBePlugged(getVm())) {
                getIsPlugged().setChangeProhibitionReason(CONSTANTS.cannotPlugDiskIncorrectVmStatus());
                getIsPlugged().setIsChangable(false);
                getIsPlugged().setEntity(false);
            }
            else {
                getIsPlugged().setIsChangable(isEditEnabled());
                getIsPlugged().setEntity(true);
            }
        }
    }

    private boolean canDiskBePlugged(VM vm) {
        return vm.getStatus() == VMStatus.Up || vm.getStatus() == VMStatus.Down || vm.getStatus() == VMStatus.Paused;
    }

    private boolean isVmAttachedToPool() {
        return getVm() != null && getVm().getVmPoolId() != null;
    }

    private void datacenter_SelectedItemChanged() {
        StoragePool datacenter = getDataCenter().getSelectedItem();
        boolean isInVm = getVm() != null;

        if (datacenter == null) {
            return;
        }

        updateShareableDiskEnabled(datacenter);
        updateDirectLunDiskEnabled(datacenter);
        updateInterface(isInVm ? getVm().getVdsGroupCompatibilityVersion() : null);

        if (getDiskStorageType().getEntity() == DiskStorageType.IMAGE) {
            updateStorageDomains(datacenter);
        }
        else {
            updateHosts(datacenter);
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
    }

    public boolean validate() {
        getDescription().validateEntity(new IValidation[] { new SpecialAsciiI18NOrNoneValidation() });

        if (getVm() == null) {
            getAlias().validateEntity(new IValidation[] { new NotEmptyValidation(), new I18NNameValidation() });
        }
        else {
            getAlias().validateEntity(new IValidation[] { new I18NNameValidation() });
        }

        StoragePool dataCenter = getDataCenter().getSelectedItem();
        if (dataCenter != null && dataCenter.getQuotaEnforcementType() == QuotaEnforcementTypeEnum.HARD_ENFORCEMENT) {
            getQuota().validateSelectedItem(new IValidation[] { new NotEmptyQuotaValidation() });
        }

        return getAlias().getIsValid() && getDescription().getIsValid() && getQuota().getIsValid()
                && getDiskInterface().getIsValid();
    }

    protected void forceCreationWarning(ArrayList<String> usedLunsMessages) {
        ConfirmationModel confirmationModel = new ConfirmationModel();
        setConfirmWindow(confirmationModel);

        confirmationModel.setTitle(CONSTANTS.forceStorageDomainCreation());
        confirmationModel.setMessage(CONSTANTS.lunsAlreadyPartOfSD());
        confirmationModel.setHelpTag(HelpTag.force_lun_disk_creation);
        confirmationModel.setHashName("force_lun_disk_creation"); //$NON-NLS-1$
        confirmationModel.setItems(usedLunsMessages);

        UICommand forceSaveCommand = new UICommand("OnForceSave", this); //$NON-NLS-1$
        forceSaveCommand.setTitle(CONSTANTS.ok());
        forceSaveCommand.setIsDefault(true);
        confirmationModel.getCommands().add(forceSaveCommand);

        UICommand cancelconfirmCommand = new UICommand("CancelConfirm", this); //$NON-NLS-1$
        cancelconfirmCommand.setTitle(CONSTANTS.cancel());
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
        if (getDiskStorageType().getEntity() == DiskStorageType.IMAGE) {
            DiskImage diskImage = getDiskImage();
            if (getQuota().getIsAvailable() && getQuota().getSelectedItem() != null) {
                diskImage.setQuotaId(getQuota().getSelectedItem().getId());
            }

            long sizeToAddInGigabytes = Long.valueOf(getSizeExtend().getEntity());
            if (sizeToAddInGigabytes > 0) {
                diskImage.setSizeInGigabytes(diskImage.getSizeInGigabytes() + sizeToAddInGigabytes);
            }

            setDisk(diskImage);
        }
        else {
            LunDisk lunDisk = getLunDisk();
            DiskInterface diskInterface = getDiskInterface().getSelectedItem();
            if (DiskInterface.VirtIO_SCSI.equals(diskInterface)) {
                lunDisk.setSgio(!getIsScsiPassthrough().getEntity() ? null :
                        getIsSgIoUnfiltered().getEntity() ?
                        ScsiGenericIO.UNFILTERED : ScsiGenericIO.FILTERED);
            }
            setDisk(lunDisk);
        }

        if (getDisk().getDiskStorageType() == DiskStorageType.IMAGE) {
            DiskProfile selectedDiskProfile = getDiskProfile().getSelectedItem();
            if (selectedDiskProfile != null) {
                ((DiskImage) getDisk()).setDiskProfileId(selectedDiskProfile.getId());
            }
        }
        getDisk().setDiskAlias(getAlias().getEntity());
        getDisk().setDiskDescription(getDescription().getEntity());
        getDisk().setDiskInterface(getDiskInterface().getSelectedItem());
        getDisk().setWipeAfterDelete(getIsWipeAfterDelete().getEntity());
        getDisk().setBoot(getIsBootable().getEntity());
        getDisk().setShareable(getIsShareable().getEntity());
        getDisk().setPlugged(getIsPlugged().getEntity());
        getDisk().setPropagateErrors(PropagateErrors.Off);
        getDisk().setReadOnly(getIsReadOnly().getIsAvailable() ? getIsReadOnly().getEntity() : null);
    }

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

        if (ev.matchesDefinition(EntityModel.entityChangedEventDefinition)) {
            if (sender == getIsReadOnly()) {
                updateScsiPassthroguhChangeability();
            } else if (sender == getIsScsiPassthrough()) {
                updateSgIoUnfilteredChangeability();
                updateReadOnlyChangeability();
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
}
