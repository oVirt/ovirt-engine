package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmNumaNode;
import org.ovirt.engine.core.common.businessentities.comparators.DiskByDiskAliasComparator;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.common.utils.CompatibilityVersionUtils;
import org.ovirt.engine.core.common.utils.VmCommonUtils;
import org.ovirt.engine.core.common.validation.VmActionByVmOriginTypeValidator;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.builders.BuilderExecutor;
import org.ovirt.engine.ui.uicommonweb.builders.vm.CommentVmBaseToUnitBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.CommonVmBaseToUnitBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.MultiQueuesVmBaseToUnitBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.NameAndDescriptionVmBaseToUnitBuilder;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes.ExistingVmInstanceTypeManager;
import org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes.InstanceTypeManager;

public class ExistingVmModelBehavior extends VmModelBehaviorBase<UnitVmModel> {
    private InstanceTypeManager instanceTypeManager;

    protected VM vm;

    private int hostCpu;
    private VDS runningOnHost;
    private Guid bootableDiskStorageDomainId;

    public ExistingVmModelBehavior(VM vm) {
        this.vm = vm;
        dedicatedHostsNames = Collections.emptyList();
    }

    public VM getVm() {
        return vm;
    }

    public void setVm(VM vm) {
        this.vm = vm;
    }

    @Override
    public void initialize() {
        super.initialize();

        toggleAutoSetVmHostname();
        getModel().getVmInitEnabled().setEntity(getVm().getVmInit() != null);
        getModel().getVmInitModel().init(getVm().getStaticData());
        getModel().getVmType().setIsChangeable(true);
        getModel().getIsSoundcardEnabled().setIsChangeable(true);
        getModel().getInstanceTypes().setIsChangeable(!vm.isRunning());
        getModel().getAffinityGroupList().setIsAvailable(true);
        getModel().getLabelList().setIsAvailable(true);

        getModel().getVmId().setIsAvailable(true);
        getModel().getVmId().setIsChangeable(false);

        loadDataCenter();
        instanceTypeManager = new ExistingVmInstanceTypeManager(getModel(), vm);

        if (vm.getVmPoolId() != null) {
            instanceTypeManager.setAlwaysEnabledFieldUpdate(true);
        }

        if (vm.isNextRunConfigurationExists()) {
            getModel().setVmNumaNodes(vm.getvNumaNodeList());
            getModel().updateNodeCount(vm.getvNumaNodeList().size());
        } else {
            Frontend.getInstance().runQuery(QueryType.GetVmNumaNodesByVmId,
                    new IdQueryParameters(vm.getId()),
                    new AsyncQuery<QueryReturnValue>(returnValue -> {
                        List<VmNumaNode> nodes = returnValue.getReturnValue();
                        getModel().setVmNumaNodes(nodes);
                        getModel().updateNodeCount(nodes.size());
                    }));
        }
        // load dedicated host names into host names list
        if (getVm().getDedicatedVmForVdsList().size() > 0) {
            Frontend.getInstance().runQuery(QueryType.GetAllHostNamesPinnedToVmById,
                    new IdQueryParameters(vm.getId()),
                    asyncQuery((QueryReturnValue returnValue) ->
                            setDedicatedHostsNames((List<String>) returnValue.getReturnValue())));
        }

        if (getVm().isHostedEngine()) {
            getModel().getIsHighlyAvailable().setEntity(false);
            getModel().getIsHighlyAvailable().setIsChangeable(false);
            getModel().getIsHighlyAvailable().setChangeProhibitionReason(constants.noHaWhenHostedEngineUsed());
        }
    }

