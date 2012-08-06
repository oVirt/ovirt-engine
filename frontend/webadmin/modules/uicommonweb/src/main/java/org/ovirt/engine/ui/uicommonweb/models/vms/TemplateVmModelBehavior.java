package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
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

        if (template.getstorage_pool_id() != null && !template.getstorage_pool_id().getValue().equals(NGuid.Empty))
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
                    template.getstorage_pool_id().getValue());
        }

        switch (template.getMigrationSupport())
        {
        case PINNED_TO_HOST:
            getModel().getRunVMOnSpecificHost().setEntity(true);
            break;
        case IMPLICITLY_NON_MIGRATABLE:
            getModel().getDontMigrateVM().setEntity(true);
            break;
        }
    }

    @Override
    public void DataCenter_SelectedItemChanged()
    {
        storage_pool dataCenter = (storage_pool) getModel().getDataCenter().getSelectedItem();

        getModel().setIsHostAvailable(dataCenter.getstorage_pool_type() != StorageType.LOCALFS);

        AsyncDataProvider.GetClusterList(new AsyncQuery(new Object[] { this, getModel() },
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        Object[] array = (Object[]) target;
                        TemplateVmModelBehavior behavior = (TemplateVmModelBehavior) array[0];
                        UnitVmModel model = (UnitVmModel) array[1];
                        VmTemplate vmTemplate = ((TemplateVmModelBehavior) (array[0])).template;
                        ArrayList<VDSGroup> clusters = (ArrayList<VDSGroup>) returnValue;
                        model.SetClusters(model, clusters, vmTemplate.getvds_group_id().getValue());
                        behavior.InitTemplate();
                        behavior.InitCdImage();

                    }
                }, getModel().getHash()), dataCenter.getId());
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
        updateQuotaByCluster(template.getQuotaId());
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

        doChangeDefautlHost(template.getdedicated_vm_for_vds());
    }

    private void InitTemplate()
    {
        // Update model state according to VM properties.
        getModel().getName().setEntity(this.template.getname());
        getModel().getDescription().setEntity(this.template.getdescription());
        getModel().getMemSize().setEntity(this.template.getmem_size_mb());
        getModel().getOSType().setSelectedItem(this.template.getos());
        getModel().getDomain().setSelectedItem(this.template.getdomain());
        getModel().getUsbPolicy().setSelectedItem(this.template.getusb_policy());
        getModel().getNumOfMonitors().setSelectedItem(this.template.getnum_of_monitors());
        getModel().getAllowConsoleReconnect().setEntity(this.template.getAllowConsoleReconnect());
        getModel().setBootSequence(this.template.getdefault_boot_sequence());
        getModel().getIsHighlyAvailable().setEntity(this.template.getauto_startup());
        getModel().getTotalCPUCores().setEntity(Integer.toString(this.template.getnum_of_cpus()));
        getModel().getNumOfSockets().setSelectedItem(this.template.getnum_of_sockets());
        getModel().getIsStateless().setEntity(this.template.getis_stateless());

        getModel().getKernel_parameters().setEntity(this.template.getkernel_params());
        getModel().getKernel_path().setEntity(this.template.getkernel_url());
        getModel().getInitrd_path().setEntity(this.template.getinitrd_url());

        if (!StringHelper.isNullOrEmpty(template.gettime_zone()))
        {
            // Patch! Create key-value pair with a right key.
            getModel().getTimeZone()
                    .setSelectedItem(new KeyValuePairCompat<String, String>(template.gettime_zone(), "")); //$NON-NLS-1$

            UpdateTimeZone();
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

            if (displayType == this.template.getdefault_display_type())
            {
                getModel().getDisplayProtocol().setSelectedItem(item);
                break;
            }
        }

        InitPriority(this.template.getpriority());
    }

    private void InitCdImage()
    {
        getModel().getCdImage().setSelectedItem(template.getiso_path());

        boolean hasCd = !StringHelper.isNullOrEmpty(template.getiso_path());
        getModel().getCdImage().setIsChangable(hasCd);
        getModel().getCdAttached().setEntity(hasCd);

        UpdateCdImage();
    }
}
