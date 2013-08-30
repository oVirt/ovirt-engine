package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.DisksAllocationModel;
import org.ovirt.engine.ui.uicommonweb.validation.ExistingPoolNameLengthValidation;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;

public class ExistingPoolModelBehavior extends PoolModelBehaviorBase {

    private final VM pool;

    public ExistingPoolModelBehavior(VM pool) {
        this.pool = pool;

    }

    @Override
    protected void changeDefualtHost() {
        super.changeDefualtHost();

        doChangeDefautlHost(pool.getDedicatedVmForVds());
    }

    @Override
    protected void setupSelectedTemplate(ListModel model, List<VmTemplate> templates) {
        setupTemplate(pool, model);
    }

    @Override
    public void template_SelectedItemChanged() {
        getModel().setIsDisksAvailable(true);
        updateHostPinning(pool.getMigrationSupport());
    }

    @Override
    protected void postInitTemplate() {
        setupWindowModelFrom(pool.getStaticData());
        getModel().setIsDisksAvailable(true);

        List<DataCenterWithCluster> dataCenterWithClusters = (List<DataCenterWithCluster>) getModel().getDataCenterWithClustersList().getItems();
        DataCenterWithCluster selectDataCenterWithCluster =
                Linq.firstOrDefault(dataCenterWithClusters,
                        new Linq.DataCenterWithClusterPredicate(pool.getStoragePoolId(), pool.getVdsGroupId()));

        getModel().getDataCenterWithClustersList()
                .setSelectedItem((selectDataCenterWithCluster != null) ? selectDataCenterWithCluster
                        : Linq.firstOrDefault(dataCenterWithClusters));
    }

    @Override
    protected DisplayType extractDisplayType(VmBase vmBase) {
        if (vmBase instanceof VmStatic) {
            return ((VmStatic) vmBase).getDefaultDisplayType();
        }

        return null;
    }

    @Override
    protected void postInitStorageDomains() {
        ArrayList<DiskModel> disks = (ArrayList<DiskModel>) getModel().getDisks();
        if (disks == null) {
            return;
        }

        StoragePool dataCenter = getModel().getSelectedDataCenter();
        AsyncDataProvider.getPermittedStorageDomainsByStoragePoolId(new AsyncQuery(this, new INewAsyncCallback() {
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
        }, getModel().getHash()), dataCenter.getId(), ActionGroup.CREATE_VM);
    }

    public boolean validate() {
        boolean parentValidation = super.validate();
        if (getModel().getNumOfDesktops().getIsValid()) {
            getModel().getNumOfDesktops().validateEntity(new IValidation[] { new ExistingPoolNameLengthValidation(
                    (String) getModel().getName().getEntity(),
                    ((Integer) getModel().getAssignedVms().getEntity()) +
                            Integer.parseInt((getModel().getNumOfDesktops().getEntity().toString())),
                    (Integer) getModel().getOSType().getSelectedItem()
            ) }
            );

            return getModel().getNumOfDesktops().getIsValid() && parentValidation;
        }

        return parentValidation;
    }
}
