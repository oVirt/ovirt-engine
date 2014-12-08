package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.builders.BuilderExecutor;
import org.ovirt.engine.ui.uicommonweb.builders.vm.CommentVmBaseToUnitBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.CommonVmBaseToUnitBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.NameAndDescriptionVmBaseToUnitBuilder;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes.ExistingVmInstanceTypeManager;
import org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes.InstanceTypeManager;

public class ExistingVmModelBehavior extends VmModelBehaviorBase
{
    private InstanceTypeManager instanceTypeManager;

    protected VM vm;

    private int hostCpu;
    private VDS runningOnHost;

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
    public void initialize(SystemTreeItemModel systemTreeSelectedItem) {
        super.initialize(systemTreeSelectedItem);

        getModel().getVmInitEnabled().setEntity(getVm().getVmInit() != null);
        getModel().getVmInitModel().init(getVm().getStaticData());
        getModel().getVmType().setIsChangable(true);
        getModel().getIsSoundcardEnabled().setIsChangable(true);
        getModel().getInstanceTypes().setIsChangable(!vm.isRunning());

        getModel().getVmId().setIsAvailable(true);
        getModel().getVmId().setIsChangable(false);

        loadDataCenter();
        instanceTypeManager = new ExistingVmInstanceTypeManager(getModel(), vm);

        if (vm.getVmPoolId() != null) {
            instanceTypeManager.setAlwaysEnabledFieldUpdate(true);
        }

        Frontend.getInstance().runQuery(VdcQueryType.GetVmNumaNodesByVmId,
                new IdQueryParameters(vm.getId()),
                new AsyncQuery(new INewAsyncCallback() {

                    @Override
                    public void onSuccess(Object model, Object returnValue) {
                        List<VmNumaNode> nodes =
                                (List<VmNumaNode>) ((VdcQueryReturnValue) returnValue).getReturnValue();
                        ExistingVmModelBehavior.this.getModel().setVmNumaNodes(nodes);
                        ExistingVmModelBehavior.this.getModel().updateNodeCount(nodes.size());
                    }
                }));
    }

