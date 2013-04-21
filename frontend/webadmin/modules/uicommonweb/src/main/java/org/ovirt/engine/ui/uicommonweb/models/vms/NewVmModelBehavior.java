package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.QuotaEnforcementTypeEnum;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.StorageType;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmType;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.compat.NGuid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemType;

public class NewVmModelBehavior extends VmModelBehaviorBase
{
    @Override
    public void Initialize(SystemTreeItemModel systemTreeSelectedItem)
    {
        super.Initialize(systemTreeSelectedItem);
        AsyncDataProvider.GetDataCenterByClusterServiceList(new AsyncQuery(getModel(),
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        UnitVmModel model = (UnitVmModel) target;
                        ArrayList<StoragePool> list = new ArrayList<StoragePool>();
                        for (StoragePool a : (ArrayList<StoragePool>) returnValue)
                        {
                            if (a.getstatus() == StoragePoolStatus.Up)
                            {
                                list.add(a);
                            }
                        }
                        model.SetDataCenter(model, list);

                    }
                }, getModel().getHash()), true, false);
        InitPriority(0);
    }

    @Override
    public void DataCenter_SelectedItemChanged()
    {
        StoragePool dataCenter = (StoragePool) getModel().getDataCenter().getSelectedItem();

        getModel().setIsHostAvailable(dataCenter.getstorage_pool_type() != StorageType.LOCALFS);

        AsyncDataProvider.GetClusterByServiceList(new AsyncQuery(new Object[] { this, getModel() },
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        Object[] array = (Object[]) target;
                        NewVmModelBehavior behavior = (NewVmModelBehavior) array[0];
                        UnitVmModel model = (UnitVmModel) array[1];
                        ArrayList<VDSGroup> clusters = (ArrayList<VDSGroup>) returnValue;
                        model.SetClusters(model, clusters, null);
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
        VmTemplate template = (VmTemplate) getModel().getTemplate().getSelectedItem();

        if (template != null)
        {
            // Copy VM parameters from template.
            getModel().getOSType().setSelectedItem(template.getOs());
            getModel().getTotalCPUCores().setEntity(Integer.toString(template.getNumOfCpus()));
            getModel().getNumOfSockets().setSelectedItem(template.getNumOfSockets());
            getModel().getNumOfMonitors().setSelectedItem(template.getNumOfMonitors());
            getModel().getDomain().setSelectedItem(template.getDomain());
            getModel().getMemSize().setEntity(template.getMemSizeMb());
            getModel().getUsbPolicy().setSelectedItem(template.getUsbPolicy());
            getModel().setBootSequence(template.getDefaultBootSequence());
            getModel().getIsHighlyAvailable().setEntity(template.isAutoStartup());

            updateHostPinning(template.getMigrationSupport());
            doChangeDefautlHost(template.getDedicatedVmForVds());

            getModel().getIsSmartcardEnabled().setEntity(template.isSmartcardEnabled());
            getModel().getIsDeleteProtected().setEntity(template.isDeleteProtected());
            getModel().getVncKeyboardLayout().setSelectedItem(template.getVncKeyboardLayout());

            if (getModel().getVmType() == VmType.Desktop) {
                getModel().getIsStateless().setEntity(template.isStateless());
                getModel().getAllowConsoleReconnect().setEntity(template.isAllowConsoleReconnect());
            }

            boolean hasCd = !StringHelper.isNullOrEmpty(template.getIsoPath());

            getModel().getCdImage().setIsChangable(hasCd);
            getModel().getCdAttached().setEntity(hasCd);
            if (hasCd) {
                getModel().getCdImage().setSelectedItem(template.getIsoPath());
            }

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
                if (dt == template.getDefaultDisplayType())
                {
                    displayProtocol = a;
                    break;
                }
            }
            getModel().getDisplayProtocol().setSelectedItem(displayProtocol);

            // By default, take kernel params from template.
            getModel().getKernel_path().setEntity(template.getKernelUrl());
            getModel().getKernel_parameters().setEntity(template.getKernelParams());
            getModel().getInitrd_path().setEntity(template.getInitrdUrl());

            if (!template.getId().equals(NGuid.Empty))
            {
                getModel().getStorageDomain().setIsChangable(true);
                getModel().getProvisioning().setIsChangable(true);

                getModel().setIsBlankTemplate(false);
                InitDisks();
            }
            else
            {
                getModel().getStorageDomain().setIsChangable(false);
                getModel().getProvisioning().setIsChangable(false);

                getModel().setIsBlankTemplate(true);
                getModel().setIsDisksAvailable(false);
                getModel().setDisks(null);
            }

            InitPriority(template.getPriority());
            InitStorageDomains();

            // use min. allocated memory from the template, if specified
            if (template.getMinAllocatedMem() == 0) {
                UpdateMinAllocatedMemory();
            } else {
                getModel().getMinAllocatedMemory().setEntity(template.getMinAllocatedMem());
            }
            updateQuotaByCluster(template.getQuotaId(), template.getQuotaName());
        }
    }

    @Override
    public void Cluster_SelectedItemChanged()
    {
        UpdateDefaultHost();
        updateCustomPropertySheet();
        UpdateMinAllocatedMemory();
        UpdateNumOfSockets();
        if (getModel().getTemplate().getSelectedItem() != null) {
            VmTemplate template = (VmTemplate) getModel().getTemplate().getSelectedItem();
            updateQuotaByCluster(template.getQuotaId(), template.getQuotaName());
        }
        updateCpuPinningVisibility();
    }

    @Override
    public void DefaultHost_SelectedItemChanged()
    {
        UpdateCdImage();
    }

    @Override
    public void Provisioning_SelectedItemChanged()
    {
        boolean provisioning = (Boolean) getModel().getProvisioning().getEntity();
        getModel().getProvisioningThin_IsSelected().setEntity(!provisioning);
        getModel().getProvisioningClone_IsSelected().setEntity(provisioning);
        getModel().getDisksAllocationModel().setIsVolumeFormatAvailable(true);
        getModel().getDisksAllocationModel().setIsVolumeFormatChangable(provisioning);
        getModel().getDisksAllocationModel().setIsAliasChangable(true);

        InitStorageDomains();
    }

    @Override
    public void UpdateMinAllocatedMemory()
    {
        VDSGroup cluster = (VDSGroup) getModel().getCluster().getSelectedItem();
        if (cluster == null) {
            return;
        }

        double overCommitFactor = 100.0 / cluster.getmax_vds_memory_over_commit();
        getModel().getMinAllocatedMemory()
                .setEntity((int) ((Integer) getModel().getMemSize().getEntity() * overCommitFactor));
    }

    private void InitTemplate()
    {
        StoragePool dataCenter = (StoragePool) getModel().getDataCenter().getSelectedItem();

        // Filter according to system tree selection.
        if (getSystemTreeSelectedItem() != null && getSystemTreeSelectedItem().getType() == SystemTreeItemType.Storage)
        {
            StorageDomain storage = (StorageDomain) getSystemTreeSelectedItem().getEntity();

            AsyncDataProvider.GetTemplateListByDataCenter(new AsyncQuery(new Object[] { this, storage },
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object target1, Object returnValue1) {

                            Object[] array1 = (Object[]) target1;
                            NewVmModelBehavior behavior1 = (NewVmModelBehavior) array1[0];
                            StorageDomain storage1 = (StorageDomain) array1[1];
                            AsyncDataProvider.GetTemplateListByStorage(new AsyncQuery(new Object[] { behavior1,
                                    returnValue1 },
                                    new INewAsyncCallback() {
                                        @Override
                                        public void onSuccess(Object target2, Object returnValue2) {

                                            Object[] array2 = (Object[]) target2;
                                            NewVmModelBehavior behavior2 = (NewVmModelBehavior) array2[0];
                                            ArrayList<VmTemplate> templatesByDataCenter =
                                                    (ArrayList<VmTemplate>) array2[1];
                                            ArrayList<VmTemplate> templatesByStorage =
                                                    (ArrayList<VmTemplate>) returnValue2;
                                            VmTemplate blankTemplate =
                                                    Linq.firstOrDefault(templatesByDataCenter,
                                                            new Linq.TemplatePredicate(NGuid.Empty));
                                            if (blankTemplate != null)
                                            {
                                                templatesByStorage.add(0, blankTemplate);
                                            }
                                            behavior2.PostInitTemplate((ArrayList<VmTemplate>) returnValue2);

                                        }
                                    }),
                                    storage1.getId());

                        }
                    }, getModel().getHash()),
                    dataCenter.getId());
        }
        else
        {
            AsyncDataProvider.GetTemplateListByDataCenter(new AsyncQuery(this,
                    new INewAsyncCallback() {
                        @Override
                        public void onSuccess(Object target, Object returnValue) {

                            NewVmModelBehavior behavior = (NewVmModelBehavior) target;
                            behavior.PostInitTemplate((ArrayList<VmTemplate>) returnValue);

                        }
                    }, getModel().getHash()), dataCenter.getId());
        }
    }

    private void PostInitTemplate(ArrayList<VmTemplate> templates)
    {
        // If there was some template selected before, try select it again.
        VmTemplate oldTemplate = (VmTemplate) getModel().getTemplate().getSelectedItem();

        getModel().getTemplate().setItems(templates);

        getModel().getTemplate().setSelectedItem(Linq.firstOrDefault(templates,
                oldTemplate != null ? new Linq.TemplatePredicate(oldTemplate.getId())
                        : new Linq.TemplatePredicate(NGuid.Empty)));

        UpdateIsDisksAvailable();
    }

    public void InitCdImage()
    {
        updateUserCdImage(((StoragePool) getModel().getDataCenter().getSelectedItem()).getId());
    }

    @Override
    public void UpdateIsDisksAvailable()
    {
        getModel().setIsDisksAvailable(getModel().getDisks() != null
                && getModel().getProvisioning().getIsChangable());
    }

}
