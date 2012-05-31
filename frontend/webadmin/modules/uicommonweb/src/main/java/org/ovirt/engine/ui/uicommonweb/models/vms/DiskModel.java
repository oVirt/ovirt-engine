package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Date;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmType;
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
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModel;
import org.ovirt.engine.ui.uicommonweb.validation.AsciiOrNoneValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class DiskModel extends Model
{
    static int maxDiskSize = 2047;
    static boolean maxDiskSizeInited = false;

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

    private SanStorageModel sanStorageModel;

    public SanStorageModel getSanStorageModel()
    {
        return sanStorageModel;
    }

    public void setSanStorageModel(SanStorageModel value)
    {
        sanStorageModel = value;
    }

    private boolean isWipeAfterDeleteChangable;
    private boolean isQuotaAvailable;

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
        getVolumeType().setItems(DataProvider.GetVolumeTypeList());
        getVolumeType().getSelectedItemChangedEvent().addListener(this);

        setStorageType(new ListModel());
        getStorageType().setIsAvailable(false);
        getStorageType().setItems(DataProvider.GetStorageTypeList());
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
        getIsInternal().setEntity(true);
        getIsInternal().getEntityChangedEvent().addListener(this);

        setIsNew(true);

        AsyncDataProvider.GetDiskMaxSize(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        if (!DiskModel.maxDiskSizeInited) {
                            DiskModel.maxDiskSizeInited = true;
                            DiskModel.maxDiskSize = ((Integer) returnValue);
                        }
                    }
                }));
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

    private void getStorageQuota(final Guid defaultQuotaId) {
        storage_domains storageDomain = (storage_domains) getStorageDomain().getSelectedItem();
        if (storageDomain != null) {
            Frontend.RunQuery(VdcQueryType.GetAllRelevantQuotasForStorage,
                    new GetAllRelevantQuotasForStorageParameters(storageDomain.getId()),
                    new AsyncQuery(this,
                            new INewAsyncCallback() {

                                @Override
                                public void OnSuccess(Object innerModel, Object innerReturnValue) {
                                    ArrayList<Quota> list =
                                            (ArrayList<Quota>) ((VdcQueryReturnValue) innerReturnValue).getReturnValue();
                                    if (list != null) {
                                        getQuota().setItems(list);
                                        if (defaultQuotaId != null) {
                                            for (Quota quota : list) {
                                                if (quota.getId().equals(defaultQuotaId)) {
                                                    getQuota().setSelectedItem(quota);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }));
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
        else if (ev.equals(ListModel.EntityChangedEventDefinition) && sender == getIsInVm())
        {
            IsInVm_EntityChanged();
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

    private void Datacenter_SelectedItemChanged()
    {
        storage_pool datacenter = (storage_pool) getDataCenter().getSelectedItem();
        if (datacenter == null)
        {
            return;
        }

        if (datacenter.getQuotaEnforcementType().equals(QuotaEnforcementTypeEnum.DISABLED)
                || !(Boolean) getIsInternal().getEntity()) {
            getQuota().setIsAvailable(false);
        } else {
            getQuota().setIsAvailable(true);
            quota_storageSelectedItemChanged(null);
        }

        getInterface().setItems(DataProvider.GetDiskInterfaceList(
                VmOsType.Unassigned, datacenter.getcompatibility_version()));
        getInterface().setSelectedItem(DataProvider.GetDefaultDiskInterface(
                VmOsType.Unassigned, null));

        AsyncDataProvider.GetStorageDomainList(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {
                DiskModel diskModel = (DiskModel) target;
                ArrayList<storage_domains> storageDomains = (ArrayList<storage_domains>) returnValue;

                ArrayList<storage_domains> filteredStorageDomains = new ArrayList<storage_domains>();
                for (storage_domains a : (ArrayList<storage_domains>) storageDomains)
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

                diskModel.getStorageDomain().setItems(filteredStorageDomains);
                diskModel.getStorageDomain().setSelectedItem(storage);

                if (storage != null) {
                    UpdateWipeAfterDelete(storage.getstorage_type(), diskModel.getWipeAfterDelete());
                    diskModel.setMessage(""); //$NON-NLS-1$
                }
                else {
                    diskModel.setMessage(ConstantsManager.getInstance().getConstants().noActiveStorageDomainInDcMsg());
                }

                AsyncDataProvider.GetDiskPresetList(new AsyncQuery(diskModel, new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        DiskModel diskModel1 = (DiskModel) target;
                        ArrayList<DiskImageBase> presets = (ArrayList<DiskImageBase>) returnValue;

                        diskModel1.getPreset().setItems(presets);
                        DiskImageBase preset = new DiskImage();
                        preset.setvolume_type(VolumeType.Preallocated);
                        preset.setvolume_format(VolumeFormat.RAW);
                        diskModel1.getPreset().setSelectedItem(preset);
                    }
                }), VmType.Server, storageType);
            }
        }), datacenter.getId());

        AsyncDataProvider.GetHostListByDataCenter(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {

                DiskModel diskModel = (DiskModel) target;
                Iterable<VDS> hosts = (Iterable<VDS>) returnValue;
                diskModel.getHost().setItems(hosts);
            }
        }), datacenter.getId());
    }

    private void UpdateWipeAfterDelete(StorageType storageType, EntityModel wipeAfterDeleteModel)
    {
        if (storageType == StorageType.NFS || storageType == StorageType.LOCALFS) {
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

    private void UpdateDatacenters() {
        Boolean isInVm = (Boolean) getIsInVm().getEntity();
        Boolean isInternal = (Boolean) getIsInternal().getEntity();

        getDataCenter().setIsAvailable(!isInVm || !isInternal);

        if (isInternal) {
            AsyncDataProvider.GetDataCenterById((new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object target, Object returnValue) {
                    DiskModel diskModel = (DiskModel) target;
                    storage_pool dataCenter = (storage_pool) returnValue;

                    ArrayList<storage_pool> dataCenters = new ArrayList<storage_pool>();
                    dataCenters.add(dataCenter);

                    diskModel.getDataCenter().setItems(dataCenters);
                }
            })), getDatacenterId());
        }
        else {
            AsyncDataProvider.GetDataCentersWithPermittedActionOnClusters(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object target, Object returnValue) {
                    DiskModel diskModel = (DiskModel) target;
                    ArrayList<storage_pool> dataCenters = (ArrayList<storage_pool>) returnValue;
                    diskModel.getDataCenter().setItems(dataCenters);
                }
            }), ActionGroup.CREATE_DISK);
        }
    }

    private void IsInVm_EntityChanged() {
        Boolean isInVm = (Boolean) getIsInVm().getEntity();
        if (!isInVm) {
            UpdateDatacenters();
        }
    }

    private void IsInternal_EntityChanged() {
        Boolean isInVm = (Boolean) getIsInVm().getEntity();
        Boolean isInternal = (Boolean) getIsInternal().getEntity();

        getSize().setIsAvailable(isInternal);
        getStorageDomain().setIsAvailable(isInternal);
        getVolumeType().setIsAvailable(isInternal);
        getHost().setIsAvailable(!isInternal);
        getStorageType().setIsAvailable(!isInternal);
        getDataCenter().setIsAvailable(!isInVm || !isInternal);

        if (!isInternal) {
            isWipeAfterDeleteChangable = getWipeAfterDelete().getIsChangable();
            isQuotaAvailable = getQuota().getIsAvailable();
            UpdateDatacenters();
        }
        getWipeAfterDelete().setIsChangable(isInternal ? isWipeAfterDeleteChangable : true);
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
        UpdateVolumeFormat();
    }

    private void UpdateVolumeFormat()
    {
        VolumeType volumeType =
                getVolumeType().getSelectedItem() == null ? org.ovirt.engine.core.common.businessentities.VolumeType.Unassigned
                        : (VolumeType) getVolumeType().getSelectedItem();

        StorageType storageType =
                getStorageDomain().getSelectedItem() == null ? StorageType.UNKNOWN
                        : ((storage_domains) getStorageDomain().getSelectedItem()).getstorage_type();

        setVolumeFormat(DataProvider.GetDiskVolumeFormat(volumeType, storageType));
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
                    ArrayList<DiskModel> diskModels = Linq.DisksToDiskModelList(disks);

                    model.getInternalAttachableDisks().setItems(Linq.ToEntityModelList(
                            Linq.FilterDisksByType(diskModels, DiskStorageType.IMAGE)));
                }
            }), getDatacenterId());

            // Get external attachable disks
            AsyncDataProvider.GetAllAttachableDisks(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object target, Object returnValue) {
                    DiskModel model = (DiskModel) target;
                    ArrayList<Disk> disks = (ArrayList<Disk>) returnValue;
                    ArrayList<DiskModel> diskModels = Linq.DisksToDiskModelList(disks);

                    model.getExternalAttachableDisks().setItems(Linq.ToEntityModelList(
                            Linq.FilterDisksByType(diskModels, DiskStorageType.LUN)));
                }
            }), null);
        }
    }

    public boolean Validate()
    {
        if ((Boolean) getAttachDisk().getEntity()) {
            return true;
        }

        IntegerValidation tempVar = new IntegerValidation();
        tempVar.setMinimum(1);
        tempVar.setMaximum(maxDiskSize);
        IntegerValidation intValidation = tempVar;
        getSize().ValidateEntity(new IValidation[] { new NotEmptyValidation(), intValidation });

        getStorageDomain().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        getAlias().ValidateEntity(new IValidation[] { new AsciiOrNoneValidation() });

        if (!(Boolean) getIsInVm().getEntity()) {
            getAlias().ValidateEntity(new IValidation[] { new NotEmptyValidation() });
        }

        return getSize().getIsValid() && getStorageDomain().getIsValid() && getAlias().getIsValid();
    }
}
