package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmOsType;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.KeyValuePairCompat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IntegerValidation;
import org.ovirt.engine.ui.uicommonweb.validation.LengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

@SuppressWarnings("unused")
public class NewPoolModelBehavior extends IVmModelBehavior
{
    private Event poolModelBehaviorInitializedEvent = new Event("PoolModelBehaviorInitializedEvent",
            NewPoolModelBehavior.class);

    public Event getPoolModelBehaviorInitializedEvent()
    {
        return poolModelBehaviorInitializedEvent;
    }

    private void setPoolModelBehaviorInitializedEvent(Event value)
    {
        poolModelBehaviorInitializedEvent = value;
    }

    @Override
    public void Initialize(SystemTreeItemModel systemTreeSelectedItem)
    {
        super.Initialize(systemTreeSelectedItem);

        getModel().getDisksAllocationModel().setIsVolumeFormatAvailable(false);

        AsyncDataProvider.GetDataCenterList(new AsyncQuery(getModel(),
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        UnitVmModel model = (UnitVmModel) target;
                        java.util.ArrayList<storage_pool> list = new java.util.ArrayList<storage_pool>();
                        for (storage_pool a : (java.util.ArrayList<storage_pool>) returnValue)
                        {
                            if (a.getstatus() == StoragePoolStatus.Up)
                            {
                                list.add(a);
                            }
                        }
                        model.SetDataCenter(model, list);

                        getPoolModelBehaviorInitializedEvent().raise(this, EventArgs.Empty);

                    }
                }, getModel().getHash()));
    }

    @Override
    public void DataCenter_SelectedItemChanged()
    {
        storage_pool dataCenter = (storage_pool) getModel().getDataCenter().getSelectedItem();

        if (dataCenter == null)
            return;

        getModel().setIsHostAvailable(dataCenter.getstorage_pool_type() != StorageType.LOCALFS);

        AsyncDataProvider.GetClusterList(new AsyncQuery(new Object[] { this, getModel() },
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        Object[] array = (Object[]) target;
                        NewPoolModelBehavior behavior = (NewPoolModelBehavior) array[0];
                        UnitVmModel model = (UnitVmModel) array[1];
                        java.util.ArrayList<VDSGroup> clusters = (java.util.ArrayList<VDSGroup>) returnValue;
                        model.SetClusters(model, clusters, null);
                        behavior.InitTemplate();
                        behavior.InitCdImage();

                    }
                }, getModel().getHash()), dataCenter.getId());
    }

    @Override
    public void Template_SelectedItemChanged()
    {
        VmTemplate template = (VmTemplate) getModel().getTemplate().getSelectedItem();

        if (template != null)
        {
            // Copy VM parameters from template.
            getModel().getOSType().setSelectedItem(template.getos());
            getModel().getNumOfSockets().setEntity(template.getnum_of_sockets());
            getModel().getTotalCPUCores().setEntity(template.getnum_of_cpus());
            getModel().getNumOfMonitors().setSelectedItem(template.getnum_of_monitors());
            getModel().getDomain().setSelectedItem(template.getdomain());
            getModel().getMemSize().setEntity(template.getmem_size_mb());
            getModel().getUsbPolicy().setSelectedItem(template.getusb_policy());
            getModel().setBootSequence(template.getdefault_boot_sequence());
            getModel().getIsHighlyAvailable().setEntity(template.getauto_startup());

            getModel().getCdImage().setIsChangable(!StringHelper.isNullOrEmpty(template.getiso_path()));
            if (getModel().getCdImage().getIsChangable())
            {
                getModel().getCdImage().setSelectedItem(template.getiso_path());
            }

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

            java.util.ArrayList<VDSGroup> clusters = (java.util.ArrayList<VDSGroup>) getModel().getCluster().getItems();
            VDSGroup selectCluster =
                    Linq.FirstOrDefault(clusters, new Linq.ClusterPredicate(template.getvds_group_id()));

            getModel().getCluster().setSelectedItem((selectCluster != null) ? selectCluster
                    : Linq.FirstOrDefault(clusters));

            // Update display protocol selected item
            EntityModel displayProtocol = null;
            boolean isFirst = true;
            for (Object item : getModel().getDisplayProtocol().getItems())
            {
                EntityModel a = (EntityModel) item;
                if (isFirst)
                {
                    displayProtocol = a;
                    isFirst = false;
                }
                DisplayType dt = (DisplayType) a.getEntity();
                if (dt == template.getdefault_display_type())
                {
                    displayProtocol = a;
                    break;
                }
            }
            getModel().getDisplayProtocol().setSelectedItem(displayProtocol);

            // By default, take kernel params from template.
            getModel().getKernel_path().setEntity(template.getkernel_url());
            getModel().getKernel_parameters().setEntity(template.getkernel_params());
            getModel().getInitrd_path().setEntity(template.getinitrd_url());

            getModel().setIsDisksAvailable(getModel().getIsNew());
            getModel().getProvisioning().setIsAvailable(false);

            if (!template.getId().equals(Guid.Empty))
            {
                getModel().getStorageDomain().setIsChangable(true);

                getModel().setIsBlankTemplate(false);
                InitDisks();
            }
            else
            {
                getModel().getStorageDomain().setIsChangable(false);

                getModel().setIsBlankTemplate(true);
                getModel().setIsDisksAvailable(false);
                getModel().setDisks(null);
            }

            getModel().getProvisioning().setEntity(false);

            InitPriority(template.getpriority());
            InitStorageDomains();
            UpdateMinAllocatedMemory();
        }
    }

    @Override
    public void Cluster_SelectedItemChanged()
    {
        UpdateDefaultHost();
        UpdateIsCustomPropertiesAvailable();
        UpdateMinAllocatedMemory();
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
        VDSGroup cluster = (VDSGroup) getModel().getCluster().getSelectedItem();
        if (cluster == null)
        {
            return;
        }

        double overCommitFactor = 100.0 / cluster.getmax_vds_memory_over_commit();
        getModel().getMinAllocatedMemory()
                .setEntity((int) ((Integer) getModel().getMemSize().getEntity() * overCommitFactor));
    }

    private void InitTemplate()
    {
        storage_pool dataCenter = (storage_pool) getModel().getDataCenter().getSelectedItem();

        AsyncDataProvider.GetTemplateListByDataCenter(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target1, Object returnValue1) {
                        java.util.ArrayList<VmTemplate> loadedTemplates =
                                (java.util.ArrayList<VmTemplate>) returnValue1;

                        java.util.ArrayList<VmTemplate> templates = new java.util.ArrayList<VmTemplate>();
                        for (VmTemplate template : loadedTemplates)
                        {
                            if (!template.getId().equals(Guid.Empty))
                            {
                                templates.add(template);
                            }
                        }
                        getModel().getTemplate().setItems(templates);
                        // Template.Value = templates.FirstOrDefault();
                        getModel().getTemplate().setSelectedItem(Linq.FirstOrDefault(templates));
                    }
                }), dataCenter.getId());

        /*
         * //Filter according to system tree selection. if (getSystemTreeSelectedItem() != null &&
         * getSystemTreeSelectedItem().getType() == SystemTreeItemType.Storage) { storage_domains storage =
         * (storage_domains)getSystemTreeSelectedItem().getEntity();
         *
         * AsyncDataProvider.GetTemplateListByDataCenter(new AsyncQuery(new Object[] { this, storage }, new
         * INewAsyncCallback() {
         *
         * @Override public void OnSuccess(Object target1, Object returnValue1) {
         *
         * Object[] array1 = (Object[])target1; NewPoolModelBehavior behavior1 = (NewPoolModelBehavior)array1[0];
         * storage_domains storage1 = (storage_domains)array1[1]; AsyncDataProvider.GetTemplateListByStorage(new
         * AsyncQuery(new Object[] { behavior1, returnValue1 }, new INewAsyncCallback() {
         *
         * @Override public void OnSuccess(Object target2, Object returnValue2) { Object[] array2 = (Object[])target2;
         * NewPoolModelBehavior behavior2 = (NewPoolModelBehavior)array2[0]; java.util.ArrayList<VmTemplate>
         * templatesByDataCenter = (java.util.ArrayList<VmTemplate>)array2[1]; java.util.ArrayList<VmTemplate>
         * templatesByStorage = (java.util.ArrayList<VmTemplate>)returnValue2; VmTemplate blankTemplate =
         * Linq.FirstOrDefault(templatesByDataCenter, new Linq.TemplatePredicate(Guid.Empty)); if (blankTemplate !=
         * null) { templatesByStorage.add(0, blankTemplate); }
         * behavior2.PostInitTemplate((java.util.ArrayList<VmTemplate>)returnValue2);
         *
         * } }), storage1.getid());
         *
         * } }, getModel().getHash()), dataCenter.getId()); } else { AsyncDataProvider.GetTemplateListByDataCenter(new
         * AsyncQuery(this, new INewAsyncCallback() {
         *
         * @Override public void OnSuccess(Object target, Object returnValue) {
         *
         * NewPoolModelBehavior behavior = (NewPoolModelBehavior)target;
         * behavior.PostInitTemplate((java.util.ArrayList<VmTemplate>)returnValue);
         *
         * } }, getModel().getHash()), dataCenter.getId()); }
         */
    }

    private void PostInitTemplate(java.util.ArrayList<VmTemplate> templates)
    {
        // If there was some template selected before, try select it again.
        VmTemplate oldTemplate = (VmTemplate) getModel().getTemplate().getSelectedItem();

        getModel().getTemplate().setItems(templates);

        getModel().getTemplate().setSelectedItem(Linq.FirstOrDefault(templates,
                oldTemplate != null ? new Linq.TemplatePredicate(oldTemplate.getId())
                        : new Linq.TemplatePredicate(Guid.Empty)));
    }

    public void InitCdImage()
    {
        UpdateCdImage();
    }

    @Override
    public void UpdateIsDisksAvailable()
    {
    }

    @Override
    public boolean Validate() {
        // VALIDATE POOLS
        getModel().setIsPoolTabValid(true);
        // Revalidate name field.
        // TODO: Make maximum characters value depend on number of desktops in pool.
        VmOsType os = (VmOsType) getModel().getOSType().getSelectedItem();

        int maxAlowedVms = DataProvider.GetMaxVmsInPool();

        LengthValidation tempVar4 = new LengthValidation();
        tempVar4.setMaxLength(4);
        IntegerValidation tempVar5 = new IntegerValidation();
        tempVar5.setMinimum(1);
        tempVar5.setMaximum(getModel().getIsNew() ? maxAlowedVms : maxAlowedVms
                - (Integer) getModel().getAssignedVms().getEntity());
        getModel().getNumOfDesktops()
                .ValidateEntity(new IValidation[] { new NotEmptyValidation(), tempVar4, tempVar5 });

        getModel().setIsGeneralTabValid(getModel().getIsGeneralTabValid() && getModel().getName().getIsValid()
                && getModel().getNumOfDesktops().getIsValid());

        getModel().setIsPoolTabValid(true);

        return super.Validate() && getModel().getName().getIsValid() && getModel().getNumOfDesktops().getIsValid();
    }
}
