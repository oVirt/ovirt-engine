package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

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
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.utils.ObjectUtils;
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
    private EntityModel<Boolean> isAttachDisk;
    private EntityModel<Boolean> isInternal;
    private EntityModel<Boolean> isDirectLunDiskAvaialable;
    private EntityModel<Boolean> isScsiPassthrough;
    private EntityModel<Boolean> isSgIoUnfiltered;
    private EntityModel<String> sizeExtend;

    private ListModel<StorageType> storageType;
    private ListModel<VDS> host;
    private ListModel<StoragePool> dataCenter;
    private ListModel<EntityModel<DiskModel>> internalAttachableDisks;
    private ListModel<EntityModel<DiskModel>> externalAttachableDisks;

    private SanStorageModel sanStorageModel;
    private VolumeFormat volumeFormat;
    private boolean previousWipeAfterDeleteEntity;
    private boolean previousIsQuotaAvailable;

    private SystemTreeItemModel systemTreeSelectedItem;
    private String hash;
    private UICommand cancelCommand;
    private int queryCounter;

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

    public EntityModel<Boolean> getIsAttachDisk() {
        return isAttachDisk;
    }

    public void setIsAttachDisk(EntityModel<Boolean> isAttachDisk) {
        this.isAttachDisk = isAttachDisk;
    }

    public EntityModel<Boolean> getIsInternal() {
        return isInternal;
    }

    public void setIsInternal(EntityModel<Boolean> isInternal) {
        this.isInternal = isInternal;
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

    public ListModel<EntityModel<DiskModel>> getInternalAttachableDisks() {
        return internalAttachableDisks;
    }

    public void setInternalAttachableDisks(ListModel<EntityModel<DiskModel>> internalAttachableDisks) {
        this.internalAttachableDisks = internalAttachableDisks;
    }

    public ListModel<EntityModel<DiskModel>> getExternalAttachableDisks() {
        return externalAttachableDisks;
    }

    public void setExternalAttachableDisks(ListModel<EntityModel<DiskModel>> externalAttachableDisks) {
        this.externalAttachableDisks = externalAttachableDisks;
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

    private EntityModel<Boolean> isVirtioScsiEnabled;

    public EntityModel<Boolean> getIsVirtioScsiEnabled() {
        return isVirtioScsiEnabled;
    }

    public void setIsVirtioScsiEnabled(EntityModel<Boolean> virtioScsiEnabled) {
        this.isVirtioScsiEnabled = virtioScsiEnabled;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
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

        setIsAttachDisk(new EntityModel<Boolean>());
        getIsAttachDisk().setEntity(false);
        getIsAttachDisk().getEntityChangedEvent().addListener(this);

        setIsInternal(new EntityModel<Boolean>());
        getIsInternal().setEntity(true);
        getIsInternal().getEntityChangedEvent().addListener(this);

        setIsWipeAfterDelete(new EntityModel<Boolean>());
        getIsWipeAfterDelete().setEntity(false);
        getIsWipeAfterDelete().getEntityChangedEvent().addListener(this);

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

        setIsDirectLunDiskAvaialable(new EntityModel<Boolean>());
        getIsDirectLunDiskAvaialable().setEntity(true);

        setDataCenter(new ListModel<StoragePool>());
        getDataCenter().setIsAvailable(false);
        getDataCenter().getSelectedItemChangedEvent().addListener(this);

        getStorageDomain().getSelectedItemChangedEvent().addListener(this);

        setStorageType(new ListModel<StorageType>());
        getStorageType().setIsAvailable(false);
        getStorageType().setItems(AsyncDataProvider.getStorageTypeList());
        getStorageType().getSelectedItemChangedEvent().addListener(this);

        setHost(new ListModel<VDS>());
        getHost().setIsAvailable(false);

        getVolumeType().getSelectedItemChangedEvent().addListener(this);
        getDiskInterface().getSelectedItemChangedEvent().addListener(this);

        setInternalAttachableDisks(new ListModel<EntityModel<DiskModel>>());
        setExternalAttachableDisks(new ListModel<EntityModel<DiskModel>>());

        setIsVirtioScsiEnabled(new EntityModel<Boolean>());
    }

    public abstract boolean getIsNew();

    public boolean getIsFloating() {
        return getVm() == null;
    }

    protected abstract boolean isDatacenterAvailable(StoragePool dataCenter);

    protected abstract void updateWipeAfterDelete(StorageType storageType);

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
        setHash(getHashName() + new Date());

        // Add progress listeners
        Frontend.getInstance().getQueryStartedEvent().addListener(this);
        Frontend.getInstance().getQueryCompleteEvent().addListener(this);
        Frontend.getInstance().subscribeAdditionalQueries(new VdcQueryType[] { VdcQueryType.Search,
                VdcQueryType.GetStoragePoolById, VdcQueryType.GetNextAvailableDiskAliasNameByVMId,
                VdcQueryType.GetPermittedStorageDomainsByStoragePoolId, VdcQueryType.GetAllVdsByStoragePool,
                VdcQueryType.GetAllAttachableDisks, VdcQueryType.GetAllDisksByVmId,
                VdcQueryType.GetAllRelevantQuotasForStorage, VdcQueryType.OsRepository });

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
        AsyncDataProvider.getPermittedStorageDomainsByStoragePoolId(new AsyncQuery(this, new INewAsyncCallback() {
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

                if (storage != null) {
                    diskModel.setMessage(""); //$NON-NLS-1$
                }
                else {
                    diskModel.setMessage(CONSTANTS.noActiveStorageDomainsInDC());
                }
            }
        }, getHash()), datacenter.getId(), ActionGroup.CREATE_DISK);
    }

    private void updateHosts(StoragePool datacenter) {
        AsyncDataProvider.getHostListByDataCenter(new AsyncQuery(this, new INewAsyncCallback() {
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
        }, getHash()), datacenter.getId());
    }

    private void updateDatacenters() {
        boolean isInVm = getVm() != null;
        getDataCenter().setIsAvailable(!isInVm);
        setMessage(""); //$NON-NLS-1$

        if (isInVm) {
            AsyncDataProvider.getDataCenterById((new AsyncQuery(this, new INewAsyncCallback() {
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
                        diskModel.setMessage(getIsInternal().getEntity() ?
                                CONSTANTS.noActiveStorageDomainsInDC() : CONSTANTS.relevantDCnotActive());
                    }
                }
            }, getHash())), getVm().getStoragePoolId());
        }
        else {
            AsyncDataProvider.getDataCenterList(new AsyncQuery(this, new INewAsyncCallback() {
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
            }, getHash()));
        }
    }

    private void updateBootableDiskAvailable() {
        AsyncDataProvider.getVmDiskList(new AsyncQuery(this, new INewAsyncCallback() {
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
        }, getHash()), getVm().getId());
    }

    private void updateShareableDiskEnabled(StoragePool datacenter) {
        boolean isShareableDiskEnabled = (Boolean) AsyncDataProvider.getConfigValuePreConverted(
                ConfigurationValues.ShareableDiskEnabled, datacenter.getcompatibility_version().getValue());

        getIsShareable().setChangeProhibitionReason(CONSTANTS.shareableDiskNotSupported());
        getIsShareable().setIsChangable(isShareableDiskEnabled && isEditEnabled());
    }

    private void updateDirectLunDiskEnabled(StoragePool datacenter) {
        boolean isInternal = getIsInternal().getEntity();
        if (isInternal) {
            return;
        }

        boolean isDirectLUNDiskkEnabled = (Boolean) AsyncDataProvider.getConfigValuePreConverted(
                ConfigurationValues.DirectLUNDiskEnabled, datacenter.getcompatibility_version().getValue());

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
        setVolumeFormat(AsyncDataProvider.getDiskVolumeFormat(volumeType, storageType));
    }

    public void updateInterface(final Version clusterVersion) {
        if (getVm() != null) {
            AsyncDataProvider.isVirtioScsiEnabledForVm(new AsyncQuery(this, new INewAsyncCallback() {
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

                    AsyncDataProvider.getDiskInterfaceList(getVm().getOs(), clusterVersion, asyncQuery);

                }
            }), getVm().getId());
        } else {
            setInterfaces(AsyncDataProvider.getDiskInterfaceList());
        }
    }

    private void setInterfaces(ArrayList<DiskInterface> diskInterfaces) {
        getDiskInterface().setItems(diskInterfaces);
        setDefaultInterface();
    }

    private void updateQuota(StoragePool datacenter) {
        if (datacenter.getQuotaEnforcementType().equals(QuotaEnforcementTypeEnum.DISABLED)
                || !getIsInternal().getEntity()) {
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
                }, getHash()));
    }

    private boolean isHostAvailable(VDS host) {
        boolean isStatusUp = host.getStatus() == VDSStatus.Up;

        return isStatusUp;
    }

    private void isInternal_EntityChanged() {
        boolean isInVm = getVm() != null;
        boolean isInternal = getIsInternal().getEntity();

        getSize().setIsAvailable(isInternal);
        getSizeExtend().setIsAvailable(isInternal && !getIsNew());
        getStorageDomain().setIsAvailable(isInternal);
        getVolumeType().setIsAvailable(isInternal);
        getIsWipeAfterDelete().setIsAvailable(isInternal);
        getHost().setIsAvailable(!isInternal);
        getStorageType().setIsAvailable(!isInternal);
        getDataCenter().setIsAvailable(!isInVm);

        if (!isInternal) {
            previousWipeAfterDeleteEntity = getIsWipeAfterDelete().getEntity();
            previousIsQuotaAvailable = getQuota().getIsAvailable();
        }

        getIsWipeAfterDelete().setEntity(isInternal ? previousWipeAfterDeleteEntity : false);
        getQuota().setIsAvailable(isInternal ? previousIsQuotaAvailable : false);

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
        boolean isInternal = getIsInternal().getEntity();
        DiskInterface diskInterface = getDiskInterface().getSelectedItem();
        getIsSgIoUnfiltered().setIsAvailable(!isInternal && DiskInterface.VirtIO_SCSI.equals(diskInterface));
        getIsScsiPassthrough().setIsAvailable(!isInternal && DiskInterface.VirtIO_SCSI.equals(diskInterface));

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

        boolean isDirectLUN = !getIsInternal().getEntity();
        boolean isScsiPassthrough = getIsScsiPassthrough().getEntity();
        if (diskInterface == DiskInterface.VirtIO_SCSI && isDirectLUN && isScsiPassthrough) {
            getIsReadOnly().setChangeProhibitionReason(CONSTANTS.cannotEnableReadonlyWhenScsiPassthroughEnabled());
            getIsReadOnly().setIsChangable(false);
            getIsReadOnly().setEntity(false);
            return;
        }


        getIsReadOnly().setIsChangable(isEditEnabled());
        getIsReadOnly().setEntity(getIsNew() ? Boolean.FALSE : getDisk().getReadOnly());
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


    private void wipeAfterDelete_EntityChanged(EventArgs e) {
        if (!getIsWipeAfterDelete().getIsChangable() && getIsWipeAfterDelete().getEntity())
        {
            getIsWipeAfterDelete().setEntity(false);
        }
    }

    private void attachDisk_EntityChanged(EventArgs e) {
        if (getIsAttachDisk().getEntity())
        {
            getIsPlugged().setIsAvailable(true);
            // Get internal attachable disks
            AsyncDataProvider.getAllAttachableDisks(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void onSuccess(Object target, Object returnValue) {
                    AbstractDiskModel model = (AbstractDiskModel) target;
                    ArrayList<Disk> disks = (ArrayList<Disk>) returnValue;
                    Collections.sort(disks, new Linq.DiskByAliasComparer());
                    ArrayList<DiskModel> diskModels = Linq.disksToDiskModelList(disks);

                    model.getInternalAttachableDisks().setItems(Linq.toEntityModelList(
                            Linq.filterDisksByType(diskModels, DiskStorageType.IMAGE)));
                }
            }, getHash()), getVm().getStoragePoolId(), getVm().getId());

            // Get external attachable disks
            AsyncDataProvider.getAllAttachableDisks(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void onSuccess(Object target, Object returnValue) {
                    AbstractDiskModel model = (AbstractDiskModel) target;
                    ArrayList<Disk> disks = (ArrayList<Disk>) returnValue;
                    Collections.sort(disks, new Linq.DiskByAliasComparer());
                    ArrayList<DiskModel> diskModels = Linq.disksToDiskModelList(disks);

                    model.getExternalAttachableDisks().setItems(Linq.toEntityModelList(
                            Linq.filterDisksByType(diskModels, DiskStorageType.LUN)));
                }
            }, getHash()), null, getVm().getId());
        }
    }

    private void datacenter_SelectedItemChanged() {
        StoragePool datacenter = getDataCenter().getSelectedItem();
        boolean isInternal = getIsInternal().getEntity() != null ? getIsInternal().getEntity() : false;
        boolean isInVm = getVm() != null;

        if (datacenter == null) {
            return;
        }

        updateShareableDiskEnabled(datacenter);
        updateDirectLunDiskEnabled(datacenter);
        updateInterface(isInVm ? getVm().getVdsGroupCompatibilityVersion() : null);

        if (isInternal) {
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
            updateWipeAfterDelete(selectedStorage.getStorageType());
        }
        updateQuota(getDataCenter().getSelectedItem());
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

        return getAlias().getIsValid() && getDescription().getIsValid() && getQuota().getIsValid() && getDiskInterface().getIsValid();
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
        boolean isInternal = getIsInternal().getEntity();
        if (isInternal) {
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
    public void eventRaised(Event ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(EntityModel.entityChangedEventDefinition)) {
            if (sender == getIsWipeAfterDelete()) {
                wipeAfterDelete_EntityChanged(args);
            } else if (sender == getIsAttachDisk()) {
                attachDisk_EntityChanged(args);
            } else if (sender == getIsReadOnly()) {
                updateScsiPassthroguhChangeability();
            } else if (sender == getIsScsiPassthrough()) {
                updateSgIoUnfilteredChangeability();
                updateReadOnlyChangeability();
            } else if (sender == getIsInternal()) {
                isInternal_EntityChanged();
            }
        }
        else if (ev.matchesDefinition(ListModel.selectedItemChangedEventDefinition) && sender == getVolumeType())
        {
            volumeType_SelectedItemChanged();
        }
        else if (ev.matchesDefinition(ListModel.selectedItemChangedEventDefinition) && sender == getDiskInterface())
        {
            DiskInterface_SelectedItemChanged();
        }
        else if (ev.matchesDefinition(ListModel.selectedItemChangedEventDefinition) && sender == getDataCenter())
        {
            datacenter_SelectedItemChanged();
        }
        else if (ev.matchesDefinition(ListModel.selectedItemChangedEventDefinition) && sender == getStorageDomain())
        {
            storageDomain_SelectedItemChanged();
        }
        else if (ev.matchesDefinition(Frontend.getInstance().getQueryStartedEventDefinition())
                && ObjectUtils.objectsEqual(Frontend.getInstance().getCurrentContext(), getHash()))
        {
            frontend_QueryStarted();
        }
        else if (ev.matchesDefinition(Frontend.getInstance().getQueryCompleteEventDefinition())
                && ObjectUtils.objectsEqual(Frontend.getInstance().getCurrentContext(), getHash()))
        {
            frontend_QueryComplete();
        }
    }

    public void frontend_QueryStarted() {
        queryCounter++;
        if (getProgress() == null) {
            startProgress(null);
        }
    }

    public void frontend_QueryComplete() {
        queryCounter--;
        if (queryCounter == 0) {
            stopProgress();
        }
    }
}
