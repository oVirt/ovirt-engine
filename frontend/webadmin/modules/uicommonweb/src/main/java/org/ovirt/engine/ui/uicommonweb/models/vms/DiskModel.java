package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Date;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
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
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModel;
import org.ovirt.engine.ui.uicommonweb.validation.AsciiOrNoneValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyQuotaValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class DiskModel extends Model
{
    private static int maxDiskSize;

    private boolean privateIsNew;

    public boolean getIsNew()
    {
        return privateIsNew;
    }

    public void setIsNew(boolean value)
    {
        privateIsNew = value;
    }

    private String privateName;

    public String getName()
    {
        return privateName;
    }

    public void setName(String value)
    {
        privateName = value;
    }

    private EntityModel privateAlias;

    public EntityModel getAlias()
    {
        return privateAlias;
    }

    public void setAlias(EntityModel value)
    {
        privateAlias = value;
    }

    private EntityModel privateDescription;

    public EntityModel getDescription()
    {
        return privateDescription;
    }

    public void setDescription(EntityModel value)
    {
        privateDescription = value;
    }

    private VolumeFormat privateVolumeFormat = getVolumeFormat().values()[0];

    public VolumeFormat getVolumeFormat()
    {
        return privateVolumeFormat;
    }

    public void setVolumeFormat(VolumeFormat value)
    {
        privateVolumeFormat = value;
    }

    private Date privateCreationDate = new Date(0);

    public Date getCreationDate()
    {
        return privateCreationDate;
    }

    public void setCreationDate(Date value)
    {
        privateCreationDate = value;
    }

    private int privateActualSize;

    public int getActualSize()
    {
        return privateActualSize;
    }

    public void setActualSize(int value)
    {
        privateActualSize = value;
    }

    private EntityModel privateSize;

    public EntityModel getSize()
    {
        return privateSize;
    }

    public void setSize(EntityModel value)
    {
        privateSize = value;
    }

    private ListModel privatePreset;

    public ListModel getPreset()
    {
        return privatePreset;
    }

    public void setPreset(ListModel value)
    {
        privatePreset = value;
    }

    private ListModel privateVolumeType;

    public ListModel getVolumeType()
    {
        return privateVolumeType;
    }

    public void setVolumeType(ListModel value)
    {
        privateVolumeType = value;
    }

    private ListModel storageType;

    public ListModel getStorageType()
    {
        return storageType;
    }

    public void setStorageType(ListModel value)
    {
        storageType = value;
    }

    private ListModel privateInterface;

    public ListModel getInterface()
    {
        return privateInterface;
    }

    public void setInterface(ListModel value)
    {
        privateInterface = value;
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

    private ListModel privateStorageDomain;

    public ListModel getStorageDomain()
    {
        return privateStorageDomain;
    }

    public void setStorageDomain(ListModel value)
    {
        privateStorageDomain = value;
    }

    private ListModel privateHost;

    public ListModel getHost()
    {
        return privateHost;
    }

    private void setHost(ListModel value)
    {
        privateHost = value;
    }

    private EntityModel privateWipeAfterDelete;

    public EntityModel getWipeAfterDelete()
    {
        return privateWipeAfterDelete;
    }

    public void setWipeAfterDelete(EntityModel value)
    {
        privateWipeAfterDelete = value;
    }

    private EntityModel privateIsBootable;

    public EntityModel getIsBootable()
    {
        return privateIsBootable;
    }

    public void setIsBootable(EntityModel value)
    {
        privateIsBootable = value;
    }

    private EntityModel privateIsShareable;

    public EntityModel getIsShareable()
    {
        return privateIsShareable;
    }

    public void setIsShareable(EntityModel value)
    {
        privateIsShareable = value;
    }

    private EntityModel privateIsPlugged;

    public EntityModel getIsPlugged()
    {
        return privateIsPlugged;
    }

    public void setIsPlugged(EntityModel value)
    {
        privateIsPlugged = value;
    }

    private Disk privateDisk;

    public Disk getDisk()
    {
        return privateDisk;
    }

    public void setDisk(Disk value)
    {
        privateDisk = value;
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

    private ListModel privateDataCenter;

    public ListModel getDataCenter()
    {
        return privateDataCenter;
    }

    private void setDataCenter(ListModel value)
    {
        privateDataCenter = value;
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

    private EntityModel privateAttachDisk;

    public EntityModel getAttachDisk()
    {
        return privateAttachDisk;
    }

    public void setAttachDisk(EntityModel value)
    {
        privateAttachDisk = value;
    }

    private ListModel internalAttachableDisks;

    public ListModel getInternalAttachableDisks()
    {
        return internalAttachableDisks;
    }

    public void setInternalAttachableDisks(ListModel value)
    {
        internalAttachableDisks = value;
    }

    private ListModel externalAttachableDisks;

    public ListModel getExternalAttachableDisks()
    {
        return externalAttachableDisks;
    }

    public void setExternalAttachableDisks(ListModel value)
    {
        externalAttachableDisks = value;
    }

    private Guid datacenterId;

    public Guid getDatacenterId()
    {
        return datacenterId;
    }

    public void setDatacenterId(Guid value)
    {
        datacenterId = value;
    }

    private EntityModel isInVm;

    public EntityModel getIsInVm()
    {
        return isInVm;
    }

    public void setIsInVm(EntityModel value)
    {
        isInVm = value;
    }

    private EntityModel isInternal;

    public EntityModel getIsInternal()
    {
        return isInternal;
    }

    public void setIsInternal(EntityModel value)
    {
        isInternal = value;
    }

    private EntityModel isDirectLunDiskAvaialable;

    public EntityModel getIsDirectLunDiskAvaialable()
    {
        return isDirectLunDiskAvaialable;
    }

    public void setIsDirectLunDiskAvaialable(EntityModel value)
    {
        isDirectLunDiskAvaialable = value;
    }

    private SanStorageModel sanStorageModel;

    public SanStorageModel getSanStorageModel()
    {
        return sanStorageModel;
    }

    public void setSanStorageModel(SanStorageModel value)
    {
        sanStorageModel = value;
    }

    private SystemTreeItemModel systemTreeSelectedItem;

    public SystemTreeItemModel getSystemTreeSelectedItem()
    {
        return systemTreeSelectedItem;
    }

    public void setSystemTreeSelectedItem(SystemTreeItemModel value)
    {
        systemTreeSelectedItem = value;
        OnPropertyChanged(new PropertyChangedEventArgs("SystemTreeSelectedItem")); //$NON-NLS-1$
    }

    private boolean isWipeAfterDeleteChangable;
    private boolean oldWipeAfterDeleteValue;
    private boolean isQuotaAvailable;
    private Guid vmId;

    public Guid getVmId() {
        return vmId;
    }

    public void setVmId(Guid vmId) {
        this.vmId = vmId;
    }

    public DiskModel()
    {
        setSize(new EntityModel());
        getSize().setIsValid(true);

        setInterface(new ListModel());
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

        setPreset(new ListModel());
        getPreset().getSelectedItemChangedEvent().addListener(this);

        setVolumeType(new ListModel());
        getVolumeType().setItems(AsyncDataProvider.GetVolumeTypeList());
        getVolumeType().getSelectedItemChangedEvent().addListener(this);

        setStorageType(new ListModel());
        getStorageType().setIsAvailable(false);
        getStorageType().setItems(AsyncDataProvider.GetStorageTypeList());
        getStorageType().getSelectedItemChangedEvent().addListener(this);

        setWipeAfterDelete(new EntityModel());
        getWipeAfterDelete().setEntity(false);
        getWipeAfterDelete().getEntityChangedEvent().addListener(this);

        setIsBootable(new EntityModel());
        getIsBootable().setEntity(false);

        setIsShareable(new EntityModel());
        getIsShareable().setEntity(false);

        setIsPlugged(new EntityModel());
        getIsPlugged().setEntity(true);

        setAttachDisk(new EntityModel());
        getAttachDisk().setEntity(false);
        getAttachDisk().getEntityChangedEvent().addListener(this);

        setQuota(new ListModel());
        getQuota().setIsAvailable(false);

        setAlias(new EntityModel());
        setDescription(new EntityModel());
        setInternalAttachableDisks(new ListModel());
        setExternalAttachableDisks(new ListModel());

        setIsInVm(new EntityModel());
        getIsInVm().getEntityChangedEvent().addListener(this);

        setIsInternal(new EntityModel());
        getIsInternal().getEntityChangedEvent().addListener(this);

        setIsDirectLunDiskAvaialable(new EntityModel());
        getIsDirectLunDiskAvaialable().setEntity(true);

        setIsNew(true);

        AsyncDataProvider.GetDiskMaxSize(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        maxDiskSize = ((Integer) returnValue);
                    }
                }));
    }

    public DiskModel(SystemTreeItemModel systemTreeSelectedItem) {
        this();
        setSystemTreeSelectedItem(systemTreeSelectedItem);
    }

    public void quota_storageSelectedItemChanged(final Guid defaultQuotaId) {
        storage_domains storageDomain = (storage_domains) getStorageDomain().getSelectedItem();
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
        storage_domains storageDomain = (storage_domains) getStorageDomain().getSelectedItem();
        if (storageDomain != null) {
            Frontend.RunQuery(VdcQueryType.GetAllRelevantQuotasForStorage,
                    new GetAllRelevantQuotasForStorageParameters(storageDomain.getId()),
                    new AsyncQuery(this,
                            new INewAsyncCallback() {

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
                            }));
        }
    }

    public void updateQuota(storage_pool datacenter) {
        if (datacenter.getQuotaEnforcementType().equals(QuotaEnforcementTypeEnum.DISABLED)
                || !(Boolean) getIsInternal().getEntity()) {
            getQuota().setIsAvailable(false);
        } else {
            getQuota().setIsAvailable(true);
            quota_storageSelectedItemChanged(null);
        }
    }

    public void updateInterface(storage_pool datacenter) {
        if (datacenter == null) {
            return;
        }

        getInterface().setItems(AsyncDataProvider.GetDiskInterfaceList(
                VmOsType.Unassigned, datacenter.getcompatibility_version()));
        getInterface().setSelectedItem(AsyncDataProvider.GetDefaultDiskInterface(
                VmOsType.Unassigned, null));
    }

    private void updateStorageDomains(storage_pool datacenter) {
        AsyncDataProvider.GetPermittedStorageDomainsByStoragePoolId(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {
                DiskModel diskModel = (DiskModel) target;
                ArrayList<storage_domains> storageDomains = (ArrayList<storage_domains>) returnValue;

                ArrayList<storage_domains> filteredStorageDomains = new ArrayList<storage_domains>();
                for (storage_domains a : storageDomains)
                {
                    if (a.getstorage_domain_type() != StorageDomainType.ISO
                            && a.getstorage_domain_type() != StorageDomainType.ImportExport
                            && a.getstatus() == StorageDomainStatus.Active)
                    {
                        filteredStorageDomains.add(a);
                    }
                }

                Linq.Sort(filteredStorageDomains, new Linq.StorageDomainByNameComparer());
                storage_domains storage = Linq.FirstOrDefault(filteredStorageDomains);
                StorageType storageType = storage == null ? StorageType.UNKNOWN : storage.getstorage_type();
                boolean isInternal = (Boolean) getIsInternal().getEntity();

                diskModel.getStorageDomain().setItems(filteredStorageDomains);
                diskModel.getStorageDomain().setSelectedItem(storage);

                if (storage != null) {
                    updateWipeAfterDelete(storage.getstorage_type(), diskModel.getWipeAfterDelete());
                    diskModel.setMessage(""); //$NON-NLS-1$
                }
                else if (isInternal) {
                    diskModel.setMessage(ConstantsManager.getInstance().getConstants().noActiveStorageDomainsInDC());
                }

                AsyncDataProvider.GetDiskPresetList(new AsyncQuery(diskModel, new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        DiskModel diskModel1 = (DiskModel) target;
                        ArrayList<DiskImageBase> presets = (ArrayList<DiskImageBase>) returnValue;

                        diskModel1.getPreset().setItems(presets);
                        diskModel1.getPreset().setSelectedItem(Linq.FirstOrDefault(presets));

                        diskModel1.StopProgress();
                    }
                }), storageType);
            }
        }), datacenter.getId(), ActionGroup.CREATE_DISK);
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

                StopProgress();
            }
        }), datacenter.getId());
    }

    private void updateDatacenters() {
        final boolean isInVm = getIsInVm().getEntity() != null ? (Boolean) getIsInVm().getEntity() : false;
        final boolean isInternal = getIsInternal().getEntity() != null ? (Boolean) getIsInternal().getEntity() : false;

        getDataCenter().setIsAvailable(!isInVm);
        setMessage(""); //$NON-NLS-1$

        if (isInternal) {
            StartProgress(null);
        }

        if (isInVm) {
            AsyncDataProvider.GetDataCenterById((new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object target, Object returnValue) {
                    DiskModel diskModel = (DiskModel) target;
                    storage_pool dataCenter = (storage_pool) returnValue;
                    ArrayList<storage_pool> dataCenters = new ArrayList<storage_pool>();

                    if (isDatacenterAvailable(dataCenter)) {
                        dataCenters.add(dataCenter);
                    }

                    diskModel.getDataCenter().setItems(dataCenters);

                    if (dataCenters.isEmpty()) {
                        diskModel.setMessage(ConstantsManager.getInstance().getConstants().noActiveStorageDomainsInDC());
                        StopProgress();
                    }
                }
            })), getDatacenterId());
        }
        else {
            AsyncDataProvider.GetDataCenterList(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object target, Object returnValue) {
                    DiskModel diskModel = (DiskModel) target;
                    ArrayList<storage_pool> dataCenters = (ArrayList<storage_pool>) returnValue;
                    ArrayList<storage_pool> filteredDataCenters = new ArrayList<storage_pool>();

                    for (storage_pool dataCenter : dataCenters) {
                        if (isDatacenterAvailable(dataCenter)) {
                            filteredDataCenters.add(dataCenter);
                        }
                    }

                    diskModel.getDataCenter().setItems(filteredDataCenters);

                    if (filteredDataCenters.isEmpty()) {
                        diskModel.setMessage(ConstantsManager.getInstance().getConstants().noActiveDataCenters());
                        StopProgress();
                    }
                }
            }));
        }
    }

    private void updateShareableDiskEnabled(storage_pool datacenter) {
        AsyncDataProvider.IsShareableDiskEnabled(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {
                DiskModel diskModel = (DiskModel) target;
                boolean isShareableDiskEnabled = (Boolean) returnValue;

                diskModel.getIsShareable().setIsChangable(isShareableDiskEnabled);
                diskModel.getIsShareable().getChangeProhibitionReasons().add(
                        ConstantsManager.getInstance().getConstants().shareableDiskNotSupported());
            }
        }), datacenter.getcompatibility_version().getValue());
    }

    private void updateDirectLunDiskEnabled(storage_pool datacenter) {
        boolean isInternal = getIsInternal().getEntity() != null ? (Boolean) getIsInternal().getEntity() : false;

        if (!isInternal) {
            AsyncDataProvider.IsDirectLunDiskEnabled(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object target, Object returnValue) {
                    DiskModel diskModel1 = (DiskModel) target;
                    boolean isDirectLUNDiskkEnabled = (Boolean) returnValue;

                    getIsDirectLunDiskAvaialable().setEntity(isDirectLUNDiskkEnabled);
                    diskModel1.setMessage(!isDirectLUNDiskkEnabled ?
                            ConstantsManager.getInstance().getConstants().directLUNDiskNotSupported() : ""); //$NON-NLS-1$
                }
            }), datacenter.getcompatibility_version().toString());
        }
    }

    private void updateWipeAfterDelete(StorageType storageType, EntityModel wipeAfterDeleteModel)
    {
        if (storageType.isFileDomain()) {
            wipeAfterDeleteModel.setIsChangable(false);
        }
        else {
            wipeAfterDeleteModel.setIsChangable(true);
            AsyncDataProvider.GetSANWipeAfterDelete(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object target, Object returnValue) {
                    DiskModel diskModel = (DiskModel) target;
                    diskModel.getWipeAfterDelete().setEntity(returnValue);
                }
            }));
        }
    }

    private void updateVolumeFormat(VolumeType volumeType, StorageType storageType)
    {
        setVolumeFormat(AsyncDataProvider.GetDiskVolumeFormat(volumeType, storageType));
    }

    private void updateShareable(VolumeType volumeType, StorageType storageType) {
        getIsShareable().setEntity(false);
        getIsShareable().setIsChangable(!(storageType.isBlockDomain() && volumeType == VolumeType.Sparse));
        getIsShareable().getChangeProhibitionReasons().add(
                ConstantsManager.getInstance().getConstants().shareableDiskNotSupportedByConfiguration());
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
        boolean isStatusUp = host.getstatus() == VDSStatus.Up;

        return isStatusUp;
    }

    private void IsInternal_EntityChanged() {
        boolean isInVm = getIsInVm().getEntity() != null ? (Boolean) getIsInVm().getEntity() : false;
        boolean isInternal = getIsInternal().getEntity() != null ? (Boolean) getIsInternal().getEntity() : false;

        getSize().setIsAvailable(isInternal);
        getStorageDomain().setIsAvailable(isInternal);
        getVolumeType().setIsAvailable(isInternal);
        getHost().setIsAvailable(!isInternal);
        getStorageType().setIsAvailable(!isInternal);
        getDataCenter().setIsAvailable(!isInVm);

        if (!isInternal) {
            oldWipeAfterDeleteValue = (Boolean) getWipeAfterDelete().getEntity();
            isWipeAfterDeleteChangable = getWipeAfterDelete().getIsChangable();
            isQuotaAvailable = getQuota().getIsAvailable();
        }
        updateDatacenters();
        getWipeAfterDelete().setEntity(isInternal ? oldWipeAfterDeleteValue : false);
        getWipeAfterDelete().setIsChangable(isInternal ? isWipeAfterDeleteChangable : false);
        getWipeAfterDelete().setIsAvailable(isInternal ? true : false);
        getQuota().setIsAvailable(isInternal ? isQuotaAvailable : false);
    }

    private void Preset_SelectedItemChanged()
    {
        DiskImageBase preset = (DiskImageBase) getPreset().getSelectedItem() != null ?
                (DiskImageBase) getPreset().getSelectedItem()
                : (DiskImageBase) Linq.<DiskImageBase> FirstOrDefault(getPreset().getItems());
        setVolumeFormat(preset.getvolume_format());
        getVolumeType().setSelectedItem(preset.getvolume_type());
    }

    private void VolumeType_SelectedItemChanged()
    {
        VolumeType volumeType =
                getVolumeType().getSelectedItem() == null ? VolumeType.Unassigned
                        : (VolumeType) getVolumeType().getSelectedItem();

        StorageType storageType =
                getStorageDomain().getSelectedItem() == null ? StorageType.UNKNOWN
                        : ((storage_domains) getStorageDomain().getSelectedItem()).getstorage_type();

        updateVolumeFormat(volumeType, storageType);
        updateShareable(volumeType, storageType);
    }

    private void WipeAfterDelete_EntityChanged(EventArgs e)
    {
        if (!getWipeAfterDelete().getIsChangable() && (Boolean) getWipeAfterDelete().getEntity())
        {
            getWipeAfterDelete().setEntity(false);
        }
    }

    private void AttachDisk_EntityChanged(EventArgs e)
    {
        if ((Boolean) getAttachDisk().getEntity())
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
            }), getDatacenterId(), vmId);

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
            }), null, vmId);
        }
    }

    private void Datacenter_SelectedItemChanged()
    {
        storage_pool datacenter = (storage_pool) getDataCenter().getSelectedItem();
        boolean isInternal = getIsInternal().getEntity() != null ? (Boolean) getIsInternal().getEntity() : false;

        if (datacenter == null) {
            StopProgress();
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

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.equals(EntityModel.EntityChangedEventDefinition) && sender == getWipeAfterDelete())
        {
            WipeAfterDelete_EntityChanged(args);
        }
        else if (ev.equals(EntityModel.EntityChangedEventDefinition) && sender == getAttachDisk())
        {
            AttachDisk_EntityChanged(args);
        }
        else if (ev.equals(ListModel.EntityChangedEventDefinition) && sender == getIsInternal())
        {
            IsInternal_EntityChanged();
        }
        else if (ev.equals(ListModel.SelectedItemChangedEventDefinition) && sender == getPreset())
        {
            Preset_SelectedItemChanged();
        }
        else if (ev.equals(ListModel.SelectedItemChangedEventDefinition) && sender == getVolumeType())
        {
            VolumeType_SelectedItemChanged();
        }
        else if (ev.equals(ListModel.SelectedItemChangedEventDefinition) && sender == getDataCenter())
        {
            Datacenter_SelectedItemChanged();
        }
    }

    public boolean Validate()
    {
        if ((Boolean) getAttachDisk().getEntity()) {
            return true;
        }

        StorageType storageType = getStorageDomain().getSelectedItem() == null ? StorageType.UNKNOWN
                : ((storage_domains) getStorageDomain().getSelectedItem()).getstorage_type();

        IntegerValidation sizeValidation = new IntegerValidation();
        sizeValidation.setMinimum(1);
        if (storageType == StorageType.ISCSI || storageType == StorageType.FCP) {
            sizeValidation.setMaximum(maxDiskSize);
        }
        getSize().ValidateEntity(new IValidation[] { new NotEmptyValidation(), sizeValidation });

        getStorageDomain().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        getAlias().ValidateEntity(new IValidation[] { new AsciiOrNoneValidation() });

        if (!(Boolean) getIsInVm().getEntity()) {
            getAlias().ValidateEntity(new IValidation[] { new NotEmptyValidation() });
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
}
