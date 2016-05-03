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
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.builders.BuilderExecutor;
import org.ovirt.engine.ui.uicommonweb.builders.vm.CommentVmBaseToUnitBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.CommonVmBaseToUnitBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.NameAndDescriptionVmBaseToUnitBuilder;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;

public class TemplateVmModelBehavior extends VmModelBehaviorBase<UnitVmModel> {
    private VmTemplate template;

    public TemplateVmModelBehavior(VmTemplate template) {
        this.template = template;
    }

    @Override
    public void initialize(SystemTreeItemModel systemTreeSelectedItem) {
        super.initialize(systemTreeSelectedItem);
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
            AsyncDataProvider.getInstance().getDataCenterById(new AsyncQuery(null,
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object nothing, Object returnValue) {
                            final StoragePool dataCenter = (StoragePool) returnValue;
                            AsyncDataProvider.getInstance().getClusterListByService(
                                    new AsyncQuery(null, new INewAsyncCallback() {

                                        @Override
                                        public void onSuccess(Object nothing, Object returnValue) {
                                            ArrayList<Cluster> clusters = (ArrayList<Cluster>) returnValue;
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

                                            AsyncDataProvider.getInstance().isSoundcardEnabled(new AsyncQuery(null,
                                                    new INewAsyncCallback() {

                                                        @Override
                                                        public void onSuccess(Object nothing, Object returnValue) {
                                                            getModel().getIsSoundcardEnabled().setEntity((Boolean) returnValue);
                                                            initTemplate();
                                                            initCdImage();
                                                        }
                                                    }), template.getId());

                                            Frontend.getInstance().runQuery(VdcQueryType.IsBalloonEnabled, new IdQueryParameters(template.getId()), new AsyncQuery(
                                                    new INewAsyncCallback() {
                                                        @Override
                                                        public void onSuccess(Object model, Object returnValue) {
                                                            getModel().getMemoryBalloonDeviceEnabled().setEntity((Boolean) ((VdcQueryReturnValue) returnValue).getReturnValue());
                                                        }
                                                    }
                                            ));

                                            AsyncDataProvider.getInstance().isVirtioScsiEnabledForVm(new AsyncQuery(new INewAsyncCallback() {
                                                @Override
                                                public void onSuccess(Object model, Object returnValue) {
                                                    getModel().getIsVirtioScsiEnabled().setEntity((Boolean) returnValue);
                                                }
                                            }), template.getId());
                                        }
                                    }),
                                    true,
                                    false);
                        }
                    }),
                    template.getStoragePoolId());
        }

        AsyncDataProvider.getInstance().getWatchdogByVmId(new AsyncQuery(null, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object nothing, Object returnValue) {
                UnitVmModel model = TemplateVmModelBehavior.this.getModel();
                @SuppressWarnings("unchecked")
                Collection<VmWatchdog> watchdogs =
                        ((VdcQueryReturnValue) returnValue).getReturnValue();
                for (VmWatchdog watchdog : watchdogs) {
                    model.getWatchdogAction().setSelectedItem(watchdog.getAction());
                    model.getWatchdogModel().setSelectedItem(watchdog.getModel());
                }
            }
        }), template.getId());

        updateRngDevice(template.getId());

        getModel().getMigrationMode().setSelectedItem(template.getMigrationSupport());

        setupBaseTemplate(template.getBaseTemplateId());

    }

    protected void setupBaseTemplate(Guid baseTemplateId) {
        AsyncDataProvider.getInstance().getTemplateById(new AsyncQuery(null,
                        new INewAsyncCallback() {
                            @Override
                            public void onSuccess(Object nothing, Object returnValue) {

                                UnitVmModel model = getModel();
                                VmTemplate template = (VmTemplate) returnValue;

                                model.getBaseTemplate().setItems(Collections.singletonList(template));
                                model.getBaseTemplate().setSelectedItem(template);
                                model.getBaseTemplate().setIsChangeable(false);
                            }
                        }),
                baseTemplateId);
    }

    @Override
    public void postDataCenterWithClusterSelectedItemChanged() {
        updateGraphics(template.getId());
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
    }

    public void buildModel(VmBase vmBase, BuilderExecutor.BuilderExecutionFinished<VmBase, UnitVmModel> callback) {
        new BuilderExecutor<>(callback,
                new NameAndDescriptionVmBaseToUnitBuilder(),
                new CommentVmBaseToUnitBuilder(),
                new CommonVmBaseToUnitBuilder())
                .build(vmBase, getModel());
    }

    private void initTemplate() {
        // Update model state according to VM properties.
        buildModel(template, new BuilderExecutor.BuilderExecutionFinished<VmBase, UnitVmModel>() {
            @Override
            public void finished(VmBase source, UnitVmModel destination) {
                updateTimeZone(template.getTimeZone());

                // Storage domain and provisioning are not available for an existing VM.
                getModel().getStorageDomain().setIsChangeable(false);
                getModel().getProvisioning().setIsAvailable(false);

                // Select display protocol.
                DisplayType displayType = template.getDefaultDisplayType();
                if (getModel().getDisplayType().getItems().contains(displayType)) {
                    getModel().getDisplayType().setSelectedItem(displayType);
                }

                updateConsoleDevice(template.getId());

                toggleAutoSetVmHostname();
                getModel().getVmInitEnabled().setEntity(template.getVmInit() != null);
                getModel().getVmInitModel().init(template);
                getModel().getTemplateVersionName().setEntity(template.getTemplateVersionName());

                getModel().getBootMenuEnabled().setEntity(template.isBootMenuEnabled());

                getModel().getSpiceFileTransferEnabled().setEntity(template.isSpiceFileTransferEnabled());
                getModel().getSpiceCopyPasteEnabled().setEntity(template.isSpiceCopyPasteEnabled());

                initPriority(template.getPriority());
            }
        });
    }

    private void initCdImage() {
        updateSelectedCdImage(template);

        updateCdImage();
    }

    public VmTemplate getVmTemplate() {
        return template;
    }

    @Override
    public void enableSinglePCI(boolean enabled) {
        super.enableSinglePCI(enabled);
        if (enabled) {
            getModel().getIsSingleQxlEnabled().setEntity(template.getSingleQxlPci() && getModel().getIsQxlSupported());
        } else {
            getModel().getIsSingleQxlEnabled().setEntity(false);
        }
    }

    @Override public int getMaxNameLength() {
        return UnitVmModel.VM_TEMPLATE_AND_INSTANCE_TYPE_NAME_MAX_LIMIT;
    }

    private void toggleAutoSetVmHostname() {
        // always switch off auto setting of hostname for a template
        getModel().getVmInitModel().disableAutoSetHostname();
    }
}
