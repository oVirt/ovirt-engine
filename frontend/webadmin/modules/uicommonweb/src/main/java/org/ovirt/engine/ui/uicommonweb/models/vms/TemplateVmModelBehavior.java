package org.ovirt.engine.ui.uicommonweb.models.vms;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;

public class TemplateVmModelBehavior extends VmModelBehaviorBase
{
    private final VmTemplate template;

    public TemplateVmModelBehavior(VmTemplate template)
    {
        this.template = template;
    }

    @Override
    public void initialize(SystemTreeItemModel systemTreeSelectedItem)
    {
        super.initialize(systemTreeSelectedItem);
        getModel().getTemplate().setIsChangable(false);
        getModel().getProvisioning().setIsChangable(false);
        getModel().getStorageDomain().setIsChangable(false);
        getModel().getIsSoundcardEnabled().setIsChangable(true);
        getModel().getVmType().setIsChangable(true);

        if (template.getStoragePoolId() != null && !template.getStoragePoolId().getValue().equals(NGuid.Empty))
        {
            AsyncDataProvider.getDataCenterById(new AsyncQuery(getModel(),
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object target, Object returnValue) {
                            final StoragePool dataCenter = (StoragePool) returnValue;
                            AsyncDataProvider.getClusterListByService(
                                    new AsyncQuery(getModel(), new INewAsyncCallback() {

                                        @Override
                                        public void onSuccess(Object target, Object returnValue) {
                                            UnitVmModel model = (UnitVmModel) target;

                                            ArrayList<VDSGroup> clusters = (ArrayList<VDSGroup>) returnValue;
                                            ArrayList<VDSGroup> filteredClusters = new ArrayList<VDSGroup>();
                                            // filter clusters supporting virt service only
                                            for (VDSGroup cluster : clusters) {
                                                if (cluster.supportsVirtService()) {
                                                    filteredClusters.add(cluster);
                                                }
                                            }
                                            model.setDataCentersAndClusters(model,
                                                    new ArrayList<StoragePool>(Arrays.asList(new StoragePool[] { dataCenter })),
                                                    filteredClusters,
                                                    template.getVdsGroupId().getValue());

                                            AsyncDataProvider.isSoundcardEnabled(new AsyncQuery(getModel(),
                                                    new INewAsyncCallback() {

                                                        @Override
                                                        public void onSuccess(Object model, Object returnValue) {
                                                            getModel().getIsSoundcardEnabled().setEntity(returnValue);
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
                    template.getStoragePoolId().getValue());
        }

        AsyncDataProvider.GetWatchdogByVmId(new AsyncQuery(this.getModel(), new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                UnitVmModel model = (UnitVmModel) target;
                @SuppressWarnings("unchecked")
                Collection<VmWatchdog> watchdogs =
                        (Collection<VmWatchdog>) ((VdcQueryReturnValue) returnValue).getReturnValue();
                for (VmWatchdog watchdog : watchdogs) {
                    model.getWatchdogAction().setSelectedItem(watchdog.getAction().name().toLowerCase());
                    model.getWatchdogModel().setSelectedItem(watchdog.getModel().name());
                }
            }
        }), template.getId());
        getModel().getMigrationMode().setSelectedItem(template.getMigrationSupport());
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
    public void updateMinAllocatedMemory()
    {
    }

    @Override
    protected void changeDefualtHost() {
        super.changeDefualtHost();

        doChangeDefautlHost(template.getDedicatedVmForVds());
    }

    private void initTemplate()
    {
        // Update model state according to VM properties.
        getModel().getName().setEntity(this.template.getName());
        getModel().getDescription().setEntity(this.template.getDescription());
        getModel().getMinAllocatedMemory().setEntity(this.template.getMinAllocatedMem());
        getModel().getMinAllocatedMemory().setIsChangable(false);
        getModel().getMemSize().setEntity(this.template.getMemSizeMb());
        getModel().getOSType().setSelectedItem(this.template.getOsId());
        getModel().getDomain().setSelectedItem(this.template.getDomain());
        getModel().getUsbPolicy().setSelectedItem(this.template.getUsbPolicy());
        getModel().getNumOfMonitors().setSelectedItem(this.template.getNumOfMonitors());
        getModel().getAllowConsoleReconnect().setEntity(this.template.isAllowConsoleReconnect());
        getModel().setBootSequence(this.template.getDefaultBootSequence());
        getModel().getIsHighlyAvailable().setEntity(this.template.isAutoStartup());
        getModel().getTotalCPUCores().setEntity(Integer.toString(this.template.getNumOfCpus()));
        getModel().getNumOfSockets().setSelectedItem(this.template.getNumOfSockets());
        getModel().getIsStateless().setEntity(this.template.isStateless());
        getModel().getIsRunAndPause().setEntity(this.template.isRunAndPause());
        getModel().getIsDeleteProtected().setEntity(this.template.isDeleteProtected());
        getModel().getIsSmartcardEnabled().setEntity(this.template.isSmartcardEnabled());
        getModel().getVncKeyboardLayout().setSelectedItem(this.template.getVncKeyboardLayout());

        getModel().getKernel_parameters().setEntity(this.template.getKernelParams());
        getModel().getKernel_path().setEntity(this.template.getKernelUrl());
        getModel().getInitrd_path().setEntity(this.template.getInitrdUrl());

        updateTimeZone(template.getTimeZone());

        // Update domain list
        updateDomain();

        // Storage domain and provisioning are not available for an existing VM.
        getModel().getStorageDomain().setIsChangable(false);
        getModel().getProvisioning().setIsAvailable(false);

        // Select display protocol.
        for (Object item : getModel().getDisplayProtocol().getItems())
        {
            EntityModel model = (EntityModel) item;
            DisplayType displayType = (DisplayType) model.getEntity();

            if (displayType == this.template.getDefaultDisplayType())
            {
                getModel().getDisplayProtocol().setSelectedItem(item);
                break;
            }
        }

        initPriority(this.template.getPriority());
    }

    private void initCdImage()
    {
        updateSelectedCdImage(template);

        updateCdImage();
    }

}
