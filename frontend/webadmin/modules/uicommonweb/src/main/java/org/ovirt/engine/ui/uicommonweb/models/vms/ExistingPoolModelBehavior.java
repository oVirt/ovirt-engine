package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.DisksAllocationModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.LatestVmTemplate;
import org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes.ExistingPoolInstanceTypeManager;
import org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes.InstanceTypeManager;
import org.ovirt.engine.ui.uicommonweb.validation.ExistingPoolNameLengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;

public class ExistingPoolModelBehavior extends PoolModelBehaviorBase {

    private final VM pool;

    private InstanceTypeManager instanceTypeManager;

    public ExistingPoolModelBehavior(VM pool) {
        this.pool = pool;
    }

    @Override
    public void initialize(SystemTreeItemModel systemTreeSelectedItem) {
        super.initialize(systemTreeSelectedItem);

        if (!StringHelper.isNullOrEmpty(pool.getVmPoolSpiceProxy())) {
            getModel().getSpiceProxyEnabled().setEntity(true);
            getModel().getSpiceProxy().setEntity(pool.getVmPoolSpiceProxy());
            getModel().getSpiceProxy().setIsChangeable(true);
        }

        instanceTypeManager = new ExistingPoolInstanceTypeManager(getModel(), pool);
        instanceTypeManager.setAlwaysEnabledFieldUpdate(true);
        getModel().getCustomProperties().setIsChangeable(false);
        getModel().getCustomPropertySheet().setIsChangeable(false);
    }

    @Override
    protected void changeDefaultHost() {
        super.changeDefaultHost();
        doChangeDefaultHost(pool.getDedicatedVmForVdsList());
    }

    @Override
    public void postDataCenterWithClusterSelectedItemChanged() {
        super.postDataCenterWithClusterSelectedItemChanged();

        Iterable<DataCenterWithCluster> dataCenterWithClusters = getModel().getDataCenterWithClustersList().getItems();
        DataCenterWithCluster selectDataCenterWithCluster =
                Linq.firstOrNull(dataCenterWithClusters,
                        new Linq.DataCenterWithClusterPredicate(pool.getStoragePoolId(), pool.getClusterId()));

        getModel().getDataCenterWithClustersList()
                .setSelectedItem((selectDataCenterWithCluster != null) ? selectDataCenterWithCluster
                        : Linq.firstOrNull(dataCenterWithClusters));
        getModel().getCpuSharesAmount().setEntity(pool.getCpuShares());
        updateCpuSharesSelection();
        initTemplate();
        instanceTypeManager.updateAll();
    }

    public void initTemplate() {
        setupTemplateWithVersion(pool.getVmtGuid(), pool.isUseLatestVersion(), true);
    }

    @Override
    public void templateWithVersion_SelectedItemChanged() {
        super.templateWithVersion_SelectedItemChanged();
        getModel().setIsDisksAvailable(true);
        VmTemplate template = getModel().getTemplateWithVersion().getSelectedItem().getTemplateVersion();
        if (template == null) {
            return;
        }

        boolean isLatestPropertyChanged = pool.isUseLatestVersion() != (template instanceof LatestVmTemplate);

        // template ID changed but latest is not set, as it would cause false-positives
        boolean isTemplateIdChangedSinceInit = !pool.getVmtGuid().equals(template.getId()) && !pool.isUseLatestVersion();

        // check if template-version selected requires to manually load the model instead of using the InstanceTypeManager
        if (isTemplateIdChangedSinceInit || isLatestPropertyChanged) {
            setupWindowFromVmBase(template);
        } else {
            setupWindowFromVmBase(pool.getStaticData());
        }
    }

    public void setupWindowFromVmBase(VmBase from) {
        doChangeDefaultHost(from.getDedicatedVmForVdsList());
        getModel().getCustomPropertySheet().deserialize(from.getCustomProperties());
        setupWindowModelFrom(from);
    }

    @Override
    public void updateIsDisksAvailable() {
        getModel().setIsDisksAvailable(getModel().getDisks() != null);
    }

    @Override
    protected void postInitStorageDomains() {
        ArrayList<DiskModel> disks = (ArrayList<DiskModel>) getModel().getDisks();
        if (disks == null) {
            return;
        }

        ActionGroup actionGroup = getModel().isCreateInstanceOnly() ? ActionGroup.CREATE_INSTANCE : ActionGroup.CREATE_VM;
        StoragePool dataCenter = getModel().getSelectedDataCenter();
        AsyncDataProvider.getInstance().getPermittedStorageDomainsByStoragePoolId(new AsyncQuery(getModel(), new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {

                VmModelBehaviorBase behavior = ExistingPoolModelBehavior.this;

                ArrayList<DiskModel> disks = (ArrayList<DiskModel>) behavior.getModel().getDisks();
                ArrayList<StorageDomain> storageDomains = (ArrayList<StorageDomain>) returnValue;
                ArrayList<StorageDomain> activeStorageDomains = filterStorageDomains(storageDomains);

                DisksAllocationModel disksAllocationModel = behavior.getModel().getDisksAllocationModel();
                disksAllocationModel.setActiveStorageDomains(activeStorageDomains);
                behavior.getModel().getStorageDomain().setItems(activeStorageDomains);

                for (DiskModel diskModel : disks) {
                    // Setting Quota
                    diskModel.getQuota().setItems(behavior.getModel().getQuota().getItems());
                    diskModel.getQuota().setIsChangeable(false);

                    ArrayList<Guid> storageIds = null;
                    for (DiskImage disk : pool.getDiskList()) {
                        if (diskModel.getDisk() instanceof DiskImage &&
                                ((DiskImage) diskModel.getDisk()).getImageId().equals(disk.getParentId())) {
                            storageIds = new ArrayList<>(disk.getStorageIds());
                            break;
                        }
                    }
                    if (storageIds == null || storageIds.size() == 0) {
                        continue;
                    }

                    Guid storageId = storageIds.get(0);
                    StorageDomain storageDomain = Linq.getStorageById(storageId, activeStorageDomains);
                    List<StorageDomain> diskStorageDomains = new ArrayList<>();
                    diskStorageDomains.add(storageDomain);
                    diskModel.getStorageDomain().setItems(diskStorageDomains);
                    diskModel.getStorageDomain().setIsChangeable(false);
                }
            }
        }), dataCenter.getId(), actionGroup);
    }

    public boolean validate() {
        boolean parentValidation = super.validate();
        if (getModel().getNumOfDesktops().getIsValid()) {
            getModel().getNumOfDesktops().validateEntity(new IValidation[] { new ExistingPoolNameLengthValidation(
                    getModel().getName().getEntity(),
                    getModel().getAssignedVms().getEntity() + getModel().getNumOfDesktops().getEntity(),
                    getModel().getOSType().getSelectedItem()
            ) }
            );

            return getModel().getNumOfDesktops().getIsValid() && parentValidation;
        }

        return parentValidation;
    }

    @Override
    protected List<Cluster> filterClusters(List<Cluster> clusters) {
        return AsyncDataProvider.getInstance().filterByArchitecture(clusters, pool.getClusterArch());
    }

    @Override
    public InstanceTypeManager getInstanceTypeManager() {
        return instanceTypeManager;
    }
}
