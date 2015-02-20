package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;
import org.ovirt.engine.core.common.businessentities.InstanceType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.builders.BuilderExecutor;
import org.ovirt.engine.ui.uicommonweb.builders.vm.CoreVmBaseToUnitBuilder;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateWithVersion;
import org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes.InstanceTypeManager;
import org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes.NewVmInstanceTypeManager;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NewVmModelBehavior extends VmModelBehaviorBase<UnitVmModel> {

    private InstanceTypeManager instanceTypeManager;

    @Override
    public void initialize(SystemTreeItemModel systemTreeSelectedItem)
    {
        super.initialize(systemTreeSelectedItem);

        getModel().getIsSoundcardEnabled().setIsChangable(true);
        getModel().getVmType().setIsChangable(true);
        getModel().getVmId().setIsAvailable(true);

        AsyncDataProvider.getInstance().getDataCenterByClusterServiceList(new AsyncQuery(getModel(),
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        final ArrayList<StoragePool> dataCenters = new ArrayList<StoragePool>();
                        for (StoragePool a : (ArrayList<StoragePool>) returnValue) {
                            if (a.getStatus() == StoragePoolStatus.Up) {
                                dataCenters.add(a);
                            }
                        }

                        if (!dataCenters.isEmpty()) {
                            AsyncDataProvider.getInstance().getClusterListByService(
                                    new AsyncQuery(getModel(), new INewAsyncCallback() {

                                        @Override
                                        public void onSuccess(Object target, Object returnValue) {
                                            UnitVmModel model = (UnitVmModel) target;
                                            List<VDSGroup> clusterList = (List<VDSGroup>) returnValue;
                                            List<VDSGroup> filteredClusterList = AsyncDataProvider.getInstance().filterClustersWithoutArchitecture(clusterList);
                                            model.setDataCentersAndClusters(model,
                                                    dataCenters,
                                                    filteredClusterList, null);
                                            initCdImage();

                                        }
                                    }),
                                    true, false);
                        } else {
                            getModel().disableEditing(ConstantsManager.getInstance().getConstants().notAvailableWithNoUpDC());
                        }
                    }
                }),
                true,
                false);

        initPriority(0);
        getModel().getVmInitModel().init(null);

        instanceTypeManager = new NewVmInstanceTypeManager(getModel());
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
        buildModel(template, new BuilderExecutor.BuilderExecutionFinished<VmBase, UnitVmModel>() {
            @Override
            public void finished(VmBase source, UnitVmModel destination) {
                setSelectedOSType(template, getModel().getSelectedCluster().getArchitecture());
                doChangeDefautlHost(template.getDedicatedVmForVds());

                getModel().getIsStateless().setEntity(template.isStateless());

                boolean hasCd = !StringHelper.isNullOrEmpty(template.getIsoPath());

                getModel().getCdImage().setIsChangable(hasCd);
                getModel().getCdAttached().setEntity(hasCd);
                if (hasCd) {
                    getModel().getCdImage().setSelectedItem(template.getIsoPath());
                }

                updateTimeZone(template.getTimeZone());

                if (!template.getId().equals(Guid.Empty))
                {
                    getModel().getStorageDomain().setIsChangable(true);
                    getModel().getProvisioning().setIsChangable(true);

                    getModel().getVmType().setSelectedItem(template.getVmType());
                    getModel().getCopyPermissions().setIsAvailable(true);
                    getModel().getAllowConsoleReconnect().setEntity(template.isAllowConsoleReconnect());
                    initDisks();
                    updateRngDevice(template.getId());
                }
                else
                {
                    getModel().getStorageDomain().setIsChangable(false);
                    getModel().getProvisioning().setIsChangable(false);

                    getModel().setIsDisksAvailable(false);
                    getModel().getCopyPermissions().setIsAvailable(false);
                    getModel().setDisks(null);
                }

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
                    updateCpuProfile(getModel().getSelectedCluster().getId(),
                            getClusterCompatibilityVersion(), template.getCpuProfileId());
                }
            }
        });
    }

    @Override
    protected void buildModel(VmBase template, BuilderExecutor.BuilderExecutionFinished callback) {
        new BuilderExecutor<>(callback,
                new CoreVmBaseToUnitBuilder())
                .build(template, getModel());
    }

    @Override
    public void postDataCenterWithClusterSelectedItemChanged()
    {
        deactivateInstanceTypeManager(new InstanceTypeManager.ActivatedListener() {
            @Override
            public void activated() {
                getInstanceTypeManager().updateAll();
            }
        });

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
    public void defaultHost_SelectedItemChanged()
    {
        updateCdImage();
    }

    @Override
    public void provisioning_SelectedItemChanged()
    {
        boolean provisioning = getModel().getProvisioning().getEntity();
        getModel().getProvisioningThin_IsSelected().setEntity(!provisioning);
        getModel().getProvisioningClone_IsSelected().setEntity(provisioning);
        getModel().getDisksAllocationModel().setIsVolumeFormatAvailable(true);
        getModel().getDisksAllocationModel().setIsVolumeFormatChangable(provisioning);
        getModel().getDisksAllocationModel().setIsAliasChangable(true);

        initStorageDomains();
    }

    @Override
    public void oSType_SelectedItemChanged() {
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
    public void updateMinAllocatedMemory()
    {
        DataCenterWithCluster dataCenterWithCluster = getModel().getDataCenterWithClustersList().getSelectedItem();
        VDSGroup cluster = dataCenterWithCluster == null ? null : dataCenterWithCluster.getCluster();
        if (cluster == null) {
            return;
        }

        double overCommitFactor = 100.0 / cluster.getMaxVdsMemoryOverCommit();
        getModel().getMinAllocatedMemory()
                .setEntity((int) (getModel().getMemSize().getEntity() * overCommitFactor));
    }

    private void updateTemplate()
    {
        final DataCenterWithCluster dataCenterWithCluster =
                getModel().getDataCenterWithClustersList().getSelectedItem();
        StoragePool dataCenter = dataCenterWithCluster == null ? null : dataCenterWithCluster.getDataCenter();
        if (dataCenter == null) {
            return;
        }

        // Filter according to system tree selection.
        if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Storage)
        {
            final StorageDomain storage = (StorageDomain) getSystemTreeSelectedItem().getEntity();

            AsyncDataProvider.getInstance().getTemplateListByDataCenter(new AsyncQuery(null,
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object nothing, Object returnValue1) {

                            final List<VmTemplate> templatesByDataCenter = (List<VmTemplate>) returnValue1;

                            AsyncDataProvider.getInstance().getTemplateListByStorage(new AsyncQuery(null,
                                    new INewAsyncCallback() {
                                        @Override
                                        public void onSuccess(Object nothing, Object returnValue2) {

                                            List<VmTemplate> templatesByStorage = (List<VmTemplate>) returnValue2;
                                            VmTemplate blankTemplate =
                                                    Linq.firstOrDefault(templatesByDataCenter,
                                                            new Linq.TemplatePredicate(Guid.Empty));
                                            if (blankTemplate != null)
                                            {
                                                templatesByStorage.add(0, blankTemplate);
                                            }

                                            List<VmTemplate> templateList = AsyncDataProvider.getInstance().filterTemplatesByArchitecture(templatesByStorage,
                                                            dataCenterWithCluster.getCluster().getArchitecture());

                                            NewVmModelBehavior.this.postInitTemplate(templateList);

                                        }
                                    }),
                                    storage.getId());

                        }
                    }),
                    dataCenter.getId());
        }
        else
        {
            AsyncDataProvider.getInstance().getTemplateListByDataCenter(new AsyncQuery(null,
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object nothing, Object returnValue) {

                            List<VmTemplate> templates = (List<VmTemplate>) returnValue;

                            NewVmModelBehavior.this.postInitTemplate(AsyncDataProvider.getInstance().filterTemplatesByArchitecture(templates,
                                    dataCenterWithCluster.getCluster().getArchitecture()));

                        }
                    }), dataCenter.getId());
        }
    }

    private void postInitTemplate(List<VmTemplate> templates) {
        initTemplateWithVersion(templates);
        updateIsDisksAvailable();
    }

    public void initCdImage()
    {
        DataCenterWithCluster dataCenterWithCluster = getModel().getDataCenterWithClustersList().getSelectedItem();
        if (dataCenterWithCluster == null || dataCenterWithCluster.getDataCenter() == null) {
            return;
        }

        updateUserCdImage(dataCenterWithCluster.getDataCenter().getId());
    }

    @Override
    public void updateIsDisksAvailable()
    {
        getModel().setIsDisksAvailable(getModel().getDisks() != null && !getModel().getDisks().isEmpty()
                && getModel().getProvisioning().getIsChangable());
    }

    @Override
    public void vmTypeChanged(VmType vmType) {
        // provisioning thin -> false
        // provisioning clone -> true
        if (getModel().getProvisioning().getIsAvailable()) {
            getModel().getProvisioning().setEntity(vmType == VmType.Server);
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
}
