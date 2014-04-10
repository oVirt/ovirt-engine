package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class NewVmModelBehavior extends VmModelBehaviorBase {

    private final ProfileBehavior networkBehavior = new EditProfileBehavior();

    @Override
    public void initialize(SystemTreeItemModel systemTreeSelectedItem)
    {
        super.initialize(systemTreeSelectedItem);
        getModel().getIsSoundcardEnabled().setIsChangable(true);
        getModel().getVmType().setIsChangable(true);

        AsyncDataProvider.getDataCenterByClusterServiceList(new AsyncQuery(getModel(),
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
                            AsyncDataProvider.getClusterListByService(
                                    new AsyncQuery(getModel(), new INewAsyncCallback() {

                                        @Override
                                        public void onSuccess(Object target, Object returnValue) {
                                            UnitVmModel model = (UnitVmModel) target;
                                            List<VDSGroup> clusterList = (List<VDSGroup>) returnValue;
                                            List<VDSGroup> filteredClusterList = AsyncDataProvider.filterClustersWithoutArchitecture(clusterList);
                                            model.setDataCentersAndClusters(model,
                                                    dataCenters,
                                                    filteredClusterList, null);
                                            initCdImage();
                                        }
                                    }, getModel().getHash()),
                                    true, false);
                        } else {
                            getModel().disableEditing(ConstantsManager.getInstance().getConstants().notAvailableWithNoUpDC());
                        }
                    }
                }, getModel().getHash()),
                true,
                false);

        initPriority(0);
        getModel().getVmInitModel().init(null);
    }

    @Override
    public void template_SelectedItemChanged()
    {
        VmTemplate template = getModel().getTemplate().getSelectedItem();

        if (template != null)
        {
            // Copy VM parameters from template.

            // If this a blank template, use the proper value for the default OS
            if (template.getId().equals(Guid.Empty))
            {
                Integer osId = AsyncDataProvider.getDefaultOs(getModel().getSelectedCluster().getArchitecture());
                if (osId != null) {
                    setSelectedOSById(osId.intValue());
                }
            } else {
                setSelectedOSById(template.getOsId());
            }
            getModel().getTotalCPUCores().setEntity(Integer.toString(template.getNumOfCpus()));
            getModel().getNumOfSockets().setSelectedItem(template.getNumOfSockets());
            getModel().getNumOfMonitors().setSelectedItem(template.getNumOfMonitors());
            getModel().getIsSingleQxlEnabled().setEntity(template.getSingleQxlPci());
            getModel().getMemSize().setEntity(template.getMemSizeMb());
            getModel().setBootSequence(template.getDefaultBootSequence());
            getModel().getIsHighlyAvailable().setEntity(template.isAutoStartup());

            updateHostPinning(template.getMigrationSupport());
            doChangeDefautlHost(template.getDedicatedVmForVds());

            getModel().getIsDeleteProtected().setEntity(template.isDeleteProtected());
            getModel().selectSsoMethod(template.getSsoMethod());
            getModel().setSelectedMigrationDowntime(template.getMigrationDowntime());

            getModel().getIsStateless().setEntity(template.isStateless());

            boolean hasCd = !StringHelper.isNullOrEmpty(template.getIsoPath());

            getModel().getCdImage().setIsChangable(hasCd);
            getModel().getCdAttached().setEntity(hasCd);
            if (hasCd) {
                getModel().getCdImage().setSelectedItem(template.getIsoPath());
            }

            updateConsoleDevice(template.getId());
            updateVirtioScsiEnabled(template.getId(), template.getOsId());
            updateTimeZone(template.getTimeZone());

            // Update display protocol selected item
            EntityModel<DisplayType> displayProtocol = null;
            boolean isFirst = true;
            for (EntityModel<DisplayType> item : getModel().getDisplayProtocol().getItems())
            {
                if (isFirst)
                {
                    displayProtocol = item;
                    isFirst = false;
                }
                DisplayType dt = item.getEntity();
                if (dt == template.getDefaultDisplayType())
                {
                    displayProtocol = item;
                    break;
                }
            }
            getModel().getDisplayProtocol().setSelectedItem(displayProtocol);
            getModel().getUsbPolicy().setSelectedItem(template.getUsbPolicy());
            getModel().getVncKeyboardLayout().setSelectedItem(template.getVncKeyboardLayout());
            getModel().getIsSmartcardEnabled().setEntity(template.isSmartcardEnabled());

            // By default, take kernel params from template.
            getModel().getKernel_path().setEntity(template.getKernelUrl());
            getModel().getKernel_parameters().setEntity(template.getKernelParams());
            getModel().getInitrd_path().setEntity(template.getInitrdUrl());

            if (!template.getId().equals(Guid.Empty))
            {
                getModel().getStorageDomain().setIsChangable(true);
                getModel().getProvisioning().setIsChangable(true);

                getModel().getVmType().setSelectedItem(template.getVmType());
                getModel().setIsBlankTemplate(false);
                getModel().getCopyPermissions().setIsAvailable(true);
                getModel().getAllowConsoleReconnect().setEntity(template.isAllowConsoleReconnect());
                initDisks();
                initSoundCard(template.getId());
            }
            else
            {
                getModel().getStorageDomain().setIsChangable(false);
                getModel().getProvisioning().setIsChangable(false);

                getModel().setIsBlankTemplate(true);
                getModel().setIsDisksAvailable(false);
                getModel().getCopyPermissions().setIsAvailable(false);
                getModel().setDisks(null);
            }

            initPriority(template.getPriority());
            initStorageDomains();

            // use min. allocated memory from the template, if specified
            if (template.getMinAllocatedMem() == 0) {
                updateMinAllocatedMemory();
            } else {
                getModel().getMinAllocatedMemory().setEntity(template.getMinAllocatedMem());
            }
            updateQuotaByCluster(template.getQuotaId(), template.getQuotaName());

            updateNetworkInterfacesByTemplate(template);
            getModel().getVmInitModel().init(template);
            getModel().getVmInitEnabled().setEntity(template.getVmInit() != null);
        }
    }

    private void setSelectedOSById (int osId) {
        for (Integer osIdList : getModel().getOSType().getItems()) {
            if (osIdList.intValue() == osId) {
                getModel().getOSType().setSelectedItem(osIdList);
                break;
            }
        }
    }

    @Override
    public void postDataCenterWithClusterSelectedItemChanged()
    {
        updateDefaultHost();
        updateCustomPropertySheet();
        updateMinAllocatedMemory();
        updateNumOfSockets();
        if (getModel().getTemplate().getSelectedItem() != null) {
            VmTemplate template = getModel().getTemplate().getSelectedItem();
            updateQuotaByCluster(template.getQuotaId(), template.getQuotaName());
        }
        updateCpuPinningVisibility();
        updateTemplate();
        updateNetworkInterfaces(networkBehavior, null);
        updateOSValues();
        updateMemoryBalloon();
        updateCpuSharesAvailability();
        updateVirtioScsiAvailability();
    }

    private void updateNetworkInterfacesByTemplate(VmTemplate template) {
        AsyncQuery query = new AsyncQuery(getModel(), new INewAsyncCallback() {

            @Override
            public void onSuccess(Object model, Object returnValue) {
                if (returnValue == null) {
                    return;
                }

                List<VmNetworkInterface> nics =
                        (List<VmNetworkInterface>) ((VdcQueryReturnValue) returnValue).getReturnValue();
                updateNetworkInterfaces(networkBehavior, nics);
            }
        });

        Frontend.getInstance().runQuery(VdcQueryType.GetTemplateInterfacesByTemplateId,
                new IdQueryParameters(template.getId()),
                query);
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
        VmTemplate template = getModel().getTemplate().getSelectedItem();
        Integer osType = getModel().getOSType().getSelectedItem();
        if (template != null && osType != null) {
            updateVirtioScsiEnabled(template.getId(), osType);
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

        double overCommitFactor = 100.0 / cluster.getmax_vds_memory_over_commit();
        getModel().getMinAllocatedMemory()
                .setEntity((int) (getModel().getMemSize().getEntity() * overCommitFactor));
    }

    private void updateTemplate()
    {
        final DataCenterWithCluster dataCenterWithCluster =
                (DataCenterWithCluster) getModel().getDataCenterWithClustersList().getSelectedItem();
        StoragePool dataCenter = dataCenterWithCluster == null ? null : dataCenterWithCluster.getDataCenter();
        if (dataCenter == null) {
            return;
        }

        // Filter according to system tree selection.
        if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Storage)
        {
            StorageDomain storage = (StorageDomain) getSystemTreeSelectedItem().getEntity();

            AsyncDataProvider.getTemplateListByDataCenter(new AsyncQuery(new Object[] { this, storage },
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object target1, Object returnValue1) {

                            Object[] array1 = (Object[]) target1;
                            NewVmModelBehavior behavior1 = (NewVmModelBehavior) array1[0];
                            StorageDomain storage1 = (StorageDomain) array1[1];
                            AsyncDataProvider.getTemplateListByStorage(new AsyncQuery(new Object[] { behavior1,
                                    returnValue1 },
                                    new INewAsyncCallback() {
                                        @Override
                                        public void onSuccess(Object target2, Object returnValue2) {

                                            Object[] array2 = (Object[]) target2;
                                            NewVmModelBehavior behavior2 = (NewVmModelBehavior) array2[0];
                                            List<VmTemplate> templatesByDataCenter = (List<VmTemplate>) array2[1];
                                            List<VmTemplate> templatesByStorage = (List<VmTemplate>) returnValue2;
                                            VmTemplate blankTemplate =
                                                    Linq.firstOrDefault(templatesByDataCenter,
                                                            new Linq.TemplatePredicate(Guid.Empty));
                                            if (blankTemplate != null)
                                            {
                                                templatesByStorage.add(0, blankTemplate);
                                            }

                                            List<VmTemplate> templateList = AsyncDataProvider.filterTemplatesByArchitecture(templatesByStorage,
                                                            dataCenterWithCluster.getCluster().getArchitecture());

                                            behavior2.postInitTemplate(templateList);

                                        }
                                    }),
                                    storage1.getId());

                        }
                    }, getModel().getHash()),
                    dataCenter.getId());
        }
        else
        {
            AsyncDataProvider.getTemplateListByDataCenter(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object target, Object returnValue) {

                            NewVmModelBehavior behavior = (NewVmModelBehavior) target;

                            List<VmTemplate> templates = (List<VmTemplate>) returnValue;

                            behavior.postInitTemplate(AsyncDataProvider.filterTemplatesByArchitecture(templates,
                                    dataCenterWithCluster.getCluster().getArchitecture()));

                        }
                    }, getModel().getHash()), dataCenter.getId());
        }
    }

    private void postInitTemplate(List<VmTemplate> templates)
    {
        List<VmTemplate> baseTemplates = filterNotBaseTemplates(templates);

        // If there was some template selected before, try select it again.
        VmTemplate prevBaseTemplate = getModel().getBaseTemplate().getSelectedItem();

        getModel().getBaseTemplate().setItems(baseTemplates);

        getModel().getBaseTemplate().setSelectedItem(Linq.firstOrDefault(baseTemplates,
                new Linq.TemplatePredicate(prevBaseTemplate != null ? prevBaseTemplate.getId() : Guid.Empty)));

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
        super.vmTypeChanged(vmType);

        // provisioning thin -> false
        // provisioning clone -> true
        if (getModel().getProvisioning().getIsAvailable()) {
            getModel().getProvisioning().setEntity(vmType == VmType.Server);
        }
    }

}
