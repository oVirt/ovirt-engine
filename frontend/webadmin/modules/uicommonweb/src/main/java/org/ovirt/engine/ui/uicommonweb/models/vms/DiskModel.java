package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Date;

import org.ovirt.engine.core.common.action.AddDiskParameters;
import org.ovirt.engine.core.common.action.AttachDettachVmDiskParameters;
import org.ovirt.engine.core.common.action.UpdateVmDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VmDiskOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.DiskInterface;
import org.ovirt.engine.core.common.businessentities.LUNs;
import org.ovirt.engine.core.common.businessentities.LunDisk;
import org.ovirt.engine.core.common.businessentities.PropagateErrors;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetAllRelevantQuotasForStorageParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ConfirmationModel;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModel;
import org.ovirt.engine.ui.uicommonweb.validation.I18NNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyQuotaValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.SpecialAsciiI18NOrNoneValidation;
import org.ovirt.engine.ui.uicompat.Constants;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

public class DiskModel extends Model
{
    private static final Constants CONSTANTS = ConstantsManager.getInstance().getConstants();

    private boolean isNew;
    private VM vm;
    private SanStorageModel sanStorageModel;
    private String hash;

    private Disk disk;
    private VolumeFormat volumeFormat;
    private EntityModel size;
    private EntityModel alias;
    private EntityModel description;
    private EntityModel sourceStorageDomainName;
    private EntityModel isWipeAfterDelete;
    private EntityModel isBootable;
    private EntityModel isShareable;
    private EntityModel isPlugged;
    private EntityModel isAttachDisk;
    private EntityModel isInternal;
    private EntityModel isDirectLunDiskAvaialable;

    private ListModel volumeType;
    private ListModel storageType;
    private ListModel diskInterface;
    private ListModel sourceStorageDomain;
    private ListModel storageDomain;
    private ListModel host;
    private ListModel dataCenter;
    private ListModel quota;
    private ListModel internalAttachableDisks;
    private ListModel externalAttachableDisks;

    private SystemTreeItemModel systemTreeSelectedItem;
    private UICommand cancelCommand;
    private boolean previousWipeAfterDeleteEntity;
    private boolean previousIsQuotaAvailable;

    public VM getVm() {
        return vm;
    }

    public void setVm(VM vm) {
        this.vm = vm;
    }

    public SanStorageModel getSanStorageModel() {
        return sanStorageModel;
    }

