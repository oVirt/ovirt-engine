package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
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
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
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
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModel;
import org.ovirt.engine.ui.uicommonweb.validation.I18NNameValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyQuotaValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.SpecialAsciiI18NOrNoneValidation;
import org.ovirt.engine.ui.uicompat.Constants;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;

public abstract class AbstractDiskModel extends DiskModel
{
    protected static final Constants CONSTANTS = ConstantsManager.getInstance().getConstants();

    private EntityModel isWipeAfterDelete;
    private EntityModel isBootable;
    private EntityModel isShareable;
    private EntityModel isPlugged;
    private EntityModel isAttachDisk;
    private EntityModel isInternal;
    private EntityModel isDirectLunDiskAvaialable;

    private ListModel storageType;
    private ListModel host;
    private ListModel dataCenter;
    private ListModel internalAttachableDisks;
    private ListModel externalAttachableDisks;

    private SanStorageModel sanStorageModel;
    private VolumeFormat volumeFormat;
    private boolean previousWipeAfterDeleteEntity;
    private boolean previousIsQuotaAvailable;

    private SystemTreeItemModel systemTreeSelectedItem;
    private String hash;
    private UICommand cancelCommand;
    private int queryCounter;

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

    public ListModel getStorageType() {
        return storageType;
    }

    public void setStorageType(ListModel storageType) {
        this.storageType = storageType;
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
        setIsAttachDisk(new EntityModel());
        getIsAttachDisk().setEntity(false);
        getIsAttachDisk().getEntityChangedEvent().addListener(this);

        setIsInternal(new EntityModel());
        getIsInternal().setEntity(true);
        getIsInternal().getEntityChangedEvent().addListener(this);

        setIsWipeAfterDelete(new EntityModel());
        getIsWipeAfterDelete().setEntity(false);
        getIsWipeAfterDelete().getEntityChangedEvent().addListener(this);

        setIsBootable(new EntityModel());
        getIsBootable().setEntity(false);

        setIsShareable(new EntityModel());
        getIsShareable().setEntity(false);

        setIsPlugged(new EntityModel());
        getIsPlugged().setEntity(true);

        setIsDirectLunDiskAvaialable(new EntityModel());
        getIsDirectLunDiskAvaialable().setEntity(true);

        setDataCenter(new ListModel());
        getDataCenter().setIsAvailable(false);
        getDataCenter().getSelectedItemChangedEvent().addListener(this);

        setStorageType(new ListModel());
        getStorageType().setIsAvailable(false);
        getStorageType().setItems(AsyncDataProvider.getStorageTypeList());
        getStorageType().getSelectedItemChangedEvent().addListener(this);

        setHost(new ListModel());
        getHost().setIsAvailable(false);

        getVolumeType().getSelectedItemChangedEvent().addListener(this);

        setInternalAttachableDisks(new ListModel());
        setExternalAttachableDisks(new ListModel());
    }

    public abstract boolean getIsNew();

    protected abstract boolean isDatacenterAvailable(StoragePool dataCenter);

    protected abstract void updateWipeAfterDelete(StorageType storageType);

    protected abstract void updateInterface(StoragePool datacenter);

    protected abstract DiskImage getDiskImage();

    protected abstract LunDisk getLunDisk();

