package org.ovirt.engine.ui.uicommonweb.builders.vm;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.ui.uicommonweb.builders.Builder;
import org.ovirt.engine.ui.uicommonweb.builders.CompositeBuilder;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

public class CoreVmBaseToUnitBuilder extends CompositeBuilder<VmBase, UnitVmModel> {
    public CoreVmBaseToUnitBuilder(Builder<VmBase, UnitVmModel>... builders) {
        super(builders);
    }

    public CoreVmBaseToUnitBuilder() {
        this(
                new KernelParamsVmBaseToUnitBuilder(),
                new SerialNumberPolicyVmBaseToUnitBuilder(),
                new IconVmBaseToUnitBuilder()
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
        model.getAutoConverge().setSelectedItem(vm.getAutoConverge());
        model.getMigrateCompressed().setSelectedItem(vm.getMigrateCompressed());
        model.getMigrateEncrypted().setSelectedItem(vm.getMigrateEncrypted());
        model.getConsoleDisconnectAction().setSelectedItem(vm.getConsoleDisconnectAction());
        model.getResumeBehavior().setSelectedItem(vm.getResumeBehavior());
        model.getVmType().setSelectedItem(vm.getVmType());
        if (!Objects.equals(model.getCustomCompatibilityVersion().getSelectedItem(), vm.getCustomCompatibilityVersion())) {
            // this has to be updated only if really changed since it triggers a refresh of the whole dialog including this
            // builder leading to an infinite recursion
            model.getCustomCompatibilityVersion().setSelectedItem(vm.getCustomCompatibilityVersion());
        }
        model.getCpuPinning().setEntity(vm.getCpuPinning());
        model.getVirtioScsiMultiQueuesEnabled().setEntity(vm.isVirtioScsiMultiQueuesEnabled());
    }
}
