package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;

@SuppressWarnings("unused")
public class CloneVmFromSnapshotModelBehavior extends ExistingVmModelBehavior {
    public CloneVmFromSnapshotModelBehavior() {
        super(null);
    }

    @Override
    public void templateWithVersion_SelectedItemChanged() {
        super.templateWithVersion_SelectedItemChanged();

        getModel().getName().setEntity(""); //$NON-NLS-1$
        getModel().getDescription().setEntity(""); //$NON-NLS-1$
        getModel().getComment().setEntity(""); //$NON-NLS-1$
        getModel().getProvisioning().setEntity(true);
        getModel().getProvisioning().setIsAvailable(true);
        getModel().getProvisioning().setIsChangeable(false);
        getModel().getVmId().setIsAvailable(true);
        getModel().getVmId().setIsChangeable(true);
        getModel().getVmId().setEntity("");

        initDisks();
        initStorageDomains();
    }

    @Override
    public void updateIsDisksAvailable() {
        getModel().setIsDisksAvailable(getModel().getDisks() != null);
    }

    @Override
    public void provisioning_SelectedItemChanged() {
        boolean provisioning = getModel().getProvisioning().getEntity();
        getModel().getProvisioningThin_IsSelected().setEntity(!provisioning);
        getModel().getProvisioningClone_IsSelected().setEntity(provisioning);
    }

    @Override
    public void initDisks() {
        ArrayList<DiskModel> disks = new ArrayList<>();
        for (DiskImage diskImage : vm.getDiskList()) {
            disks.add(DiskModel.diskToModel(diskImage));
        }
        getModel().setDisks(disks);
        getModel().getDisksAllocationModel().setIsVolumeTypeAvailable(true);
        getModel().getDisksAllocationModel().setIsVolumeTypeChangeable(true);
        updateIsDisksAvailable();
    }

    @Override
    public void initStorageDomains() {
        postInitStorageDomains();
    }

    @Override
    protected void updateNumaEnabled() {
    }
}
