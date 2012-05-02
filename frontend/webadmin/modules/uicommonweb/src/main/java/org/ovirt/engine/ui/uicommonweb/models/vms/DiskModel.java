package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Date;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageBase;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageType;
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

    private EntityModel privateIsPlugged;

    public EntityModel getIsPlugged()
    {
        return privateIsPlugged;
    }

    public void setIsPlugged(EntityModel value)
    {
        privateIsPlugged = value;
    }

    private DiskImage privateDiskImage;

    public DiskImage getDiskImage()
    {
        return privateDiskImage;
    }

    public void setDiskImage(DiskImage value)
    {
        privateDiskImage = value;
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

    private ListModel attachableDisks;

    public ListModel getAttachableDisks()
    {
        return attachableDisks;
    }

    public void setAttachableDisks(ListModel value)
    {
        attachableDisks = value;
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

    public DiskModel()
    {
        setSize(new EntityModel());
        getSize().setIsValid(true);

        setInterface(new ListModel());
        setStorageDomain(new ListModel());
        setQuota(new ListModel());

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

        setWipeAfterDelete(new EntityModel());
        getWipeAfterDelete().setEntity(false);
        getWipeAfterDelete().getEntityChangedEvent().addListener(this);

        setIsBootable(new EntityModel());
        getIsBootable().setEntity(false);

        setIsPlugged(new EntityModel());
        getIsPlugged().setEntity(true);

        setAttachDisk(new EntityModel());
        getAttachDisk().setEntity(false);
        getAttachDisk().getEntityChangedEvent().addListener(this);

        setQuota(new ListModel());
        getQuota().setIsAvailable(false);

        setAlias(new EntityModel());
        setDescription(new EntityModel());
        setAttachableDisks(new ListModel());

        setIsInVm(new EntityModel());
        getIsInVm().setEntity(true);
        getIsInVm().getEntityChangedEvent().addListener(this);

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

        if (datacenter.getQuotaEnforcementType().equals(QuotaEnforcementTypeEnum.DISABLED)) {
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
                    }
                }), VmType.Server, storageType);
            }
        }),
                datacenter.getId());
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

    private void IsInVm_EntityChanged() {
        if ((Boolean) getIsInVm().getEntity()) {
            return;
        }

        AsyncDataProvider.GetDataCenterList(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {
                DiskModel diskModel = (DiskModel) target;
                ArrayList<storage_pool> dataCenters = (ArrayList<storage_pool>) returnValue;

                diskModel.getDataCenter().setItems(dataCenters);
                diskModel.getDataCenter().setIsAvailable(true);
            }
        }));
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
        if ((Boolean) getAttachDisk().getEntity() && getDatacenterId() != null)
        {
            AsyncDataProvider.GetAllAttachableDisks(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void OnSuccess(Object target, Object returnValue) {
                    DiskModel model = (DiskModel) target;
                    ArrayList<DiskImage> diskImages = (ArrayList<DiskImage>) returnValue;
                    ArrayList<DiskModel> diskModels = new ArrayList<DiskModel>();

                    for (DiskImage disk : diskImages) {
                        DiskModel diskModel = new DiskModel();
                        diskModel.setDiskImage(disk);
                        diskModels.add(diskModel);
                    }

                    model.getAttachableDisks().setItems(Linq.ToEntityModelList(diskModels));
                }
            }), getDatacenterId());
        }
    }

    public boolean Validate()
    {
        if ((Boolean) getAttachDisk().getEntity()) {
            return true;
        }

        if (!(Boolean) getIsInVm().getEntity()) {
            getAlias().ValidateEntity(new IValidation[] { new NotEmptyValidation() });
        }

        IntegerValidation tempVar = new IntegerValidation();
        tempVar.setMinimum(1);
        tempVar.setMaximum(maxDiskSize);
        IntegerValidation intValidation = tempVar;
        getSize().ValidateEntity(new IValidation[] { new NotEmptyValidation(), intValidation });

        getStorageDomain().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });
        getAlias().ValidateEntity(new IValidation[] { new AsciiOrNoneValidation() });

        return getSize().getIsValid() && getStorageDomain().getIsValid() && getAlias().getIsValid();
    }
}
