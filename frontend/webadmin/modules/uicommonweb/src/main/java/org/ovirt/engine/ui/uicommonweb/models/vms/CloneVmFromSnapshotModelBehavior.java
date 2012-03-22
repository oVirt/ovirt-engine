package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.storage_domains;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.DisksAllocationModel;

@SuppressWarnings("unused")
public class CloneVmFromSnapshotModelBehavior extends ExistingVmModelBehavior
{
    public CloneVmFromSnapshotModelBehavior() {
        super(null);
    }

    @Override
    public void Initialize(SystemTreeItemModel systemTreeSelectedItem)
    {
        super.Initialize(systemTreeSelectedItem);
    }

    public void InitTemplate()
    {
        AsyncDataProvider.GetTemplateById(new AsyncQuery(getModel(),
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        UnitVmModel model = (UnitVmModel) target;
                        VmTemplate template = (VmTemplate) returnValue;
                        model.getTemplate()
                                .setItems(new ArrayList<VmTemplate>(Arrays.asList(new VmTemplate[] { template })));
                        model.getTemplate().setSelectedItem(template);
                        model.getTemplate().setIsChangable(false);
                    }
                },
                getModel().getHash()),
                vm.getvmt_guid());
    }

    @Override
    public void Template_SelectedItemChanged()
    {
        super.Template_SelectedItemChanged();

        getModel().getName().setEntity("");
        getModel().getDescription().setEntity("");
        getModel().getProvisioning().setEntity(true);
        getModel().getProvisioning().setIsAvailable(true);
        getModel().getProvisioning().setIsChangable(false);

        InitDisks();
        InitStorageDomains();
    }

    @Override
    public void UpdateIsDisksAvailable()
    {
        getModel().setIsDisksAvailable(getModel().getDisks() != null);
    }

    @Override
    public void Provisioning_SelectedItemChanged()
    {
        boolean provisioning = (Boolean) getModel().getProvisioning().getEntity();
        getModel().getProvisioningThin_IsSelected().setEntity(!provisioning);
        getModel().getProvisioningClone_IsSelected().setEntity(provisioning);
    }

    @Override
    public void InitDisks() {
        ArrayList<DiskModel> disks = new ArrayList<DiskModel>();
        for (DiskImage diskImage : vm.getDiskList()) {
            disks.add(Linq.DiskImageToModel(diskImage));
        }
        getModel().setDisks(disks);
        getModel().getDisksAllocationModel().setIsVolumeFormatAvailable(true);
        getModel().getDisksAllocationModel().setIsVolumeFormatChangable(true);
        UpdateIsDisksAvailable();
    }

    @Override
    public void InitStorageDomains()
    {
        if (getModel().getDisks() == null) {
            return;
        }

        storage_pool dataCenter = (storage_pool) getModel().getDataCenter().getSelectedItem();

        AsyncDataProvider.GetStorageDomainList(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {
                IVmModelBehavior behavior = (IVmModelBehavior) target;
                ArrayList<storage_domains> storageDomains = (ArrayList<storage_domains>) returnValue;
                ArrayList<storage_domains> activeStorageDomains = FilterStorageDomains(storageDomains);
                DisksAllocationModel disksAllocationModel = getModel().getDisksAllocationModel();

                boolean provisioning = (Boolean) behavior.getModel().getProvisioning().getEntity();
                ArrayList<DiskModel> disks = (ArrayList<DiskModel>) behavior.getModel().getDisks();
                Linq.Sort(activeStorageDomains, new Linq.StorageDomainByNameComparer());
                disksAllocationModel.setActiveStorageDomains(activeStorageDomains);

                for (DiskModel diskModel : disks) {
                    ArrayList<storage_domains> availableDiskStorageDomains;
                    if (provisioning) {
                        availableDiskStorageDomains = activeStorageDomains;
                    }
                    else
                    {
                        ArrayList<Guid> storageIds = diskModel.getDiskImage().getstorage_ids();
                        availableDiskStorageDomains =
                                Linq.getStorageDomainsByIds(storageIds, activeStorageDomains);
                    }
                    Linq.Sort(availableDiskStorageDomains, new Linq.StorageDomainByNameComparer());
                    diskModel.getStorageDomain().setItems(availableDiskStorageDomains);
                }

                ArrayList<storage_domains> storageDomainsDisjoint =
                        Linq.getStorageDomainsDisjoint(disks, activeStorageDomains);

                Linq.Sort(storageDomainsDisjoint, new Linq.StorageDomainByNameComparer());

                ArrayList<storage_domains> singleDestDomains =
                        provisioning ? activeStorageDomains : storageDomainsDisjoint;
                getModel().getStorageDomain().setItems(singleDestDomains);
                getModel().getStorageDomain().setSelectedItem(Linq.FirstOrDefault(singleDestDomains));
            }
        }, getModel().getHash()), dataCenter.getId());
    }
}
