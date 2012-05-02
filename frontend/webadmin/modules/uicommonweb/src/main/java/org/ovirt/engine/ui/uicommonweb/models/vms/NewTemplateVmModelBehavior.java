package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

@SuppressWarnings("unused")
public class NewTemplateVmModelBehavior extends VmModelBehaviorBase
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
                                    .setItems(new ArrayList<storage_pool>(Arrays.asList(new storage_pool[] {dataCenter})));
                            behavior.getModel().getDataCenter().setSelectedItem(dataCenter);
                            behavior.getModel().getDataCenter().setIsChangable(false);
                        }

                    }
                },
                getModel().getHash()),
                vm.getstorage_pool_id());
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
                        model.SetClusters(model, clusters, vm.getvds_group_id().getValue());
                        behavior.InitTemplate();

                    }
                }, getModel().getHash()), dataCenter.getId());

        // If a VM has at least one disk, present its storage domain.
        AsyncDataProvider.GetVmDiskList(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        NewTemplateVmModelBehavior behavior = (NewTemplateVmModelBehavior) target;
                        ArrayList<DiskImage> disks = new ArrayList<DiskImage>();
                        Iterable disksEnumerable = (Iterable) returnValue;
                        Iterator disksIterator = disksEnumerable.iterator();
                        while (disksIterator.hasNext())
                        {
                            disks.add((DiskImage) disksIterator.next());
                        }
                        if (disks.isEmpty())
                        {
                            behavior.DisableNewTemplateModel(ConstantsManager.getInstance()
                                    .getConstants()
                                    .cannotCreateTemplateVmHasNoDisksMsg());
                        }
                        else
                        {
                            behavior.InitStorageDomains(disks.get(0).getstorage_ids().get(0));
                        }

                        InitDisks(disks);
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

    private void InitDisks(ArrayList<DiskImage> disks)
    {
        Collections.sort(disks, new Linq.DiskByInternalDriveMappingComparer());
        ArrayList<DiskModel> list = new ArrayList<DiskModel>();
        for (DiskImage a : disks)
        {
            DiskModel diskModel = new DiskModel();
            diskModel.setIsNew(true);
            diskModel.setName(a.getinternal_drive_mapping());
            EntityModel tempVar = new EntityModel();
            tempVar.setEntity(a.getSizeInGigabytes());
            diskModel.setSize(tempVar);
            ListModel tempVar2 = new ListModel();
            tempVar2.setItems((a.getvolume_type() == VolumeType.Preallocated ? new ArrayList<VolumeType>(Arrays.asList(new VolumeType[] { VolumeType.Preallocated }))
                    : DataProvider.GetVolumeTypeList()));
            tempVar2.setSelectedItem(a.getvolume_type());
            diskModel.setVolumeType(tempVar2);
            diskModel.setDiskImage(a);
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
        updateQuotaByCluster(vm.getQuotaId());
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
        getModel().getMemSize().setEntity(this.vm.getvm_mem_size_mb());
        getModel().getOSType().setSelectedItem(this.vm.getvm_os());
        getModel().getDomain().setSelectedItem(this.vm.getvm_domain());
        getModel().getUsbPolicy().setSelectedItem(this.vm.getusb_policy());
        getModel().getNumOfMonitors().setSelectedItem(this.vm.getnum_of_monitors());
        getModel().setBootSequence(this.vm.getdefault_boot_sequence());
        getModel().getNumOfSockets().setEntity(this.vm.getnum_of_sockets());
        getModel().getTotalCPUCores().setEntity(this.vm.getnum_of_cpus());
        getModel().getIsStateless().setEntity(this.vm.getis_stateless());

        if (!StringHelper.isNullOrEmpty(this.vm.gettime_zone()))
        {
            getModel().getTimeZone()
                    .setSelectedItem(new KeyValuePairCompat<String, String>(this.vm.gettime_zone(), "")); //$NON-NLS-1$
            UpdateTimeZone();
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

            if (displayType == this.vm.getdefault_display_type())
            {
                getModel().getDisplayProtocol().setSelectedItem(item);
                break;
            }
        }

        InitPriority(this.vm.getpriority());
    }

    public void InitStorageDomains(NGuid storageDomainId)
    {
        if (storageDomainId == null)
        {
            return;
        }

        AsyncDataProvider.GetStorageDomainById(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        NewTemplateVmModelBehavior behavior = (NewTemplateVmModelBehavior) target;
                        storage_domains currentStorageDomain = (storage_domains) returnValue;
                        behavior.PostInitStorageDomains((storage_pool) behavior.getModel()
                                .getDataCenter()
                                .getSelectedItem(), currentStorageDomain);

                    }
                }, getModel().getHash()),
                storageDomainId.getValue());
    }

    public void PostInitStorageDomains(storage_pool dataCenter, storage_domains storage)
    {
        AsyncDataProvider.GetStorageDomainList(new AsyncQuery(new Object[] { this, storage },
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        Object[] array = (Object[]) target;
                        NewTemplateVmModelBehavior behavior = (NewTemplateVmModelBehavior) array[0];
                        storage_domains currentStorageDomain = (storage_domains) array[1];
                        storage_domains vmStorageDomain = null;
                        ArrayList<storage_domains> activeStorageDomainList =
                                new ArrayList<storage_domains>();
                        for (storage_domains storageDomain : (ArrayList<storage_domains>) returnValue)
                        {
                            if (storageDomain.getstatus() == StorageDomainStatus.Active
                                    && (storageDomain.getstorage_domain_type() == StorageDomainType.Data || storageDomain.getstorage_domain_type() == StorageDomainType.Master))
                            {
                                if (currentStorageDomain.getId().equals(storageDomain.getId()))
                                {
                                    vmStorageDomain = storageDomain;
                                }
                                activeStorageDomainList.add(storageDomain);
                            }
                        }
                        if (activeStorageDomainList.size() > 0 && vmStorageDomain != null)
                        {
                            if (getSystemTreeSelectedItem() != null
                                    && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Storage)
                            {
                                storage_domains selectStorage =
                                        (storage_domains) getSystemTreeSelectedItem().getEntity();
                                storage_domains s =
                                        Linq.FirstOrDefault(activeStorageDomainList,
                                                new Linq.StoragePredicate(selectStorage.getId()));
                                activeStorageDomainList =new ArrayList<storage_domains>(Arrays.asList(new storage_domains[] { s }));

                                behavior.getModel().getStorageDomain().setItems(activeStorageDomainList);
                                behavior.getModel().getStorageDomain().setIsChangable(false);
                                behavior.getModel().getStorageDomain().setSelectedItem(s);
                            }
                            else
                            {
                                behavior.getModel().getStorageDomain().setItems(activeStorageDomainList);
                                behavior.getModel().getStorageDomain().setIsChangable(true);
                                behavior.getModel().getStorageDomain().setSelectedItem(vmStorageDomain);
                            }
                        }
                        else
                        {
                            behavior.DisableNewTemplateModel(ConstantsManager.getInstance()
                                    .getMessages()
                                    .vmStorageDomainIsNotAccessible(currentStorageDomain.getstorage_name()));
                        }

                        ArrayList<DiskModel> disks =
                                (ArrayList<DiskModel>) behavior.getModel().getDisksAllocationModel().getDisks();
                        if (disks != null) {
                            for (DiskModel diskModel : disks) {
                                diskModel.getStorageDomain().setItems(activeStorageDomainList);
                                diskModel.getQuota().setItems(behavior.getModel().getQuota().getItems());
                            }
                        }
                    }
                },
                getModel().getHash()),
                dataCenter.getId());
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
        getModel().getIsTemplatePrivate().setIsChangable(false);
    }

    @Override
    public boolean Validate()
    {
        getModel().getStorageDomain().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        return super.Validate() && getModel().getStorageDomain().getIsValid();
    }
}
