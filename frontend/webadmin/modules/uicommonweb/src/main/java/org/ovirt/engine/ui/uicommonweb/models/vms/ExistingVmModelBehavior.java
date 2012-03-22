package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcmentTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.common.queries.GetQuotaByStoragePoolIdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;

@SuppressWarnings("unused")
public class ExistingVmModelBehavior extends IVmModelBehavior
{
    protected VM vm;

    public ExistingVmModelBehavior(VM vm)
    {
        this.vm = vm;
    }

    public VM getVm() {
        return vm;
    }

    public void setVm(VM vm) {
        this.vm = vm;
    }

    @Override
    public void Initialize(SystemTreeItemModel systemTreeSelectedItem)
    {
        super.Initialize(systemTreeSelectedItem);
        AsyncDataProvider.GetDataCenterById(new AsyncQuery(getModel(),
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        UnitVmModel model = (UnitVmModel) target;
                        if (returnValue != null)
                        {
                            storage_pool dataCenter = (storage_pool) returnValue;
                            java.util.ArrayList<storage_pool> list =
                                    new java.util.ArrayList<storage_pool>(java.util.Arrays.asList(new storage_pool[] { dataCenter }));
                            model.SetDataCenter(model, list);
                            model.getDataCenter().setIsChangable(false);
                        }
                        else
                        {
                            ExistingVmModelBehavior behavior = (ExistingVmModelBehavior) model.getBehavior();
                            VM currentVm = behavior.vm;
                            VDSGroup tempVar = new VDSGroup();
                            tempVar.setId(currentVm.getvds_group_id());
                            tempVar.setname(currentVm.getvds_group_name());
                            tempVar.setcompatibility_version(currentVm.getvds_group_compatibility_version());
                            tempVar.setstorage_pool_id(currentVm.getstorage_pool_id());
                            VDSGroup cluster = tempVar;
                            model.getCluster()
                                    .setItems(new java.util.ArrayList<VDSGroup>(java.util.Arrays.asList(new VDSGroup[] { cluster })));
                            model.getCluster().setSelectedItem(cluster);
                            behavior.InitTemplate();
                            behavior.InitCdImage();
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
                        ExistingVmModelBehavior behavior = (ExistingVmModelBehavior) array[0];
                        UnitVmModel model = (UnitVmModel) array[1];
                        VM vm = ((ExistingVmModelBehavior) array[0]).vm;
                        java.util.ArrayList<VDSGroup> clusters = (java.util.ArrayList<VDSGroup>) returnValue;
                        model.SetClusters(model, clusters, vm.getvds_group_id().getValue());
                        behavior.InitTemplate();
                        behavior.InitCdImage();

                    }
                }, getModel().getHash()), dataCenter.getId());
        if (dataCenter.getQuotaEnforcementType() != QuotaEnforcmentTypeEnum.DISABLED) {
            getModel().getQuota().setIsAvailable(true);
            GetQuotaByStoragePoolIdQueryParameters params = new GetQuotaByStoragePoolIdQueryParameters();
            params.setStoragePoolId(dataCenter.getId());
            Frontend.RunQuery(VdcQueryType.GetQuotaByStoragePoolId, params
                    , new AsyncQuery(new Object[] { this,
                            getModel() }, new INewAsyncCallback() {

                        @Override
                        public void OnSuccess(Object model, Object returnValue) {
                            Object[] array = (Object[]) model;
                            ExistingVmModelBehavior behavior = (ExistingVmModelBehavior) array[0];
                            UnitVmModel unitVmModel = (UnitVmModel) array[1];
                            VM vm = behavior.vm;
                            ArrayList<Quota> quotaList =
                                    (ArrayList<Quota>) ((VdcQueryReturnValue) returnValue).getReturnValue();
                            unitVmModel.getQuota().setItems(quotaList);
                            if (vm.getStaticData().getQuotaId() != null) {
                                for (Quota quota : quotaList) {
                                    if (quota.getId().equals(quota.getId())) {
                                        unitVmModel.getQuota().setSelectedItem(quota);
                                        break;
                                    }
                                }
                            }
                        }
                    }));
        } else {
            getModel().getQuota().setIsAvailable(false);
        }
    }

    @Override
    public void Template_SelectedItemChanged()
    {
        // This method will be called even if a VM created from Blank template.

        // Update model state according to VM properties.
        getModel().getName().setEntity(vm.getvm_name());
        getModel().getDescription().setEntity(vm.getvm_description());
        getModel().getMemSize().setEntity(vm.getvm_mem_size_mb());
        getModel().getMinAllocatedMemory().setEntity(vm.getMinAllocatedMem());
        getModel().getOSType().setSelectedItem(vm.getvm_os());
        getModel().getDomain().setSelectedItem(vm.getvm_domain());
        getModel().getUsbPolicy().setSelectedItem(vm.getusb_policy());
        getModel().getNumOfMonitors().setSelectedItem(vm.getnum_of_monitors());
        getModel().setBootSequence(vm.getdefault_boot_sequence());
        getModel().getIsHighlyAvailable().setEntity(vm.getauto_startup());

        getModel().getNumOfSockets().setEntity(vm.getnum_of_sockets());
        getModel().getNumOfSockets().setIsChangable(!vm.isStatusUp());

        getModel().getTotalCPUCores().setEntity(vm.getnum_of_cpus());
        getModel().getTotalCPUCores().setIsChangable(!vm.isStatusUp());

        getModel().getIsStateless().setEntity(vm.getis_stateless());
        getModel().getIsStateless().setIsAvailable(vm.getVmPoolId() == null);

        getModel().getKernel_parameters().setEntity(vm.getkernel_params());
        getModel().getKernel_path().setEntity(vm.getkernel_url());
        getModel().getInitrd_path().setEntity(vm.getinitrd_url());

        getModel().getCustomProperties().setEntity(vm.getCustomProperties());

        if (vm.getis_initialized())
        {
            getModel().getTimeZone().setIsChangable(false);
            getModel().getTimeZone()
                    .getChangeProhibitionReasons()
                    .add("Time Zone cannot be change since the Virtual Machine was booted at the first time.");
        }

        getModel().getTimeZone().setSelectedItem(new KeyValuePairCompat<String, String>(vm.gettime_zone(), ""));
        UpdateTimeZone();

        // Update domain list
        UpdateDomain();

        switch (vm.getMigrationSupport())
        {
        case PINNED_TO_HOST:
            getModel().getRunVMOnSpecificHost().setEntity(true);
            break;
        case IMPLICITLY_NON_MIGRATABLE:
            getModel().getDontMigrateVM().setEntity(true);
            break;
        }

        // Storage domain and provisioning are not available for an existing VM.
        getModel().getStorageDomain().setIsChangable(false);
        getModel().getProvisioning().setIsAvailable(false);
        getModel().getProvisioning().setEntity(Guid.Empty.equals(vm.getvmt_guid()));

        // Select display protocol.
        for (Object item : getModel().getDisplayProtocol().getItems())
        {
            EntityModel model = (EntityModel) item;
            DisplayType displayType = (DisplayType) model.getEntity();

            if (displayType == vm.getdefault_display_type())
            {
                getModel().getDisplayProtocol().setSelectedItem(item);
                break;
            }
        }

        InitPriority(vm.getpriority());
    }

    @Override
    public void Cluster_SelectedItemChanged()
    {
        UpdateDefaultHost();
        UpdateIsCustomPropertiesAvailable();
        UpdateNumOfSockets();
    }

    @Override
    protected void ChangeDefualtHost()
    {
        super.ChangeDefualtHost();

        if (vm.getdedicated_vm_for_vds() != null)
        {
            Guid vdsId = vm.getdedicated_vm_for_vds().getValue();
            if (getModel().getDefaultHost().getItems() != null)
            {
                getModel().getDefaultHost().setSelectedItem(Linq.FirstOrDefault(getModel().getDefaultHost().getItems(),
                        new Linq.HostPredicate(vdsId)));
            }
            getModel().getIsAutoAssign().setEntity(false);
        }
        else
        {
            getModel().getIsAutoAssign().setEntity(true);
        }
    }

    @Override
    public void DefaultHost_SelectedItemChanged()
    {
        UpdateCdImage();
    }

    @Override
    public void Provisioning_SelectedItemChanged()
    {
    }

    @Override
    public void UpdateMinAllocatedMemory()
    {
        VDSGroup cluster = (VDSGroup) getModel().getCluster().getSelectedItem();

        if (cluster == null)
        {
            return;
        }

        if ((Integer) getModel().getMemSize().getEntity() < vm.getvm_mem_size_mb())
        {
            double overCommitFactor = 100.0 / cluster.getmax_vds_memory_over_commit();
            getModel().getMinAllocatedMemory()
                    .setEntity((int) ((Integer) getModel().getMemSize().getEntity() * overCommitFactor));
        }
        else
        {
            getModel().getMinAllocatedMemory().setEntity(vm.getMinAllocatedMem());
        }
    }

    public void InitTemplate()
    {
        AsyncDataProvider.GetTemplateById(new AsyncQuery(getModel(),
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        UnitVmModel model = (UnitVmModel) target;
                        VmTemplate template = (VmTemplate) returnValue;
                        model.getTemplate()
                                .setItems(new java.util.ArrayList<VmTemplate>(java.util.Arrays.asList(new VmTemplate[] { template })));
                        model.getTemplate().setSelectedItem(template);
                        model.getTemplate().setIsChangable(false);

                    }
                },
                getModel().getHash()),
                vm.getvmt_guid());
    }

    public void InitCdImage()
    {
        getModel().getCdImage().setSelectedItem(vm.getiso_path());
        getModel().getCdImage().setIsChangable(!StringHelper.isNullOrEmpty(vm.getiso_path()));

        UpdateCdImage();
    }
}
