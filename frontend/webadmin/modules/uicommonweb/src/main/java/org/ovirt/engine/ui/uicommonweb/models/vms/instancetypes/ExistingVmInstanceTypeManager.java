package org.ovirt.engine.ui.uicommonweb.models.vms.instancetypes;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.vms.EditProfileBehavior;
import org.ovirt.engine.ui.uicommonweb.models.vms.ProfileBehavior;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class ExistingVmInstanceTypeManager extends VmInstanceTypeManager {

    private final ProfileBehavior networkBehavior = new EditProfileBehavior();

    private final VM vm;

    public ExistingVmInstanceTypeManager(UnitVmModel model, VM vm) {
        super(model);
        this.vm = vm;
    }

    @Override
    protected void doUpdateManagedFieldsFrom(VmBase vmBase) {
        boolean numOfSocketsChangable = getModel().getNumOfSockets().getIsChangable();
        boolean coresPerSocket = getModel().getCoresPerSocket().getIsChangable();
        boolean threadsPerCore = getModel().getThreadsPerCore().getIsChangable();

        super.doUpdateManagedFieldsFrom(vmBase);

        deactivate();
        getModel().getNumOfSockets().setIsChangeable(numOfSocketsChangable);
        getModel().getCoresPerSocket().setIsChangeable(coresPerSocket);
        getModel().getThreadsPerCore().setIsChangeable(threadsPerCore);
        activate();
    }

    @Override
    protected VmBase getSource() {
        return vm.getStaticData();
    }

    @Override
    protected boolean isNextRunConfigurationExists() {
        return vm.isNextRunConfigurationExists();
    }

    @Override
    protected ProfileBehavior getNetworkProfileBehavior() {
        return networkBehavior;
    }

    @Override
    protected Guid getSelectedInstanceTypeId() {
        return super.getSelectedInstanceTypeId() == null ? vm.getInstanceTypeId() : super.getSelectedInstanceTypeId();
    }
}
