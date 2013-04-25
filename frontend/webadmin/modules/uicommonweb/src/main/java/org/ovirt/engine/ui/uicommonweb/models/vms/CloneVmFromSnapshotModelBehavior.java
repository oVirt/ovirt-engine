package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.Arrays;

import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.SystemTreeItemModel;

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
                    public void onSuccess(Object target, Object returnValue) {

                        UnitVmModel model = (UnitVmModel) target;
                        VmTemplate template = (VmTemplate) returnValue;
                        model.getTemplate()
                                .setItems(new ArrayList<VmTemplate>(Arrays.asList(new VmTemplate[] { template })));
                        model.getTemplate().setSelectedItem(template);
                        model.getTemplate().setIsChangable(false);
                    }
                },
                getModel().getHash()),
                vm.getVmtGuid());
    }

    @Override
    public void Template_SelectedItemChanged()
    {
        super.Template_SelectedItemChanged();

        getModel().getName().setEntity(""); //$NON-NLS-1$
        getModel().getDescription().setEntity(""); //$NON-NLS-1$
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
            disks.add(Linq.diskToModel(diskImage));
        }
        getModel().setDisks(disks);
        getModel().getDisksAllocationModel().setIsVolumeFormatAvailable(true);
        getModel().getDisksAllocationModel().setIsVolumeFormatChangable(true);
        UpdateIsDisksAvailable();
    }

    @Override
    public void InitStorageDomains()
    {
        PostInitStorageDomains();
    }
}
