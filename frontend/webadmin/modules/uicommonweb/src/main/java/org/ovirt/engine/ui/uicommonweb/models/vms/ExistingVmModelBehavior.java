package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;

@SuppressWarnings("unused")
public class ExistingVmModelBehavior extends VmModelBehaviorBase
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
                            ArrayList<storage_pool> list =
                                    new ArrayList<storage_pool>(Arrays.asList(new storage_pool[] { dataCenter }));
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
                                    .setItems(new ArrayList<VDSGroup>(Arrays.asList(new VDSGroup[] { cluster })));
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
                        ArrayList<VDSGroup> clusters = (ArrayList<VDSGroup>) returnValue;
                        model.SetClusters(model, clusters, vm.getvds_group_id().getValue());
                        behavior.InitTemplate();
                        behavior.InitCdImage();

                    }
                }, getModel().getHash()), dataCenter.getId());
        if (dataCenter.getQuotaEnforcementType() != QuotaEnforcementTypeEnum.DISABLED) {
            getModel().getQuota().setIsAvailable(true);
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
        getModel().getAllowConsoleReconnect().setEntity(vm.getAllowConsoleReconnect());
        getModel().setBootSequence(vm.getdefault_boot_sequence());
        getModel().getIsHighlyAvailable().setEntity(vm.getauto_startup());

        getModel().getTotalCPUCores().setEntity(Integer.toString(vm.getnum_of_cpus()));
        getModel().getTotalCPUCores().setIsChangable(!vm.isStatusUp());

        getModel().getIsStateless().setEntity(vm.getis_stateless());
        getModel().getIsStateless().setIsAvailable(vm.getVmPoolId() == null);

        getModel().getIsSmartcardEnabled().setEntity(vm.isSmartcardEnabled());
        getModel().getIsDeleteProtected().setEntity(vm.isDeleteProtected());

        getModel().getNumOfSockets().setSelectedItem(vm.getnum_of_sockets());
        getModel().getNumOfSockets().setIsChangable(!vm.isStatusUp());

        getModel().getKernel_parameters().setEntity(vm.getkernel_params());
        getModel().getKernel_path().setEntity(vm.getkernel_url());
        getModel().getInitrd_path().setEntity(vm.getinitrd_url());

        getModel().getCustomProperties().setEntity(vm.getCustomProperties());
        getModel().getCustomPropertySheet().setEntity(vm.getCustomProperties());
        getModel().getCpuPinning().setEntity(vm.getCpuPinning());

        if (vm.getis_initialized())
        {
            getModel().getTimeZone().setIsChangable(false);
            getModel().getTimeZone()
                    .getChangeProhibitionReasons()
                    .add("Time Zone cannot be change since the Virtual Machine was booted at the first time."); //$NON-NLS-1$
        }

        updateTimeZone(vm.gettime_zone());

        // Update domain list
        UpdateDomain();

        updateHostPinning(vm.getMigrationSupport());

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
        UpdateCustomProperties();
        UpdateNumOfSockets();
        updateQuotaByCluster(vm.getQuotaId(), vm.getQuotaName());
        updateCpuPinningVisibility();
    }

    @Override
    protected void UpdateCustomProperties() {
        super.UpdateCustomProperties();

        updateCustomPropertySheet();
    }

    @Override
    protected void ChangeDefualtHost()
    {
        super.ChangeDefualtHost();
        doChangeDefautlHost(vm.getdedicated_vm_for_vds());
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
        setupTemplate(vm, getModel().getTemplate());
    }

    public void InitCdImage()
    {
        getModel().getCdImage().setSelectedItem(vm.getiso_path());

        boolean hasCd = !StringHelper.isNullOrEmpty(vm.getiso_path());
        getModel().getCdImage().setIsChangable(hasCd);
        getModel().getCdAttached().setEntity(hasCd);

        UpdateCdImage();
    }
}