    private void loadDataCenter() {
        // Preinitialize the VM compatibility version because it's needed during init
        Version newCustomCompatibilityVersion =
                ((ExistingVmModelBehavior) getModel().getBehavior()).getVm().getStaticData().getCustomCompatibilityVersion();
        if (newCustomCompatibilityVersion != null) {
            getModel().getCustomCompatibilityVersion().setItems(
                    Collections.singletonList(newCustomCompatibilityVersion), newCustomCompatibilityVersion);
        }

        AsyncDataProvider.getInstance().getDataCenterById(asyncQuery(
                dataCenter -> {

                    if (dataCenter != null) {
                        final List<StoragePool> dataCenters =
                                new ArrayList<>(Arrays.asList(new StoragePool[]{dataCenter}));

                        initClusters(dataCenters);
                    } else {
                        ExistingVmModelBehavior behavior = (ExistingVmModelBehavior) getModel().getBehavior();
                        VM currentVm = behavior.vm;
                        Cluster cluster = new Cluster();
                        cluster.setId(currentVm.getClusterId());
                        cluster.setName(currentVm.getClusterName());
                        cluster.setCompatibilityVersion(currentVm.getClusterCompatibilityVersion());
                        cluster.setStoragePoolId(currentVm.getStoragePoolId());
                        DataCenterWithCluster dataCenterWithCluster =
                                new DataCenterWithCluster(null, cluster);
                        getModel().getDataCenterWithClustersList().setItems(Arrays.asList(dataCenterWithCluster));
                        getModel().getDataCenterWithClustersList().setSelectedItem(dataCenterWithCluster);
                        behavior.initTemplate();
                        behavior.updateCdImage();
                    }

                }),
                vm.getStoragePoolId());
    }

    protected void initClusters(final List<StoragePool> dataCenters) {
        AsyncDataProvider.getInstance().getClusterListByService(
                asyncQuery(clusters -> {
                    List<Cluster> filteredClusters =
                            AsyncDataProvider.getInstance().filterByArchitecture(clusters, vm.getClusterArch());

                    getModel().setDataCentersAndClusters(getModel(),
                            dataCenters,
                            filteredClusters, vm.getClusterId());
                    updateCompatibilityVersion();
                    initTemplate();
                    updateCdImage();
                }),
                true, false);
    }

    @Override
    public void templateWithVersion_SelectedItemChanged() {
        // This method will be called even if a VM created from Blank template.

        // Update model state according to VM properties.
        buildModel(vm.getStaticData(), (source, destination) -> {
            getModel().getIsStateless().setIsAvailable(vm.getVmPoolId() == null);

            getModel().getIsRunAndPause().setIsAvailable(vm.getVmPoolId() == null);

            getModel().getCpuSharesAmount().setEntity(vm.getCpuShares());
            updateCpuSharesSelection();

            updateRngDevice(getVm().getId());
            updateTimeZone(vm.getTimeZone());

            updateGraphics(vm.getId());

            getModel().getHostCpu().setEntity(isHostCpuValueStillBasedOnTemp() ? vm.isUseHostCpuFlags() : false);

            // Storage domain and provisioning are not available for an existing VM.
            getModel().getStorageDomain().setIsChangeable(false);
            getModel().getProvisioning().setIsAvailable(false);
            getModel().getProvisioning().setEntity(Guid.Empty.equals(vm.getVmtGuid()));

            getModel().getCpuPinning().setEntity(vm.getCpuPinning());

            getModel().getCustomPropertySheet().deserialize(vm.getCustomProperties());

            if (isHotSetCpuSupported()) {
                // cancel related events while fetching data
                getModel().getTotalCPUCores().getEntityChangedEvent().removeListener(getModel());
                getModel().getCoresPerSocket().getSelectedItemChangedEvent().removeListener(getModel());
                getModel().getThreadsPerCore().getSelectedItemChangedEvent().removeListener(getModel());
                getModel().getNumOfSockets().getSelectedItemChangedEvent().removeListener(getModel());

                AsyncDataProvider.getInstance().getHostById(new AsyncQuery<>(returnValue -> {
                    runningOnHost = returnValue;
                    hostCpu = calculateHostCpus();
                    updateNumOfSockets();
                }), vm.getRunOnVds());
            }

            updateCpuProfile(vm.getClusterId(), vm.getCpuProfileId());
            getModel().updateResumeBehavior();

            getModel().getMigrationMode().setSelectedItem(vm.getMigrationSupport());

            getModel().getTscFrequency().setEntity(vm.getUseTscFrequency());

            updateBiosType();

            getInstanceTypeManager().updateInstanceTypeFieldsFromSource();
        });
    }

    @Override
    protected void buildModel(VmBase vm,
                              BuilderExecutor.BuilderExecutionFinished<VmBase, UnitVmModel> callback) {
        new BuilderExecutor<>(callback,
                              new NameAndDescriptionVmBaseToUnitBuilder(),
                              new CommentVmBaseToUnitBuilder(),
                              new CommonVmBaseToUnitBuilder(),
                              new MultiQueuesVmBaseToUnitBuilder())
                .build(vm, getModel());
    }

