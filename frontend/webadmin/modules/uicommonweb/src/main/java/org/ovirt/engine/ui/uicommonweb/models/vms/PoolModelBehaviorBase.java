package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.InstanceType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.UsbPolicy;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.RepoImage;
import org.ovirt.engine.core.common.utils.VmCommonUtils;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.uicommonweb.builders.BuilderExecutor;
import org.ovirt.engine.ui.uicommonweb.builders.vm.CoreVmBaseToUnitBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.MultiQueuesVmBaseToUnitBuilder;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.TabName;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolModel;
import org.ovirt.engine.ui.uicommonweb.validation.HostWithProtocolAndPortAddressValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicommonweb.validation.PoolNameValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;

public abstract class PoolModelBehaviorBase extends VmModelBehaviorBase<PoolModel> {

    private final Event<EventArgs> poolModelBehaviorInitializedEvent = new Event<>("PoolModelBehaviorInitializedEvent", //$NON-NLS-1$
            NewPoolModelBehavior.class);

    public Event<EventArgs> getPoolModelBehaviorInitializedEvent() {
        return poolModelBehaviorInitializedEvent;
    }

    @Override
    public void initialize() {
        super.initialize();

        getModel().getIsSealed().setIsAvailable(true);
        getModel().getIsSoundcardEnabled().setIsChangeable(true);

        getModel().getDisksAllocationModel().setIsVolumeFormatAvailable(true);
        getModel().getDisksAllocationModel().setIsThinProvisioning(true);
        getModel().getDisksAllocationModel().setIsAliasChangeable(true);

        getModel().getProvisioning().setIsAvailable(false);
        getModel().getProvisioning().setEntity(false);

        AsyncDataProvider.getInstance().getDataCenterByClusterServiceList(asyncQuery(returnValue -> {

            final List<StoragePool> dataCenters = new ArrayList<>();
            for (StoragePool a : returnValue) {
                if (a.getStatus() == StoragePoolStatus.Up) {
                    dataCenters.add(a);
                }
            }

            if (!dataCenters.isEmpty()) {
                postDataCentersLoaded(dataCenters);
            } else {
                getModel().disableEditing(ConstantsManager.getInstance().getConstants().notAvailableWithNoUpDC());
            }


        }), true, false);

        getModel().getSpiceProxyEnabled().setEntity(false);
        getModel().getSpiceProxy().setIsChangeable(false);

        getModel().getSpiceProxyEnabled().getEntityChangedEvent().addListener((ev, sender, args) -> getModel().getSpiceProxy().setIsChangeable(getModel().getSpiceProxyEnabled().getEntity()));
    }

    protected void postDataCentersLoaded(final List<StoragePool> dataCenters) {
        AsyncDataProvider.getInstance().getClusterListByService(
                new AsyncQuery<>(new AsyncCallback<List<Cluster>>() {

                    @Override
                    public void onSuccess(List<Cluster> clusters) {
                        List<Cluster> filteredClusters = filterClusters(clusters);
                        getModel().setDataCentersAndClusters(getModel(),
                                dataCenters,
                                filteredClusters, null);
                        getPoolModelBehaviorInitializedEvent().raise(this, EventArgs.EMPTY);
                    }
                }),
                true, false);
    }

    protected abstract List<Cluster> filterClusters(List<Cluster> clusters);

    @Override
    public void templateWithVersion_SelectedItemChanged() {
        super.templateWithVersion_SelectedItemChanged();
        updateCdImage();
    }

    @Override
    protected void setImagesToModel(UnitVmModel model, List<RepoImage> images) {
        if (getModel().getTemplateWithVersion().getSelectedItem() == null) {
            return;
        }
        VmTemplate template = getModel().getTemplateWithVersion().getSelectedItem().getTemplateVersion();
        model.getCdImage().setItems(images);
        if (!StringHelper.isNullOrEmpty(template.getIsoPath())) {
            RepoImage oldCdImage = images.stream().filter(i -> i.getRepoImageId().equals(template.getIsoPath())).findFirst().orElse(null);
            model.getCdImage().setSelectedItem(oldCdImage);
        }
    }

