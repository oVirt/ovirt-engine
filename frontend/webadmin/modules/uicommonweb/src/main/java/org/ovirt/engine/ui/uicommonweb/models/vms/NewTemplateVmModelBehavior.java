package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicommonweb.models.storage.DisksAllocationModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.external.StringUtils;

public class NewTemplateVmModelBehavior extends VmModelBehaviorBase<UnitVmModel>
{
    private final VM vm;

    public NewTemplateVmModelBehavior(VM vm)
    {
        this.vm = vm;
    }

    @Override
    public void Initialize(SystemTreeItemModel systemTreeSelectedItem)
    {
        super.Initialize(systemTreeSelectedItem);
        getModel().getTemplate().setIsChangable(false);

        DisksAllocationModel disksAllocationModel = getModel().getDisksAllocationModel();
        disksAllocationModel.setIsAliasChangable(true);

        AsyncDataProvider.GetDataCenterById(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        NewTemplateVmModelBehavior behavior = (NewTemplateVmModelBehavior) target;
                        storage_pool dataCenter = (storage_pool) returnValue;
                        if (dataCenter == null)
                        {
                            DisableNewTemplateModel(ConstantsManager.getInstance()
                                    .getConstants()
                                    .dataCenterIsNotAccessibleMsg());
                        }
                        else
                        {
                            behavior.getModel()
                                    .getDataCenter()
                                    .setItems(new ArrayList<storage_pool>(Arrays.asList(new storage_pool[] { dataCenter })));
                            behavior.getModel().getDataCenter().setSelectedItem(dataCenter);
                            behavior.getModel().getDataCenter().setIsChangable(false);
                        }

                    }
                },
                getModel().getHash()),
                vm.getStoragePoolId());
    }

    @Override
    public void DataCenter_SelectedItemChanged()
    {
        storage_pool dataCenter = (storage_pool) getModel().getDataCenter().getSelectedItem();

        getModel().setIsHostAvailable(dataCenter.getstorage_pool_type() != StorageType.LOCALFS);

        AsyncDataProvider.GetClusterList(new AsyncQuery(new Object[] { this, getModel() },
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        Object[] array = (Object[]) target;
                        NewTemplateVmModelBehavior behavior = (NewTemplateVmModelBehavior) array[0];
                        UnitVmModel model = (UnitVmModel) array[1];
                        ArrayList<VDSGroup> clusters = (ArrayList<VDSGroup>) returnValue;
                        model.SetClusters(model, clusters, vm.getVdsGroupId().getValue());
                        behavior.InitTemplate();

                    }
                }, getModel().getHash()), dataCenter.getId());

        // If a VM has at least one disk, present its storage domain.
        AsyncDataProvider.GetVmDiskList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        NewTemplateVmModelBehavior behavior = (NewTemplateVmModelBehavior) target;
                        ArrayList<Disk> disks = new ArrayList<Disk>();
                        List<Disk> nonExportableDisks = new ArrayList<Disk>();
                        Iterable disksEnumerable = (Iterable) returnValue;
                        Iterator disksIterator = disksEnumerable.iterator();

                        while (disksIterator.hasNext())
                        {
                            Disk disk = (Disk) disksIterator.next();

                            if (disk.getDiskStorageType() == DiskStorageType.IMAGE && !disk.isShareable()) {
                                disks.add(disk);
                            } else if (!disk.isAllowSnapshot()) {
                                nonExportableDisks.add(disk);
                            }
                        }

                        behavior.InitStorageDomains();
                        InitDisks(disks);
                        sendWarningForNonExportableDisks(nonExportableDisks);
                    }
                }, getModel().getHash()),
                vm.getId(),
                true);

        if (dataCenter.getQuotaEnforcementType() != QuotaEnforcementTypeEnum.DISABLED) {
            getModel().getQuota().setIsAvailable(true);
        } else {
            getModel().getQuota().setIsAvailable(false);
        }
    }

    private void sendWarningForNonExportableDisks(List<Disk> nonExportableDisks) {
        if (!nonExportableDisks.isEmpty()) {
            final List<String> list = new ArrayList<String>();
            for (Disk disk : nonExportableDisks) {
                list.add(disk.getDiskAlias());
            }

            final String s = StringUtils.join(list, ", "); //$NON-NLS-1$

            // append warning message
            getModel().setMessage(ConstantsManager.getInstance().getMessages().disksWillNotBePartOfTheExportedVMTemplate(s));
        }
    }

    private void InitDisks(ArrayList<Disk> disks)
    {
        Collections.sort(disks, new Linq.DiskByAliasComparer());
        ArrayList<DiskModel> list = new ArrayList<DiskModel>();

        for (Disk disk : disks)
        {
            DiskModel diskModel = new DiskModel();
            diskModel.setIsNew(true);

            if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
                DiskImage diskImage = (DiskImage) disk;
                EntityModel size = new EntityModel();
                size.setEntity(diskImage.getSizeInGigabytes());
                diskModel.setSize(size);
                ListModel volumes = new ListModel();
                volumes.setItems((diskImage.getVolumeType() == VolumeType.Preallocated ? new ArrayList<VolumeType>(Arrays.asList(new VolumeType[] { VolumeType.Preallocated }))
                        : AsyncDataProvider.GetVolumeTypeList()));
                volumes.setSelectedItem(diskImage.getVolumeType());
                diskModel.setVolumeType(volumes);
                diskModel.getAlias().setEntity(diskImage.getDiskAlias());
            }

            diskModel.setDisk(disk);
            list.add(diskModel);
        }
        getModel().setDisks(list);
    }

    @Override
    public void Template_SelectedItemChanged()
    {
    }

    @Override
    public void Cluster_SelectedItemChanged()
    {
        updateQuotaByCluster(null, null);
    }

    @Override
    public void DefaultHost_SelectedItemChanged()
    {
    }

    @Override
    public void Provisioning_SelectedItemChanged()
    {
    }

    @Override
    public void UpdateMinAllocatedMemory()
    {
    }

    private void InitTemplate()
    {
        // Update model state according to VM properties.
        getModel().getMemSize().setEntity(this.vm.getVmMemSizeMb());
        getModel().getOSType().setSelectedItem(this.vm.getVmOs());
        getModel().getDomain().setSelectedItem(this.vm.getVmDomain());
        getModel().getUsbPolicy().setSelectedItem(this.vm.getUsbPolicy());
        getModel().getNumOfMonitors().setSelectedItem(this.vm.getNumOfMonitors());
        getModel().getAllowConsoleReconnect().setEntity(this.vm.getAllowConsoleReconnect());
        getModel().setBootSequence(this.vm.getDefaultBootSequence());
        getModel().getTotalCPUCores().setEntity(Integer.toString(this.vm.getNumOfCpus()));
        getModel().getNumOfSockets().setSelectedItem(this.vm.getNumOfSockets());
        getModel().getIsStateless().setEntity(this.vm.isStateless());
        getModel().getIsDeleteProtected().setEntity(this.vm.isDeleteProtected());
        getModel().getIsSmartcardEnabled().setEntity(this.vm.isSmartcardEnabled());

        if (!StringHelper.isNullOrEmpty(this.vm.getTimeZone()))
        {
            updateTimeZone(this.vm.getTimeZone());
        }
        else
        {
            UpdateDefaultTimeZone();
        }

        // Update domain list
        UpdateDomain();

        getModel().getStorageDomain().setIsChangable(true);
        getModel().getProvisioning().setIsAvailable(false);

        // Select display protocol.
        for (Object item : getModel().getDisplayProtocol().getItems())
        {
            EntityModel model = (EntityModel) item;
            DisplayType displayType = (DisplayType) model.getEntity();

            if (displayType == this.vm.getDefaultDisplayType())
            {
                getModel().getDisplayProtocol().setSelectedItem(item);
                break;
            }
        }

        InitPriority(this.vm.getPriority());
    }

    @Override
    public void InitStorageDomains()
    {
        AsyncDataProvider.GetPermittedStorageDomainsByStoragePoolId(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        NewTemplateVmModelBehavior behavior = (NewTemplateVmModelBehavior) target;
                        ArrayList<StorageDomain> activeStorageDomainList =
                                new ArrayList<StorageDomain>();

                        for (StorageDomain storageDomain : (ArrayList<StorageDomain>) returnValue)
                        {
                            if (storageDomain.getStatus() == StorageDomainStatus.Active
                                    && (storageDomain.getStorageDomainType() == StorageDomainType.Data || storageDomain.getStorageDomainType() == StorageDomainType.Master))
                            {
                                activeStorageDomainList.add(storageDomain);
                            }
                        }

                        for (DiskModel diskModel : getModel().getDisks()) {
                            if (diskModel.getDisk().getDiskStorageType() == DiskStorageType.IMAGE) {
                                DiskImage diskImage = (DiskImage) diskModel.getDisk();
                                ArrayList<StorageDomain> activeDiskStorages =
                                        Linq.getStorageDomainsByIds(diskImage.getStorageIds(), activeStorageDomainList);

                                if (activeDiskStorages.isEmpty()) {
                                    behavior.DisableNewTemplateModel(
                                            ConstantsManager.getInstance()
                                                    .getMessages()
                                                    .vmStorageDomainIsNotAccessible());

                                    return;
                                }
                            }
                        }

                        if (activeStorageDomainList.size() > 0)
                        {
                            if (getSystemTreeSelectedItem() != null
                                    && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Storage)
                            {
                                StorageDomain selectStorage =
                                        (StorageDomain) getSystemTreeSelectedItem().getEntity();
                                StorageDomain s =
                                        Linq.FirstOrDefault(activeStorageDomainList,
                                                new Linq.StoragePredicate(selectStorage.getId()));
                                activeStorageDomainList =
                                        new ArrayList<StorageDomain>(Arrays.asList(new StorageDomain[] { s }));

                                behavior.getModel().getStorageDomain().setItems(activeStorageDomainList);
                                behavior.getModel().getStorageDomain().setIsChangable(false);
                                behavior.getModel().getStorageDomain().setSelectedItem(s);
                            }
                            else
                            {
                                behavior.getModel().getStorageDomain().setItems(activeStorageDomainList);
                                behavior.getModel().getStorageDomain().setIsChangable(true);
                            }
                        }
                        else
                        {
                            behavior.DisableNewTemplateModel(ConstantsManager.getInstance()
                                    .getMessages()
                                    .noActiveStorageDomain());
                        }

                        ArrayList<DiskModel> disks =
                                (ArrayList<DiskModel>) behavior.getModel().getDisksAllocationModel().getDisks();

                        Linq.Sort(activeStorageDomainList, new Linq.StorageDomainByNameComparer());
                        if (disks != null) {
                            for (DiskModel diskModel : disks) {
                                diskModel.getStorageDomain().setItems(activeStorageDomainList);
                                diskModel.getQuota().setItems(behavior.getModel().getQuota().getItems());
                            }
                        }
                    }
                },
                getModel().getHash()),
                vm.getStoragePoolId(),
                ActionGroup.CREATE_TEMPLATE);
    }

    private void DisableNewTemplateModel(String errMessage)
    {
        getModel().setIsValid(false);
        getModel().setMessage(errMessage);
        getModel().getName().setIsChangable(false);
        getModel().getDescription().setIsChangable(false);
        getModel().getCluster().setIsChangable(false);
        getModel().getStorageDomain().setIsChangable(false);
        getModel().getIsTemplatePublic().setIsChangable(false);
        getModel().getDefaultCommand().setIsAvailable(false);
    }

    @Override
    public boolean Validate()
    {
        return super.Validate();
    }
}
