package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_pool;
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
    public void Initialize(SystemTreeItemModel systemTreeSelectedItem)
    {
        super.Initialize(systemTreeSelectedItem);
        getModel().getTemplate().setIsChangable(false);
        getModel().getProvisioning().setIsChangable(false);
        getModel().getStorageDomain().setIsChangable(false);

        if (template.getStoragePoolId() != null && !template.getStoragePoolId().getValue().equals(NGuid.Empty))
        {
            AsyncDataProvider.GetDataCenterById(new AsyncQuery(getModel(),
                    new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target, Object returnValue) {

                            UnitVmModel model = (UnitVmModel) target;
                            storage_pool dataCenter = (storage_pool) returnValue;
                            model.SetDataCenter(model,
                                    new ArrayList<storage_pool>(Arrays.asList(new storage_pool[] { dataCenter })));
                            model.getDataCenter().setIsChangable(false);

                        }
                    },
                    getModel().getHash()),
                    template.getStoragePoolId().getValue());
        }

        getModel().getMigrationMode().setSelectedItem(template.getMigrationSupport());
    }

    @Override
    public void DataCenter_SelectedItemChanged()
    {
        storage_pool dataCenter = (storage_pool) getModel().getDataCenter().getSelectedItem();

        getModel().setIsHostAvailable(dataCenter.getstorage_pool_type() != StorageType.LOCALFS);

        AsyncDataProvider.GetClusterByServiceList(new AsyncQuery(new Object[] { this, getModel() },
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

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

                        model.SetClusters(model, filteredClusters, vmTemplate.getVdsGroupId().getValue());
                        behavior.InitTemplate();
                        behavior.InitCdImage();

                    }
                }, getModel().getHash()), dataCenter.getId(), true, false);
        if (dataCenter.getQuotaEnforcementType() != QuotaEnforcementTypeEnum.DISABLED) {
            getModel().getQuota().setIsAvailable(true);
        } else {
            getModel().getQuota().setIsAvailable(false);
        }
    }

    @Override
    public void Template_SelectedItemChanged()
    {
        // Leave this method empty. Not relevant for template.
    }

    @Override
    public void Cluster_SelectedItemChanged()
    {
        UpdateDefaultHost();
        UpdateNumOfSockets();
        updateQuotaByCluster(template.getQuotaId(), template.getQuotaName());
    }

    @Override
    public void DefaultHost_SelectedItemChanged()
    {
        UpdateCdImage();
    }

    @Override
    public void Provisioning_SelectedItemChanged()
    {
    }

    @Override
    public void UpdateMinAllocatedMemory()
    {
    }

    @Override
    protected void ChangeDefualtHost() {
        super.ChangeDefualtHost();

        doChangeDefautlHost(template.getDedicatedVmForVds());
    }

    private void InitTemplate()
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
            UpdateDefaultTimeZone();
        }

        // Update domain list
        UpdateDomain();

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

        InitPriority(this.template.getPriority());
    }

    private void InitCdImage()
    {
        getModel().getCdImage().setSelectedItem(template.getIsoPath());

        boolean hasCd = !StringHelper.isNullOrEmpty(template.getIsoPath());
        getModel().getCdImage().setIsChangable(hasCd);
        getModel().getCdAttached().setEntity(hasCd);

        UpdateCdImage();
    }
}
