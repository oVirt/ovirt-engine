package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.builders.BuilderExecutor;
import org.ovirt.engine.ui.uicommonweb.builders.vm.CommentVmBaseToUnitBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.CommonVmBaseToUnitBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.vm.NameAndDescriptionVmBaseToUnitBuilder;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;

public class TemplateVmModelBehavior extends VmModelBehaviorBase
{
    private VmTemplate template;

    public TemplateVmModelBehavior(VmTemplate template)
    {
        this.template = template;
    }

    @Override
    public void initialize(SystemTreeItemModel systemTreeSelectedItem)
    {
        super.initialize(systemTreeSelectedItem);
        getModel().getTemplate().setIsChangable(false);
        getModel().getBaseTemplate().setIsChangable(false);
        getModel().getProvisioning().setIsChangable(false);
        getModel().getStorageDomain().setIsChangable(false);
        getModel().getIsSoundcardEnabled().setIsChangable(true);
        getModel().getVmType().setIsChangable(true);
        getModel().getTemplateVersionName().setIsChangable(!template.isBaseTemplate());
        getModel().getName().setIsChangable(template.isBaseTemplate());

        if (template.getStoragePoolId() != null && !template.getStoragePoolId().equals(Guid.Empty))
        {
            AsyncDataProvider.getInstance().getDataCenterById(new AsyncQuery(getModel(),
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object target, Object returnValue) {
                            final StoragePool dataCenter = (StoragePool) returnValue;
                            AsyncDataProvider.getInstance().getClusterListByService(
                                    new AsyncQuery(getModel(), new INewAsyncCallback() {

                                        @Override
                                        public void onSuccess(Object target, Object returnValue) {
                                            UnitVmModel model = (UnitVmModel) target;

                                            ArrayList<VDSGroup> clusters = (ArrayList<VDSGroup>) returnValue;
                                            ArrayList<VDSGroup> clustersSupportingVirt = new ArrayList<VDSGroup>();
                                            // filter clusters supporting virt service only
                                            for (VDSGroup cluster : clusters) {
                                                if (cluster.supportsVirtService()) {
                                                    clustersSupportingVirt.add(cluster);
                                                }
                                            }

                                            List<VDSGroup> filteredClusters =
                                                    AsyncDataProvider.getInstance().filterByArchitecture(clustersSupportingVirt,
                                                            template.getClusterArch());

                                            model.setDataCentersAndClusters(model,
                                                    new ArrayList<StoragePool>(Arrays.asList(new StoragePool[] { dataCenter })),
                                                    filteredClusters,
                                                    template.getVdsGroupId());

                                            AsyncDataProvider.getInstance().isSoundcardEnabled(new AsyncQuery(getModel(),
                                                    new INewAsyncCallback() {

                                                        @Override
                                                        public void onSuccess(Object model, Object returnValue) {
                                                            getModel().getIsSoundcardEnabled().setEntity((Boolean) returnValue);
                                                            initTemplate();
                                                            initCdImage();
                                                        }
                                                    }), template.getId());
                                        }
                                    }, getModel().getHash()),
                                    true,
                                    false);
                        }
                    },
                    getModel().getHash()),
                    template.getStoragePoolId());
        }

        AsyncDataProvider.getInstance().getWatchdogByVmId(new AsyncQuery(this.getModel(), new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                UnitVmModel model = (UnitVmModel) target;
                @SuppressWarnings("unchecked")
                Collection<VmWatchdog> watchdogs =
                        ((VdcQueryReturnValue) returnValue).getReturnValue();
                for (VmWatchdog watchdog : watchdogs) {
                    model.getWatchdogAction().setSelectedItem(watchdog.getAction().name().toLowerCase());
                    model.getWatchdogModel().setSelectedItem(watchdog.getModel().name());
                }
            }
        }), template.getId());

        updateRngDevice(template.getId());

        getModel().getMigrationMode().setSelectedItem(template.getMigrationSupport());

        setupBaseTemplate(template.getBaseTemplateId());
    }

    @Override
    protected void baseTemplateSelectedItemChanged() {
    }

    @Override
    public void template_SelectedItemChanged()
    {
        // Leave this method empty. Not relevant for template.
    }

    @Override
    public void postDataCenterWithClusterSelectedItemChanged()
    {
        updateDefaultHost();
        updateNumOfSockets();
        updateQuotaByCluster(template.getQuotaId(), template.getQuotaName());
        updateMemoryBalloon();
        updateCpuSharesAvailability();
        updateVirtioScsiAvailability();
        updateMigrationForLocalSD();
        updateOSValues();
        if (getModel().getSelectedCluster() != null) {
            updateCpuProfile(getModel().getSelectedCluster().getId(),
                    getClusterCompatibilityVersion(), template.getCpuProfileId());
        }
    }

    @Override
    public void defaultHost_SelectedItemChanged()
    {
        updateCdImage();
    }

    @Override
    public void provisioning_SelectedItemChanged()
    {
    }

    @Override
    public void oSType_SelectedItemChanged()
    {
    }

    @Override
    public void updateMinAllocatedMemory()
    {
    }

    @Override
    protected void changeDefualtHost() {
        super.changeDefualtHost();

        doChangeDefautlHost(template.getDedicatedVmForVds());
    }

    public void buildModel(VmBase vm) {
        BuilderExecutor.build(vm, getModel(),
                              new NameAndDescriptionVmBaseToUnitBuilder(),
                              new CommentVmBaseToUnitBuilder(),
                              new CommonVmBaseToUnitBuilder());
    }

    private void initTemplate()
    {
        // Update model state according to VM properties.
        buildModel(template);

        getModel().getMinAllocatedMemory().setIsChangable(false);

        updateTimeZone(template.getTimeZone());

        // Storage domain and provisioning are not available for an existing VM.
        getModel().getStorageDomain().setIsChangable(false);
        getModel().getProvisioning().setIsAvailable(false);

        // Select display protocol.
        for (EntityModel<DisplayType> model : getModel().getDisplayProtocol().getItems())
        {
            DisplayType displayType = model.getEntity();

            if (displayType == template.getDefaultDisplayType())
            {
                getModel().getDisplayProtocol().setSelectedItem(model);
                break;
            }
        }

        updateConsoleDevice(template.getId());
        getModel().getVmInitEnabled().setEntity(template.getVmInit() != null);
        getModel().getVmInitModel().init(template);
        getModel().getTemplateVersionName().setEntity(template.getTemplateVersionName());

        getModel().getBootMenuEnabled().setEntity(template.isBootMenuEnabled());

        getModel().getSpiceFileTransferEnabled().setEntity(template.isSpiceFileTransferEnabled());
        getModel().getSpiceCopyPasteEnabled().setEntity(template.isSpiceCopyPasteEnabled());

        initPriority(template.getPriority());
    }

    private void initCdImage()
    {
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
}