    public void setSanStorageModel(SanStorageModel sanStorageModel) {
        this.sanStorageModel = sanStorageModel;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public boolean getIsNew() {
        return isNew;
    }

    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

    public EntityModel getIsWipeAfterDelete() {
        return isWipeAfterDelete;
    }

    public void setIsWipeAfterDelete(EntityModel isWipeAfterDelete) {
        this.isWipeAfterDelete = isWipeAfterDelete;
    }

    public EntityModel getIsBootable() {
        return isBootable;
    }

    public void setIsBootable(EntityModel isBootable) {
        this.isBootable = isBootable;
    }

    public EntityModel getIsShareable() {
        return isShareable;
    }

    public void setIsShareable(EntityModel isShareable) {
        this.isShareable = isShareable;
    }

    public EntityModel getIsPlugged() {
        return isPlugged;
    }

    public void setIsPlugged(EntityModel isPlugged) {
        this.isPlugged = isPlugged;
    }

    public EntityModel getIsAttachDisk() {
        return isAttachDisk;
    }

    public void setIsAttachDisk(EntityModel isAttachDisk) {
        this.isAttachDisk = isAttachDisk;
    }

    public EntityModel getIsInternal() {
        return isInternal;
    }

    public void setIsInternal(EntityModel isInternal) {
        this.isInternal = isInternal;
    }

    public EntityModel getIsDirectLunDiskAvaialable() {
        return isDirectLunDiskAvaialable;
    }

    public void setIsDirectLunDiskAvaialable(EntityModel isDirectLunDiskAvaialable) {
        this.isDirectLunDiskAvaialable = isDirectLunDiskAvaialable;
    }

    public EntityModel getSize() {
        return size;
    }

    public void setSize(EntityModel size) {
        this.size = size;
    }

    public EntityModel getAlias() {
        return alias;
    }

    public void setAlias(EntityModel alias) {
        this.alias = alias;
    }

    public EntityModel getDescription() {
        return description;
    }

    public void setDescription(EntityModel description) {
        this.description = description;
    }

    public EntityModel getSourceStorageDomainName() {
        return sourceStorageDomainName;
    }

    public void setSourceStorageDomainName(EntityModel sourceStorageDomainName) {
        this.sourceStorageDomainName = sourceStorageDomainName;
    }

    public VolumeFormat getVolumeFormat() {
        return volumeFormat;
    }

    public void setVolumeFormat(VolumeFormat volumeFormat) {
        this.volumeFormat = volumeFormat;
    }

    public Disk getDisk() {
        return disk;
    }

    public void setDisk(Disk disk) {
        this.disk = disk;
    }

    public ListModel getVolumeType() {
        return volumeType;
    }

    public void setVolumeType(ListModel volumeType) {
        this.volumeType = volumeType;
    }

    public ListModel getStorageType() {
        return storageType;
    }

    public void setStorageType(ListModel storageType) {
        this.storageType = storageType;
    }

    public ListModel getDiskInterface() {
        return diskInterface;
    }

    public void setDiskInterface(ListModel diskInterface) {
        this.diskInterface = diskInterface;
    }

    public ListModel getSourceStorageDomain() {
        return sourceStorageDomain;
    }

    public void setSourceStorageDomain(ListModel sourceStorageDomain) {
        this.sourceStorageDomain = sourceStorageDomain;
    }

    public ListModel getStorageDomain() {
        return storageDomain;
    }

    public void setStorageDomain(ListModel storageDomain) {
        this.storageDomain = storageDomain;
    }

    public ListModel getHost() {
        return host;
    }

    public void setHost(ListModel host) {
        this.host = host;
    }

    public ListModel getDataCenter() {
        return dataCenter;
    }

    public void setDataCenter(ListModel dataCenter) {
        this.dataCenter = dataCenter;
    }

    public ListModel getQuota() {
        return quota;
    }

    public void setQuota(ListModel quota) {
        this.quota = quota;
    }

    public ListModel getInternalAttachableDisks() {
        return internalAttachableDisks;
    }

    public void setInternalAttachableDisks(ListModel internalAttachableDisks) {
        this.internalAttachableDisks = internalAttachableDisks;
    }

    public ListModel getExternalAttachableDisks() {
        return externalAttachableDisks;
    }

    public void setExternalAttachableDisks(ListModel externalAttachableDisks) {
        this.externalAttachableDisks = externalAttachableDisks;
    }

    public SystemTreeItemModel getSystemTreeSelectedItem() {
        return systemTreeSelectedItem;
    }

    public void setSystemTreeSelectedItem(SystemTreeItemModel systemTreeSelectedItem) {
        this.systemTreeSelectedItem = systemTreeSelectedItem;
    }

    public UICommand getCancelCommand() {
        return cancelCommand;
    }

    public void setCancelCommand(UICommand cancelCommand) {
        this.cancelCommand = cancelCommand;
    }

    public boolean isPreviousWipeAfterDeleteEntity() {
        return previousWipeAfterDeleteEntity;
    }

    public void setPreviousWipeAfterDeleteEntity(boolean previousWipeAfterDeleteEntity) {
        this.previousWipeAfterDeleteEntity = previousWipeAfterDeleteEntity;
    }

    public boolean isPreviousIsQuotaAvailable() {
        return previousIsQuotaAvailable;
    }

    public void setPreviousIsQuotaAvailable(boolean previousIsQuotaAvailable) {
        this.previousIsQuotaAvailable = previousIsQuotaAvailable;
    }

    public int getQueryCounter() {
        return queryCounter;
    }

    public void setQueryCounter(int queryCounter) {
        this.queryCounter = queryCounter;
    }

    public DiskModel() {
        setSize(new EntityModel());
        getSize().setIsValid(true);

        setDiskInterface(new ListModel());
        setStorageDomain(new ListModel());
        setQuota(new ListModel());

        setHost(new ListModel());
        getHost().setIsAvailable(false);

        setSourceStorageDomain(new ListModel());
        getSourceStorageDomain().setIsAvailable(false);

        setSourceStorageDomainName(new EntityModel());
        getSourceStorageDomainName().setIsAvailable(false);

        setDataCenter(new ListModel());
        getDataCenter().setIsAvailable(false);
        getDataCenter().getSelectedItemChangedEvent().addListener(this);

        setVolumeType(new ListModel());
        getVolumeType().setItems(AsyncDataProvider.GetVolumeTypeList());
        getVolumeType().getSelectedItemChangedEvent().addListener(this);

        setStorageType(new ListModel());
        getStorageType().setIsAvailable(false);
        getStorageType().setItems(AsyncDataProvider.GetStorageTypeList());
        getStorageType().getSelectedItemChangedEvent().addListener(this);

        setIsWipeAfterDelete(new EntityModel());
        getIsWipeAfterDelete().setEntity(false);
        getIsWipeAfterDelete().getEntityChangedEvent().addListener(this);

        setIsAttachDisk(new EntityModel());
        getIsAttachDisk().setEntity(false);
        getIsAttachDisk().getEntityChangedEvent().addListener(this);

        setIsBootable(new EntityModel());
        getIsBootable().setEntity(false);

        setIsShareable(new EntityModel());
        getIsShareable().setEntity(false);

        setIsPlugged(new EntityModel());
        getIsPlugged().setEntity(true);

        setQuota(new ListModel());
        getQuota().setIsAvailable(false);

        setAlias(new EntityModel());
        setDescription(new EntityModel());
        setInternalAttachableDisks(new ListModel());
        setExternalAttachableDisks(new ListModel());

        setIsDirectLunDiskAvaialable(new EntityModel());
        getIsDirectLunDiskAvaialable().setEntity(true);

        setIsInternal(new EntityModel());
        getIsInternal().setEntity(true);
        getIsInternal().getEntityChangedEvent().addListener(this);

        setIsNew(true);
    }

    public DiskModel(SystemTreeItemModel systemTreeSelectedItem) {
        this();
        setSystemTreeSelectedItem(systemTreeSelectedItem);
    }

    @Override
    public void Initialize() {
        super.Initialize();
        setHash(getHashName() + new Date());

        // Add progress listeners
        Frontend.getQueryStartedEvent().addListener(this);
        Frontend.getQueryCompleteEvent().addListener(this);
        Frontend.subscribeAdditionalQueries(new VdcQueryType[] { VdcQueryType.Search,
                VdcQueryType.GetStoragePoolById, VdcQueryType.GetNextAvailableDiskAliasNameByVMId,
                VdcQueryType.GetPermittedStorageDomainsByStoragePoolId, VdcQueryType.GetAllVdsByStoragePool,
                VdcQueryType.GetAllAttachableDisks, VdcQueryType.GetAllDisksByVmId,
                VdcQueryType.GetAllRelevantQuotasForStorage });

        // Create and set commands
        UICommand onSaveCommand = new UICommand("OnSave", this); //$NON-NLS-1$
        onSaveCommand.setTitle(CONSTANTS.ok());
        onSaveCommand.setIsDefault(true);
        getCommands().add(onSaveCommand);
        getCommands().add(getCancelCommand());

        // Update data
        if (getVm() != null) {
            updateSuggestedDiskAlias();
            updateBootableDiskAvailable();
        }
        if (!getIsNew()) {
            initializeEdit();
        }
        updateDatacenters();
    }

    private void initializeEdit() {
        getStorageDomain().setIsChangable(false);
        getHost().setIsChangable(false);
        getStorageType().setIsChangable(false);
        getDataCenter().setIsChangable(false);
        getVolumeType().setIsChangable(false);
        getSize().setIsChangable(false);
        getSize().setEntity(getDisk().getDiskStorageType() == DiskStorageType.IMAGE ?
                ((DiskImage) getDisk()).getSizeInGigabytes() :
                ((LunDisk) getDisk()).getLun().getDeviceSize());

        getIsInternal().setEntity(getDisk().getDiskStorageType() == DiskStorageType.IMAGE);
        getAlias().setEntity(getDisk().getDiskAlias());
        getDescription().setEntity(getDisk().getDiskDescription());
        getIsBootable().setEntity(getDisk().isBoot());
        getIsShareable().setEntity(getDisk().isShareable());
        getIsWipeAfterDelete().setEntity(getDisk().isWipeAfterDelete());

        if (getDisk().getDiskStorageType() == DiskStorageType.IMAGE) {
            DiskImage diskImage = (DiskImage) getDisk();

            getVolumeType().setSelectedItem(diskImage.getVolumeType());
            setVolumeFormat(diskImage.getVolumeFormat());
            Guid storageDomainId = diskImage.getStorageIds().get(0);

            AsyncDataProvider.GetStorageDomainById(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object target, Object returnValue) {
                    DiskModel diskModel = (DiskModel) target;
                    StorageDomain storageDomain = (StorageDomain) returnValue;

                    diskModel.getStorageDomain().setSelectedItem(storageDomain);
                }
            }, getHash()), storageDomainId);
        }
    }

    private void updateSuggestedDiskAlias() {
        if (!getIsNew()) {
            return;
        }

        AsyncDataProvider.GetNextAvailableDiskAliasNameByVMId(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object returnValue) {
                String suggestedDiskAlias = (String) returnValue;
                DiskModel diskModel = (DiskModel) model;
                diskModel.getAlias().setEntity(suggestedDiskAlias);
            }
        }, getHash()), getVm().getId());
    }

    public void quota_storageSelectedItemChanged(final Guid defaultQuotaId) {
        StorageDomain storageDomain = (StorageDomain) getStorageDomain().getSelectedItem();
        if (storageDomain != null) {
            getStorageQuota(defaultQuotaId);
        }
        getStorageDomain().getSelectedItemChangedEvent().addListener(new IEventListener() {

            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                getStorageQuota(defaultQuotaId);
            }
        });
    }

    private void getStorageQuota(final Guid defaultQuota) {
        StorageDomain storageDomain = (StorageDomain) getStorageDomain().getSelectedItem();
        if (storageDomain != null) {
            Frontend.RunQuery(VdcQueryType.GetAllRelevantQuotasForStorage,
                    new GetAllRelevantQuotasForStorageParameters(storageDomain.getId()),
                    new AsyncQuery(this, new INewAsyncCallback() {
                                @Override
                                public void OnSuccess(Object innerModel, Object innerReturnValue) {
                                    ArrayList<Quota> quotaList =
                                            (ArrayList<Quota>) ((VdcQueryReturnValue) innerReturnValue).getReturnValue();
                                    if (quotaList != null && !quotaList.isEmpty()) {
                                        getQuota().setItems(quotaList);
                                    }
                                    if (defaultQuota != null) {
                                        boolean hasQuotaInList = false;
                                        for (Quota quota : quotaList) {
                                            if (quota.getId().equals(defaultQuota)) {
                                                getQuota().setSelectedItem(quota);
                                                hasQuotaInList = true;
                                                break;
                                            }
                                        }
                                        if (!hasQuotaInList) {
                                            Quota quota = new Quota();
                                            quota.setId(defaultQuota);
                                            if (getDisk() instanceof DiskImageBase) {
                                                quota.setQuotaName(((DiskImageBase) getDisk()).getQuotaName());
                                            }
                                            quotaList.add(quota);
                                            getQuota().setItems(quotaList);
                                            getQuota().setSelectedItem(quota);
                                        }
                                    }
                                }
                    }, getHash()));
        }
    }

    public void updateQuota(storage_pool datacenter) {
        if (datacenter.getQuotaEnforcementType().equals(QuotaEnforcementTypeEnum.DISABLED)
                || !(Boolean) getIsInternal().getEntity()) {
            getQuota().setIsAvailable(false);
        } else {
            getQuota().setIsAvailable(true);
            quota_storageSelectedItemChanged(getIsNew() ? null : ((DiskImage) getDisk()).getQuotaId());
        }
    }

    public void updateInterface(storage_pool datacenter) {
        if (datacenter == null) {
            return;
        }

        getDiskInterface().setItems(AsyncDataProvider.GetDiskInterfaceList());
        getDiskInterface().setSelectedItem(getIsNew() ? DiskInterface.VirtIO : getDisk().getDiskInterface());
    }

    private void updateStorageDomains(storage_pool datacenter) {
        AsyncDataProvider.GetPermittedStorageDomainsByStoragePoolId(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {
                DiskModel diskModel = (DiskModel) target;
                ArrayList<StorageDomain> storageDomains = (ArrayList<StorageDomain>) returnValue;

                ArrayList<StorageDomain> filteredStorageDomains = new ArrayList<StorageDomain>();
                for (StorageDomain a : storageDomains)
                {
                    if (a.getStorageDomainType() != StorageDomainType.ISO
                            && a.getStorageDomainType() != StorageDomainType.ImportExport
                            && a.getStatus() == StorageDomainStatus.Active)
                    {
                        filteredStorageDomains.add(a);
                    }
                }

                Linq.Sort(filteredStorageDomains, new Linq.StorageDomainByNameComparer());
                StorageDomain storage = Linq.FirstOrDefault(filteredStorageDomains);

                diskModel.getStorageDomain().setItems(filteredStorageDomains);
                diskModel.getStorageDomain().setSelectedItem(storage);

                if (storage != null) {
                    updateWipeAfterDelete(storage.getStorageType());
                    updateVolumeType(storage.getStorageType().isBlockDomain() ?
                            VolumeType.Preallocated : VolumeType.Sparse);
                    diskModel.setMessage(""); //$NON-NLS-1$
                }
                else {
                    diskModel.setMessage(CONSTANTS.noActiveStorageDomainsInDC());
                }
            }
        }, getHash()), datacenter.getId(), ActionGroup.CREATE_DISK);
    }

    private void updateHosts(storage_pool datacenter) {
        AsyncDataProvider.GetHostListByDataCenter(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {
                DiskModel diskModel = (DiskModel) target;
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
            AsyncDataProvider.GetDataCenterById((new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object target, Object returnValue) {
                    DiskModel diskModel = (DiskModel) target;
                    storage_pool dataCenter = (storage_pool) returnValue;
                    ArrayList<storage_pool> dataCenters = new ArrayList<storage_pool>();

                    if (isDatacenterAvailable(dataCenter) || !getIsNew()) {
                        dataCenters.add(dataCenter);
                    }

                    diskModel.getDataCenter().setItems(dataCenters);

                    if (dataCenters.isEmpty()) {
                        diskModel.setMessage((Boolean) getIsInternal().getEntity() ?
                                CONSTANTS.noActiveStorageDomainsInDC() : CONSTANTS.relevantDCnotActive());
                    }
                }
            }, getHash())), getVm().getStoragePoolId());
        }
        else {
            AsyncDataProvider.GetDataCenterList(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object target, Object returnValue) {
                    DiskModel diskModel = (DiskModel) target;
                    ArrayList<storage_pool> dataCenters = (ArrayList<storage_pool>) returnValue;
                    ArrayList<storage_pool> filteredDataCenters = new ArrayList<storage_pool>();

                    for (storage_pool dataCenter : dataCenters) {
                        if (isDatacenterAvailable(dataCenter) || !getIsNew()) {
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
        AsyncDataProvider.GetVmDiskList(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {
                DiskModel diskModel = (DiskModel) target;
                ArrayList<Disk> disks = (ArrayList<Disk>) returnValue;

                diskModel.getIsBootable().setEntity(true);
                for (Disk disk : disks) {
                    if (disk.isBoot() && !disk.equals(getDisk())) {
                        diskModel.getIsBootable().setChangeProhibitionReason(CONSTANTS.onlyOneBootableDisk());
                        diskModel.getIsBootable().setEntity(false);
                        diskModel.getIsBootable().setIsChangable(false);
                        break;
                    }
                }
            }
        }, getHash()), getVm().getId());
    }

    private void updateShareableDiskEnabled(storage_pool datacenter) {
        boolean isShareableDiskEnabled = (Boolean) AsyncDataProvider.GetConfigValuePreConverted(
                ConfigurationValues.ShareableDiskEnabled, datacenter.getcompatibility_version().getValue());

        getIsShareable().setChangeProhibitionReason(CONSTANTS.shareableDiskNotSupported());
        getIsShareable().setIsChangable(isShareableDiskEnabled);
    }

    private void updateDirectLunDiskEnabled(storage_pool datacenter) {
        boolean isInternal = (Boolean) getIsInternal().getEntity();
        if (isInternal) {
            return;
        }

        boolean isDirectLUNDiskkEnabled = (Boolean) AsyncDataProvider.GetConfigValuePreConverted(
                ConfigurationValues.DirectLUNDiskEnabled, datacenter.getcompatibility_version().getValue());

        getIsDirectLunDiskAvaialable().setEntity(isDirectLUNDiskkEnabled);
        setMessage(!isDirectLUNDiskkEnabled ? CONSTANTS.directLUNDiskNotSupported() : ""); //$NON-NLS-1$
    }

    private void updateWipeAfterDelete(StorageType storageType) {
        if (storageType.isFileDomain()) {
            getIsWipeAfterDelete().setChangeProhibitionReason(CONSTANTS.wipeAfterDeleteNotSupportedForFileDomains());
            getIsWipeAfterDelete().setIsChangable(false);
            getIsWipeAfterDelete().setEntity(false);
        }
        else {
            getIsWipeAfterDelete().setIsChangable(true);
            getIsWipeAfterDelete().setEntity((Boolean) AsyncDataProvider.GetConfigValuePreConverted(ConfigurationValues.SANWipeAfterDelete));
        }

        if (!getIsNew()) {
            getIsWipeAfterDelete().setEntity(getDisk().isWipeAfterDelete());
        }
    }

    private void updateShareable(VolumeType volumeType, StorageType storageType) {
        if (storageType.isBlockDomain() && volumeType == VolumeType.Sparse) {
            getIsShareable().setChangeProhibitionReason(CONSTANTS.shareableDiskNotSupportedByConfiguration());
            getIsShareable().setIsChangable(false);
            getIsShareable().setEntity(false);
        }
        else {
            getIsShareable().setIsChangable(true);
        }
    }

    private void updateVolumeFormat(VolumeType volumeType, StorageType storageType) {
        setVolumeFormat(AsyncDataProvider.GetDiskVolumeFormat(volumeType, storageType));
    }

    private void updateVolumeType(VolumeType volumeType) {
        getVolumeType().setSelectedItem(volumeType);
    }

    private boolean isDatacenterAvailable(storage_pool dataCenter)
    {
        boolean isStatusUp = dataCenter.getstatus() == StoragePoolStatus.Up;

        boolean isInTreeContext = true;
        if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() != SystemTreeItemType.System)
        {
            switch (getSystemTreeSelectedItem().getType())
            {
            case DataCenter:
                storage_pool selectedDataCenter = (storage_pool) getSystemTreeSelectedItem().getEntity();
                isInTreeContext = selectedDataCenter.getId().equals(dataCenter.getId());
            default:
                break;
            }
        }

        return isStatusUp && isInTreeContext;
    }

    private boolean isHostAvailable(VDS host)
    {
        boolean isStatusUp = host.getStatus() == VDSStatus.Up;

        return isStatusUp;
    }

    private void IsInternal_EntityChanged() {
        boolean isInVm = getVm() != null;
        boolean isInternal = (Boolean) getIsInternal().getEntity();

        getSize().setIsAvailable(isInternal);
        getStorageDomain().setIsAvailable(isInternal);
        getVolumeType().setIsAvailable(isInternal);
        getIsWipeAfterDelete().setIsAvailable(isInternal);
        getHost().setIsAvailable(!isInternal);
        getStorageType().setIsAvailable(!isInternal);
        getDataCenter().setIsAvailable(!isInVm);

        if (!isInternal) {
            previousWipeAfterDeleteEntity = (Boolean) getIsWipeAfterDelete().getEntity();
            previousIsQuotaAvailable = getQuota().getIsAvailable();
        }

        getIsWipeAfterDelete().setEntity(isInternal ? previousWipeAfterDeleteEntity : false);
        getQuota().setIsAvailable(isInternal ? previousIsQuotaAvailable : false);

        updateDatacenters();
    }

    private void VolumeType_SelectedItemChanged()
    {
        VolumeType volumeType = (VolumeType) getVolumeType().getSelectedItem();
        StorageType storageType = getStorageDomain().getSelectedItem() == null ? StorageType.UNKNOWN
                : ((StorageDomain) getStorageDomain().getSelectedItem()).getStorageType();

        updateVolumeFormat(volumeType, storageType);
        updateShareable(volumeType, storageType);
    }

    private void WipeAfterDelete_EntityChanged(EventArgs e)
    {
        if (!getIsWipeAfterDelete().getIsChangable() && (Boolean) getIsWipeAfterDelete().getEntity())
        {
            getIsWipeAfterDelete().setEntity(false);
        }
    }

    private void AttachDisk_EntityChanged(EventArgs e)
    {
        if ((Boolean) getIsAttachDisk().getEntity())
        {
            // Get internal attachable disks
            AsyncDataProvider.GetAllAttachableDisks(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object target, Object returnValue) {
                    DiskModel model = (DiskModel) target;
                    ArrayList<Disk> disks = (ArrayList<Disk>) returnValue;
                    Linq.Sort(disks, new Linq.DiskByAliasComparer());
                    ArrayList<DiskModel> diskModels = Linq.DisksToDiskModelList(disks);

                    model.getInternalAttachableDisks().setItems(Linq.ToEntityModelList(
                            Linq.FilterDisksByType(diskModels, DiskStorageType.IMAGE)));
                }
            }, getHash()), getVm().getStoragePoolId(), getVm().getId());

            // Get external attachable disks
            AsyncDataProvider.GetAllAttachableDisks(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object target, Object returnValue) {
                    DiskModel model = (DiskModel) target;
                    ArrayList<Disk> disks = (ArrayList<Disk>) returnValue;
                    Linq.Sort(disks, new Linq.DiskByAliasComparer());
                    ArrayList<DiskModel> diskModels = Linq.DisksToDiskModelList(disks);

                    model.getExternalAttachableDisks().setItems(Linq.ToEntityModelList(
                            Linq.FilterDisksByType(diskModels, DiskStorageType.LUN)));
                }
            }, getHash()), null, getVm().getId());
        }
    }

    private void Datacenter_SelectedItemChanged()
    {
        storage_pool datacenter = (storage_pool) getDataCenter().getSelectedItem();
        boolean isInternal = getIsInternal().getEntity() != null ? (Boolean) getIsInternal().getEntity() : false;

        if (datacenter == null) {
            return;
        }

        updateInterface(datacenter);
        updateQuota(datacenter);
        updateShareableDiskEnabled(datacenter);
        updateDirectLunDiskEnabled(datacenter);

        if (isInternal) {
            updateStorageDomains(datacenter);
        }
        else {
            updateHosts(datacenter);
        }
    }

    public boolean Validate()
    {
        if ((Boolean) getIsAttachDisk().getEntity()) {
            if (isSelectionsEmpty(getInternalAttachableDisks()) && isSelectionsEmpty(getExternalAttachableDisks())) {
                getInvalidityReasons().add(CONSTANTS.noDisksSelected());
                setIsValid(false);
                return false;
            }

            return true;
        }

        StorageType storageType = getStorageDomain().getSelectedItem() == null ? StorageType.UNKNOWN
                : ((StorageDomain) getStorageDomain().getSelectedItem()).getStorageType();

        IntegerValidation sizeValidation = new IntegerValidation();
        sizeValidation.setMinimum(1);
        if (storageType == StorageType.ISCSI || storageType == StorageType.FCP) {
            sizeValidation.setMaximum((Integer) AsyncDataProvider.GetConfigValuePreConverted(ConfigurationValues.MaxBlockDiskSize));
        }
        getSize().ValidateEntity(new IValidation[] { new NotEmptyValidation(), sizeValidation });

        getStorageDomain().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        getDescription().ValidateEntity(new IValidation[] { new SpecialAsciiI18NOrNoneValidation() });

        if (getVm() == null) {
            getAlias().ValidateEntity(new IValidation[] { new NotEmptyValidation(), new I18NNameValidation() });
        }
        else {
            getAlias().ValidateEntity(new IValidation[] { new I18NNameValidation() });
        }

        boolean isSanStorageModelValid = true;
        if (!(Boolean) isInternal.getEntity() && getSanStorageModel() != null && getIsNew()) {
            getSanStorageModel().Validate();
            isSanStorageModelValid = getSanStorageModel().getIsValid();
        }

        storage_pool dataCenter = (storage_pool) getDataCenter().getSelectedItem();
        if (dataCenter != null && dataCenter.getQuotaEnforcementType() == QuotaEnforcementTypeEnum.HARD_ENFORCEMENT) {
            getQuota().ValidateSelectedItem(new IValidation[] { new NotEmptyQuotaValidation() });
        }

        return getSize().getIsValid() && getStorageDomain().getIsValid() && getAlias().getIsValid()
                && isSanStorageModelValid && getQuota().getIsValid();
    }

    private boolean isSelectionsEmpty(ListModel listModel) {
        return listModel.getSelectedItems() == null || listModel.getSelectedItems().isEmpty();
    }

    private void ForceCreationWarning(ArrayList<String> usedLunsMessages) {
        ConfirmationModel confirmationModel = new ConfirmationModel();
        setConfirmWindow(confirmationModel);

        confirmationModel.setTitle(CONSTANTS.forceStorageDomainCreation());
        confirmationModel.setMessage(CONSTANTS.lunsAlreadyPartOfSD());
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

    private void OnAttachDisks()
    {
        ArrayList<VdcActionType> actionTypes = new ArrayList<VdcActionType>();
        ArrayList<VdcActionParametersBase> paramerterList = new ArrayList<VdcActionParametersBase>();
        ArrayList<IFrontendActionAsyncCallback> callbacks = new ArrayList<IFrontendActionAsyncCallback>();

        IFrontendActionAsyncCallback onFinishCallback = new IFrontendActionAsyncCallback() {
            @Override
            public void Executed(FrontendActionAsyncResult result) {
                DiskModel diskModel = (DiskModel) result.getState();
                diskModel.StopProgress();
                diskModel.cancel();
            }
        };

        ArrayList<EntityModel> disksToAttach = (Boolean) getIsInternal().getEntity() ?
                (ArrayList<EntityModel>) getInternalAttachableDisks().getSelectedItems() :
                (ArrayList<EntityModel>) getExternalAttachableDisks().getSelectedItems();

        for (int i = 0; i < disksToAttach.size(); i++) {
            DiskModel disk = (DiskModel) disksToAttach.get(i).getEntity();
            AttachDettachVmDiskParameters parameters = new AttachDettachVmDiskParameters(
                    getVm().getId(), disk.getDisk().getId(), (Boolean) getIsPlugged().getEntity());

            actionTypes.add(VdcActionType.AttachDiskToVm);
            paramerterList.add(parameters);
            callbacks.add(i == disksToAttach.size() - 1 ? onFinishCallback : null);
        }

        StartProgress(null);

        Frontend.RunMultipleActions(actionTypes, paramerterList, callbacks, null, this);
    }

    private void onForceSave() {
        ConfirmationModel confirmationModel = (ConfirmationModel) getConfirmWindow();
        if (confirmationModel != null && !confirmationModel.Validate()) {
            return;
        }
        cancelConfirm();

        getSanStorageModel().setForce(true);
        onSave();
    }

    private void cancelConfirm() {
        setConfirmWindow(null);
    }

    private void cancel() {
        getCancelCommand().Execute();
    }

    public void onSave() {
        if (getProgress() != null || !Validate()) {
            return;
        }

        if ((Boolean) getIsAttachDisk().getEntity()) {
            OnAttachDisks();
            return;
        }

        boolean isInternal = (Boolean) getIsInternal().getEntity();
        Disk disk = null;

        if (isInternal) {
            DiskImage diskImage = getIsNew() ? new DiskImage() : (DiskImage) getDisk();
            if (getQuota().getIsAvailable() && getQuota().getSelectedItem() != null) {
                diskImage.setQuotaId(((Quota) getQuota().getSelectedItem()).getId());
            }
            diskImage.setSizeInGigabytes(Integer.parseInt(getSize().getEntity().toString()));
            diskImage.setVolumeType((VolumeType) getVolumeType().getSelectedItem());
            diskImage.setvolumeFormat(getVolumeFormat());

            disk = diskImage;
        }
        else {
            LunDisk lunDisk = getIsNew() ? new LunDisk() : (LunDisk) getDisk();
            ArrayList<String> partOfSdLunsMessages = getSanStorageModel().getPartOfSdLunsMessages();
            if (!partOfSdLunsMessages.isEmpty() && !getSanStorageModel().isForce()) {
                ForceCreationWarning(partOfSdLunsMessages);
                return;
            }
            LUNs luns = (LUNs) getSanStorageModel().getAddedLuns().get(0).getEntity();
            luns.setLunType((StorageType) getStorageType().getSelectedItem());
            lunDisk.setLun(luns);

            disk = lunDisk;
        }

        disk.setDiskAlias((String) getAlias().getEntity());
        disk.setDiskDescription((String) getDescription().getEntity());
        disk.setDiskInterface((DiskInterface) getDiskInterface().getSelectedItem());
        disk.setWipeAfterDelete((Boolean) getIsWipeAfterDelete().getEntity());
        disk.setBoot((Boolean) getIsBootable().getEntity());
        disk.setShareable((Boolean) getIsShareable().getEntity());
        disk.setPlugged((Boolean) getIsPlugged().getEntity());
        disk.setPropagateErrors(PropagateErrors.Off);

        VdcActionType actionType;
        VmDiskOperationParameterBase parameters;
        Guid vmId = getVm() != null ? getVm().getId() : Guid.Empty;

        if (getIsNew()) {
            actionType = VdcActionType.AddDisk;
            parameters = new AddDiskParameters(vmId, disk);

            if (isInternal) {
                StorageDomain storageDomain = (StorageDomain) getStorageDomain().getSelectedItem();
                ((AddDiskParameters) parameters).setStorageDomainId(storageDomain.getId());
            }
        }
        else {
            actionType = VdcActionType.UpdateVmDisk;
            parameters = new UpdateVmDiskParameters(vmId, disk.getId(), disk);
        }

        StartProgress(null);

        Frontend.RunAction(actionType, parameters, new IFrontendActionAsyncCallback() {
            @Override
            public void Executed(FrontendActionAsyncResult result) {
                DiskModel diskModel = (DiskModel) result.getState();
                diskModel.StopProgress();
                diskModel.cancel();
            }
        }, this);
    }

    @Override
    public void ExecuteCommand(UICommand command) {
        super.ExecuteCommand(command);

        if (StringHelper.stringsEqual(command.getName(), "OnSave")) { //$NON-NLS-1$
            onSave();
        }
        if (StringHelper.stringsEqual(command.getName(), "OnForceSave")) { //$NON-NLS-1$
            onForceSave();
        }
        else if (StringHelper.stringsEqual(command.getName(), "CancelConfirm")) { //$NON-NLS-1$
            cancelConfirm();
        }
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(EntityModel.EntityChangedEventDefinition) && sender == getIsWipeAfterDelete())
        {
            WipeAfterDelete_EntityChanged(args);
        }
        else if (ev.matchesDefinition(EntityModel.EntityChangedEventDefinition) && sender == getIsAttachDisk())
        {
            AttachDisk_EntityChanged(args);
        }
        else if (ev.matchesDefinition(ListModel.EntityChangedEventDefinition) && sender == getIsInternal())
        {
            IsInternal_EntityChanged();
        }
        else if (ev.matchesDefinition(ListModel.SelectedItemChangedEventDefinition) && sender == getVolumeType())
        {
            VolumeType_SelectedItemChanged();
        }
        else if (ev.matchesDefinition(ListModel.SelectedItemChangedEventDefinition) && sender == getDataCenter())
        {
            Datacenter_SelectedItemChanged();
        }
        else if (ev.matchesDefinition(Frontend.QueryStartedEventDefinition)
                && StringHelper.stringsEqual(Frontend.getCurrentContext(), getHash()))
        {
            Frontend_QueryStarted();
        }
        else if (ev.matchesDefinition(Frontend.QueryCompleteEventDefinition)
                && StringHelper.stringsEqual(Frontend.getCurrentContext(), getHash()))
        {
            Frontend_QueryComplete();
        }
    }

    private int queryCounter;

    public void Frontend_QueryStarted()
    {
        queryCounter++;
        if (getProgress() == null) {
            StartProgress(null);
        }
    }

    public void Frontend_QueryComplete()
    {
        queryCounter--;
        if (queryCounter == 0) {
            StopProgress();
        }
    }

}