    @Override
    public void dataCenterWithClusterSelectedItemChanged() {
        super.dataCenterWithClusterSelectedItemChanged();
        if (getModel().getSelectedCluster() != null) {
            updateCpuProfile(getModel().getSelectedCluster().getId(), vm.getCpuProfileId());

            if (isInStateWithMemoryVolume(getVm()) && !isRestoreMemoryVolumeSupported()) {
                getModel().getEditingEnabled().setMessage(constants.suspendedVMsWhenClusterChange());
            }
        }
    }

    private boolean isInStateWithMemoryVolume(VM vm) {
        return EnumSet.of(VMStatus.Suspended, VMStatus.SavingState, VMStatus.RestoringState).contains(vm.getStatus());
    }

    private boolean isRestoreMemoryVolumeSupported() {
        Version oldVmEffectiveVersion = getVm().getCompatibilityVersion(); // before edit

        Version newVmCustomCompatibilityVersion = getModel().getCustomCompatibilityVersion() == null ?
                null : getModel().getCustomCompatibilityVersion().getSelectedItem();
        Version newClusterVersion = getModel().getSelectedCluster() == null ?
                null : getModel().getSelectedCluster().getCompatibilityVersion();
        Version newVmEffectiveVersion = CompatibilityVersionUtils.getEffective(newVmCustomCompatibilityVersion,
                newClusterVersion, Version.getLast());

        return oldVmEffectiveVersion.equals(newVmEffectiveVersion);
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
        updateInstanceImages();
        updateLeaseStorageDomains(vm.getLeaseStorageDomainId());

        instanceTypeManager.updateAll();
    }

    @Override
    protected void commonInitialize() {
        super.commonInitialize();

        getModel().getIsHighlyAvailable().getEntityChangedEvent().addListener((ev, sender, args) -> {
            boolean ha = getModel().getIsHighlyAvailable().getEntity();
            // VM set as HA
            if (ha && !vm.isAutoStartup()) {
                // bootableDiskStorageDomainId can be null in case of diskless VM or
                // VM without bootable disk or in case of race condition when the
                // VM disks aren't initialized yet
                if (bootableDiskStorageDomainId != null) {
                    setVmLeaseDefaultStorageDomain(bootableDiskStorageDomainId);
                }
            }
        });
    }

    private void updateInstanceImages() {
        AsyncDataProvider.getInstance().getVmDiskList(asyncQuery(disks -> {
            List<InstanceImageLineModel> imageLineModels = new ArrayList<>();
            boolean isChangeable = vm == null || VmActionByVmOriginTypeValidator.isCommandAllowed(vm, ActionType.UpdateDisk);

            Collections.sort(disks, new DiskByDiskAliasComparator());
            for (Disk disk : disks) {
                InstanceImageLineModel lineModel = new InstanceImageLineModel(getModel().getInstanceImages());
                lineModel.initialize(disk, getVm());
                lineModel.setEnabled(isChangeable);
                imageLineModels.add(lineModel);
            }

            getModel().getInstanceImages().setIsChangeable(isChangeable);
            getModel().getInstanceImages().setItems(imageLineModels);
            getModel().getInstanceImages().setVm(getVm());

            bootableDiskStorageDomainId = findDefaultStorageDomainForVmLease(disks);
        }), getVm().getId());
    }

    @Override
    protected void changeDefaultHost() {
        super.changeDefaultHost();
        doChangeDefaultHost(vm.getDedicatedVmForVdsList());

        if (isHostCpuValueStillBasedOnTemp()) {
            getModel().getHostCpu().setEntity(vm.isUseHostCpuFlags());
        }
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
        super.oSType_SelectedItemChanged();
        Integer osType = getModel().getOSType().getSelectedItem();

        if (osType != null) {
            Guid id = basedOnCustomInstanceType() ? vm.getId() : getModel().getInstanceTypes().getSelectedItem().getId();
            updateVirtioScsiEnabledWithoutDetach(id, osType);
        }
    }

    @Override
    public void updateMinAllocatedMemory() {
        if (getModel().getMemSize().getEntity() == null) {
            return;
        }

        Cluster cluster = getModel().getSelectedCluster();
        if (cluster == null) {
            return;
        }

        int minMemory = VmCommonUtils.calcMinMemory(
                getModel().getMemSize().getEntity(), cluster.getMaxVdsMemoryOverCommit());
        getModel().getMinAllocatedMemory().setEntity(minMemory);
    }

