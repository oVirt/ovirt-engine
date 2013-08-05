package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.Disk.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.comparators.NameableComparator;
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

public class NewTemplateVmModelBehavior extends VmModelBehaviorBase<UnitVmModel>
{
    private final VM vm;

    public NewTemplateVmModelBehavior(VM vm)
    {
        this.vm = vm;
    }

    @Override
    public void initialize(SystemTreeItemModel systemTreeSelectedItem)
    {
        super.initialize(systemTreeSelectedItem);
        getModel().getTemplate().setIsChangable(false);

        getModel().getVmType().setIsChangable(true);
        getModel().getCopyPermissions().setIsAvailable(true);

        DisksAllocationModel disksAllocationModel = getModel().getDisksAllocationModel();
        disksAllocationModel.setIsAliasChangable(true);

        AsyncDataProvider.getDataCenterById(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        NewTemplateVmModelBehavior behavior = (NewTemplateVmModelBehavior) target;
                        final StoragePool dataCenter = (StoragePool) returnValue;
                        if (dataCenter == null)
                        {
                            disableNewTemplateModel(ConstantsManager.getInstance()
                                    .getConstants()
                                    .dataCenterIsNotAccessibleMsg());
                        }
                        else
                        {

                            AsyncDataProvider.getClusterListByService(
                                    new AsyncQuery(getModel(), new INewAsyncCallback() {

                                        @Override
                                        public void onSuccess(Object target, Object returnValue) {
                                            UnitVmModel model = (UnitVmModel) target;

                                            List<VDSGroup> clusters = (List<VDSGroup>) returnValue;
                                            model.setDataCentersAndClusters(model,
                                                    Arrays.asList(dataCenter),
                                                    clusters,
                                                    vm.getVdsGroupId());

                                            initTemplate();

                                        }
                                    }, getModel().getHash()),
                                    true,
                                    false);

                            AsyncDataProvider.isSoundcardEnabled(new AsyncQuery(getModel(),
                                    new INewAsyncCallback() {

                                        @Override
                                        public void onSuccess(Object model, Object returnValue) {
                                            getModel().getIsSoundcardEnabled().setEntity(returnValue);
                                        }
                                    }, getModel().getHash()), vm.getId());
                        }

                    }
                },
                getModel().getHash()),
                vm.getStoragePoolId());
    }

    @Override
    public void dataCenterWithClusterSelectedItemChanged()
    {
        super.dataCenterWithClusterSelectedItemChanged();

        // If a VM has at least one disk, present its storage domain.
        AsyncDataProvider.getVmDiskList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        NewTemplateVmModelBehavior behavior = (NewTemplateVmModelBehavior) target;
                        ArrayList<Disk> imageDisks = new ArrayList<Disk>();
                        ArrayList<Disk> vmDisks = (ArrayList<Disk>) returnValue;

                        for (Disk disk : vmDisks) {
                            if (disk.getDiskStorageType() == DiskStorageType.IMAGE && !disk.isShareable()) {
                                imageDisks.add(disk);
                            }
                        }

                        behavior.initStorageDomains();
                        initDisks(imageDisks);

                        VmModelHelper.sendWarningForNonExportableDisks(getModel(),
                                vmDisks,
                                VmModelHelper.WarningType.VM_TEMPLATE);
                    }
                }, getModel().getHash()),
                vm.getId(),
                true);
    }

    private void initDisks(ArrayList<Disk> disks)
    {
        Collections.sort(disks, new Linq.DiskByAliasComparer());
        ArrayList<DiskModel> list = new ArrayList<DiskModel>();

        for (Disk disk : disks)
        {
            DiskModel diskModel = new DiskModel();

            if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
                DiskImage diskImage = (DiskImage) disk;
                EntityModel size = new EntityModel();
                size.setEntity(diskImage.getSizeInGigabytes());
                diskModel.setSize(size);
                ListModel volumes = new ListModel();
                volumes.setItems((diskImage.getVolumeType() == VolumeType.Preallocated ? new ArrayList<VolumeType>(Arrays.asList(new VolumeType[] { VolumeType.Preallocated }))
                        : AsyncDataProvider.getVolumeTypeList()));
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
    public void template_SelectedItemChanged()
    {
    }

    @Override
    public void postDataCenterWithClusterSelectedItemChanged()
    {
        updateQuotaByCluster(null, null);
        updateMemoryBalloon();
        updateCpuSharesAvailability();
    }

    @Override
    public void defaultHost_SelectedItemChanged()
    {
    }

    @Override
    public void provisioning_SelectedItemChanged()
    {
    }

    @Override
    public void updateMinAllocatedMemory()
    {
    }

    private void initTemplate()
    {
        // Update model state according to VM properties.
        getModel().getMemSize().setEntity(this.vm.getVmMemSizeMb());
        getModel().getMinAllocatedMemory().setEntity(this.vm.getMinAllocatedMem());
        getModel().getOSType().setSelectedItem(this.vm.getVmOsId());
        getModel().getDomain().setSelectedItem(this.vm.getVmDomain());
        getModel().getNumOfMonitors().setSelectedItem(this.vm.getNumOfMonitors());
        getModel().getAllowConsoleReconnect().setEntity(this.vm.getAllowConsoleReconnect());
        getModel().setBootSequence(this.vm.getDefaultBootSequence());
        getModel().getTotalCPUCores().setEntity(Integer.toString(this.vm.getNumOfCpus()));
        getModel().getNumOfSockets().setSelectedItem(this.vm.getNumOfSockets());
        getModel().getIsStateless().setEntity(this.vm.isStateless());
        getModel().getIsRunAndPause().setEntity(this.vm.isRunAndPause());
        getModel().getIsDeleteProtected().setEntity(this.vm.isDeleteProtected());

        updateSelectedCdImage(this.vm.getStaticData());
        updateTimeZone(this.vm.getTimeZone());
        updateConsoleDevice(this.vm.getId());

        // Update domain list
        updateDomain();

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

        getModel().getUsbPolicy().setSelectedItem(this.vm.getUsbPolicy());
        getModel().getIsSmartcardEnabled().setEntity(this.vm.isSmartcardEnabled());
        getModel().getVncKeyboardLayout().setSelectedItem(this.vm.getVncKeyboardLayout());

        initPriority(this.vm.getPriority());
    }

    @Override
    public void initStorageDomains()
    {
        AsyncDataProvider.getPermittedStorageDomainsByStoragePoolId(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {
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
                                    behavior.disableNewTemplateModel(
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
                                        Linq.firstOrDefault(activeStorageDomainList,
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
                            behavior.disableNewTemplateModel(ConstantsManager.getInstance()
                                    .getMessages()
                                    .noActiveStorageDomain());
                        }

                        ArrayList<DiskModel> disks =
                                (ArrayList<DiskModel>) behavior.getModel().getDisksAllocationModel().getDisks();

                        Collections.sort(activeStorageDomainList, new NameableComparator());
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

    private void disableNewTemplateModel(String errMessage)
    {
        getModel().setIsValid(false);
        getModel().setMessage(errMessage);
        getModel().getName().setIsChangable(false);
        getModel().getDescription().setIsChangable(false);
        getModel().getDataCenterWithClustersList().setIsChangable(false);
        getModel().getComment().setIsChangable(false);
        getModel().getStorageDomain().setIsChangable(false);
        getModel().getIsTemplatePublic().setIsChangable(false);
        getModel().getDefaultCommand().setIsAvailable(false);
    }

    @Override
    public boolean validate()
    {
        return super.validate();
    }
}
