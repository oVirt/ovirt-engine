package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VmWatchdog;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.StringHelper;
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

        if (template.getStoragePoolId() != null && !template.getStoragePoolId().getValue().equals(NGuid.Empty))
        {
            AsyncDataProvider.getDataCenterById(new AsyncQuery(getModel(),
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object target, Object returnValue) {

                            UnitVmModel model = (UnitVmModel) target;
                            StoragePool dataCenter = (StoragePool) returnValue;
                            model.setDataCenter(model,
                                    new ArrayList<StoragePool>(Arrays.asList(new StoragePool[] { dataCenter })));
                            model.getDataCenter().setIsChangable(false);

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
                for(VmWatchdog watchdog: watchdogs) {
                    model.getWatchdogAction().setSelectedItem(watchdog.getAction().name().toLowerCase());
                    model.getWatchdogModel().setSelectedItem(watchdog.getModel().name());
                }
            }
        }), template.getId());
        getModel().getMigrationMode().setSelectedItem(template.getMigrationSupport());
    }

    @Override
    public void dataCenter_SelectedItemChanged()
    {
        StoragePool dataCenter = (StoragePool) getModel().getDataCenter().getSelectedItem();

        getModel().setIsHostAvailable(dataCenter.getstorage_pool_type() != StorageType.LOCALFS);

        AsyncDataProvider.getClusterByServiceList(new AsyncQuery(new Object[] { this, getModel() },
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        Object[] array = (Object[]) target;
                        TemplateVmModelBehavior behavior = (TemplateVmModelBehavior) array[0];
                        UnitVmModel model = (UnitVmModel) array[1];
                        VmTemplate vmTemplate = ((TemplateVmModelBehavior) (array[0])).template;
                        ArrayList<VDSGroup> clusters = (ArrayList<VDSGroup>) returnValue;
                        ArrayList<VDSGroup> filteredClusters = new ArrayList<VDSGroup>();
                        // filter clusters supporting virt service only
                        for (VDSGroup cluster : clusters) {
                            if (cluster.supportsVirtService()) {
                                filteredClusters.add(cluster);
                            }
                        }

                        model.setClusters(model, filteredClusters, vmTemplate.getVdsGroupId().getValue());
                        behavior.initTemplate();
                        behavior.initCdImage();

                    }
                }, getModel().getHash()), dataCenter.getId(), true, false);
        if (dataCenter.getQuotaEnforcementType() != QuotaEnforcementTypeEnum.DISABLED) {
            getModel().getQuota().setIsAvailable(true);
        } else {
            getModel().getQuota().setIsAvailable(false);
        }
    }

    @Override
    public void template_SelectedItemChanged()
    {
        // Leave this method empty. Not relevant for template.
    }

    @Override
    public void cluster_SelectedItemChanged()
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
        getModel().getOSType().setSelectedItem(this.template.getOs());
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

        if (!StringHelper.isNullOrEmpty(template.getTimeZone()))
        {
            updateTimeZone(template.getTimeZone());
        }
        else
        {
            updateDefaultTimeZone();
        }

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
        getModel().getCdImage().setSelectedItem(template.getIsoPath());

        boolean hasCd = !StringHelper.isNullOrEmpty(template.getIsoPath());
        getModel().getCdImage().setIsChangable(hasCd);
        getModel().getCdAttached().setEntity(hasCd);

        updateCdImage();
    }
}
