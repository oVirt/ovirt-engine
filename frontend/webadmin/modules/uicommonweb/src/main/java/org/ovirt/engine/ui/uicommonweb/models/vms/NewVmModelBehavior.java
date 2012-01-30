package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcmentTypeEnum;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.queries.GetQuotaByStoragePoolIdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicommonweb.models.storage.DisksAllocationModel;

@SuppressWarnings("unused")
public class NewVmModelBehavior extends IVmModelBehavior
{
    @Override
    public void Initialize(SystemTreeItemModel systemTreeSelectedItem)
    {
        super.Initialize(systemTreeSelectedItem);
        AsyncDataProvider.GetDataCenterList(new AsyncQuery(getModel(),
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        UnitVmModel model = (UnitVmModel) target;
                        java.util.ArrayList<storage_pool> list = new java.util.ArrayList<storage_pool>();
                        for (storage_pool a : (java.util.ArrayList<storage_pool>) returnValue)
                        {
                            if (a.getstatus() == StoragePoolStatus.Up)
                            {
                                list.add(a);
                            }
                        }
                        model.SetDataCenter(model, list);

                    }
                }, getModel().getHash()));
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
                        NewVmModelBehavior behavior = (NewVmModelBehavior) array[0];
                        UnitVmModel model = (UnitVmModel) array[1];
                        java.util.ArrayList<VDSGroup> clusters = (java.util.ArrayList<VDSGroup>) returnValue;
                        model.SetClusters(model, clusters, null);
                        behavior.InitTemplate();
                        behavior.InitCdImage();

                    }
                }, getModel().getHash()), dataCenter.getId());
        if (dataCenter.getQuotaEnforcementType() != QuotaEnforcmentTypeEnum.DISABLED) {
            getModel().getQuota().setIsAvailable(true);
            AsyncQuery asyncQuery = new AsyncQuery();
            asyncQuery.Model = getModel();
            asyncQuery.asyncCallback = new INewAsyncCallback() {

                @Override
                public void OnSuccess(Object model, Object returnValue) {
                    UnitVmModel vmModel = (UnitVmModel) model;
                    ArrayList<Quota> quotaList =
                            (ArrayList<Quota>) ((VdcQueryReturnValue) returnValue).getReturnValue();
                    vmModel.getQuota().setItems(quotaList);
                }
            };
            GetQuotaByStoragePoolIdQueryParameters params = new GetQuotaByStoragePoolIdQueryParameters();
            params.setStoragePoolId(dataCenter.getId());
            Frontend.RunQuery(VdcQueryType.GetQuotaByStoragePoolId, params, asyncQuery);
        } else {
            getModel().getQuota().setIsAvailable(false);
        }
    }

    @Override
    public void Template_SelectedItemChanged()
    {
        VmTemplate template = (VmTemplate) getModel().getTemplate().getSelectedItem();

        if (template != null)
        {
            // Copy VM parameters from template.
            getModel().getOSType().setSelectedItem(template.getos());
            getModel().getNumOfSockets().setEntity(template.getnum_of_sockets());
            getModel().getTotalCPUCores().setEntity(template.getnum_of_cpus());
            getModel().getNumOfMonitors().setSelectedItem(template.getnum_of_monitors());
            getModel().getDomain().setSelectedItem(template.getdomain());
            getModel().getMemSize().setEntity(template.getmem_size_mb());
            getModel().getUsbPolicy().setSelectedItem(template.getusb_policy());
            getModel().setBootSequence(template.getdefault_boot_sequence());
            getModel().getIsHighlyAvailable().setEntity(template.getauto_startup());

            if (getModel().getVmType() == VmType.Desktop) {
                getModel().getIsStateless().setEntity(template.getis_stateless());
            }

            getModel().getCdImage().setIsChangable(!StringHelper.isNullOrEmpty(template.getiso_path()));
            if (getModel().getCdImage().getIsChangable())
            {
                getModel().getCdImage().setSelectedItem(template.getiso_path());
            }

            if (!StringHelper.isNullOrEmpty(template.gettime_zone()))
            {
                // Patch! Create key-value pair with a right key.
                getModel().getTimeZone()
                        .setSelectedItem(new KeyValuePairCompat<String, String>(template.gettime_zone(), ""));

                UpdateTimeZone();
            }
            else
            {
                UpdateDefaultTimeZone();
            }

            // Update domain list
            UpdateDomain();

            java.util.ArrayList<VDSGroup> clusters = (java.util.ArrayList<VDSGroup>) getModel().getCluster().getItems();
            VDSGroup selectCluster =
                    Linq.FirstOrDefault(clusters, new Linq.ClusterPredicate(template.getvds_group_id()));

            getModel().getCluster().setSelectedItem((selectCluster != null) ? selectCluster
                    : Linq.FirstOrDefault(clusters));

            // Update display protocol selected item
            EntityModel displayProtocol = null;
            boolean isFirst = true;
            for (Object item : getModel().getDisplayProtocol().getItems())
            {
                EntityModel a = (EntityModel) item;
                if (isFirst)
                {
                    displayProtocol = a;
                    isFirst = false;
                }
                DisplayType dt = (DisplayType) a.getEntity();
                if (dt == template.getdefault_display_type())
                {
                    displayProtocol = a;
                    break;
                }
            }
            getModel().getDisplayProtocol().setSelectedItem(displayProtocol);

            // By default, take kernel params from template.
            getModel().getKernel_path().setEntity(template.getkernel_url());
            getModel().getKernel_parameters().setEntity(template.getkernel_params());
            getModel().getInitrd_path().setEntity(template.getinitrd_url());

            if (!template.getId().equals(NGuid.Empty))
            {
                getModel().getStorageDomain().setIsChangable(true);
                getModel().getProvisioning().setIsChangable(true);

                getModel().setIsBlankTemplate(false);
                InitDisks();
            }
            else
            {
                getModel().getStorageDomain().setIsChangable(false);
                getModel().getProvisioning().setIsChangable(false);

                getModel().setIsBlankTemplate(true);
                getModel().setIsDisksAvailable(false);
                getModel().setDisks(null);
            }

            InitPriority(template.getpriority());
            InitStorageDomains();
            UpdateMinAllocatedMemory();
        }
    }

    @Override
    public void Cluster_SelectedItemChanged()
    {
        UpdateDefaultHost();
        UpdateIsCustomPropertiesAvailable();
        UpdateMinAllocatedMemory();
        UpdateNumOfSockets();
    }

    @Override
    public void DefaultHost_SelectedItemChanged()
    {
        UpdateCdImage();
    }

    @Override
    public void Provisioning_SelectedItemChanged()
    {
        boolean provisioning = (Boolean) getModel().getProvisioning().getEntity();
        getModel().getProvisioningThin_IsSelected().setEntity(!provisioning);
        getModel().getProvisioningClone_IsSelected().setEntity(provisioning);
        getModel().getDisksAllocationModel().setIsVolumeFormatAvailable(provisioning);

        UpdateIsDisksAvailable();
        InitStorageDomains();
    }

    @Override
    public void UpdateMinAllocatedMemory()
    {
        VDSGroup cluster = (VDSGroup) getModel().getCluster().getSelectedItem();
        if (cluster == null)
        {
            return;
        }

        double overCommitFactor = 100.0 / cluster.getmax_vds_memory_over_commit();
        getModel().getMinAllocatedMemory()
                .setEntity((int) ((Integer) getModel().getMemSize().getEntity() * overCommitFactor));
    }

    private void InitTemplate()
    {
        storage_pool dataCenter = (storage_pool) getModel().getDataCenter().getSelectedItem();

        // Filter according to system tree selection.
        if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Storage)
        {
            storage_domains storage = (storage_domains) getSystemTreeSelectedItem().getEntity();

            AsyncDataProvider.GetTemplateListByDataCenter(new AsyncQuery(new Object[] { this, storage },
                    new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target1, Object returnValue1) {

                            Object[] array1 = (Object[]) target1;
                            NewVmModelBehavior behavior1 = (NewVmModelBehavior) array1[0];
                            storage_domains storage1 = (storage_domains) array1[1];
                            AsyncDataProvider.GetTemplateListByStorage(new AsyncQuery(new Object[] { behavior1,
                                    returnValue1 },
                                    new INewAsyncCallback() {
                                        @Override
                                        public void OnSuccess(Object target2, Object returnValue2) {

                                            Object[] array2 = (Object[]) target2;
                                            NewVmModelBehavior behavior2 = (NewVmModelBehavior) array2[0];
                                            java.util.ArrayList<VmTemplate> templatesByDataCenter =
                                                    (java.util.ArrayList<VmTemplate>) array2[1];
                                            java.util.ArrayList<VmTemplate> templatesByStorage =
                                                    (java.util.ArrayList<VmTemplate>) returnValue2;
                                            VmTemplate blankTemplate =
                                                    Linq.FirstOrDefault(templatesByDataCenter,
                                                            new Linq.TemplatePredicate(NGuid.Empty));
                                            if (blankTemplate != null)
                                            {
                                                templatesByStorage.add(0, blankTemplate);
                                            }
                                            behavior2.PostInitTemplate((java.util.ArrayList<VmTemplate>) returnValue2);

                                        }
                                    }),
                                    storage1.getId());

                        }
                    }, getModel().getHash()),
                    dataCenter.getId());
        }
        else
        {
            AsyncDataProvider.GetTemplateListByDataCenter(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target, Object returnValue) {

                            NewVmModelBehavior behavior = (NewVmModelBehavior) target;
                            behavior.PostInitTemplate((java.util.ArrayList<VmTemplate>) returnValue);

                        }
                    }, getModel().getHash()), dataCenter.getId());
        }
    }

    private void PostInitTemplate(java.util.ArrayList<VmTemplate> templates)
    {
        // If there was some template selected before, try select it again.
        VmTemplate oldTemplate = (VmTemplate) getModel().getTemplate().getSelectedItem();

        getModel().getTemplate().setItems(templates);

        getModel().getTemplate().setSelectedItem(Linq.FirstOrDefault(templates,
                oldTemplate != null ? new Linq.TemplatePredicate(oldTemplate.getId())
                        : new Linq.TemplatePredicate(NGuid.Empty)));

        UpdateIsDisksAvailable();
    }

    public void InitCdImage()
    {
        UpdateCdImage();
    }

    private void InitDisks()
    {
        VmTemplate template = (VmTemplate) getModel().getTemplate().getSelectedItem();

        AsyncDataProvider.GetTemplateDiskList(new AsyncQuery(getModel(),
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        UnitVmModel model = (UnitVmModel) target;
                        java.util.ArrayList<DiskImage> disks = (java.util.ArrayList<DiskImage>) returnValue;
                        Collections.sort(disks, new Linq.DiskByInternalDriveMappingComparer());
                        java.util.ArrayList<DiskModel> list = new java.util.ArrayList<DiskModel>();
                        for (DiskImage a : disks)
                        {
                            DiskModel diskModel = new DiskModel();
                            diskModel.setIsNew(true);
                            diskModel.setName(a.getinternal_drive_mapping());
                            EntityModel tempVar = new EntityModel();
                            tempVar.setEntity(a.getSizeInGigabytes());
                            diskModel.setSize(tempVar);
                            ListModel tempVar2 = new ListModel();
                            tempVar2.setItems((a.getvolume_type() == VolumeType.Preallocated ? new java.util.ArrayList<VolumeType>(java.util.Arrays.asList(new VolumeType[] { VolumeType.Preallocated }))
                                    : DataProvider.GetVolumeTypeList()));
                            tempVar2.setSelectedItem(a.getvolume_type());
                            diskModel.setVolumeType(tempVar2);
                            diskModel.setDiskImage(a);
                            diskModel.getVolumeType().setIsAvailable(false);
                            list.add(diskModel);
                        }
                        model.setDisks(list);
                        UpdateIsDisksAvailable();
                        InitStorageDomains();
                    }
                },
                getModel().getHash()),
                template.getId());
    }

    public void UpdateIsDisksAvailable()
    {
        getModel().setIsDisksAvailable(getModel().getDisks() != null);
    }

    private void InitStorageDomains()
    {
        if (getModel().getDisks() == null) {
            return;
        }

        VmTemplate template = (VmTemplate) getModel().getTemplate().getSelectedItem();

        if (template != null && !template.getId().equals(NGuid.Empty))
        {
            storage_pool dataCenter = (storage_pool) getModel().getDataCenter().getSelectedItem();

            AsyncDataProvider.GetStorageDomainList(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target, Object returnValue) {
                            NewVmModelBehavior behavior = (NewVmModelBehavior) target;
                            ArrayList<storage_domains> storageDomains = (ArrayList<storage_domains>) returnValue;
                            ArrayList<storage_domains> activeStorageDomains = FilterStorageDomains(storageDomains);
                            DisksAllocationModel disksAllocationModel = getModel().getDisksAllocationModel();

                            if (activeStorageDomains.size() <= 1) {
                                disksAllocationModel.setIsChangable(false);
                                return;
                            }

                            boolean provisioning = (Boolean) behavior.getModel().getProvisioning().getEntity();
                            ArrayList<DiskModel> disks = (ArrayList<DiskModel>) behavior.getModel().getDisks();
                            Linq.Sort(activeStorageDomains, new Linq.StorageDomainByNameComparer());

                            for (DiskModel diskModel : disks) {
                                ArrayList<storage_domains> availableDiskStorageDomains;
                                if (provisioning) {
                                    availableDiskStorageDomains = activeStorageDomains;
                                }
                                else
                                {
                                    ArrayList<Guid> storageIds = diskModel.getDiskImage().getstorage_ids();
                                    availableDiskStorageDomains =
                                            Linq.getStorageDomainsByIds(storageIds, activeStorageDomains);
                                }
                                Linq.Sort(availableDiskStorageDomains, new Linq.StorageDomainByNameComparer());
                                diskModel.getStorageDomain().setItems(availableDiskStorageDomains);
                            }

                            ArrayList<storage_domains> storageDomainsDisjoint =
                                    Linq.getStorageDomainsDisjoint(disks, activeStorageDomains);

                            Linq.Sort(storageDomainsDisjoint, new Linq.StorageDomainByNameComparer());

                            disksAllocationModel.getStorageDomain().setItems(
                                    provisioning ? activeStorageDomains : storageDomainsDisjoint);
                        }
                    }, getModel().getHash()), dataCenter.getId());
        }
        else
        {
            getModel().getStorageDomain().setItems(new java.util.ArrayList<storage_domains>());
            getModel().getStorageDomain().setSelectedItem(null);
            getModel().getStorageDomain().setIsChangable(false);
        }
    }

    public ArrayList<storage_domains> FilterStorageDomains(ArrayList<storage_domains> storageDomains)
    {
        // filter only the Active storage domains (Active regarding the relevant storage pool).
        ArrayList<storage_domains> list = new ArrayList<storage_domains>();
        for (storage_domains a : storageDomains)
        {
            if (Linq.IsDataActiveStorageDomain(a))
            {
                list.add(a);
            }
        }

        // Filter according to system tree selection.
        if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Storage)
        {
            storage_domains selectStorage = (storage_domains) getSystemTreeSelectedItem().getEntity();
            storage_domains sd = Linq.FirstOrDefault(list, new Linq.StoragePredicate(selectStorage.getId()));
            list = new ArrayList<storage_domains>(Arrays.asList(new storage_domains[] { sd }));

            getModel().getStorageDomain().setItems(list);
            getModel().getStorageDomain().setSelectedItem(sd);
            getModel().getStorageDomain().setIsChangable(false);
        }
        else
        {
            getModel().getStorageDomain().setItems(list);
            getModel().getStorageDomain().setSelectedItem(Linq.FirstOrDefault(list));
            getModel().getStorageDomain().setIsChangable(true);
        }

        return list;
    }

}