    @Override
    public void initialize() {
        super.initialize();
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
            updateBootableDiskAvailable();
        }
        updateDatacenters();
    }

    private void updateStorageDomains(final StoragePool datacenter) {
        AsyncDataProvider.getPermittedStorageDomainsByStoragePoolId(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
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

                Linq.sort(filteredStorageDomains, new Linq.StorageDomainByNameComparer());
                StorageDomain storage = Linq.firstOrDefault(filteredStorageDomains);

                diskModel.getStorageDomain().setItems(filteredStorageDomains);
                diskModel.getStorageDomain().setSelectedItem(storage);

                if (storage != null) {
                    updateWipeAfterDelete(storage.getStorageType());
                    diskModel.setMessage(""); //$NON-NLS-1$
                }
                else {
                    diskModel.setMessage(CONSTANTS.noActiveStorageDomainsInDC());
                }

                updateQuota(datacenter);
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
                        diskModel.setMessage((Boolean) getIsInternal().getEntity() ?
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
                for (Disk disk : disks) {
                    if (disk.isBoot() && !disk.equals(getDisk())) {
                        diskModel.getIsBootable().setChangeProhibitionReason(CONSTANTS.onlyOneBootableDisk());
                        diskModel.getIsBootable().setEntity(false);
                        diskModel.getIsBootable().setIsChangable(false);
                        break;
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
        getIsShareable().setIsChangable(isShareableDiskEnabled);
    }

    private void updateDirectLunDiskEnabled(StoragePool datacenter) {
        boolean isInternal = (Boolean) getIsInternal().getEntity();
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
            getIsShareable().setIsChangable(true);
        }
    }

    private void updateVolumeFormat(VolumeType volumeType, StorageType storageType) {
        setVolumeFormat(AsyncDataProvider.getDiskVolumeFormat(volumeType, storageType));
    }

    private void updateVolumeType(StorageType storageType) {
        getVolumeType().setSelectedItem(storageType.isBlockDomain() ? VolumeType.Preallocated : VolumeType.Sparse);
        volumeType_SelectedItemChanged();
    }

    private void updateQuota(StoragePool datacenter) {
        if (datacenter.getQuotaEnforcementType().equals(QuotaEnforcementTypeEnum.DISABLED)
                || !(Boolean) getIsInternal().getEntity()) {
            getQuota().setIsAvailable(false);
            return;
        }

        getQuota().setIsAvailable(true);
        StorageDomain storageDomain = (StorageDomain) getStorageDomain().getSelectedItem();
        if (storageDomain == null) {
            return;
        }

        GetAllRelevantQuotasForStorageParameters parameters =
                new GetAllRelevantQuotasForStorageParameters(storageDomain.getId());
        Frontend.RunQuery(VdcQueryType.GetAllRelevantQuotasForStorage, parameters, new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object innerModel, Object innerReturnValue) {
                        ArrayList<Quota> quotaList =
                                (ArrayList<Quota>) ((VdcQueryReturnValue) innerReturnValue).getReturnValue();
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

    private void volumeType_SelectedItemChanged() {
        if (getVolumeType().getSelectedItem() == null || getDataCenter().getSelectedItem() == null) {
            return;
        }

        VolumeType volumeType = (VolumeType) getVolumeType().getSelectedItem();
        StorageType storageType = ((StoragePool) getDataCenter().getSelectedItem()).getstorage_pool_type();

        updateVolumeFormat(volumeType, storageType);
        updateShareable(volumeType, storageType);
    }

    private void wipeAfterDelete_EntityChanged(EventArgs e) {
        if (!getIsWipeAfterDelete().getIsChangable() && (Boolean) getIsWipeAfterDelete().getEntity())
        {
            getIsWipeAfterDelete().setEntity(false);
        }
    }

    private void attachDisk_EntityChanged(EventArgs e) {
        if ((Boolean) getIsAttachDisk().getEntity())
        {
            // Get internal attachable disks
            AsyncDataProvider.getAllAttachableDisks(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void onSuccess(Object target, Object returnValue) {
                    AbstractDiskModel model = (AbstractDiskModel) target;
                    ArrayList<Disk> disks = (ArrayList<Disk>) returnValue;
                    Linq.sort(disks, new Linq.DiskByAliasComparer());
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
                    Linq.sort(disks, new Linq.DiskByAliasComparer());
                    ArrayList<DiskModel> diskModels = Linq.disksToDiskModelList(disks);

                    model.getExternalAttachableDisks().setItems(Linq.toEntityModelList(
                            Linq.filterDisksByType(diskModels, DiskStorageType.LUN)));
                }
            }, getHash()), null, getVm().getId());
        }
    }

    private void datacenter_SelectedItemChanged() {
        StoragePool datacenter = (StoragePool) getDataCenter().getSelectedItem();
        boolean isInternal = getIsInternal().getEntity() != null ? (Boolean) getIsInternal().getEntity() : false;

        if (datacenter == null) {
            return;
        }

        updateInterface(datacenter);
        updateVolumeType(datacenter.getstorage_pool_type());
        updateShareableDiskEnabled(datacenter);
        updateDirectLunDiskEnabled(datacenter);

        if (isInternal) {
            updateStorageDomains(datacenter);
        }
        else {
            updateHosts(datacenter);
        }
    }

    public boolean validate() {
        getDescription().validateEntity(new IValidation[] { new SpecialAsciiI18NOrNoneValidation() });

        if (getVm() == null) {
            getAlias().validateEntity(new IValidation[] { new NotEmptyValidation(), new I18NNameValidation() });
        }
        else {
            getAlias().validateEntity(new IValidation[] { new I18NNameValidation() });
        }

        StoragePool dataCenter = (StoragePool) getDataCenter().getSelectedItem();
        if (dataCenter != null && dataCenter.getQuotaEnforcementType() == QuotaEnforcementTypeEnum.HARD_ENFORCEMENT) {
            getQuota().validateSelectedItem(new IValidation[] { new NotEmptyQuotaValidation() });
        }

        return getAlias().getIsValid() && getDescription().getIsValid() && getQuota().getIsValid();
    }

    protected void forceCreationWarning(ArrayList<String> usedLunsMessages) {
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
        boolean isInternal = (Boolean) getIsInternal().getEntity();
        if (isInternal) {
            DiskImage diskImage = getDiskImage();
            if (getQuota().getIsAvailable() && getQuota().getSelectedItem() != null) {
                diskImage.setQuotaId(((Quota) getQuota().getSelectedItem()).getId());
            }
            setDisk(diskImage);
        }
        else {
            LunDisk lunDisk = getLunDisk();
            setDisk(lunDisk);
        }

        getDisk().setDiskAlias((String) getAlias().getEntity());
        getDisk().setDiskDescription((String) getDescription().getEntity());
        getDisk().setDiskInterface((DiskInterface) getDiskInterface().getSelectedItem());
        getDisk().setWipeAfterDelete((Boolean) getIsWipeAfterDelete().getEntity());
        getDisk().setBoot((Boolean) getIsBootable().getEntity());
        getDisk().setShareable((Boolean) getIsShareable().getEntity());
        getDisk().setPlugged((Boolean) getIsPlugged().getEntity());
        getDisk().setPropagateErrors(PropagateErrors.Off);
    }

    @Override
    public void executeCommand(UICommand command) {
        super.executeCommand(command);

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
    public void eventRaised(Event ev, Object sender, EventArgs args) {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(EntityModel.EntityChangedEventDefinition) && sender == getIsWipeAfterDelete())
        {
            wipeAfterDelete_EntityChanged(args);
        }
        else if (ev.matchesDefinition(EntityModel.EntityChangedEventDefinition) && sender == getIsAttachDisk())
        {
            attachDisk_EntityChanged(args);
        }
        else if (ev.matchesDefinition(ListModel.EntityChangedEventDefinition) && sender == getIsInternal())
        {
            isInternal_EntityChanged();
        }
        else if (ev.matchesDefinition(ListModel.selectedItemChangedEventDefinition) && sender == getVolumeType())
        {
            volumeType_SelectedItemChanged();
        }
        else if (ev.matchesDefinition(ListModel.selectedItemChangedEventDefinition) && sender == getDataCenter())
        {
            datacenter_SelectedItemChanged();
        }
        else if (ev.matchesDefinition(Frontend.QueryStartedEventDefinition)
                && StringHelper.stringsEqual(Frontend.getCurrentContext(), getHash()))
        {
            frontend_QueryStarted();
        }
        else if (ev.matchesDefinition(Frontend.QueryCompleteEventDefinition)
                && StringHelper.stringsEqual(Frontend.getCurrentContext(), getHash()))
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
