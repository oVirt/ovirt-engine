package org.ovirt.engine.ui.uicommonweb.builders.vm;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.ui.uicommonweb.builders.Builder;
import org.ovirt.engine.ui.uicommonweb.builders.CompositeBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.MigrationsEntityToModelBuilder;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class CoreVmBaseToUnitBuilder extends CompositeBuilder<VmBase, UnitVmModel> {
    public CoreVmBaseToUnitBuilder(Builder<VmBase, UnitVmModel>... builders) {
        super(builders);
    }

    public CoreVmBaseToUnitBuilder() {
        this(
                new KernelParamsVmBaseToUnitBuilder(),
                new SerialNumberPolicyVmBaseToUnitBuilder(),
                new IconVmBaseToUnitBuilder(),
                new MigrationsEntityToModelBuilder(),
                new CpuPinningVmBaseToUnitBuilder());
    }

    @Override
    protected void postBuild(VmBase vm, UnitVmModel model) {
        model.setSecureBootOriginallyEnabled(vm.getBiosType() == BiosType.Q35_SECURE_BOOT);
        model.getBiosType().setSelectedItem(vm.getBiosType());
        model.getBootMenuEnabled().setEntity(vm.isBootMenuEnabled());
        model.getVncKeyboardLayout().setSelectedItem(vm.getVncKeyboardLayout());
        model.getIsDeleteProtected().setEntity(vm.isDeleteProtected());
        model.selectSsoMethod(vm.getSsoMethod());
        model.getSpiceFileTransferEnabled().setEntity(vm.isSpiceFileTransferEnabled());
        model.getSpiceCopyPasteEnabled().setEntity(vm.isSpiceCopyPasteEnabled());
        model.getConsoleDisconnectAction().setSelectedItem(vm.getConsoleDisconnectAction());
        model.getConsoleDisconnectActionDelay().setEntity(vm.getConsoleDisconnectActionDelay());
        model.getResumeBehavior().setSelectedItem(vm.getResumeBehavior());
        model.getVmType().setSelectedItem(vm.getVmType());
        if (!Objects.equals(model.getCustomCompatibilityVersion().getSelectedItem(), vm.getCustomCompatibilityVersion())) {
            // this has to be updated only if really changed since it triggers a refresh of the whole dialog including this
            // builder leading to an infinite recursion
            model.getCustomCompatibilityVersion().setSelectedItem(vm.getCustomCompatibilityVersion());
        }
    }
}
