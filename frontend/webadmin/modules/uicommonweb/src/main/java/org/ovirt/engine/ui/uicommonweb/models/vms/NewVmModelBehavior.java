package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.InstanceType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.builders.BuilderExecutor;
import org.ovirt.engine.ui.uicommonweb.builders.vm.CoreVmBaseToUnitBuilder;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateWithVersion;
import org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes.InstanceTypeManager;
import org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes.NewVmInstanceTypeManager;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NewVmModelBehavior extends VmModelBehaviorBase<UnitVmModel> {

    private InstanceTypeManager instanceTypeManager;

    private boolean updateStatelessFlag = true;

    @Override
    public void initialize() {
        super.initialize();

        getModel().getIsSoundcardEnabled().setIsChangeable(true);
        getModel().getVmType().setIsChangeable(true);
        getModel().getVmId().setIsAvailable(true);
        getModel().getLabelList().setIsAvailable(true);

        loadDataCenters();

        initPriority(0);
        getModel().getVmInitModel().init(null);

        instanceTypeManager = new NewVmInstanceTypeManager(getModel());
    }

    @Override
    protected void commonInitialize() {
        super.commonInitialize();

        getModel().getIsStateless().getEntityChangedEvent().addListener(new UpdateTemplateWithVersionListener() {
            @Override
            protected void beforeUpdate() {
                // will be moved back in the callback which can be async
                updateStatelessFlag = false;
            }

            @Override
            protected boolean isAddLatestVersion() {
                return getModel().getIsStateless().getEntity();
            }
        });
    }

    protected void loadDataCenters() {
        AsyncDataProvider.getInstance().getDataCenterByClusterServiceList(new AsyncQuery<>(
                        returnValue -> {

                            final ArrayList<StoragePool> dataCenters = new ArrayList<>();
                            for (StoragePool a : returnValue) {
                                if (a.getStatus() == StoragePoolStatus.Up) {
                                    dataCenters.add(a);
                                }
                            }

                            if (!dataCenters.isEmpty()) {
                                AsyncDataProvider.getInstance().getClusterListByService(
                                        new AsyncQuery<>(clusterList -> {
                                            List<Cluster> filteredClusterList = AsyncDataProvider.getInstance().filterClustersWithoutArchitecture(clusterList);
                                            getModel().setDataCentersAndClusters(getModel(),
                                                    dataCenters,
                                                    filteredClusterList, null);
                                            initCdImage();

                                        }),
                                        true, false);
                            } else {
                                getModel().disableEditing(ConstantsManager.getInstance().getConstants().notAvailableWithNoUpDC());
                            }
                        }),
                true,
                false);
    }

    @Override
    public void templateWithVersion_SelectedItemChanged() {
        TemplateWithVersion selectedTemplateWithVersion = getModel().getTemplateWithVersion().getSelectedItem();
        if (selectedTemplateWithVersion != null) {
            VmTemplate selectedTemplate = selectedTemplateWithVersion.getTemplateVersion();
            selectedTemplateChanged(selectedTemplate);
        }
    }

    private void selectedTemplateChanged(final VmTemplate template) {
        // Copy VM parameters from template.
        buildModel(template, (source, destination) -> {
            setSelectedOSType(template, getModel().getSelectedCluster().getArchitecture());

            doChangeDefaultHost(template.getDedicatedVmForVdsList());

            if (updateStatelessFlag) {
                getModel().getIsStateless().setEntity(template.isStateless());
            }

            updateStatelessFlag = true;

            getModel().getIsRunAndPause().setEntity(template.isRunAndPause());

            boolean hasCd = !StringHelper.isNullOrEmpty(template.getIsoPath());

            getModel().getCdImage().setIsChangeable(hasCd);
            getModel().getCdAttached().setEntity(hasCd);
            if (hasCd) {
                RepoImage currentCD = getModel().getCdImage().getItems().stream()
                        .filter(i -> i.getRepoImageId().equals(template.getIsoPath())).findFirst().orElse(null);
                getModel().getCdImage().setSelectedItem(currentCD);
            }

            updateTimeZone(template.getTimeZone());

            if (!template.getId().equals(Guid.Empty)) {
                getModel().getStorageDomain().setIsChangeable(true);
                getModel().getProvisioning().setIsChangeable(true);

                getModel().getCopyPermissions().setIsAvailable(true);
                initDisks();
            } else {
                getModel().getStorageDomain().setIsChangeable(false);
                getModel().getProvisioning().setIsChangeable(false);

                getModel().getCopyPermissions().setIsAvailable(false);
                getModel().setDisks(null);
                getModel().setIsDisksAvailable(false);
                getModel().getInstanceImages().setIsChangeable(true);
            }

            getModel().getAllowConsoleReconnect().setEntity(template.isAllowConsoleReconnect());
            getModel().getVmType().setSelectedItem(template.getVmType());
            getModel().getUsbPolicy().setSelectedItem(template.getUsbPolicy());
            updateRngDevice(template.getId());

            initStorageDomains();

            InstanceType selectedInstanceType = getModel().getInstanceTypes().getSelectedItem();
            int instanceTypeMinAllocatedMemory = selectedInstanceType != null ? selectedInstanceType.getMinAllocatedMem() : 0;

            // do not update if specified on template or instance type
            if (template.getMinAllocatedMem() == 0 && instanceTypeMinAllocatedMemory == 0) {
                updateMinAllocatedMemory();
            }

            updateQuotaByCluster(template.getQuotaId(), template.getQuotaName());
            getModel().getCustomPropertySheet().deserialize(template.getCustomProperties());

            getModel().getVmInitModel().init(template);
            getModel().getVmInitEnabled().setEntity(template.getVmInit() != null);

            if (getModel().getSelectedCluster() != null) {
                updateCpuProfile(getModel().getSelectedCluster().getId(), template.getCpuProfileId());
            }
            provisioning_SelectedItemChanged();
            updateMigrationForLocalSD();

            // A workaround for setting the current saved CustomCompatibilityVersion value after
            // it was reset by getTemplateWithVersion event
            getModel().getCustomCompatibilityVersion().setSelectedItem(getSavedCurrentCustomCompatibilityVersion());
            setCustomCompatibilityVersionChangeInProgress(false);
            updateLeaseStorageDomains(template.getLeaseStorageDomainId());
        });
    }

    @Override
    protected void buildModel(VmBase template, BuilderExecutor.BuilderExecutionFinished callback) {
        new BuilderExecutor<>(callback,
                new CoreVmBaseToUnitBuilder())
                .build(template, getModel());
    }

    @Override
    public void postDataCenterWithClusterSelectedItemChanged() {
        deactivateInstanceTypeManager(() -> getInstanceTypeManager().updateAll());

        updateDefaultHost();
        updateCustomPropertySheet();
        updateMinAllocatedMemory();
        updateNumOfSockets();
        if (getModel().getTemplateWithVersion().getSelectedItem() != null) {
            VmTemplate template = getModel().getTemplateWithVersion().getSelectedItem().getTemplateVersion();
            updateQuotaByCluster(template.getQuotaId(), template.getQuotaName());
        }
        updateCpuPinningVisibility();
        updateTemplate();
        updateOSValues();
        updateMemoryBalloon();
        updateCpuSharesAvailability();
        updateVirtioScsiAvailability();
        activateInstanceTypeManager();
    }

    private boolean profilesExist(List<VnicProfileView> profiles) {
        return !profiles.isEmpty() && profiles.get(0) != null;
    }

    @Override
    public void defaultHost_SelectedItemChanged() {
        updateCdImage();
    }

    @Override
    public void provisioning_SelectedItemChanged() {
        boolean provisioning = getModel().getProvisioning().getEntity();
        getModel().getProvisioningThin_IsSelected().setEntity(!provisioning);
        getModel().getProvisioningClone_IsSelected().setEntity(provisioning);
        getModel().getDisksAllocationModel().setIsVolumeFormatAvailable(true);
        getModel().getDisksAllocationModel().setIsVolumeFormatChangeable(provisioning);
        getModel().getDisksAllocationModel().setIsThinProvisioning(!provisioning);
        getModel().getDisksAllocationModel().setIsAliasChangable(true);

        initStorageDomains();
    }

    @Override
    public void oSType_SelectedItemChanged() {
        super.oSType_SelectedItemChanged();
        VmTemplate template = getModel().getTemplateWithVersion().getSelectedItem() == null
                ? null
                : getModel().getTemplateWithVersion().getSelectedItem().getTemplateVersion();
        Integer osType = getModel().getOSType().getSelectedItem();
        if ((template != null || !basedOnCustomInstanceType()) && osType != null) {
            Guid id = basedOnCustomInstanceType() ? template.getId() : getModel().getInstanceTypes().getSelectedItem().getId();
            updateVirtioScsiEnabledWithoutDetach(id, osType);
        }
    }

    @Override
    public void updateMinAllocatedMemory() {
        if (getModel().getMemSize().getEntity() == null) {
            return;
        }

        final Cluster cluster = getModel().getSelectedCluster();
        if (cluster == null) {
            return;
        }

        double overCommitFactor = 100.0 / cluster.getMaxVdsMemoryOverCommit();
        getModel().getMinAllocatedMemory()
                .setEntity((int) (getModel().getMemSize().getEntity() * overCommitFactor));
    }

    private void updateTemplate() {
        final DataCenterWithCluster dataCenterWithCluster =
                getModel().getDataCenterWithClustersList().getSelectedItem();
        StoragePool dataCenter = dataCenterWithCluster == null ? null : dataCenterWithCluster.getDataCenter();
        if (dataCenter == null) {
            return;
        }

        AsyncDataProvider.getInstance().getTemplateListByDataCenter(asyncQuery(
                templates -> postInitTemplate(AsyncDataProvider.getInstance().filterTemplatesByArchitecture(templates,
                        dataCenterWithCluster.getCluster().getArchitecture()))), dataCenter.getId());
    }

    protected void postInitTemplate(List<VmTemplate> templates) {
        initTemplateWithVersion(templates, null, false, getModel().getIsStateless().getEntity());
        updateIsDisksAvailable();
    }

    public void initCdImage() {
        DataCenterWithCluster dataCenterWithCluster = getModel().getDataCenterWithClustersList().getSelectedItem();
        if (dataCenterWithCluster == null || dataCenterWithCluster.getDataCenter() == null) {
            return;
        }

        updateUserCdImage(dataCenterWithCluster.getDataCenter().getId());
    }

    @Override
    public void updateIsDisksAvailable() {
        getModel().setIsDisksAvailable(getModel().getDisks() != null && !getModel().getDisks().isEmpty()
                && getModel().getProvisioning().getIsChangable());
    }

    @Override
    public void vmTypeChanged(VmType vmType) {
        // provisioning thin -> false
        // provisioning clone -> true
        if (getModel().getProvisioning().getIsAvailable()) {
            getModel().getProvisioning().setEntity(vmType == VmType.Server || vmType == VmType.HighPerformance);
        }

        super.vmTypeChanged(vmType);
    }

    @Override
    public InstanceTypeManager getInstanceTypeManager() {
        return instanceTypeManager;
    }

    @Override
    public void enableSinglePCI(boolean enabled) {
        super.enableSinglePCI(enabled);
        getModel().getIsSingleQxlEnabled().setEntity(enabled);
    }

    @Override
    protected void updateNumaEnabled() {
        super.updateNumaEnabled();
        updateNumaEnabledHelper();
    }

    @Override
    protected void initTemplateDisks(List<DiskImage> disks) {
        // can not mix template disks with instance images
        adjustInstanceImages(disks.isEmpty());
        super.initTemplateDisks(disks);
    }

    private void adjustInstanceImages(boolean instanceImagesEnabled) {
        getModel().getInstanceImages().setIsChangeable(instanceImagesEnabled);

        // if disabling, remove all except the ghost
        if (!instanceImagesEnabled) {
            Collection<InstanceImageLineModel> keepImages = new ArrayList<>();

            for (InstanceImageLineModel image : getModel().getInstanceImages().getItems()) {
                if (image.isGhost()) {
                    keepImages.add(image);
//                    only one ghost allowed
                    break;
                }
            }

            getModel().getInstanceImages().setItems(keepImages);
        }
    }

    @Override
    public void setCustomCompatibilityVersionChangeInProgress(boolean isCustomCompatibilityVersionChangeInProgress) {
        this.isCustomCompatibilityVersionChangeInProgress = isCustomCompatibilityVersionChangeInProgress;
    }

    @Override
    protected TemplateWithVersion computeTemplateWithVersionToSelect(
            List<TemplateWithVersion> newItems,
            Guid previousTemplateId, boolean useLatest, boolean addLatest) {
        if (previousTemplateId == null) {
            return computeNewTemplateWithVersionToSelect(newItems, addLatest);
        }
        TemplateWithVersion oldTemplateToSelect = Linq.firstOrNull(
                newItems,
                new Linq.TemplateWithVersionPredicateForNewVm(previousTemplateId, useLatest));
        return oldTemplateToSelect != null
                ? oldTemplateToSelect
                : computeNewTemplateWithVersionToSelect(newItems, addLatest);
    }
}
