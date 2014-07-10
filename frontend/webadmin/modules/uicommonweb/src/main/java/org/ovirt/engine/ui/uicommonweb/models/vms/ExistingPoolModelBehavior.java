package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.DisksAllocationModel;
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
        initTemplate();
        super.initialize(systemTreeSelectedItem);

        if (!StringHelper.isNullOrEmpty(pool.getVmPoolSpiceProxy())) {
            getModel().getSpiceProxyEnabled().setEntity(true);
            getModel().getSpiceProxy().setEntity(pool.getVmPoolSpiceProxy());
            getModel().getSpiceProxy().setIsChangable(true);
        }

        instanceTypeManager = new ExistingPoolInstanceTypeManager(getModel(), pool);
        instanceTypeManager.setAlwaysEnabledFieldUpdate(true);
    }

    @Override
    protected void changeDefualtHost() {
        super.changeDefualtHost();

        doChangeDefautlHost(pool.getDedicatedVmForVds());
    }

    @Override
    public void postDataCenterWithClusterSelectedItemChanged() {
        super.postDataCenterWithClusterSelectedItemChanged();

        setupWindowModelFrom(pool.getStaticData());
        getModel().setIsDisksAvailable(true);

        Iterable<DataCenterWithCluster> dataCenterWithClusters = getModel().getDataCenterWithClustersList().getItems();
        DataCenterWithCluster selectDataCenterWithCluster =
                Linq.firstOrDefault(dataCenterWithClusters,
                        new Linq.DataCenterWithClusterPredicate(pool.getStoragePoolId(), pool.getVdsGroupId()));

        getModel().getDataCenterWithClustersList()
                .setSelectedItem((selectDataCenterWithCluster != null) ? selectDataCenterWithCluster
                        : Linq.firstOrDefault(dataCenterWithClusters));

        instanceTypeManager.updateAll();
    }

    public void initTemplate() {
        setupTemplate(pool.getVmtGuid(), pool.isUseLatestVersion());
    }

    @Override
    public void template_SelectedItemChanged() {
        getModel().setIsDisksAvailable(true);
        VmTemplate template = getModel().getTemplate().getSelectedItem();
        updateRngDevice(template.getId());
    }

    @Override
    protected void baseTemplateSelectedItemChanged() {
    }

    @Override
    protected void postInitStorageDomains() {
        ArrayList<DiskModel> disks = (ArrayList<DiskModel>) getModel().getDisks();
        if (disks == null) {
            return;
        }

        ActionGroup actionGroup = getModel().isCreateInstanceOnly() ? ActionGroup.CREATE_INSTANCE : ActionGroup.CREATE_VM;
        StoragePool dataCenter = getModel().getSelectedDataCenter();
        AsyncDataProvider.getInstance().getPermittedStorageDomainsByStoragePoolId(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {

                VmModelBehaviorBase behavior = (VmModelBehaviorBase) target;

                ArrayList<DiskModel> disks = (ArrayList<DiskModel>) behavior.getModel().getDisks();
                ArrayList<StorageDomain> storageDomains = (ArrayList<StorageDomain>) returnValue;
                ArrayList<StorageDomain> activeStorageDomains = filterStorageDomains(storageDomains);

                DisksAllocationModel disksAllocationModel = behavior.getModel().getDisksAllocationModel();
                disksAllocationModel.setActiveStorageDomains(activeStorageDomains);
                behavior.getModel().getStorageDomain().setItems(activeStorageDomains);

                for (DiskModel diskModel : disks) {
                    // Setting Quota
                    diskModel.getQuota().setItems(behavior.getModel().getQuota().getItems());
                    diskModel.getQuota().setIsChangable(false);

                    ArrayList<Guid> storageIds = ((DiskImage) diskModel.getDisk()).getStorageIds();
                    // We only have one storage ID, as the object is a VM, not a template
                    if (storageIds.size() == 0) {
                        continue;
                    }

                    Guid storageId = storageIds.get(0);
                    StorageDomain storageDomain = Linq.getStorageById(storageId, activeStorageDomains);
                    List<StorageDomain> diskStorageDomains = new ArrayList<StorageDomain>();
                    diskStorageDomains.add(storageDomain);
                    diskModel.getStorageDomain().setItems(diskStorageDomains);
                    diskModel.getStorageDomain().setIsChangable(false);
                }
            }
        }, getModel().getHash()), dataCenter.getId(), actionGroup);
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
    protected List<VDSGroup> filterClusters(List<VDSGroup> clusters) {
        return AsyncDataProvider.getInstance().filterByArchitecture(clusters, pool.getClusterArch());
    }

    @Override
    public InstanceTypeManager getInstanceTypeManager() {
        return instanceTypeManager;
    }
}