    protected void setupWindowModelFrom(final VmBase vmBase) {
        if (vmBase != null) {
            updateQuotaByCluster(vmBase.getQuotaId(), vmBase.getQuotaName());

            // Copy VM parameters from template.
            buildModel(vmBase, (source, destination) -> {
                setSelectedOSType(vmBase, getModel().getSelectedCluster().getArchitecture());
                getModel().getVmType().setSelectedItem(vmBase.getVmType());
                getModel().getIsUsbEnabled().setEntity(vmBase.getUsbPolicy() != UsbPolicy.DISABLED);
                getModel().getIsRunAndPause().setEntity(false);

                boolean hasCd = !StringHelper.isNullOrEmpty(vmBase.getIsoPath());

                getModel().getCdImage().setIsChangeable(hasCd);
                getModel().getCdAttached().setEntity(hasCd);

                updateTimeZone(vmBase.getTimeZone());

                if (!vmBase.getId().equals(Guid.Empty)) {
                    getModel().getStorageDomain().setIsChangeable(true);

                    initDisks();
                } else {
                    getModel().getStorageDomain().setIsChangeable(false);

                    getModel().setIsDisksAvailable(false);
                    getModel().setDisks(null);
                }

                getModel().getProvisioning().setEntity(false);

                initStorageDomains();

                InstanceType selectedInstanceType = getModel().getInstanceTypes().getSelectedItem();
                int instanceTypeMinAllocatedMemory = selectedInstanceType != null ? selectedInstanceType.getMinAllocatedMem() : 0;

                // do not update if specified on template or instance type
                if (vmBase.getMinAllocatedMem() == 0 && instanceTypeMinAllocatedMemory == 0) {
                    updateMinAllocatedMemory();
                }

                getModel().getAllowConsoleReconnect().setEntity(vmBase.isAllowConsoleReconnect());

                toggleAutoSetVmHostname();
                getModel().getVmInitModel().init(vmBase);
                getModel().getVmInitEnabled().setEntity(vmBase.getVmInit() != null);

                if (getModel().getSelectedCluster() != null) {
                    updateCpuProfile(getModel().getSelectedCluster().getId(), vmBase.getCpuProfileId());
                }

                getModel().getCpuSharesAmount().setEntity(vmBase.getCpuShares());
                updateCpuSharesSelection();

                // A workaround for setting the current saved CustomCompatibilityVersion value after
                // it was reset by getTemplateWithVersion event
                getModel().getCustomCompatibilityVersion().setSelectedItem(getSavedCurrentCustomCompatibilityVersion());
                setCustomCompatibilityVersionChangeInProgress(false);

                getModel().updateResumeBehavior();

                getModel().getHostCpu().setEntity(isHostCpuValueStillBasedOnTemp() ? vmBase.isUseHostCpuFlags() : false);
                getModel().getBiosType().setSelectedItem(vmBase.getBiosType());
            });
        }
    }

    @Override
    protected void buildModel(VmBase vmBase, BuilderExecutor.BuilderExecutionFinished<VmBase, UnitVmModel> callback) {
        new BuilderExecutor<>(callback, new CoreVmBaseToUnitBuilder(), new MultiQueuesVmBaseToUnitBuilder())
                .build(vmBase, getModel());
    }

    @Override
    public void postDataCenterWithClusterSelectedItemChanged() {
        updateDefaultHost();
        updateCustomPropertySheet();
        updateMinAllocatedMemory();
        updateNumOfSockets();
        updateOSValues();

        if (getModel().getTemplateWithVersion().getSelectedItem() != null) {
            VmTemplate template = getModel().getTemplateWithVersion().getSelectedItem().getTemplateVersion();
            updateQuotaByCluster(template.getQuotaId(), template.getQuotaName());
        }
        updateMemoryBalloon();
        updateCpuSharesAvailability();
        updateVirtioScsiAvailability();
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

        Cluster cluster = getModel().getSelectedCluster();
        if (cluster == null) {
            return;
        }

        int minMemory = VmCommonUtils.calcMinMemory(
                getModel().getMemSize().getEntity(), cluster.getMaxVdsMemoryOverCommit());
        getModel().getMinAllocatedMemory().setEntity(minMemory);
    }

    @Override
    public void updateIsDisksAvailable() {
        getModel().setIsDisksAvailable(getModel().getDisks() != null
                && getModel().getProvisioning().getIsChangable());
    }

    @Override
    public boolean validate() {
        boolean isNew = getModel().getIsNew();
        int maxAllowedVms = getMaxVmsInPool();
        int assignedVms = getModel().getAssignedVms().asConvertible().integer();
        int futureNumOfDesktops = (isNew ? 0 : assignedVms) + getModel().getNumOfDesktops().getEntity();

        getModel().getNumOfDesktops().validateEntity(

                new IValidation[] {
                        new NotEmptyValidation(),
                        new LengthValidation(4),
                        new IntegerValidation(isNew ? 1 : 0, isNew ? maxAllowedVms : maxAllowedVms - assignedVms)
                });

        getModel().getPrestartedVms().validateEntity(
                new IValidation[] {
                        new NotEmptyValidation(),
                        new IntegerValidation(0, futureNumOfDesktops)
                });

        getModel().getMaxAssignedVmsPerUser().validateEntity(
                new IValidation[] {
                        new NotEmptyValidation(),
                        new IntegerValidation(1, futureNumOfDesktops)
                });

        getModel().setValidTab(TabName.GENERAL_TAB, getModel().isValidTab(TabName.GENERAL_TAB)
                && getModel().getName().getIsValid()
                && getModel().getNumOfDesktops().getIsValid()
                && getModel().getPrestartedVms().getIsValid()
                && getModel().getMaxAssignedVmsPerUser().getIsValid());

        getModel().setValidTab(TabName.POOL_TAB, true);

        if (getModel().getSpiceProxyEnabled().getEntity()) {
            getModel().getSpiceProxy().validateEntity(new IValidation[]{ new HostWithProtocolAndPortAddressValidation()});
        } else {
            getModel().getSpiceProxy().setIsValid(true);
        }

        return super.validate()
                && getModel().getName().getIsValid()
                && getModel().getNumOfDesktops().getIsValid()
                && getModel().getPrestartedVms().getIsValid()
                && getModel().getMaxAssignedVmsPerUser().getIsValid()
                && getModel().getSpiceProxy().getIsValid();
    }

    @Override
    public IValidation getNameAllowedCharactersIValidation() {
        return new PoolNameValidation();
    }

    private void toggleAutoSetVmHostname() {
        // always switch off auto setting of hostname for pool VMs
        getModel().getVmInitModel().disableAutoSetHostname();
    }

    @Override
    public void setCustomCompatibilityVersionChangeInProgress(boolean isCustomCompatibilityVersionChangeInProgress) {
        this.isCustomCompatibilityVersionChangeInProgress = isCustomCompatibilityVersionChangeInProgress;
    }
}
