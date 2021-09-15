package org.ovirt.engine.core.bll;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.errors.EngineMessage;

@RequestScoped
public class WindowsVmWithSecondaryDiskClusterEditChecker extends AbstractWindowsVmClusterEditChecker {

    @Inject
    protected VmHandler vmHandler;

    @Override
    public boolean check(VM vm) {
        return isLinux(vm) || hasCustomCompatibilityVersion(vm) || hasCustomEmulatedMachine(vm) || !hasSecondaryDisk(vm);
    }

    @Override
    public String getMainMessage() {
        return EngineMessage.CLUSTER_WARN_VM_DUE_TO_EMULATED_MACHINE_CHANGE_ON_WINDOWS_WITH_SECONDARY_DISK.name();
    }

    @Override
    public String getDetailMessage(VM vm) {
        return null;
    }

    protected boolean hasSecondaryDisk(VM vm) {
        vmHandler.updateDisksFromDb(vm);
        return vm.getDiskList().size() > 1;
    }
}
