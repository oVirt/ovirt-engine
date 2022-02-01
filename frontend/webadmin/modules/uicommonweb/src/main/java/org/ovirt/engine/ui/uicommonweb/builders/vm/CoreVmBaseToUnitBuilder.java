package org.ovirt.engine.ui.uicommonweb.builders.vm;

import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.BiosType;
import org.ovirt.engine.core.common.businessentities.VmBase;
import org.ovirt.engine.ui.uicommonweb.builders.Builder;
import org.ovirt.engine.ui.uicommonweb.builders.CompositeBuilder;
import org.ovirt.engine.ui.uicommonweb.builders.MigrationsEntityToModelBuilder;
import org.ovirt.engine.ui.uicommonweb.models.vms.UnitVmModel;

/**
 * The common part used for both, creating of a new entities as
 * well as editing ones.
 *
 * The builder does not contain values that needs to be specifically
 * initialized for the new entities, e.g. os, time zone.
 *
 */
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
        // Header
        model.getVmType().setSelectedItem(vm.getVmType());
        model.getBiosType().setSelectedItem(vm.getBiosType());
        model.setSecureBootOriginallyEnabled(vm.getBiosType() == BiosType.Q35_SECURE_BOOT);
        // General
        model.getIsDeleteProtected().setEntity(vm.isDeleteProtected());
        // System
        if (!Objects.equals(model.getCustomCompatibilityVersion().getSelectedItem(), vm.getCustomCompatibilityVersion())) {
            // this has to be updated only if really changed since it triggers a refresh of the whole dialog including this
            // builder leading to an infinite recursion
            model.getCustomCompatibilityVersion().setSelectedItem(vm.getCustomCompatibilityVersion());
        }
        // Console
        model.selectSsoMethod(vm.getSsoMethod());
        model.getVncKeyboardLayout().setSelectedItem(vm.getVncKeyboardLayout());
        model.getConsoleDisconnectAction().setSelectedItem(vm.getConsoleDisconnectAction());
        model.getConsoleDisconnectActionDelay().setEntity(vm.getConsoleDisconnectActionDelay());
        model.getSpiceFileTransferEnabled().setEntity(vm.isSpiceFileTransferEnabled());
        model.getSpiceCopyPasteEnabled().setEntity(vm.isSpiceCopyPasteEnabled());
        model.getAllowConsoleReconnect().setEntity(vm.isAllowConsoleReconnect());
        // Host
        model.getHostCpu().setEntity(vm.isUseHostCpuFlags());
        model.getTscFrequency().setEntity(vm.getUseTscFrequency());
        // High availability
        model.getResumeBehavior().setSelectedItem(vm.getResumeBehavior());
        // Resource allocation
        model.getCpuSharesAmount().setEntity(vm.getCpuShares());
        // Boot
        model.getBootMenuEnabled().setEntity(vm.isBootMenuEnabled());
        // Custom Properties
        model.getCustomPropertySheet().deserialize(vm.getCustomProperties());
    }
}