    protected void initTemplate() {
        setupTemplateWithVersion(vm.getVmtGuid(), vm.isUseLatestVersion(), false);
    }

    @Override
    protected void setImagesToModel(UnitVmModel model, List<RepoImage> images) {
        model.getCdImage().setItems(images);

        boolean hasCd = !StringHelper.isNullOrEmpty(vm.getIsoPath());
        getModel().getCdImage().setIsChangeable(hasCd);
        getModel().getCdAttached().setEntity(hasCd);

        if (hasCd) {
            RepoImage selectedImage = images.stream().filter(i -> vm.getIsoPath().equals(i.getRepoImageId())).findFirst().orElse(null);
            model.getCdImage().setSelectedItem((selectedImage != null) ? selectedImage
                    : Linq.firstOrNull(images));
        }
    }

    private void toggleAutoSetVmHostname() {
        if (getVm().getVmInit() != null && vm.getName() != null
                && !vm.getName().equals(getVm().getVmInit().getHostname())) {
            getModel().getVmInitModel().disableAutoSetHostname();
        }
    }

    private void addCpuListeners() {
        getModel().getTotalCPUCores().getEntityChangedEvent().addListener(getModel());
        getModel().getNumOfSockets().getSelectedItemChangedEvent().addListener(getModel());
    }

    private void removeCpuListeners() {// remove if exists
        getModel().getTotalCPUCores().getEntityChangedEvent().removeListener(getModel());
        getModel().getNumOfSockets().getSelectedItemChangedEvent().removeListener(getModel());
    }

    @Override
    public void numOfSocketChanged() {
        if (isHotSetCpuSupported()) {
            int numOfSockets = extractIntFromListModel(getModel().getNumOfSockets());
            int coresPerSocket = vm.getCpuPerSocket();
            int threadsPerCore = vm.getThreadsPerCpu();

            removeCpuListeners();
            getModel().getTotalCPUCores().setEntity(Integer.toString(numOfSockets * coresPerSocket * threadsPerCore));
            addCpuListeners();
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

            int totalCpuCores = getTotalCpuCores();
            if (totalCpuCores == 0) {
                return;
            }

            removeCpuListeners();

            if ((totalCpuCores / vm.getThreadsPerCpu()) > getHostCpu()) {
                totalCpuCores = getHostCpu() * vm.getThreadsPerCpu();
                getModel().getTotalCPUCores().setEntity(Integer.toString(totalCpuCores));
            }

            // Do not change threads/cores
            List<Integer> coresPerSockets = Arrays.asList(new Integer[]{vm.getCpuPerSocket()});
            List<Integer> threadsPerCore = Arrays.asList(new Integer[]{vm.getThreadsPerCpu()});

            getModel().getThreadsPerCore().setItems(threadsPerCore);
            getModel().getCoresPerSocket().setItems(coresPerSockets);
            getModel().getThreadsPerCore().setSelectedItem(vm.getThreadsPerCpu());
            getModel().getCoresPerSocket().setSelectedItem(vm.getCpuPerSocket());

            // change num of sockets
            getModel().getNumOfSockets().setItems(createSocketsRange());
            int sockets = totalCpuCores / (vm.getCpuPerSocket() * vm.getThreadsPerCpu());
            getModel().getNumOfSockets().setSelectedItem(sockets);

            addCpuListeners();
        } else {
            super.totalCpuCoresChanged();
        }
    }

    /**
     *  span a list of all possible sockets values
     */
    private List<Integer> createSocketsRange() {
        List<Integer> res = new ArrayList<>();
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
        Cluster selectedCluster = getModel().getSelectedCluster();
        Version compatibilityVersion = getModel().getCompatibilityVersion();
        boolean hotplugCpuSupported = Boolean.parseBoolean(
                    ((Map<String, String>) AsyncDataProvider.getInstance().getConfigValuePreConverted(
                            ConfigValues.HotPlugCpuSupported,
                            compatibilityVersion.getValue()))
                    .get(selectedCluster.getArchitecture().name()));

        return getVm().getStatus() == VMStatus.Up && hotplugCpuSupported;
    }

    public int getHostCpu() {
        return hostCpu;
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

    @Override
    public void updateMaxMemory() {
        if (vm.getStatus() != VMStatus.Up) {
            super.updateMaxMemory();
        }
    }
}
