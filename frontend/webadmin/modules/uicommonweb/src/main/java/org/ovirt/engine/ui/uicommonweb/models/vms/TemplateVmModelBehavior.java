package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.uicommonweb.builders.BuilderExecutor;
import org.ovirt.engine.ui.uicommonweb.builders.vm.CommentVmBaseToUnitBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.CommonVmBaseToUnitBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.MultiQueuesVmBaseToUnitBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.NameAndDescriptionVmBaseToUnitBuilder;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;

public class TemplateVmModelBehavior extends VmModelBehaviorBase<UnitVmModel> {
    private VmTemplate template;

    public TemplateVmModelBehavior(VmTemplate template) {
        this.template = template;
    }

    @Override
    public void initialize() {
        super.initialize();
        getModel().getTemplateWithVersion().setIsChangeable(false);
        getModel().getBaseTemplate().setIsChangeable(false);
        getModel().getTemplateWithVersion().setIsChangeable(false);
        getModel().getProvisioning().setIsChangeable(false);
        getModel().getStorageDomain().setIsChangeable(false);
        getModel().getIsSoundcardEnabled().setIsChangeable(true);
        getModel().getVmType().setIsChangeable(true);
        getModel().getTemplateVersionName().setIsChangeable(!template.isBaseTemplate());
        getModel().getName().setIsChangeable(template.isBaseTemplate());

        if (template.getStoragePoolId() != null && !template.getStoragePoolId().equals(Guid.Empty)) {
            AsyncDataProvider.getInstance().getDataCenterById(new AsyncQuery<>(
                            dataCenter -> AsyncDataProvider.getInstance().getClusterListByService(
                                    new AsyncQuery<>(clusters -> {
                                        ArrayList<Cluster> clustersSupportingVirt = new ArrayList<>();
                                        // filter clusters supporting virt service only
                                        for (Cluster cluster : clusters) {
                                            if (cluster.supportsVirtService()) {
                                                clustersSupportingVirt.add(cluster);
                                            }
                                        }

                                        List<Cluster> filteredClusters =
                                                AsyncDataProvider.getInstance().filterByArchitecture(clustersSupportingVirt,
                                                        template.getClusterArch());

                                        getModel().setDataCentersAndClusters(getModel(),
                                                new ArrayList<>(Arrays.asList(new StoragePool[]{dataCenter})),
                                                filteredClusters,
                                                template.getClusterId());

                                        updateRngDevice(template.getId());

                                        AsyncDataProvider.getInstance().isSoundcardEnabled(new AsyncQuery<>(
                                                returnValue -> {
                                                    getModel().getIsSoundcardEnabled().setEntity(returnValue);
                                                    initTemplate();
                                                    initCdImage();
                                                }), template.getId());

                                        AsyncDataProvider.getInstance().isVirtioScsiEnabledForVm(new AsyncQuery<>(
                                                returnValue -> getModel().getIsVirtioScsiEnabled().setEntity(returnValue)), template.getId());
                                    }),
                                    true,
                                    false)),
                    template.getStoragePoolId());
        }

        AsyncDataProvider.getInstance().getWatchdogByVmId(new AsyncQuery<>((AsyncCallback<QueryReturnValue>) returnValue -> {
            UnitVmModel model = getModel();
            @SuppressWarnings("unchecked")
            Collection<VmWatchdog> watchdogs = returnValue.getReturnValue();
            for (VmWatchdog watchdog : watchdogs) {
                model.getWatchdogAction().setSelectedItem(watchdog.getAction());
                model.getWatchdogModel().setSelectedItem(watchdog.getModel());
            }
        }), template.getId());

        getModel().getMigrationMode().setSelectedItem(template.getMigrationSupport());

        setupBaseTemplate(template.getBaseTemplateId());

        getModel().getIsSealed().setIsAvailable(true);
        getModel().getIsSealed().setEntity(template.isSealed());
    }

