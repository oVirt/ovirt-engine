package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.storage.DisksAllocationModel;
import org.ovirt.engine.ui.uicommonweb.models.templates.LatestVmTemplate;
import org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes.ExistingPoolInstanceTypeManager;
import org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes.InstanceTypeManager;
import org.ovirt.engine.ui.uicommonweb.validation.ExistingPoolNameLengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;

public class ExistingPoolModelBehavior extends PoolModelBehaviorBase {

    private final VM pool;
    private final VmPool actualPool;

    private InstanceTypeManager instanceTypeManager;

    public ExistingPoolModelBehavior(VM pool, VmPool actualPool) {

        this.pool = pool;
        this.actualPool = actualPool;
    }

    @Override
    public void initialize() {
        super.initialize();

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

        Collection<DataCenterWithCluster> dataCenterWithClusters = getModel().getDataCenterWithClustersList().getItems();
        DataCenterWithCluster selectDataCenterWithCluster =
                Linq.firstOrNull(dataCenterWithClusters,
                        new Linq.DataCenterWithClusterPredicate(pool.getStoragePoolId(), pool.getClusterId()));

        getModel().getDataCenterWithClustersList()
                .setSelectedItem((selectDataCenterWithCluster != null) ? selectDataCenterWithCluster
                        : Linq.firstOrNull(dataCenterWithClusters));
        getModel().getCpuSharesAmount().setEntity(pool.getCpuShares());
        updateCpuSharesSelection();

        if (!isCustomCompatibilityVersionChangeInProgress) {
            initTemplate();
        }

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
            //can be set only from DB since it is not part of the Template's fields
            getModel().getCpuPinning().setEntity(pool.getCpuPinning());
        }

        updateBiosType();

        getInstanceTypeManager().updateInstanceTypeFieldsFromSource();
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
        AsyncDataProvider.getInstance().getPermittedStorageDomainsByStoragePoolId(asyncQuery(storageDomains -> {
            ArrayList<DiskModel> disks1 = (ArrayList<DiskModel>) getModel().getDisks();
            ArrayList<StorageDomain> activeStorageDomains = filterStorageDomains(storageDomains);

            DisksAllocationModel disksAllocationModel = getModel().getDisksAllocationModel();
            disksAllocationModel.setActiveStorageDomains(activeStorageDomains);
            getModel().getStorageDomain().setItems(activeStorageDomains);

            for (DiskModel diskModel : disks1) {
                // Setting Quota
                diskModel.getQuota().setItems(getModel().getQuota().getItems());
                diskModel.getQuota().setIsChangeable(false);

                List<Guid> storageIds = ((DiskImage) diskModel.getDisk()).getStorageIds();
                for (DiskImage disk : pool.getDiskList()) {
                    if (diskModel.getDisk() instanceof DiskImage &&
                            ((DiskImage) diskModel.getDisk()).getImageId().equals(disk.getImageTemplateId())) {
                        storageIds = new ArrayList<>(disk.getStorageIds());
                        break;
                    }
                }
                if (storageIds == null || storageIds.size() == 0) {
                    continue;
                }

                Guid storageId = storageIds.get(0);
                StorageDomain storageDomain =
                        activeStorageDomains.stream()
                                .filter(new Linq.IdPredicate<>(storageId))
                                .findFirst()
                                .orElse(null);
                List<StorageDomain> diskStorageDomains = new ArrayList<>();
                diskStorageDomains.add(storageDomain);
                diskModel.getStorageDomain().setItems(diskStorageDomains);
                diskModel.getStorageDomain().setIsChangeable(false);
            }
        }), dataCenter.getId(), actionGroup);

        getModel().getDisksAllocationModel().initializeAutoSelectTarget(false, actualPool.isAutoStorageSelect());
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
