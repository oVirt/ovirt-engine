package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.DisplayType;
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

@SuppressWarnings("unused")
public class TemplateVmModelBehavior extends IVmModelBehavior
{
    private VmTemplate template;

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
                                    new java.util.ArrayList<storage_pool>(java.util.Arrays.asList(new storage_pool[] { dataCenter })));
                            model.getDataCenter().setIsChangable(false);

                        }
                    },
                    getModel().getHash()),
                    template.getstorage_pool_id().getValue());
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
                        java.util.ArrayList<VDSGroup> clusters = (java.util.ArrayList<VDSGroup>) returnValue;
                        model.SetClusters(model, clusters, vmTemplate.getvds_group_id().getValue());
                        behavior.InitTemplate();
                        behavior.InitCdImage();

                    }
                }, getModel().getHash()), dataCenter.getId());
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
        getModel().setBootSequence(this.template.getdefault_boot_sequence());
        getModel().getIsHighlyAvailable().setEntity(this.template.getauto_startup());
        getModel().getNumOfSockets().setEntity(this.template.getnum_of_sockets());
        getModel().getTotalCPUCores().setEntity(this.template.getnum_of_cpus());
        getModel().getIsStateless().setEntity(this.template.getis_stateless());

        getModel().getKernel_parameters().setEntity(this.template.getkernel_params());
        getModel().getKernel_path().setEntity(this.template.getkernel_url());
        getModel().getInitrd_path().setEntity(this.template.getinitrd_url());

        if (!StringHelper.isNullOrEmpty(template.gettime_zone()))
        {
            // Patch! Create key-value pair with a right key.
            getModel().getTimeZone()
                    .setSelectedItem(new KeyValuePairCompat<String, String>(template.gettime_zone(), ""));

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
        getModel().getCdImage().setIsChangable(!StringHelper.isNullOrEmpty(template.getiso_path()));

        UpdateCdImage();
    }
}