    protected void setupBaseTemplate(Guid baseTemplateId) {
        AsyncDataProvider.getInstance().getTemplateById(new AsyncQuery<>(
                        template -> {
                            UnitVmModel model = getModel();

                            model.getBaseTemplate().setItems(Collections.singletonList(template));
                            model.getBaseTemplate().setSelectedItem(template);
                            model.getBaseTemplate().setIsChangeable(false);
                        }),
                baseTemplateId);
    }

    @Override
    public void postDataCenterWithClusterSelectedItemChanged() {
        updateDefaultHost();
        updateNumOfSockets();
        updateQuotaByCluster(template.getQuotaId(), template.getQuotaName());
        updateMemoryBalloon();
        updateCpuSharesAvailability();
        getModel().getCpuSharesAmount().setEntity(template.getCpuShares());
        updateCpuSharesSelection();
        updateVirtioScsiAvailability();
        updateMigrationForLocalSD();
        updateOSValues();
        if (getModel().getSelectedCluster() != null) {
            updateCpuProfile(getModel().getSelectedCluster().getId(), template.getCpuProfileId());
        }
        updateCustomPropertySheet();
        getModel().getCustomPropertySheet().deserialize(template.getCustomProperties());
        updateLeaseStorageDomains(template.getLeaseStorageDomainId());
    }

    @Override
    public void defaultHost_SelectedItemChanged() {
        updateCdImage();
    }

    @Override
    public void provisioning_SelectedItemChanged() {
    }

    @Override
    protected void changeDefaultHost() {
        super.changeDefaultHost();
        doChangeDefaultHost(template.getDedicatedVmForVdsList());

        if (isHostCpuValueStillBasedOnTemp()) {
            getModel().getHostCpu().setEntity(template.isUseHostCpuFlags());
        }
    }

    public void buildModel(VmBase vmBase, BuilderExecutor.BuilderExecutionFinished<VmBase, UnitVmModel> callback) {
        new BuilderExecutor<>(callback,
                new NameAndDescriptionVmBaseToUnitBuilder(),
                new CommentVmBaseToUnitBuilder(),
                new CommonVmBaseToUnitBuilder(),
                new MultiQueuesVmBaseToUnitBuilder())
                .build(vmBase, getModel());
    }

    private void initTemplate() {
        // Update model state according to VM properties.
        buildModel(template, (source, destination) -> {
            if (!isHostCpuValueStillBasedOnTemp()) {
                getModel().getHostCpu().setEntity(false);
            }

            updateTimeZone(template.getTimeZone());

            // Storage domain and provisioning are not available for an existing VM.
            getModel().getStorageDomain().setIsChangeable(false);
            getModel().getProvisioning().setIsAvailable(false);

            // Select display protocol.
            DisplayType displayType = template.getDefaultDisplayType();
            if (getModel().getDisplayType().getItems().contains(displayType)) {
                getModel().getDisplayType().setSelectedItem(displayType);
            }

            updateGraphics(template.getId());

            updateTpm(template.getId());
            updateConsoleDevice(template.getId());

            toggleAutoSetVmHostname();
            getModel().getVmInitEnabled().setEntity(template.getVmInit() != null);
            getModel().getVmInitModel().init(template);
            getModel().getTemplateVersionName().setEntity(template.getTemplateVersionName());

            getModel().getBootMenuEnabled().setEntity(template.isBootMenuEnabled());

            getModel().getSpiceFileTransferEnabled().setEntity(template.isSpiceFileTransferEnabled());
            getModel().getSpiceCopyPasteEnabled().setEntity(template.isSpiceCopyPasteEnabled());

            getModel().getMigrationMode().setSelectedItem(template.getMigrationSupport());

            initPriority(template.getPriority());
            getModel().updateResumeBehavior();
        });
    }

    private void initCdImage() {
        updateSelectedCdImage(template);

        updateCdImage();
    }

    public VmTemplate getVmTemplate() {
        return template;
    }

    @Override public int getMaxNameLength() {
        return UnitVmModel.VM_TEMPLATE_AND_INSTANCE_TYPE_NAME_MAX_LIMIT;
    }

    private void toggleAutoSetVmHostname() {
        // always switch off auto setting of hostname for a template
        getModel().getVmInitModel().disableAutoSetHostname();
    }
}