    private void loadDataCenter() {
        AsyncDataProvider.getInstance().getDataCenterById(new AsyncQuery(getModel(),
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        UnitVmModel model = (UnitVmModel) target;
                        if (returnValue != null) {
                            StoragePool dataCenter = (StoragePool) returnValue;
                            final List<StoragePool> dataCenters =
                                    new ArrayList<StoragePool>(Arrays.asList(new StoragePool[]{dataCenter}));

                            initClusters(dataCenters);
                        } else {
                            ExistingVmModelBehavior behavior = (ExistingVmModelBehavior) model.getBehavior();
                            VM currentVm = behavior.vm;
                            VDSGroup tempVar = new VDSGroup();
                            tempVar.setId(currentVm.getVdsGroupId());
                            tempVar.setName(currentVm.getVdsGroupName());
                            tempVar.setcompatibility_version(currentVm.getVdsGroupCompatibilityVersion());
                            tempVar.setStoragePoolId(currentVm.getStoragePoolId());
                            VDSGroup cluster = tempVar;
                            DataCenterWithCluster dataCenterWithCluster =
                                    new DataCenterWithCluster(null, cluster);
                            model.getDataCenterWithClustersList().setItems(Arrays.asList(dataCenterWithCluster));
                            model.getDataCenterWithClustersList().setSelectedItem(dataCenterWithCluster);
                            behavior.initTemplate();
                            behavior.initCdImage();
                        }

                    }
                },
                getModel().getHash()),
                vm.getStoragePoolId());
    }

    protected void initClusters(final List<StoragePool> dataCenters) {
        AsyncDataProvider.getInstance().getClusterListByService(
                new AsyncQuery(getModel(), new INewAsyncCallback() {

                    @Override
                    public void onSuccess(Object target, Object returnValue) {
                        UnitVmModel model = (UnitVmModel) target;

                        List<VDSGroup> clusters = (List<VDSGroup>) returnValue;

                        List<VDSGroup> filteredClusters =
                                AsyncDataProvider.getInstance().filterByArchitecture(clusters, vm.getClusterArch());

                        model.setDataCentersAndClusters(model,
                                dataCenters,
                                filteredClusters, vm.getVdsGroupId());
                        initTemplate();
                        initCdImage();
                    }
                }, getModel().getHash()),
                true, false);
    }

    @Override
    protected void baseTemplateSelectedItemChanged() {
    }

    @Override
    public void template_SelectedItemChanged()
    {
        // This method will be called even if a VM created from Blank template.

        // Update model state according to VM properties.
        buildModel(vm.getStaticData());

        getModel().getIsStateless().setIsAvailable(vm.getVmPoolId() == null);

        getModel().getIsRunAndPause().setIsAvailable(vm.getVmPoolId() == null);

        getModel().getCpuSharesAmount().setEntity(vm.getCpuShares());
        updateCpuSharesSelection();

        updateRngDevice(getVm().getId());
        updateTimeZone(vm.getTimeZone());

        getModel().getHostCpu().setEntity(vm.isUseHostCpuFlags());

        // Storage domain and provisioning are not available for an existing VM.
        getModel().getStorageDomain().setIsChangable(false);
        getModel().getProvisioning().setIsAvailable(false);
        getModel().getProvisioning().setEntity(Guid.Empty.equals(vm.getVmtGuid()));

        getModel().getCpuPinning().setEntity(vm.getCpuPinning());

        getModel().getCustomPropertySheet().deserialize(vm.getCustomProperties());

        if (isHotSetCpuSupported()) {
            // cancel related events while fetching data
            getModel().getTotalCPUCores().getEntityChangedEvent().removeListener(getModel());
            getModel().getCoresPerSocket().getSelectedItemChangedEvent().removeListener(getModel());
            getModel().getNumOfSockets().getSelectedItemChangedEvent().removeListener(getModel());

            AsyncDataProvider.getInstance().getHostById(new AsyncQuery(this, new INewAsyncCallback() {
                @Override
                public void onSuccess(Object model, Object returnValue) {
                    ExistingVmModelBehavior existingVmModelBehavior = (ExistingVmModelBehavior) model;
                    runningOnHost = (VDS) returnValue;
                    hostCpu = calculateHostCpus();
                    existingVmModelBehavior.updateNumOfSockets();
                }
            }), vm.getRunOnVds());
        }

    updateCpuProfile(vm.getVdsGroupId(), vm.getVdsGroupCompatibilityVersion(), vm.getCpuProfileId());
    }

    @Override
    protected void buildModel(VmBase vm) {
        BuilderExecutor.build(vm, getModel(),
                              new NameAndDescriptionVmBaseToUnitBuilder(),
                              new CommentVmBaseToUnitBuilder(),
                              new CommonVmBaseToUnitBuilder());
    }

    @Override
    public void dataCenterWithClusterSelectedItemChanged() {
        super.dataCenterWithClusterSelectedItemChanged();
        if (getModel().getSelectedCluster() != null) {
            updateCpuProfile(getModel().getSelectedCluster().getId(),
                             getClusterCompatibilityVersion(),
                             vm.getCpuProfileId());
        }
    }

    private int calculateHostCpus() {
        return  getModel().getSelectedCluster().getCountThreadsAsCores() ? runningOnHost.getCpuThreads() : runningOnHost.getCpuCores();
    }

    @Override
    public void postDataCenterWithClusterSelectedItemChanged() {
        updateDefaultHost();
        updateCustomPropertySheet();
        updateNumOfSockets();
        updateQuotaByCluster(vm.getQuotaId(), vm.getQuotaName());
        updateCpuPinningVisibility();
        updateMemoryBalloon();
        updateCpuSharesAvailability();
        updateVirtioScsiAvailability();
        updateOSValues();

        instanceTypeManager.updateAll();
    }

    @Override
    protected void changeDefualtHost() {
        super.changeDefualtHost();
        doChangeDefautlHost(vm.getDedicatedVmForVds());
    }

    @Override
    public void defaultHost_SelectedItemChanged() {
        updateCdImage();
    }

    @Override
    public void provisioning_SelectedItemChanged() {
    }

    @Override
    public void oSType_SelectedItemChanged() {
        Integer osType = getModel().getOSType().getSelectedItem();

        if (osType != null) {
            Guid id = basedOnCustomInstanceType() ? vm.getId() : getModel().getInstanceTypes().getSelectedItem().getId();
            updateVirtioScsiEnabledWithoutDetach(id, osType);
        }
    }

    @Override
    public void updateMinAllocatedMemory()
    {
        DataCenterWithCluster dataCenterWithCluster = getModel().getDataCenterWithClustersList().getSelectedItem();
        if (dataCenterWithCluster == null) {
            return;
        }

        VDSGroup cluster = dataCenterWithCluster.getCluster();

        if (cluster == null)
        {
            return;
        }

        if (getModel().getMemSize().getEntity() < vm.getVmMemSizeMb())
        {
            double overCommitFactor = 100.0 / cluster.getmax_vds_memory_over_commit();
            getModel().getMinAllocatedMemory()
                    .setEntity((int) (getModel().getMemSize().getEntity() * overCommitFactor));
        }
        else
        {
            getModel().getMinAllocatedMemory().setEntity(vm.getMinAllocatedMem());
        }
    }

    protected void initTemplate()
    {
        setupTemplate(vm.getVmtGuid(), vm.isUseLatestVersion());
    }

    public void initCdImage()
    {
        getModel().getCdImage().setSelectedItem(vm.getIsoPath());

        boolean hasCd = !StringHelper.isNullOrEmpty(vm.getIsoPath());
        getModel().getCdImage().setIsChangable(hasCd);
        getModel().getCdAttached().setEntity(hasCd);

        updateCdImage();
    }

    @Override
    public void numOfSocketChanged() {
        if (isHotSetCpuSupported()) {
            int numOfSockets = extractIntFromListModel(getModel().getNumOfSockets());
            int coresPerSocket = vm.getCpuPerSocket();
            getModel().getTotalCPUCores().setEntity(Integer.toString(numOfSockets * coresPerSocket));
        } else {
            super.numOfSocketChanged();
        }
    }

    @Override
    public void totalCpuCoresChanged() {
        if (isHotSetCpuSupported()) {
            if (runningOnHost == null) {
                return; //async call didn't return with the host yet
            }
            // must not change the num of cpu per socket so the list has only 1 item
            List<Integer> coresPerSockets = Arrays.asList(new Integer[]{vm.getCpuPerSocket()});

            getModel().getCoresPerSocket().setItems(coresPerSockets);
            getModel().getNumOfSockets().setItems(createSocketsRange());

            getModel().getCoresPerSocket().setSelectedItem(vm.getCpuPerSocket());
            getModel().getNumOfSockets().setSelectedItem(vm.getNumOfSockets());

            getModel().getNumOfSockets().getSelectedItemChangedEvent().addListener(getModel());
            numOfSocketChanged();
        } else {
            super.totalCpuCoresChanged();
        }
    }

    /**
     *  span a list of all possible sockets values
     */
    private List<Integer> createSocketsRange() {
        List<Integer> res = new ArrayList<Integer>();
        int maxHostCpu = getHostCpu();
        int cpusPerSockets = vm.getCpuPerSocket();

        for (int i = 1; i <= maxHostCpu; i++) {
            // sockets stepping must not exceed the host maximum
            if (i * cpusPerSockets <= maxHostCpu) {
                res.add(i);
            }
        }
        return res;
    }

    public boolean isHotSetCpuSupported() {
        VDSGroup selectedCluster = getModel().getSelectedCluster();
        Version clusterVersion = selectedCluster.getcompatibility_version();
        Boolean hotplugEnabled = (Boolean) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.HotPlugEnabled, clusterVersion.getValue());
        boolean hotplugCpuSupported = Boolean.parseBoolean(((Map<String, String>) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigurationValues.HotPlugCpuSupported,
                clusterVersion.getValue())).get(selectedCluster.getArchitecture().name()));

        return getVm().getStatus() == VMStatus.Up && hotplugEnabled && hotplugCpuSupported;
    }

    public int getHostCpu() {
        return hostCpu;
    }

    @Override
    public void enableSinglePCI(boolean enabled) {
        super.enableSinglePCI(enabled);
        if (getInstanceTypeManager() != null) {
            getInstanceTypeManager().maybeSetSingleQxlPci(vm.getStaticData());
        }
    }

    @Override
    public ExistingVmInstanceTypeManager getInstanceTypeManager() {
        return (ExistingVmInstanceTypeManager) instanceTypeManager;
    }

    @Override
    protected VM getVmWithNuma() {
        VM dummyVm = super.getVmWithNuma();
        dummyVm.setId(vm.getId());
        List<VmNumaNode> vmNumaNodes = getModel().getVmNumaNodes();
        if (vmNumaNodes != null && !vmNumaNodes.isEmpty() && vmNumaNodes.size() == dummyVm.getvNumaNodeList().size()) {
            dummyVm.setvNumaNodeList(vmNumaNodes);
        }

        return dummyVm;
    }

    @Override
    protected void updateNumaEnabled() {
        super.updateNumaEnabled();
        updateNumaEnabledHelper();
        if (Boolean.TRUE.equals(getModel().getNumaEnabled().getEntity()) && getModel().getVmNumaNodes() != null) {
            getModel().getNumaNodeCount().setEntity(getModel().getVmNumaNodes().size());
        }
    }
}
