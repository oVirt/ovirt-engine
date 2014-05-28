package org.ovirt.engine.ui.uicommonweb.builders.vm;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.ui.uicommonweb.builders.CompositeBuilder;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class CoreVmBaseToUnitBuilder extends CompositeBuilder<VmBase, UnitVmModel> {
    public CoreVmBaseToUnitBuilder() {
        super(
                new KernelParamsVmBaseToUnitBuilder(),
                new SerialNumberPolicyVmBaseToUnitBuilder()
        );
    }

    @Override
    protected void postBuild(VmBase vm, UnitVmModel model) {
        model.getBootMenuEnabled().setEntity(vm.isBootMenuEnabled());
        model.getVncKeyboardLayout().setSelectedItem(vm.getVncKeyboardLayout());
        model.getIsDeleteProtected().setEntity(vm.isDeleteProtected());
        model.selectSsoMethod(vm.getSsoMethod());
        model.getSpiceFileTransferEnabled().setEntity(vm.isSpiceFileTransferEnabled());
        model.getSpiceCopyPasteEnabled().setEntity(vm.isSpiceCopyPasteEnabled());
    }
}
